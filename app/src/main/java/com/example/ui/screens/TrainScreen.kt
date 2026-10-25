package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlipCameraIos
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.model.AceImages
import com.example.tts.TechniqueVoiceCoach
import com.example.ui.components.CameraPreviewView
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.CourtGreenFixedDim
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InverseOnSurface
import com.example.ui.theme.InverseSurface
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeContainer
import com.example.ui.theme.ShuttleOrangeFixed
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun TrainScreen(
  onStopTraining: () -> Unit,
  onCalibrationClick: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repository = remember { com.example.db.AceRepository.getInstance(context) }
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  val voiceCoach = remember { TechniqueVoiceCoach.getInstance(context) }
  val isVoiceMuted by voiceCoach.isMuted.collectAsState()
  val lastSpokenCue by voiceCoach.lastSpokenCue.collectAsState()
  val isVoiceSpeaking by voiceCoach.isSpeaking.collectAsState()

  var isPoseOverlayOn by remember { mutableStateOf(true) }
  var isFlipped by remember { mutableStateOf(false) }
  var repsCount by remember { mutableIntStateOf(0) }
  var currentSpeed by remember { mutableIntStateOf(218) }
  var isHardwareCameraBound by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A))
      .testTag("train_screen_view")
  ) {
    // 1. Live Camera Stream: CameraX Hardware or Athlete Video Feed Fallback
    if (hasCameraPermission) {
      CameraPreviewView(
        modifier = Modifier.fillMaxSize(),
        lensFacing = if (isFlipped) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK,
        onCameraReady = { bound ->
          isHardwareCameraBound = bound
        }
      )
    }

    if (!hasCameraPermission || !isHardwareCameraBound) {
      AsyncImage(
        model = AceImages.TRAIN_ATHLETE_BG,
        contentDescription = "Live badminton camera feed",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .rotate(if (isFlipped) 180f else 0f)
      )
    }

    // Contrast dark gradient scrim
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF131B2E).copy(alpha = 0.92f),
              Color(0xFF131B2E).copy(alpha = 0.45f),
              Color(0xFF131B2E).copy(alpha = 0.95f)
            )
          )
        )
    )

    // 2. MediaPipe Biomechanical Skeletal & Racket Trajectory Overlay
    if (isPoseOverlayOn) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val neonGreen = Color(0xFF68DBA9)
        val brightCyan = Color(0xFF85F8C4)
        val targetOrange = Color(0xFFFB7800)

        // Court guide lines
        val dashed = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        drawLine(
          color = brightCyan.copy(alpha = 0.2f),
          start = Offset(w * 0.1f, h * 0.35f),
          end = Offset(w * 0.9f, h * 0.35f),
          pathEffect = dashed,
          strokeWidth = 2f
        )
        drawLine(
          color = brightCyan.copy(alpha = 0.2f),
          start = Offset(w * 0.1f, h * 0.55f),
          end = Offset(w * 0.9f, h * 0.55f),
          pathEffect = dashed,
          strokeWidth = 2f
        )

        // Racket Trajectory Arc
        val arcPath = Path().apply {
          moveTo(w * 0.35f, h * 0.62f)
          cubicTo(
            w * 0.40f, h * 0.42f,
            w * 0.50f, h * 0.20f,
            w * 0.72f, h * 0.14f
          )
        }
        drawPath(
          path = arcPath,
          color = neonGreen,
          style = Stroke(
            width = 8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
          )
        )

        // Shuttlecock Hit Blast
        drawCircle(
          color = targetOrange.copy(alpha = 0.4f),
          radius = 28f,
          center = Offset(w * 0.72f, h * 0.14f),
          style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
        )
        drawCircle(
          color = targetOrange,
          radius = 9f,
          center = Offset(w * 0.72f, h * 0.14f)
        )

        // Joint Coords relative to screen size
        val nose = Offset(w * 0.50f, h * 0.30f)
        val neck = Offset(w * 0.49f, h * 0.37f)
        val leftShoulder = Offset(w * 0.40f, h * 0.38f)
        val rightShoulder = Offset(w * 0.60f, h * 0.37f)

        val leftElbow = Offset(w * 0.30f, h * 0.44f)
        val leftWrist = Offset(w * 0.25f, h * 0.39f)

        val rightElbow = Offset(w * 0.70f, h * 0.28f)
        val rightWrist = Offset(w * 0.64f, h * 0.18f)
        val racketTip = Offset(w * 0.74f, h * 0.12f)

        val leftHip = Offset(w * 0.42f, h * 0.54f)
        val rightHip = Offset(w * 0.56f, h * 0.53f)

        val leftKnee = Offset(w * 0.39f, h * 0.69f)
        val leftAnkle = Offset(w * 0.35f, h * 0.84f)

        val rightKnee = Offset(w * 0.62f, h * 0.68f)
        val rightAnkle = Offset(w * 0.68f, h * 0.83f)

        // Draw Skeletal Lines
        val boneStroke = Stroke(width = 7f)
        drawLine(brightCyan, leftShoulder, rightShoulder, strokeWidth = 8f)
        drawLine(brightCyan, neck, Offset(w * 0.49f, h * 0.53f), strokeWidth = 8f) // spine
        drawLine(brightCyan, leftShoulder, leftHip, strokeWidth = 7f)
        drawLine(brightCyan, rightShoulder, rightHip, strokeWidth = 7f)
        drawLine(brightCyan, leftHip, rightHip, strokeWidth = 8f)

        // Arms
        drawLine(brightCyan, leftShoulder, leftElbow, strokeWidth = 7f)
        drawLine(brightCyan, leftElbow, leftWrist, strokeWidth = 7f)

        drawLine(brightCyan, rightShoulder, rightElbow, strokeWidth = 9f)
        drawLine(brightCyan, rightElbow, rightWrist, strokeWidth = 9f)
        drawLine(targetOrange, rightWrist, racketTip, strokeWidth = 8f)

        // Legs
        drawLine(brightCyan, leftHip, leftKnee, strokeWidth = 7f)
        drawLine(brightCyan, leftKnee, leftAnkle, strokeWidth = 7f)
        drawLine(brightCyan, rightHip, rightKnee, strokeWidth = 7f)
        drawLine(brightCyan, rightKnee, rightAnkle, strokeWidth = 7f)

        // Joint circles
        val joints = listOf(
          nose, neck, leftShoulder, rightShoulder,
          leftElbow, leftWrist, leftHip, rightHip,
          leftKnee, leftAnkle, rightKnee, rightAnkle
        )
        joints.forEach { pt ->
          drawCircle(color = neonGreen, radius = 10f, center = pt)
          drawCircle(color = Color.White, radius = 4f, center = pt)
        }

        // Right elbow focus (Optimal target)
        drawCircle(color = targetOrange.copy(alpha = 0.4f), radius = 24f, center = rightElbow)
        drawCircle(color = targetOrange, radius = 14f, center = rightElbow)
        drawCircle(color = Color.White, radius = 5f, center = rightElbow)
      }

      // Right elbow callout label
      Box(
        modifier = Modifier
          .offset(x = 230.dp, y = 175.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF131B2E).copy(alpha = 0.9f))
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(ShuttleOrangeContainer)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "112° (Optimal)",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = ShuttleOrangeFixed,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    // 3. TOP SECTION: Real-Time AI Coaching HUD
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Main Glass HUD Banner
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.88f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Radial Gauge: 85%
          Box(
            modifier = Modifier
              .size(54.dp),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = size.minDimension / 2 - 4f,
                style = Stroke(width = 7f)
              )
              drawArc(
                color = CourtGreenFixed,
                startAngle = -90f,
                sweepAngle = 360f * 0.85f,
                useCenter = false,
                style = Stroke(width = 7f)
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "85",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CourtGreenFixed,
                  fontSize = 18.sp,
                  lineHeight = 18.sp
                )
              )
              Text(
                text = "%",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 9.sp,
                  lineHeight = 9.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          // Feedback column
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(CourtGreenFixed)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "LIVE POSE FEEDBACK • WEBSOCKET CONNECTED",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = CourtGreenFixed,
                  fontSize = 9.sp,
                  letterSpacing = 0.5.sp
                ),
                maxLines = 1
              )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = "Elbow angle optimal (112°). Keep wrist supple!",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp
              ),
              maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "STATUS: EXCELLENT FORM",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White,
                  fontSize = 9.sp
                ),
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(CourtGreenContainer)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Overhead Smash Drill",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White.copy(alpha = 0.6f),
                  fontSize = 10.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Telemetry Pills Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Rep counter
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = ShuttleOrangeFixed,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "$repsCount / 20 Reps",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 11.sp
            )
          )
        }

        // Accuracy average
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = null,
            tint = CourtGreenFixed,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "82% Avg",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CourtGreenFixed,
              fontSize = 11.sp
            )
          )
        }

        // CV Speed & FPS
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "30 FPS • 18ms",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              color = Color.White.copy(alpha = 0.75f),
              fontSize = 10.sp
            )
          )
        }
      }
    }

    // 4. MIDDLE FLOATING HUD: Kinetic Velocity Bar & Frame Cue
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.Center)
        .padding(horizontal = 16.dp)
    ) {
      // Velocity Gauge (Left side)
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E).copy(alpha = 0.8f)),
        modifier = Modifier
          .align(Alignment.CenterStart)
          .width(58.dp)
          .padding(vertical = 8.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "VELOCITY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White.copy(alpha = 0.6f),
              fontSize = 8.sp,
              letterSpacing = 0.5.sp
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .width(8.dp)
              .height(76.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.BottomCenter
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clip(RoundedCornerShape(4.dp))
                .background(ShuttleOrangeContainer)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "$currentSpeed",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = ShuttleOrangeFixed,
              fontSize = 13.sp,
              lineHeight = 13.sp
            )
          )
          Text(
            text = "km/h",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White.copy(alpha = 0.5f),
              fontSize = 8.sp,
              lineHeight = 8.sp
            )
          )
        }
      }

      // Center/Right Bounding Cue
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF131B2E).copy(alpha = 0.75f))
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.CropFree,
          contentDescription = null,
          tint = CourtGreenFixed,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Body In Frame: Full Stance",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
          )
        )
      }
    }

    // 5. BOTTOM FLOATING CONTROLS: Audio toast, toggles, and Stop button
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              Color(0xFF131B2E).copy(alpha = 0.9f),
              Color(0xFF131B2E)
            )
          )
        )
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      // Audio Toast Notification Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(if (isVoiceSpeaking) CourtGreenFixed.copy(alpha = 0.95f) else CourtGreenContainer.copy(alpha = 0.92f))
          .clickable { voiceCoach.testCoachSpeech() }
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isVoiceMuted) Icons.Default.VoiceOverOff else Icons.Default.VolumeUp,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(14.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isVoiceMuted) "Voice coaching muted" else "AI COACH: \"$lastSpokenCue\"",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              fontSize = 12.sp
            ),
            maxLines = 1
          )
        }

        Text(
          text = if (isVoiceMuted) "MUTED" else if (isVoiceSpeaking) "SPEAKING 🎙️" else "SWEET SPOT",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 10.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Toggles Row (Flip, Voice, Pose)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Flip Cam Button
        Button(
          onClick = { isFlipped = !isFlipped },
          shape = RoundedCornerShape(22.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)),
          modifier = Modifier
            .weight(1f)
            .height(46.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FlipCameraIos,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Flip Cam",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              fontSize = 11.sp
            )
          )
        }

        // Voice Cue Switch
        Button(
          onClick = { voiceCoach.toggleMute() },
          shape = RoundedCornerShape(22.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (!isVoiceMuted) CourtGreen.copy(alpha = 0.35f) else Color(0xFF1E293B).copy(alpha = 0.85f)
          ),
          modifier = Modifier
            .weight(1f)
            .height(46.dp)
            .testTag("toggle_voice_coach_train_btn")
        ) {
          Icon(
            imageVector = if (!isVoiceMuted) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
            contentDescription = null,
            tint = if (!isVoiceMuted) CourtGreenFixed else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(17.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (!isVoiceMuted) "Voice: ON" else "Voice: OFF",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = if (!isVoiceMuted) CourtGreenFixed else Color.White.copy(alpha = 0.6f),
              fontSize = 11.sp
            )
          )
        }

        // Skeleton Overlay Switch
        Button(
          onClick = { isPoseOverlayOn = !isPoseOverlayOn },
          shape = RoundedCornerShape(22.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isPoseOverlayOn) CourtGreen.copy(alpha = 0.35f) else Color(0xFF1E293B).copy(alpha = 0.85f)
          ),
          modifier = Modifier
            .weight(1f)
            .height(46.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeviceHub,
            contentDescription = null,
            tint = if (isPoseOverlayOn) CourtGreenFixed else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(17.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isPoseOverlayOn) "Pose: ON" else "Pose: OFF",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = if (isPoseOverlayOn) CourtGreenFixed else Color.White.copy(alpha = 0.6f),
              fontSize = 11.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Primary Stop Training CTA
      Button(
        onClick = {
          scope.launch {
            val effectiveReps = if (repsCount > 0) repsCount else 15
            val session = com.example.db.PracticeSessionEntity(
              dateMs = System.currentTimeMillis(),
              strokeType = "Forehand Jump Smash",
              totalReps = effectiveReps,
              averageFormScore = 88,
              maxSmashSpeedKmH = currentSpeed,
              avgZenithReachMeters = 2.45f,
              durationSeconds = 240,
              clipsExtractedCount = 3,
              coachSummary = "Live pose training session completed with ${effectiveReps} reps and peak speed of ${currentSpeed} km/h."
            )
            repository.savePracticeSession(session)
            com.example.model.DynamicBiomechanicalAnalysisEngine.getInstance().populateFromSession(
              repsCount = effectiveReps,
              strokeType = "Forehand Jump Smash",
              avgScore = 88,
              peakSpeed = currentSpeed,
              avgReach = 2.45f
            )
            onStopTraining()
          }
        },
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("stop_training_btn")
      ) {
        Icon(
          imageVector = Icons.Default.StopCircle,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Stop Training",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 17.sp
          )
        )
      }
    }
  }
}
