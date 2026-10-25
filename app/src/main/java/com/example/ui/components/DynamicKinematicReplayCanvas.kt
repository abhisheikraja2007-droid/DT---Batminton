package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.DynamicRepAnalysis
import com.example.model.SlowMoKinematicFrame
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeFixed
import kotlin.math.roundToInt

/**
 * Dynamic Interactive Slow-Motion Kinematic Analysis Canvas.
 * Renders real-time biomechanical skeleton, joint angle telemetry arcs,
 * energy velocity vectors, and sweet-spot shuttle trajectory directly inside the app.
 */
@Composable
fun DynamicKinematicReplayCanvas(
  frame: SlowMoKinematicFrame,
  rep: DynamicRepAnalysis,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(Color(0xFF090E18))
  ) {
    Canvas(
      modifier = Modifier.fillMaxSize()
    ) {
      val w = size.width
      val h = size.height

      // 1. Cyber Biomechanical Grid & Apex Reference Lines
      drawBiomechanicalGrid(w, h)

      // Zenith Apex Target Line
      val targetZenithY = h * 0.16f
      val actualZenithY = h * (0.24f - (rep.contactZenithMeters - 2.5f) * 0.25f)

      // Target Zenith Line (Green Dotted)
      drawLine(
        color = CourtGreen.copy(alpha = 0.5f),
        start = Offset(0f, targetZenithY),
        end = Offset(w, targetZenithY),
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
      )

      // Actual Zenith Apex Reach Line (Rep specific)
      drawLine(
        color = if (rep.isFault) ErrorRed.copy(alpha = 0.7f) else ShuttleOrange.copy(alpha = 0.8f),
        start = Offset(0f, actualZenithY),
        end = Offset(w, actualZenithY),
        strokeWidth = 2.5f
      )

      // 2. Kinetic Uncoil Trajectory Arc behind racket arm
      if (frame.normalizedProgress > 0.30f) {
        val arcPath = Path().apply {
          moveTo(frame.rightShoulder.x * w, frame.rightShoulder.y * h)
          quadraticTo(
            frame.rightElbow.x * w + 30f,
            frame.rightElbow.y * h - 40f,
            frame.racketTip.x * w,
            frame.racketTip.y * h
          )
        }
        drawPath(
          path = arcPath,
          color = (if (rep.isFault) ErrorRed else CourtGreen).copy(alpha = 0.35f),
          style = Stroke(width = 8f)
        )
      }

      // 3. Draw Biomechanical Skeletal Bones (Bilateral Joints)
      val boneColor = Color.White.copy(alpha = 0.85f)
      val boneStroke = 5f

      // Torso & Spine
      drawLine(
        color = boneColor,
        start = Offset(frame.headPosition.x * w, (frame.headPosition.y + 0.05f) * h),
        end = Offset(frame.hipCenter.x * w, frame.hipCenter.y * h),
        strokeWidth = boneStroke + 1.5f
      )

      // Clavicle (Left Shoulder to Right Shoulder)
      drawLine(
        color = boneColor,
        start = Offset(frame.leftShoulder.x * w, frame.leftShoulder.y * h),
        end = Offset(frame.rightShoulder.x * w, frame.rightShoulder.y * h),
        strokeWidth = boneStroke
      )

      // Left Arm (Counter Balance)
      val leftArmColor = Color(0xFF6B7D96)
      drawLine(
        color = leftArmColor,
        start = Offset(frame.leftShoulder.x * w, frame.leftShoulder.y * h),
        end = Offset(frame.leftElbow.x * w, frame.leftElbow.y * h),
        strokeWidth = boneStroke - 1f
      )
      drawLine(
        color = leftArmColor,
        start = Offset(frame.leftElbow.x * w, frame.leftElbow.y * h),
        end = Offset(frame.leftWrist.x * w, frame.leftWrist.y * h),
        strokeWidth = boneStroke - 1f
      )

      // Right Arm (Hitting Arm - Color coded based on elbow fault)
      val hittingArmColor = if (rep.isFault && frame.elbowAngle < 100f) ErrorRed else CourtGreenFixed
      drawLine(
        color = hittingArmColor,
        start = Offset(frame.rightShoulder.x * w, frame.rightShoulder.y * h),
        end = Offset(frame.rightElbow.x * w, frame.rightElbow.y * h),
        strokeWidth = boneStroke + 2f
      )
      drawLine(
        color = hittingArmColor,
        start = Offset(frame.rightElbow.x * w, frame.rightElbow.y * h),
        end = Offset(frame.rightWrist.x * w, frame.rightWrist.y * h),
        strokeWidth = boneStroke + 2f
      )

      // Badminton Racket Shaft & Head
      val racketColor = ShuttleOrangeFixed
      drawLine(
        color = racketColor,
        start = Offset(frame.rightWrist.x * w, frame.rightWrist.y * h),
        end = Offset(frame.racketTip.x * w, frame.racketTip.y * h),
        strokeWidth = 4f
      )

      // Racket Head Oval
      val racketHeadRadius = 24f
      drawOval(
        color = racketColor,
        topLeft = Offset(frame.racketTip.x * w - racketHeadRadius, frame.racketTip.y * h - racketHeadRadius * 1.3f),
        size = Size(racketHeadRadius * 2, racketHeadRadius * 2.6f),
        style = Stroke(width = 3f)
      )

      // Pelvis to Legs
      val legColor = Color(0xFFA4B3C6)
      // Right Leg
      drawLine(
        color = legColor,
        start = Offset(frame.hipCenter.x * w + 12f, frame.hipCenter.y * h),
        end = Offset(frame.rightKnee.x * w, frame.rightKnee.y * h),
        strokeWidth = boneStroke
      )
      drawLine(
        color = legColor,
        start = Offset(frame.rightKnee.x * w, frame.rightKnee.y * h),
        end = Offset(frame.rightAnkle.x * w, frame.rightAnkle.y * h),
        strokeWidth = boneStroke
      )

      // Left Leg
      drawLine(
        color = legColor.copy(alpha = 0.7f),
        start = Offset(frame.hipCenter.x * w - 12f, frame.hipCenter.y * h),
        end = Offset(frame.leftKnee.x * w, frame.leftKnee.y * h),
        strokeWidth = boneStroke
      )
      drawLine(
        color = legColor.copy(alpha = 0.7f),
        start = Offset(frame.leftKnee.x * w, frame.leftKnee.y * h),
        end = Offset(frame.leftAnkle.x * w, frame.leftAnkle.y * h),
        strokeWidth = boneStroke
      )

      // 4. Joint Nodes & Telemetry Rings
      drawJointNode(Offset(frame.headPosition.x * w, frame.headPosition.y * h), 16f, Color(0xFFE2E8F0))
      drawJointNode(Offset(frame.rightShoulder.x * w, frame.rightShoulder.y * h), 7f, CourtGreen)
      drawJointNode(
        Offset(frame.rightElbow.x * w, frame.rightElbow.y * h),
        9f,
        if (rep.isFault && frame.elbowAngle < 100f) ErrorRed else CourtGreenFixed
      )
      drawJointNode(Offset(frame.rightWrist.x * w, frame.rightWrist.y * h), 7f, ShuttleOrangeFixed)
      drawJointNode(Offset(frame.hipCenter.x * w, frame.hipCenter.y * h), 8f, Color.White)
      drawJointNode(Offset(frame.rightKnee.x * w, frame.rightKnee.y * h), 7f, Color.White)
      drawJointNode(Offset(frame.rightAnkle.x * w, frame.rightAnkle.y * h), 7f, Color.White)

      // 5. Elbow Angle Telemetry Arc Indicator on Canvas
      val elbowScreen = Offset(frame.rightElbow.x * w, frame.rightElbow.y * h)
      drawCircle(
        color = (if (rep.isFault) ErrorRed else CourtGreen).copy(alpha = 0.25f),
        radius = 32f,
        center = elbowScreen
      )
      drawCircle(
        color = if (rep.isFault) ErrorRed else CourtGreenFixed,
        radius = 32f,
        center = elbowScreen,
        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
      )

      // 6. Shuttlecock Trajectory & Sweet-Spot Spark
      frame.shuttlePosition?.let { sPos ->
        val sOffset = Offset(sPos.x * w, sPos.y * h)

        // Impact spark glow if at strike moment
        if (frame.normalizedProgress in 0.60f..0.75f) {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                (if (rep.isFault) ErrorRed else CourtGreenFixed).copy(alpha = 0.9f),
                Color.Transparent
              ),
              center = sOffset,
              radius = 45f
            ),
            radius = 45f,
            center = sOffset
          )
        }

        // Cork and Feathers
        drawCircle(
          color = Color.White,
          radius = 7f,
          center = sOffset
        )
        drawLine(
          color = Color.White.copy(alpha = 0.8f),
          start = sOffset,
          end = Offset(sOffset.x - 14f, sOffset.y - 10f),
          strokeWidth = 3f
        )
        drawLine(
          color = Color.White.copy(alpha = 0.8f),
          start = sOffset,
          end = Offset(sOffset.x - 14f, sOffset.y + 10f),
          strokeWidth = 3f
        )
      }
    }
  }
}

private fun DrawScope.drawBiomechanicalGrid(w: Float, h: Float) {
  val gridColor = Color(0xFF1E293B).copy(alpha = 0.45f)
  val stepX = w / 8f
  val stepY = h / 6f

  for (i in 1..7) {
    drawLine(
      color = gridColor,
      start = Offset(i * stepX, 0f),
      end = Offset(i * stepX, h),
      strokeWidth = 1f
    )
  }
  for (j in 1..5) {
    drawLine(
      color = gridColor,
      start = Offset(0f, j * stepY),
      end = Offset(w, j * stepY),
      strokeWidth = 1f
    )
  }
}

private fun DrawScope.drawJointNode(center: Offset, radius: Float, color: Color) {
  drawCircle(
    color = color.copy(alpha = 0.35f),
    radius = radius + 4f,
    center = center
  )
  drawCircle(
    color = color,
    radius = radius,
    center = center
  )
}
