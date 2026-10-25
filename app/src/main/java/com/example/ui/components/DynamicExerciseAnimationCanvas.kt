package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WarmupFocusArea
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ShuttleOrange
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated dynamic mobility visualization showing the anatomical kinetic movement
 * (such as rotational figure-8 loops for wrist, circular sweep for shoulder, or pogo bounce for ankle).
 */
@Composable
fun DynamicExerciseAnimationCanvas(
  focusArea: WarmupFocusArea,
  isActive: Boolean,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "MobilityAnimation")

  val cycleAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "CycleAngle"
  )

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseScale"
  )

  val primaryColor = Color(focusArea.badgeHex)
  val secondaryColor = if (focusArea == WarmupFocusArea.WRIST_FLEXIBILITY) ShuttleOrange else CourtGreen

  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(Color(0xFF0F172A))
      .border(2.dp, primaryColor.copy(alpha = 0.35f), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
      val w = size.width
      val h = size.height
      val cx = w / 2f
      val cy = h / 2f
      val radius = minOf(w, h) * 0.40f

      // 1. Static guideline orbital tracks
      drawCircle(
        color = Color(0xFF334155).copy(alpha = 0.4f),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 2f)
      )

      when (focusArea) {
        WarmupFocusArea.WRIST_FLEXIBILITY -> {
          // Dynamic Figure-8 (Lemniscate) Wrist Trajectory
          val path = Path()
          val steps = 80
          val a = radius * 0.9f
          for (i in 0..steps) {
            val t = (i / steps.toFloat()) * (2f * Math.PI.toFloat())
            val denom = 1f + sin(t) * sin(t)
            val x = cx + (a * cos(t)) / denom
            val y = cy + (a * sin(t) * cos(t)) / denom
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
          }

          drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.5f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
          )

          // Active moving pronation indicator dot
          val rad = Math.toRadians(cycleAngle.toDouble())
          val denom = 1.0 + sin(rad) * sin(rad)
          val activeX = cx + (a * cos(rad).toFloat()) / denom.toFloat()
          val activeY = cy + (a * (sin(rad) * cos(rad)).toFloat()) / denom.toFloat()

          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(primaryColor, Color.Transparent),
              center = Offset(activeX, activeY),
              radius = 24f
            ),
            radius = 18f,
            center = Offset(activeX, activeY)
          )
          drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(activeX, activeY)
          )
        }

        WarmupFocusArea.SHOULDER_MOBILITY -> {
          // Multi-plane 360° Rotator Envelope
          val sweepAngle = cycleAngle
          val headX = cx + (radius * cos(Math.toRadians(sweepAngle.toDouble()))).toFloat()
          val headY = cy + (radius * sin(Math.toRadians(sweepAngle.toDouble()))).toFloat()

          // Draw rotating orbital arc sweep
          drawArc(
            color = primaryColor,
            startAngle = sweepAngle - 90f,
            sweepAngle = 75f,
            useCenter = false,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
          )

          // Scapular pivot center
          drawCircle(
            color = secondaryColor.copy(alpha = 0.7f),
            radius = 10f * pulseScale,
            center = Offset(cx, cy)
          )

          // Racket overhead reach point
          drawLine(
            color = Color(0xFF64748B),
            start = Offset(cx, cy),
            end = Offset(headX, headY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
          )

          drawCircle(
            color = Color.White,
            radius = 7f,
            center = Offset(headX, headY)
          )
        }

        WarmupFocusArea.ANKLE_KNEE_AGILITY -> {
          // Elastic spring pogo wave
          val bounceOffset = (pulseScale - 1f) * 40f
          val springY = cy + bounceOffset

          val path = Path()
          path.moveTo(cx - radius * 0.7f, cy + 30f)
          path.quadraticTo(cx, springY - 25f, cx + radius * 0.7f, cy + 30f)

          drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
          )

          // Ground landing baseline
          drawLine(
            color = Color(0xFF475569),
            start = Offset(cx - radius, cy + 35f),
            end = Offset(cx + radius, cy + 35f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
          )

          // Bouncing foot contact sphere
          drawCircle(
            color = Color.White,
            radius = 8f,
            center = Offset(cx, springY - 25f)
          )
        }

        else -> {
          // Thoracic / Full Body Kinetic Spiral
          val rotRad = Math.toRadians(cycleAngle.toDouble())
          val spiralRadius = radius * pulseScale * 0.8f
          val dotX = cx + (spiralRadius * cos(rotRad)).toFloat()
          val dotY = cy + (spiralRadius * sin(rotRad)).toFloat()

          drawArc(
            color = primaryColor,
            startAngle = cycleAngle,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
          )

          drawCircle(
            color = Color.White,
            radius = 7f,
            center = Offset(dotX, dotY)
          )
        }
      }
    }

    // Centered emoji emblem
    Text(
      text = focusArea.iconEmoji,
      fontSize = 24.sp
    )
  }
}
