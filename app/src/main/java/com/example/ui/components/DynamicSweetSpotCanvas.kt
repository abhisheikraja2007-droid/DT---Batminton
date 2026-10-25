package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.DynamicRepAnalysis
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeFixed

/**
 * Interactive Dynamic Sweet Spot Impact Heatmap Canvas.
 * Renders the badminton racket head string bed, concentric sweet-spot zones,
 * and the exact computed impact coordinates and millimeter offset for any stroke rep.
 */
@Composable
fun DynamicSweetSpotCanvas(
  rep: DynamicRepAnalysis,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = w / 2f
      val cy = h / 2f

      val racketRadiusX = w * 0.42f
      val racketRadiusY = h * 0.46f

      // 1. Racket Frame Outer Bezel
      drawOval(
        color = Color(0xFF1E293B),
        topLeft = Offset(cx - racketRadiusX, cy - racketRadiusY),
        size = Size(racketRadiusX * 2, racketRadiusY * 2),
        style = Stroke(width = 8f)
      )
      drawOval(
        color = ShuttleOrangeFixed.copy(alpha = 0.6f),
        topLeft = Offset(cx - racketRadiusX + 3f, cy - racketRadiusY + 3f),
        size = Size((racketRadiusX - 3f) * 2, (racketRadiusY - 3f) * 2),
        style = Stroke(width = 2f)
      )

      // 2. String Bed Grid (Mains & Crosses)
      val stringColor = Color.White.copy(alpha = 0.18f)
      val mainLines = 10
      val crossLines = 12

      for (i in 1..mainLines) {
        val x = cx - racketRadiusX + (i * (racketRadiusX * 2 / (mainLines + 1)))
        drawLine(
          color = stringColor,
          start = Offset(x, cy - racketRadiusY * 0.85f),
          end = Offset(x, cy + racketRadiusY * 0.85f),
          strokeWidth = 1f
        )
      }

      for (j in 1..crossLines) {
        val y = cy - racketRadiusY + (j * (racketRadiusY * 2 / (crossLines + 1)))
        drawLine(
          color = stringColor,
          start = Offset(cx - racketRadiusX * 0.85f, y),
          end = Offset(cx + racketRadiusX * 0.85f, y),
          strokeWidth = 1f
        )
      }

      // 3. Concentric Sweet Spot Target Rings
      // 100% Core Target Zone (Green)
      drawOval(
        color = CourtGreen.copy(alpha = 0.25f),
        topLeft = Offset(cx - racketRadiusX * 0.32f, cy - racketRadiusY * 0.32f),
        size = Size(racketRadiusX * 0.64f, racketRadiusY * 0.64f),
        style = Stroke(width = 2f)
      )

      // Center Bullseye Crosshairs
      drawLine(
        color = CourtGreenFixed.copy(alpha = 0.7f),
        start = Offset(cx - 15f, cy),
        end = Offset(cx + 15f, cy),
        strokeWidth = 2f
      )
      drawLine(
        color = CourtGreenFixed.copy(alpha = 0.7f),
        start = Offset(cx, cy - 15f),
        end = Offset(cx, cy + 15f),
        strokeWidth = 2f
      )

      // 4. Actual Rep Impact Point
      val impactPxX = cx + (rep.impactX * (racketRadiusX * 0.85f))
      val impactPxY = cy + (rep.impactY * (racketRadiusY * 0.85f))
      val impactCenter = Offset(impactPxX, impactPxY)

      val impactColor = when {
        rep.isBest -> CourtGreenFixed
        rep.isFault -> ErrorRed
        rep.sweetSpotOffsetMm <= 8f -> CourtGreen
        else -> ShuttleOrange
      }

      // Radial Impact Glow Heatmap
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            impactColor.copy(alpha = 0.85f),
            impactColor.copy(alpha = 0.35f),
            Color.Transparent
          ),
          center = impactCenter,
          radius = 35f
        ),
        radius = 35f,
        center = impactCenter
      )

      // Solid Impact Core Indicator
      drawCircle(
        color = Color.White,
        radius = 6f,
        center = impactCenter
      )
      drawCircle(
        color = impactColor,
        radius = 9f,
        center = impactCenter,
        style = Stroke(width = 2.5f)
      )

      // Offset Connector Line to Center
      if (rep.sweetSpotOffsetMm > 3f) {
        drawLine(
          color = impactColor.copy(alpha = 0.6f),
          start = Offset(cx, cy),
          end = impactCenter,
          strokeWidth = 1.5f
        )
      }
    }
  }
}
