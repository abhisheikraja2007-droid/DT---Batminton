package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/**
 * CameraPreviewView renders the hardware camera stream via CameraX PreviewView.
 * It integrates CameraX lifecycle management and attaches an ImageAnalysis use-case
 * configured with STRATEGY_KEEP_ONLY_LATEST for real-time pose detection processing.
 */
@Composable
fun CameraPreviewView(
  modifier: Modifier = Modifier,
  lensFacing: Int = CameraSelector.LENS_FACING_BACK,
  onFrameAnalyzed: ((imageProxy: ImageProxy) -> Unit)? = null,
  onCameraReady: ((hasCamera: Boolean) -> Unit)? = null
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

  val previewView = remember {
    PreviewView(context).apply {
      implementationMode = PreviewView.ImplementationMode.COMPATIBLE
      scaleType = PreviewView.ScaleType.FILL_CENTER
    }
  }

  LaunchedEffect(lensFacing, lifecycleOwner) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
      try {
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
          .requireLensFacing(lensFacing)
          .build()

        val preview = Preview.Builder().build().also {
          it.surfaceProvider = previewView.surfaceProvider
        }

        val imageAnalysis = if (onFrameAnalyzed != null) {
          ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
              analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                try {
                  onFrameAnalyzed(imageProxy)
                } finally {
                  imageProxy.close()
                }
              }
            }
        } else null

        if (imageAnalysis != null) {
          cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
          )
        } else {
          cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview
          )
        }
        onCameraReady?.invoke(true)
      } catch (e: Exception) {
        Log.e("CameraPreviewView", "Camera binding failed: ${e.message}", e)
        onCameraReady?.invoke(false)
      }
    }, ContextCompat.getMainExecutor(context))
  }

  AndroidView(
    factory = { previewView },
    modifier = modifier
  )

  DisposableEffect(Unit) {
    onDispose {
      try {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()
      } catch (e: Exception) {
        Log.e("CameraPreviewView", "Error unbinding camera on dispose: ${e.message}")
      }
      cameraExecutor.shutdown()
    }
  }
}

