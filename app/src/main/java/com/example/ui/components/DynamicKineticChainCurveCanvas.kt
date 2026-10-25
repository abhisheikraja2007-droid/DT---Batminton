package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.DynamicRepAnalysis
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange

/**
 * Dynamic Kinetic Chain Energy Transfer Curve Canvas.
 * Graphs the sequence of kinetic acceleration through:
 * [1. Leg Drive -> 2. Hip Turn -> 3. Torso Torque -> 4. Elbow Drive -> 5. Wrist Snap].
 */
@Composable
fun DynamicKineticChainCurveCanvas(
  rep: DynamicRepAnalysis,
  currentProgress: Float = 0.68f,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      val samples = rep.kineticCurve
      if (samples.size < 2) return@Canvas

      val stepX = w / (samples.size - 1)
      val baselineY = h * 0.90f
      val peakH = h * 0.75f

      // 1. Fill Area Gradient Under Curve
      val fillPath = Path().apply {
        moveTo(0f, baselineY)
        samples.forEachIndexed { i, v ->
          val px = i * stepX
          val py = baselineY - (v * peakH)
          if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        lineTo(w, baselineY)
        close()
      }

      val curveColor = if (rep.isFault) ErrorRed else CourtGreen

      drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
          colors = listOf(
            curveColor.copy(alpha = 0.30f),
            Color.Transparent
          ),
          startY = 0f,
          endY = baselineY
        )
      )

      // 2. Stroke Path
      val strokePath = Path().apply {
        samples.forEachIndexed { i, v ->
          val px = i * stepX
          val py = baselineY - (v * peakH)
          if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
      }

      drawPath(
        path = strokePath,
        color = curveColor,
        style = Stroke(width = 4f)
      )

      // 3. Playhead Marker Line
      val playheadX = (currentProgress.coerceIn(0f, 1f)) * w
      drawLine(
        color = Color.White.copy(alpha = 0.8f),
        start = Offset(playheadX, 0f),
        end = Offset(playheadX, baselineY),
        strokeWidth = 2f
      )
      drawCircle(
        color = ShuttleOrange,
        radius = 5f,
        center = Offset(playheadX, baselineY * 0.4f)
      )

      // 4. Sample Points
      samples.forEachIndexed { i, v ->
        val px = i * stepX
        val py = baselineY - (v * peakH)
        drawCircle(
          color = Color.White,
          radius = 3.5f,
          center = Offset(px, py)
        )
      }
    }
  }
}
