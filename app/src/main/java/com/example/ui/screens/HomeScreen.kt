package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.db.AceRepository
import com.example.model.AceImages
import com.example.model.AuthState
import com.example.model.CameraCalibrationManager
import com.example.model.FirebaseAuthService
import com.example.ui.components.CloudSyncBanner
import com.example.ui.components.DailyGoalReminderSheet
import com.example.ui.components.FirebaseAuthDialog
import com.example.ui.components.LogManualPracticeSheet
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

@Composable
fun HomeScreen(
  onStartLiveCoaching: () -> Unit,
  onOpenDrillDetail: (String) -> Unit,
  onOpenOpticalAlignment: () -> Unit,
  onOpenCalibrationHub: () -> Unit,
  onOpenProfile: () -> Unit = {},
  onOpenLiveCameraPose: () -> Unit = onStartLiveCoaching,
  onOpenDrillsLibrary: () -> Unit = {},
  onOpenWarmupRoutine: () -> Unit = {},
  onOpenAIChat: () -> Unit = {},
  onOpenLiveVoiceCoach: () -> Unit = {},
  onOpenLogin: () -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repository = remember { AceRepository.getInstance(context) }
  val sessions by repository.allSessions.collectAsState(initial = emptyList())
  val userProfile by repository.userProfile.collectAsState(initial = null)
  val drills by repository.allDrills.collectAsState(initial = emptyList())

  val scrollState = rememberScrollState()
  val authService = remember { FirebaseAuthService.getInstance(context) }
  val authState by authService.authState.collectAsState()
  val syncStatus by authService.syncStatus.collectAsState()
  val calibrationManager = remember { CameraCalibrationManager.getInstance(context) }
  val isCalibrationLocked by calibrationManager.isCalibrationLocked.collectAsState()
  val calibrationTelemetry by calibrationManager.telemetry.collectAsState()
  var showAuthDialog by remember { mutableStateOf(false) }
  var showGoalReminderSheet by remember { mutableStateOf(false) }
  var showLogManualSheet by remember { mutableStateOf(false) }

  // Dynamic Real-Time Calculations driven directly by Room DB sessions & userProfile
  val totalRepsLogged = remember(sessions) { sessions.sumOf { it.totalReps } }
  val totalPracticeMinutes = remember(sessions) { sessions.sumOf { it.durationSeconds } / 60 }
  val avgFormScore = remember(sessions) {
    if (sessions.isNotEmpty()) sessions.map { it.averageFormScore }.average().toInt() else (userProfile?.averageFormScore ?: 0)
  }
  val maxSmashSpeed = remember(sessions) {
    if (sessions.isNotEmpty()) sessions.maxOf { it.maxSmashSpeedKmH } else 0
  }
  val totalXp = remember(sessions) {
    sessions.sumOf { it.totalReps * 10 + it.averageFormScore * 5 }
  }

  val (currentLevel, rankBadge, nextLevelXp) = remember(totalXp) {
    when {
      totalXp < 500 -> Triple(1, "🥉 Beginner I", 500)
      totalXp < 1000 -> Triple(2, "🥉 Beginner II", 1000)
      totalXp < 2000 -> Triple(3, "🥉 Beginner III", 2000)
      totalXp < 3500 -> Triple(4, "🥈 Silver I", 3500)
      totalXp < 5500 -> Triple(5, "🥈 Silver II", 5500)
      totalXp < 8000 -> Triple(6, "🥇 Gold Elite", 8000)
      else -> Triple(7, "🏆 Master Pro", 12000)
    }
  }

  val xpProgressFraction = remember(totalXp, nextLevelXp) {
    if (totalXp == 0) 0f
    else {
      val prevXp = when (nextLevelXp) {
        500 -> 0
        1000 -> 500
        2000 -> 1000
        3500 -> 2000
        5500 -> 3500
        8000 -> 5500
        else -> 8000
      }
      val currentInTier = (totalXp - prevXp).coerceAtLeast(0)
      val span = nextLevelXp - prevXp
      (currentInTier.toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }
  }

  val currentStreakDays = remember(sessions) {
    if (sessions.isEmpty()) 0
    else {
      val days = sessions.map {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.dateMs))
      }.toSet()
      days.size
    }
  }

  val greetingName = remember(authState) {
    if (authState is AuthState.Authenticated) {
      val user = (authState as AuthState.Authenticated).user
      user.displayName?.substringBefore(' ') ?: "Athlete"
    } else {
      "Athlete"
    }
  }

  if (showAuthDialog) {
    FirebaseAuthDialog(
      authService = authService,
      authState = authState,
      onDismiss = { showAuthDialog = false }
    )
  }

  if (showGoalReminderSheet) {
    DailyGoalReminderSheet(
      onDismiss = { showGoalReminderSheet = false }
    )
  }

  if (showLogManualSheet) {
    LogManualPracticeSheet(
      onDismiss = { showLogManualSheet = false },
      onSessionLogged = {
        // Triggered upon successful manual practice save
      }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("home_screen_content")
  ) {
    // 1. Greeting & Live CV Status Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Hello, $greetingName! 🏸",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Ready for today's smash practice?",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = DarkSurfaceVariant
          )
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Gemini 3.8 Live Voice & Lyria AI Button
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(ShuttleOrange.copy(alpha = 0.15f))
            .clickable { onOpenLiveVoiceCoach() }
            .testTag("home_live_voice_lyria_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Gemini Live Voice & Lyria AI Music",
            tint = ShuttleOrange,
            modifier = Modifier.size(20.dp)
          )
        }

        // Daily Drill Reminders Action Pill
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(CourtGreen.copy(alpha = 0.12f))
            .clickable { showGoalReminderSheet = true }
            .testTag("home_daily_reminder_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Daily Goal Reminders",
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
        }

        // Reset All Data Action Button
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .clickable {
              scope.launch {
                repository.clearAllPracticeSessions()
                Toast.makeText(context, "All practice session data reset to 0", Toast.LENGTH_SHORT).show()
              }
            }
            .testTag("home_reset_all_data_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Reset All Data",
            tint = DarkSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }

        // Cloud Auth Status Pill
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .clickable {
              if (authState !is AuthState.Authenticated) {
                onOpenLogin()
              } else {
                showAuthDialog = true
              }
            }
            .testTag("home_quick_cloud_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = "Cloud Sync & Profile",
            tint = DarkSurface,
            modifier = Modifier.size(20.dp)
          )
          Box(
            modifier = Modifier
              .size(8.dp)
              .align(Alignment.TopEnd)
              .padding(top = 4.dp, end = 4.dp)
              .clip(CircleShape)
              .background(if (syncStatus.isCloudConnected) CourtGreen else ShuttleOrangeContainer)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Live CV Engine Online Badge
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(SurfaceContainerHigh)
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(CourtGreen)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "LIVE CV ENGINE ONLINE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurfaceVariant,
          letterSpacing = 0.8.sp
        )
      )
      Spacer(modifier = Modifier.width(6.dp))
      Icon(
        imageVector = Icons.Default.SportsTennis,
        contentDescription = null,
        tint = CourtGreen,
        modifier = Modifier.size(15.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Firebase Cloud Sync Banner
    CloudSyncBanner(
      authState = authState,
      syncStatus = syncStatus,
      onOpenAuthDialog = {
        if (authState !is AuthState.Authenticated) {
          onOpenLogin()
        } else {
          showAuthDialog = true
        }
      },
      onTriggerSync = { authService.triggerCloudSync() }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Gamified Hero Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      modifier = Modifier
        .fillMaxWidth()
        .shadow(10.dp, RoundedCornerShape(20.dp))
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              CourtGreen,
              CourtGreenContainer,
              CourtGreen
            )
          ),
          shape = RoundedCornerShape(20.dp)
        )
        .clickable { onOpenProfile() }
        .testTag("hero_profile_card")
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        // Watermark Court Lines
        Canvas(modifier = Modifier.matchParentSize()) {
          val stroke = 1.5f
          val courtColor = Color.White.copy(alpha = 0.08f)
          drawRect(
            color = courtColor,
            topLeft = Offset(10f, 10f),
            size = Size(size.width - 20f, size.height - 20f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
          )
          drawLine(
            color = courtColor,
            start = Offset(size.width / 2, 10f),
            end = Offset(size.width / 2, size.height - 10f),
            strokeWidth = stroke
          )
          drawLine(
            color = courtColor,
            start = Offset(10f, size.height / 2),
            end = Offset(size.width - 10f, size.height / 2),
            strokeWidth = stroke
          )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
          // Top badge row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
              Text(
                text = rankBadge,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(ShuttleOrangeContainer)
                .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$currentStreakDays Day Streak 🔥",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // XP numbers
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "%,d".format(totalXp),
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White,
                  fontSize = 24.sp
                )
              )
              Text(
                text = " / %,d XP".format(nextLevelXp),
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 2.dp)
              )
            }

            val xpNeeded = (nextLevelXp - totalXp).coerceAtLeast(0)
            Text(
              text = if (xpNeeded > 0) "%,d XP TO NEXT LEVEL".format(xpNeeded) else "MAX LEVEL REACHED",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreenFixed,
                fontSize = 10.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Progress bar
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(Color.White.copy(alpha = 0.25f))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(xpProgressFraction)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Daily goal row (clickable to open reminder settings)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color.White.copy(alpha = 0.18f))
              .clickable { showGoalReminderSheet = true }
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .testTag("daily_goal_card_banner"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = null,
                tint = CourtGreenFixed,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Daily Goal: 3 Sessions Logged",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
              )
            }

            Text(
              text = "${sessions.size.coerceAtMost(3)} / 3 Drills completed",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Real-Time Quick Stats Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Total Smashes
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.weight(1f)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "TOTAL SMASHES",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "%,d".format(totalRepsLogged),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = CourtGreen,
              fontSize = 18.sp
            )
          )
        }
      }

      // Avg Form Score
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.weight(1f)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "AVG FORM SCORE",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (avgFormScore > 0) "$avgFormScore%" else "--",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = ShuttleOrange,
              fontSize = 18.sp
            )
          )
        }
      }

      // Peak Speed
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.weight(1f)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "PEAK SPEED",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (maxSmashSpeed > 0) "$maxSmashSpeed km/h" else "--",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DarkSurface,
              fontSize = 16.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Real-Time Action Row: Start Live Camera vs Log Manual Practice
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { showLogManualSheet = true },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("home_log_manual_practice_btn")
      ) {
        Icon(
          imageVector = Icons.Default.FitnessCenter,
          contentDescription = null,
          tint = DarkSurface,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Log Practice",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }

      Button(
        onClick = { onOpenLiveCameraPose() },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
        modifier = Modifier
          .weight(1.3f)
          .height(50.dp)
          .testTag("home_start_camera_session_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Videocam,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Live Camera",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 3. START LIVE COACHING CTA Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenLiveCameraPose() }
        .testTag("start_live_coaching_card")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          // Play Button Icon
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(CourtGreen),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CourtGreen.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(CourtGreen)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "AI POSE DETECTION ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CourtGreen,
                  fontSize = 9.sp,
                  letterSpacing = 0.8.sp
                )
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "START LIVE COACHING",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DarkSurface,
                letterSpacing = (-0.5).sp
              )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = "Real-time posture feedback, swing angle & stroke accuracy",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                lineHeight = 16.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Requires Camera & 1.5m clearance",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = DarkSurfaceVariant
              )
            )
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Proceed",
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Featured Coach Maya AI Chatbox Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenAIChat() }
        .testTag("ai_coach_chat_entry_card")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(CourtGreen),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "COACH MAYA AI • INBUILT MODEL",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = CourtGreen,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "Ask AI Coach About Your Telemetry",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface,
              fontSize = 14.sp
            )
          )

          Text(
            text = "Ask about fault Rep #7, elbow angles, power drills & BWF pro stats",
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CourtGreen.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Chat ↗",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CourtGreen,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. COURT CAMERA READY Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenOpticalAlignment() }
        .testTag("court_camera_ready_card")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(width = 72.dp, height = 58.dp)
            .clip(RoundedCornerShape(10.dp))
        ) {
          AsyncImage(
            model = AceImages.COURT_CAMERA_READY,
            contentDescription = "Court camera preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(CourtGreen.copy(alpha = 0.2f))
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isCalibrationLocked) "OPTICAL CALIBRATION LOCKED [98%]" else "COURT CAMERA READY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CourtGreen,
              fontSize = 10.sp
            )
          )
          Text(
            text = "Angle: ${"%.1f".format(calibrationTelemetry.pitchDeg)}° • Distance: ${"%.1f".format(calibrationTelemetry.estimatedDistanceM)}m",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = DarkSurface,
              fontSize = 13.sp
            ),
            maxLines = 1
          )
          Text(
            text = "Tilt ${"%.1f".format(calibrationTelemetry.rollDeg)}° horizon • 90 FPS 3D Kinematics",
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(CourtGreen)
              .clickable { onOpenLiveCameraPose() }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("open_live_camera_preview_btn")
          ) {
            Text(
              text = "Live View",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(SurfaceContainerHigh)
              .clickable { onOpenOpticalAlignment() }
              .padding(horizontal = 12.dp, vertical = 5.dp)
              .testTag("calibrate_btn")
          ) {
            Text(
              text = "Calibrate",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen
              )
            )
          }
        }
      }
    }

    // Recent Practice Sessions (Live Room DB Flow)
    Spacer(modifier = Modifier.height(14.dp))
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth().testTag("home_recent_sessions_card")
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(CourtGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "RECENT PRACTICE SESSIONS (${sessions.size})",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = CourtGreen,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
              )
            )
          }
          Text(
            text = "Live DB",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 10.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sessions.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(SurfaceContainerLow)
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "No practice sessions recorded yet",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Start a Live Camera tracking session or tap 'Log Practice' to record your first reps and build your streak!",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              )
            }
          }
        } else {
          sessions.take(3).forEach { s ->
            val dateStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(s.dateMs))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .clickable { onOpenProfile() }
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = s.strokeType,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkSurface
                  )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "$dateStr • ${s.totalReps} Reps • ${s.durationSeconds / 60}m • ${s.maxSmashSpeedKmH} km/h",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkSurfaceVariant,
                    fontSize = 11.sp
                  )
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(CourtGreen.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${s.averageFormScore}% Form",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtGreen,
                    fontSize = 11.sp
                  )
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Dedicated Searchable Drills Library Feature Card
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenDrillsLibrary() }
        .testTag("open_drills_library_card")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(CourtGreen.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.LibraryBooks,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Drills Library",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(CourtGreen)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "10+ DRILLS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 8.sp
                  )
                )
              }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Searchable routines with skill metrics, steps & voice cues",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Open Drills Library",
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Dedicated Dynamic Warmup & Injury Prevention Card
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenWarmupRoutine() }
        .testTag("open_warmup_routine_card")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(ShuttleOrange.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "💪",
              fontSize = 22.sp
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Warmup Routine",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(ShuttleOrange)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "INJURY SHIELD",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 8.sp
                  )
                )
              }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Dynamic stretches for wrist snap, shoulder cuff & ankles",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Open Warmup Routine",
          tint = ShuttleOrange,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 5. Recommended Drills Carousel
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Recommended Drills",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurface
        )
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable { onOpenDrillsLibrary() }
          .testTag("recommended_drills_view_all_btn")
      ) {
        Text(
          text = "View All",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = CourtGreen
          )
        )
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Horizontal list of drills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Drill 1: Ready Position Stance
      DrillCardItem(
        category = "Foundational",
        categoryColor = CourtGreen,
        time = "3 mins",
        title = "Ready Position Stance",
        desc = "Base center posture & athletic balance",
        personalBest = "88% Best",
        icon = Icons.Default.SportsMartialArts,
        isHot = false,
        onClick = { onOpenDrillDetail("ready-stance") }
      )

      // Drill 2: Forehand High Clear (HOT)
      DrillCardItem(
        category = "Intermediate",
        categoryColor = ShuttleOrange,
        time = "5 mins",
        title = "Forehand High Clear",
        desc = "Full arc pronation & wrist whip",
        personalBest = "72% Best",
        icon = Icons.Default.SportsTennis,
        isHot = true,
        onClick = { onOpenDrillDetail("forehand-smash") }
      )

      // Drill 3: Split-Step Footwork
      DrillCardItem(
        category = "Agility",
        categoryColor = DarkSurfaceVariant,
        time = "4 mins",
        title = "Split-Step Footwork",
        desc = "First-step reaction speed",
        personalBest = "65% Best",
        icon = Icons.Default.DirectionsRun,
        isHot = false,
        onClick = { onOpenDrillDetail("split-step") }
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 6. Recent Coach Bio-Feedback Tip Widget
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(CourtGreenFixed),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = CourtGreenContainer,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "COACH BIO-FEEDBACK",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen,
                fontSize = 10.sp
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFBCCAC0))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Overhead Clear",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Coach Tip: Keep your elbow relaxed at ~110° before uncoiling the wrist snap for overhead clears.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = DarkSurface,
              fontSize = 13.sp,
              lineHeight = 18.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 7. Quick Pose Calibration CTA Button
    Button(
      onClick = { onOpenOpticalAlignment() },
      shape = RoundedCornerShape(26.dp),
      colors = ButtonDefaults.buttonColors(containerColor = ShuttleOrangeContainer),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("quick_pose_calibration_btn")
    ) {
      Icon(
        imageVector = Icons.Default.Videocam,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Quick Pose Calibration",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun DrillCardItem(
  category: String,
  categoryColor: Color,
  time: String,
  title: String,
  desc: String,
  personalBest: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isHot: Boolean,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier
      .width(220.dp)
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = category,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = categoryColor,
              fontSize = 10.sp
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(categoryColor.copy(alpha = 0.12f))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          )

          if (isHot) {
            Text(
              text = "HOT",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 9.sp
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(ShuttleOrangeContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            )
          } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = DarkSurfaceVariant,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = time,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface,
            fontSize = 15.sp
          ),
          maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = desc,
          style = MaterialTheme.typography.bodySmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 11.sp
          ),
          maxLines = 2,
          minLines = 2
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceContainerLow)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Personal Best",
          style = MaterialTheme.typography.labelSmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 10.sp
          )
        )
        Text(
          text = personalBest,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = categoryColor,
            fontSize = 11.sp
          )
        )
      }
    }
  }
}
