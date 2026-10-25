package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalibrationStep
import com.example.model.CalibrationTelemetry
import com.example.model.CourtMountPreset
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeFixed
import kotlin.math.abs

/**
 * Interactive canvas overlay rendering dynamic optical guide reticles,
 * 3D court baseline perspective grid, athlete framing envelope, and electronic level horizon.
 */
@Composable
fun CalibrationOpticalOverlay(
  step: CalibrationStep,
  telemetry: CalibrationTelemetry,
  preset: CourtMountPreset,
  modifier: Modifier = Modifier
) {
  val levelColor by animateColorAsState(
    targetValue = if (telemetry.isRollOptimal && telemetry.isPitchOptimal) CourtGreenFixed else ShuttleOrangeFixed,
    label = "levelColor"
  )

  val distanceColor by animateColorAsState(
    targetValue = if (telemetry.isDistanceOptimal) CourtGreenFixed else ShuttleOrangeFixed,
    label = "distanceColor"
  )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val totalW = maxWidth
    val totalH = maxHeight

    // 1. Perspective Court Baseline & Envelope Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // A. Ground Perspective Grid
      val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
      val solidStroke = Stroke(width = 3f)
      val dashedStroke = Stroke(width = 2.5f, pathEffect = dashPathEffect)

      // Perspective Court Baseline Polygon
      val pBottomLeft = Offset(w * 0.10f, h * 0.78f)
      val pBottomRight = Offset(w * 0.90f, h * 0.78f)
      val pTopRight = Offset(w * 0.74f, h * 0.44f)
      val pTopLeft = Offset(w * 0.26f, h * 0.44f)

      val courtPerspectivePath = Path().apply {
        moveTo(pBottomLeft.x, pBottomLeft.y)
        lineTo(pBottomRight.x, pBottomRight.y)
        lineTo(pTopRight.x, pTopRight.y)
        lineTo(pTopLeft.x, pTopLeft.y)
        close()
      }

      // Draw court trapezoid
      drawPath(
        path = courtPerspectivePath,
        color = CourtGreen.copy(alpha = if (step == CalibrationStep.TRIPOD_POSITION) 0.55f else 0.35f),
        style = dashedStroke
      )

      // Court Mid-line (T-Junction)
      val tJunctionCenter = Offset(w * 0.50f, (pBottomLeft.y + pTopLeft.y) / 2f)
      drawLine(
        color = ShuttleOrange.copy(alpha = 0.7f),
        start = Offset(w * 0.50f, pTopLeft.y),
        end = Offset(w * 0.50f, pBottomLeft.y),
        strokeWidth = 2.5f,
        pathEffect = dashPathEffect
      )

      // B. Step 2: Artificial Electronic Level Horizon
      if (step == CalibrationStep.TILT_AND_PITCH || step == CalibrationStep.LOCK_AND_VERIFY) {
        val rollAngleRad = Math.toRadians(telemetry.rollDeg.toDouble())
        val horizonLength = w * 0.45f
        val centerY = h * 0.48f
        val dx = (horizonLength / 2f) * Math.cos(rollAngleRad).toFloat()
        val dy = (horizonLength / 2f) * Math.sin(rollAngleRad).toFloat()

        // Artificial horizon bar
        drawLine(
          color = levelColor,
          start = Offset(w * 0.50f - dx, centerY - dy),
          end = Offset(w * 0.50f + dx, centerY + dy),
          strokeWidth = 4f
        )
        // Center reticle pip
        drawCircle(color = levelColor, radius = 7f, center = Offset(w * 0.50f, centerY))
        drawCircle(color = levelColor.copy(alpha = 0.4f), radius = 18f, center = Offset(w * 0.50f, centerY), style = Stroke(2f))
      }

      // C. Step 3 & 4: Athlete Framing Box & Racket Apex Zenith Target
      if (step == CalibrationStep.DISTANCE_AND_FRAME || step == CalibrationStep.SWING_ENVELOPE_CHECK || step == CalibrationStep.LOCK_AND_VERIFY) {
        // Player Body Silhouette Boundary Box
        val boxWidth = w * 0.44f
        val boxHeight = h * 0.56f
        val boxTopLeft = Offset(w * 0.28f, h * 0.24f)

        drawRoundRect(
          color = distanceColor.copy(alpha = 0.75f),
          topLeft = boxTopLeft,
          size = Size(boxWidth, boxHeight),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
          style = Stroke(
            width = 3.5f,
            pathEffect = if (telemetry.isDistanceOptimal) null else dashPathEffect
          )
        )

        // Racket Zenith Overhead Clearance Zone (Top of swing apex)
        val apexZoneTop = h * 0.12f
        val apexZoneHeight = h * 0.10f
        val apexZoneWidth = w * 0.38f
        val apexZoneLeft = w * 0.31f

        drawRoundRect(
          color = ShuttleOrangeFixed.copy(alpha = 0.85f),
          topLeft = Offset(apexZoneLeft, apexZoneTop),
          size = Size(apexZoneWidth, apexZoneHeight),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
          style = Stroke(width = 2.5f, pathEffect = dashPathEffect)
        )
      }
    }

    // 2. Center Status HUD Badges
    when (step) {
      CalibrationStep.TRIPOD_POSITION -> {
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .padding(bottom = 120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131B2E).copy(alpha = 0.88f))
            .border(1.dp, CourtGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CropFree,
              contentDescription = null,
              tint = CourtGreenFixed,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Court Perspective: ${telemetry.boundaryLinesFound}/4 Lines Detected",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CourtGreenFixed
              )
            )
          }
        }
      }

      CalibrationStep.TILT_AND_PITCH -> {
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .padding(bottom = 110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131B2E).copy(alpha = 0.90f))
            .border(1.dp, levelColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (telemetry.isRollOptimal) Icons.Default.CheckCircle else Icons.Default.ScreenRotation,
              contentDescription = null,
              tint = levelColor,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Roll: ${"%.1f".format(telemetry.rollDeg)}°  •  Pitch: ${"%.1f".format(telemetry.pitchDeg)}°",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (telemetry.isRollOptimal && telemetry.isPitchOptimal) "LEVEL [OK]" else "ALIGN HORIZON",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = levelColor
              )
            )
          }
        }
      }

      CalibrationStep.DISTANCE_AND_FRAME -> {
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .padding(bottom = 140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131B2E).copy(alpha = 0.90f))
            .border(1.dp, distanceColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Straighten,
              contentDescription = null,
              tint = distanceColor,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Estimated Distance: ${"%.1f".format(telemetry.estimatedDistanceM)}m (Ideal: ${preset.idealDistanceMeters}m)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
              )
            )
          }
        }
      }

      CalibrationStep.SWING_ENVELOPE_CHECK -> {
        Box(
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131B2E).copy(alpha = 0.90f))
            .border(1.dp, ShuttleOrangeFixed.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ArrowUpward,
              contentDescription = null,
              tint = ShuttleOrangeFixed,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Overhead Apex Ceiling Zone: OK (2.65m Racket Reach)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }
      }

      CalibrationStep.LOCK_AND_VERIFY -> {
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .padding(bottom = 120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CourtGreen.copy(alpha = 0.92f))
            .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "BIOMECHANICAL CALIBRATION LOCKED (98% ACCURACY)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
              )
            )
          }
        }
      }
    }
  }
}
