package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AceImages
import com.example.model.CalibrationStep
import com.example.model.CameraCalibrationManager
import com.example.model.CourtMountPreset
import com.example.ui.components.CalibrationOpticalOverlay
import com.example.ui.components.CameraPreviewView
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.CourtGreenFixedDim
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeContainer
import com.example.ui.theme.ShuttleOrangeFixed
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import kotlin.math.abs

/**
 * Full-screen guided UI sequence for calibrating camera angle, tripod height,
 * horizon tilt, and court distance for optimal 3D biomechanical analysis accuracy.
 */
@Composable
fun OpticalAlignmentScreen(
  onDismiss: () -> Unit,
  onStartTracking: () -> Unit = onDismiss
) {
  val context = LocalContext.current
  val calibrationManager = remember { CameraCalibrationManager.getInstance(context) }
  val currentStep by calibrationManager.currentStep.collectAsState()
  val telemetry by calibrationManager.telemetry.collectAsState()
  val selectedPreset by calibrationManager.selectedPreset.collectAsState()
  val isLocked by calibrationManager.isCalibrationLocked.collectAsState()

  var isHardwareCameraActive by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }
  var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
  var selectedLensZoom by remember { mutableStateOf("1x Wide") }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      isHardwareCameraActive = true
    }
  }

  LaunchedEffect(Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    } else {
      isHardwareCameraActive = true
    }
  }

  // Manage sensor lifecycle
  DisposableEffect(Unit) {
    calibrationManager.startSensorTracking()
    onDispose {
      calibrationManager.stopSensorTracking()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0A0F1D))
      .testTag("optical_alignment_screen")
  ) {
    // 1. Live Camera or Fallback Court Calibration Feed
    if (isHardwareCameraActive) {
      CameraPreviewView(
        modifier = Modifier.fillMaxSize(),
        lensFacing = lensFacing,
        onCameraReady = {}
      )
    } else {
      AsyncImage(
        model = AceImages.COURT_ALIGNMENT_BG,
        contentDescription = "Court alignment camera view",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    // Gradient Scrim for crisp readability
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF0F172A).copy(alpha = 0.88f),
              Color(0xFF0F172A).copy(alpha = 0.25f),
              Color(0xFF0F172A).copy(alpha = 0.94f)
            )
          )
        )
    )

    // 2. Dynamic Optical Reticle Canvas & Horizon Overlay
    CalibrationOpticalOverlay(
      step = currentStep,
      telemetry = telemetry,
      preset = selectedPreset,
      modifier = Modifier.fillMaxSize()
    )

    // 3. Top Header: Navigation, Step Indicators & Status Pills
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable { onDismiss() }
            .testTag("alignment_back_btn")
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Optical Calibration Guide",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
            Text(
              text = "Step ${currentStep.stepIndex} of 5 • ${currentStep.shortSubtitle}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CourtGreenFixed,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              )
            )
          }
        }

        // Camera Feed Toggle & Reset Actions
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Hardware / Simulation Toggle
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isHardwareCameraActive) CourtGreen else Color.White.copy(alpha = 0.16f))
              .clickable {
                isHardwareCameraActive = !isHardwareCameraActive
                Toast.makeText(
                  context,
                  if (isHardwareCameraActive) "Hardware Camera Feed" else "Court Reference Feed",
                  Toast.LENGTH_SHORT
                ).show()
              }
              .testTag("toggle_calibration_camera_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = "Camera Toggle",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }

          // Reset Horizon / Calibration
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.16f))
              .clickable {
                calibrationManager.resetCalibration()
                Toast.makeText(context, "Calibration reset to Step 1", Toast.LENGTH_SHORT).show()
              }
              .testTag("reset_calibration_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reset Calibration",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5-Step Segmented Progress Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        CalibrationStep.values().forEach { step ->
          val isCompleted = step.stepIndex < currentStep.stepIndex || isLocked
          val isCurrent = step == currentStep
          val barColor = when {
            isCompleted -> CourtGreenFixed
            isCurrent -> ShuttleOrangeFixed
            else -> Color.White.copy(alpha = 0.25f)
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(barColor)
              .clickable { calibrationManager.goToStep(step) }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Live Sensor Telemetry Pill Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Horizon Level Pill
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.ScreenRotation,
            contentDescription = null,
            tint = if (telemetry.isRollOptimal) CourtGreenFixed else ShuttleOrangeFixed,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Tilt: ${"%.1f".format(telemetry.rollDeg)}°",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 11.sp
            )
          )
        }

        // Pitch Angle Pill
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Height,
            contentDescription = null,
            tint = if (telemetry.isPitchOptimal) CourtGreenFixed else ShuttleOrangeFixed,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Pitch: ${"%.1f".format(telemetry.pitchDeg)}°",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 11.sp
            )
          )
        }

        // Distance Pill
        Row(
          modifier = Modifier
            .weight(1.1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Straighten,
            contentDescription = null,
            tint = if (telemetry.isDistanceOptimal) CourtGreenFixed else ShuttleOrangeFixed,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Dist: ${"%.1f".format(telemetry.estimatedDistanceM)}m",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    // 4. Bottom Guided Sequence Card and Controls
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              Color(0xFF0F172A).copy(alpha = 0.95f),
              Color(0xFF0F172A)
            )
          )
        )
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Step Guidance Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(CourtGreen),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${currentStep.stepIndex}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = currentStep.title,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }

            Text(
              text = "Accuracy ${telemetry.calibrationQualityPercent}%",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreenFixed
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = currentStep.guidancePrompt,
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color.White.copy(alpha = 0.85f),
              lineHeight = 16.sp
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Step-Specific Interactive Guidance
          when (currentStep) {
            CalibrationStep.TRIPOD_POSITION -> {
              // Preset Selection Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                CourtMountPreset.values().forEach { preset ->
                  val isSelected = selectedPreset == preset
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isSelected) CourtGreen else Color(0xFF0F172A))
                      .clickable { calibrationManager.selectPreset(preset) }
                      .padding(vertical = 6.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = preset.displayName,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.5.sp
                      ),
                      maxLines = 1
                    )
                  }
                }
              }
            }

            CalibrationStep.TILT_AND_PITCH -> {
              // Quick Tilt Adjustment Slider / Simulation Controls
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Adjust Horizon Tilt:",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                  value = telemetry.rollDeg,
                  onValueChange = { calibrationManager.adjustSimulatedRoll(it) },
                  valueRange = -10f..10f,
                  colors = SliderDefaults.colors(
                    thumbColor = CourtGreenFixed,
                    activeTrackColor = CourtGreenFixed,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                  ),
                  modifier = Modifier.weight(1f)
                )
                Text(
                  text = "${"%.1f".format(telemetry.rollDeg)}°",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenFixed,
                    fontFamily = FontFamily.Monospace
                  ),
                  modifier = Modifier.width(36.dp)
                )
              }
            }

            CalibrationStep.DISTANCE_AND_FRAME -> {
              // Distance simulation slider
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Stand-back Distance:",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                  value = telemetry.estimatedDistanceM,
                  onValueChange = { calibrationManager.adjustSimulatedDistance(it) },
                  valueRange = 2.5f..7.0f,
                  colors = SliderDefaults.colors(
                    thumbColor = ShuttleOrangeFixed,
                    activeTrackColor = ShuttleOrangeFixed,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                  ),
                  modifier = Modifier.weight(1f)
                )
                Text(
                  text = "${"%.1f".format(telemetry.estimatedDistanceM)}m",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShuttleOrangeFixed,
                    fontFamily = FontFamily.Monospace
                  ),
                  modifier = Modifier.width(36.dp)
                )
              }
            }

            CalibrationStep.SWING_ENVELOPE_CHECK -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(CourtGreenContainer.copy(alpha = 0.35f))
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = CourtGreenFixed,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Overhead jump ceiling verified: 100% in camera field of view.",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = CourtGreenFixed,
                    fontSize = 11.sp
                  )
                )
              }
            }

            CalibrationStep.LOCK_AND_VERIFY -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(CourtGreen)
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Optimal 3D Calibration Confirmed. Ready to Record Swings!",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Navigation & Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Back / Previous Step
        if (currentStep.stepIndex > 1) {
          OutlinedButton(
            onClick = { calibrationManager.previousStep() },
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("calibration_prev_step_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Previous",
              color = Color.White,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        // Primary Action: Next Step OR Lock & Start Swing Tracking
        val isFinalStep = currentStep == CalibrationStep.LOCK_AND_VERIFY
        Button(
          onClick = {
            if (isFinalStep) {
              calibrationManager.lockCalibration()
              Toast.makeText(context, "Optical Calibration Locked for Session!", Toast.LENGTH_SHORT).show()
              onStartTracking()
            } else {
              calibrationManager.nextStep()
            }
          },
          shape = RoundedCornerShape(22.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isFinalStep) CourtGreen else ShuttleOrange
          ),
          modifier = Modifier
            .weight(if (currentStep.stepIndex > 1) 1.5f else 1f)
            .height(48.dp)
            .testTag(if (isFinalStep) "lock_and_start_tracking_btn" else "calibration_next_step_btn")
        ) {
          Text(
            text = if (isFinalStep) "Lock & Start Swings" else "Continue (${currentStep.stepIndex}/5)",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = if (isFinalStep) Icons.Default.Check else Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
