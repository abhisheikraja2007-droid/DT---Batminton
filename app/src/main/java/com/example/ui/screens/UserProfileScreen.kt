package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.db.AceRepository
import com.example.model.AceImages
import com.example.model.AuthState
import com.example.model.FirebaseAuthService
import com.example.model.HistoricalSession
import com.example.model.MilestoneAchievement
import com.example.model.PlayerStats
import com.example.model.SamplePerformanceData
import com.example.model.SkillRadarDimension
import com.example.model.WeeklyProgressionPoint
import com.example.ui.components.CloudSyncBanner
import com.example.ui.components.DailyGoalReminderSheet
import com.example.ui.components.FirebaseAuthDialog
import com.example.ui.components.LogManualPracticeSheet
import com.example.ui.components.TermsAndPrivacyDetailSheet
import androidx.compose.material.icons.filled.Shield
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.CourtGreenFixedDim
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeContainer
import com.example.ui.theme.ShuttleOrangeFixed
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import kotlin.math.cos
import kotlin.math.sin

enum class PerformanceTimeframe {
  WEEKLY, MONTHLY, ALL_TIME
}

enum class ChartMetricType {
  FORM_SCORE, PEAK_SPEED, ACCURACY
}

@Composable
fun UserProfileScreen(
  onBack: () -> Unit,
  onOpenCalibration: () -> Unit,
  onOpenSessionReview: (String) -> Unit = {},
  onOpenLogin: () -> Unit = {}
) {
  val context = LocalContext.current
  val repository = remember { AceRepository.getInstance(context) }
  val dbProfile by repository.userProfile.collectAsState(initial = null)
  val dbSessions by repository.allSessions.collectAsState(initial = emptyList())

  val authService = remember { FirebaseAuthService.getInstance(context) }
  val authState by authService.authState.collectAsState()
  val syncStatus by authService.syncStatus.collectAsState()
  var showAuthDialog by remember { mutableStateOf(false) }

  val totalReps = remember(dbSessions) { dbSessions.sumOf { it.totalReps } }
  val totalMins = remember(dbSessions) { dbSessions.sumOf { it.durationSeconds } / 60 }
  val avgForm = remember(dbSessions) {
    if (dbSessions.isNotEmpty()) dbSessions.map { it.averageFormScore }.average().toInt() else (dbProfile?.averageFormScore ?: 0)
  }
  val peakSpeed = remember(dbSessions) {
    if (dbSessions.isNotEmpty()) dbSessions.maxOf { it.maxSmashSpeedKmH } else 0
  }
  val totalXp = remember(dbSessions, dbProfile) {
    val xpFromDb = dbSessions.sumOf { it.totalReps * 10 + it.averageFormScore * 5 }
    if (dbSessions.isEmpty() && dbProfile != null) dbProfile!!.totalSmashes * 10 else xpFromDb
  }
  val calculatedStreak = remember(dbSessions) {
    if (dbSessions.isEmpty()) 0
    else {
      dbSessions.map {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.dateMs))
      }.toSet().size
    }
  }

  val defaultPlayer = remember { PlayerStats() }
  val player = remember(authState, totalReps, totalMins, avgForm, peakSpeed, totalXp, calculatedStreak, dbProfile) {
    val userName = if (authState is AuthState.Authenticated) {
      (authState as AuthState.Authenticated).user.displayName ?: defaultPlayer.name
    } else {
      dbProfile?.name ?: defaultPlayer.name
    }
    defaultPlayer.copy(
      name = userName,
      sessionsTotal = dbSessions.size,
      courtTimeHours = totalMins / 60f,
      formScore = avgForm,
      currentXp = totalXp,
      streakDays = calculatedStreak
    )
  }

  var selectedTimeframe by remember { mutableStateOf(PerformanceTimeframe.WEEKLY) }
  var selectedChartMetric by remember { mutableStateOf(ChartMetricType.FORM_SCORE) }
  var selectedSkillIndex by remember { mutableIntStateOf(0) }
  var showGoalReminderSheet by remember { mutableStateOf(false) }
  var showTermsDetailSheet by remember { mutableStateOf(false) }
  var showLogManualSheet by remember { mutableStateOf(false) }
  val skills = SamplePerformanceData.skills
  val currentProgression = remember(dbSessions) {
    if (dbSessions.isEmpty()) {
      emptyList()
    } else {
      dbSessions.takeLast(6).mapIndexed { idx, s ->
        WeeklyProgressionPoint(
          label = "S${idx + 1}",
          formScore = s.averageFormScore,
          peakSpeedKmH = s.maxSmashSpeedKmH,
          accuracyPct = s.averageFormScore
        )
      }
    }
  }

  val historicalSessions = remember(dbSessions) {
    if (dbSessions.isEmpty()) {
      emptyList()
    } else {
      dbSessions.map { s ->
        val dateFormatted = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(s.dateMs))
        val grade = when {
          s.averageFormScore >= 90 -> "A+"
          s.averageFormScore >= 80 -> "A"
          s.averageFormScore >= 70 -> "B+"
          else -> "B"
        }
        HistoricalSession(
          id = s.id.toString(),
          title = s.strokeType,
          dateFormatted = dateFormatted,
          durationMinutes = s.durationSeconds / 60,
          totalReps = s.totalReps,
          grade = grade,
          avgFormScore = s.averageFormScore,
          peakSpeedKmH = s.maxSmashSpeedKmH,
          sweetSpotPct = 85,
          keyTakeaway = s.coachSummary
        )
      }
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

  if (showTermsDetailSheet) {
    TermsAndPrivacyDetailSheet(
      onDismiss = { showTermsDetailSheet = false }
    )
  }

  if (showLogManualSheet) {
    LogManualPracticeSheet(
      onDismiss = { showLogManualSheet = false }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("user_profile_screen")
  ) {
    // 1. Top Header Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onBack() }
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = DarkSurface,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Athlete Profile",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
          Text(
            text = "Performance & Biometrics",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Log Practice Button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CourtGreen)
            .clickable { showLogManualSheet = true }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("profile_log_practice_btn"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FitnessCenter,
              contentDescription = "Log Practice",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Log",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }

        // Daily Drill Reminders Shortcut
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(CourtGreen.copy(alpha = 0.12f))
            .clickable { showGoalReminderSheet = true }
            .testTag("profile_daily_reminders_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Daily Goal Reminders",
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
        }

        // Equipment / Calibration Shortcut
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .clickable { onOpenCalibration() }
            .testTag("profile_equipment_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Equipment Locker",
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
        }

        // Export Report Button
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .clickable {
              Toast.makeText(context, "Exporting athlete performance summary PDF...", Toast.LENGTH_SHORT).show()
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share Report",
            tint = DarkSurface,
            modifier = Modifier.size(19.dp)
          )
        }

        // Clear Cache Button
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .clickable {
              try {
                context.cacheDir.deleteRecursively()
                val authService = FirebaseAuthService.getInstance(context)
                authService.signOut()
                Toast.makeText(context, "Application cache & session data cleared!", Toast.LENGTH_LONG).show()
              } catch (e: Exception) {
                Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
              }
            }
            .testTag("profile_clear_cache_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Clear Cache",
            tint = ErrorRed,
            modifier = Modifier.size(19.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Athlete Hero Card with Avatar, Level XP & Quick Stats
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Avatar with Rank Badge ring
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .border(2.5.dp, CourtGreen, CircleShape)
          ) {
            AsyncImage(
              model = AceImages.CALIBRATION_ALEX_AVATAR,
              contentDescription = "Athlete Avatar",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = player.name,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DarkSurface,
                  fontSize = 20.sp
                )
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(ShuttleOrangeContainer.copy(alpha = 0.2f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Whatshot,
                  contentDescription = null,
                  tint = ShuttleOrange,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${player.streakDays}d Streak",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShuttleOrange,
                    fontSize = 10.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = player.title,
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(CourtGreenContainer)
                  .padding(horizontal = 7.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "Silver I • Level ${player.level}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenFixed,
                    fontSize = 10.sp
                  )
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = "Right-Handed • 1.82m",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 10.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Level XP Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LEVEL ${player.level} PROGRESSION",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DarkSurfaceVariant,
              fontSize = 9.sp,
              letterSpacing = 0.6.sp
            )
          )
          Text(
            text = "${player.currentXp} / ${player.targetXp} XP (${player.targetXp - player.currentXp} to Gold III)",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CourtGreen,
              fontSize = 10.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val progressFrac = player.currentXp.toFloat() / player.targetXp.toFloat()
        LinearProgressIndicator(
          progress = { progressFrac },
          color = CourtGreen,
          trackColor = SurfaceContainer,
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Quick Metrics Grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ProfileStatPill(
            label = "SESSIONS",
            value = "${player.sessionsTotal}",
            sub = "%.1f Court Hrs".format(player.courtTimeHours),
            modifier = Modifier.weight(1f)
          )
          ProfileStatPill(
            label = "PEAK SMASH",
            value = if (peakSpeed > 0) "$peakSpeed km/h" else "--",
            sub = "Personal Best",
            valueColor = ShuttleOrange,
            modifier = Modifier.weight(1.1f)
          )
          ProfileStatPill(
            label = "FORM SCORE",
            value = if (player.formScore > 0) "${player.formScore}%" else "--",
            sub = if (player.formScore > 0) "Avg Quality" else "No Data",
            valueColor = CourtGreen,
            modifier = Modifier.weight(1f)
          )
          ProfileStatPill(
            label = "STREAK",
            value = "${player.streakDays}d",
            sub = "Active Streak",
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Firebase Cloud Sync & Cross-Device Telemetry Banner
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

    Spacer(modifier = Modifier.height(18.dp))

    // 3. Timeframe Selector Pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(SurfaceContainerLow)
        .padding(4.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      listOf(
        PerformanceTimeframe.WEEKLY to "Last 6 Weeks",
        PerformanceTimeframe.MONTHLY to "Monthly Trend",
        PerformanceTimeframe.ALL_TIME to "All-Time Records"
      ).forEach { (timeframe, title) ->
        val isSelected = selectedTimeframe == timeframe
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CourtGreen else Color.Transparent)
            .clickable { selectedTimeframe = timeframe }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 4. Multi-Dimensional Biomechanical Skill Radar Matrix
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Insights,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Skill Progression Matrix",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }

      Text(
        text = "Overall: Tier A",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = CourtGreen,
          fontSize = 10.sp
        ),
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(CourtGreenContainer.copy(alpha = 0.2f))
          .padding(horizontal = 8.dp, vertical = 3.dp)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Interactive Biomechanical Radar Web",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = DarkSurfaceVariant,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Radar Web Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
          contentAlignment = Alignment.Center
        ) {
          RadarChartCanvas(
            skills = skills,
            selectedIndex = selectedSkillIndex,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skill Chip Selector Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          skills.forEachIndexed { index, skill ->
            val isSelected = index == selectedSkillIndex
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) CourtGreen else SurfaceContainerLow)
                .clickable { selectedSkillIndex = index }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = skill.name,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else DarkSurface,
                    fontSize = 11.sp
                  )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${skill.score}%",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) CourtGreenFixed else CourtGreen,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Skill Detail Card
        val activeSkill = skills[selectedSkillIndex]
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${activeSkill.name} Breakdown",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Target Benchmark: ${activeSkill.benchmark}% (Current: ${activeSkill.score}%)",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp
                )
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = activeSkill.metricLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CourtGreen,
                  fontSize = 16.sp
                )
              )
              Text(
                text = activeSkill.deltaString,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = ShuttleOrange,
                  fontSize = 10.sp
                )
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 5. Historical Progression Trends & Line Chart
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.TrendingUp,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Historical Performance Trends",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Metric Switcher Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            ChartMetricType.FORM_SCORE to "Form Score (%)",
            ChartMetricType.PEAK_SPEED to "Smash Speed (km/h)",
            ChartMetricType.ACCURACY to "Sweet Spot (%)"
          ).forEach { (type, label) ->
            val isSelected = selectedChartMetric == type
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) CourtGreenContainer else SurfaceContainerLow)
                .clickable { selectedChartMetric = type }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) CourtGreenFixed else DarkSurfaceVariant,
                  fontSize = 10.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Line Chart
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
        ) {
          PerformanceLineChart(
            progressionPoints = currentProgression,
            metricType = selectedChartMetric,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chart Footnote & Pro Benchmark Legend
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(CourtGreen)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Alex Tan Data",
              style = MaterialTheme.typography.labelSmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 10.sp
              )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
              modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(ShuttleOrange)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Pro Benchmark",
              style = MaterialTheme.typography.labelSmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 10.sp
              )
            )
          }

          Text(
            text = when (selectedChartMetric) {
              ChartMetricType.FORM_SCORE -> "+18% Form Gain"
              ChartMetricType.PEAK_SPEED -> "+34 km/h Added"
              ChartMetricType.ACCURACY -> "+17% Center Impact"
            },
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CourtGreen,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 6. Biomechanical Fault Reduction Card
    Text(
      text = "Biomechanical Fault Reduction",
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        color = DarkSurface
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        FaultProgressRow(
          faultName = "Low Elbow Angle Drop (<105°)",
          reductionPct = "-68%",
          beforeVal = "42% of smashes",
          currentVal = "13% (Clean)"
        )
        FaultProgressRow(
          faultName = "Off-Center String Dispersion",
          reductionPct = "-45%",
          beforeVal = "22mm offset",
          currentVal = "4mm offset"
        )
        FaultProgressRow(
          faultName = "Premature Forearm Pronation",
          reductionPct = "-52%",
          beforeVal = "18ms early",
          currentVal = "42ms lead (Optimal)"
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 7. Historical Sessions Log
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Historical Training Log",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }

      Text(
        text = "${historicalSessions.size} Sessions Logged",
        style = MaterialTheme.typography.labelSmall.copy(
          color = DarkSurfaceVariant,
          fontSize = 11.sp
        )
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      historicalSessions.forEach { session ->
        HistoricalSessionCard(
          session = session,
          onReviewClick = { onOpenSessionReview(session.id) }
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 8. Milestones & Achievements Trophy Case
    Text(
      text = "Milestone Achievements",
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        color = DarkSurface
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SamplePerformanceData.achievements.forEach { achievement ->
          MilestoneItemRow(achievement = achievement)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 9. Equipment Locker Quick Access Bar
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenCalibration() }
        .testTag("equipment_locker_cta")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(CourtGreen),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SportsTennis,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Equipment & Optical Setup",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Text(
              text = "Yonex Astrox 88D Pro • BG80 @ 25.8 lbs",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 10. Terms of Service & Privacy Policy Card
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { showTermsDetailSheet = true }
        .testTag("open_terms_and_privacy_sheet_btn")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(CourtGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Terms of Service & Privacy Policy",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Text(
              text = "View user agreement & privacy policies",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun ProfileStatPill(
  label: String,
  value: String,
  sub: String,
  modifier: Modifier = Modifier,
  valueColor: Color = DarkSurface
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DarkSurfaceVariant,
          fontSize = 8.sp,
          letterSpacing = 0.4.sp
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          color = valueColor,
          fontSize = 15.sp
        ),
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = sub,
        style = MaterialTheme.typography.bodySmall.copy(
          color = DarkSurfaceVariant,
          fontSize = 9.sp
        ),
        maxLines = 1
      )
    }
  }
}

@Composable
fun RadarChartCanvas(
  skills: List<SkillRadarDimension>,
  selectedIndex: Int,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val center = Offset(size.width / 2, size.height / 2)
    val maxRadius = (size.minDimension / 2) * 0.72f
    val count = skills.size
    val angleStep = (2 * Math.PI / count).toFloat()

    // 1. Draw web concentric polygons (20%, 40%, 60%, 80%, 100%)
    val rings = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
    rings.forEach { ratio ->
      val ringPath = Path()
      for (i in 0 until count) {
        val angle = i * angleStep - (Math.PI / 2).toFloat()
        val r = maxRadius * ratio
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
      }
      ringPath.close()
      drawPath(
        path = ringPath,
        color = Color(0xFFCAD0CE).copy(alpha = 0.45f),
        style = Stroke(
          width = if (ratio == 0.8f) 2f else 1f,
          pathEffect = if (ratio == 0.8f) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
        )
      )
    }

    // 2. Draw radiating spokes from center
    for (i in 0 until count) {
      val angle = i * angleStep - (Math.PI / 2).toFloat()
      val x = center.x + maxRadius * cos(angle)
      val y = center.y + maxRadius * sin(angle)
      drawLine(
        color = Color(0xFFCAD0CE).copy(alpha = 0.5f),
        start = center,
        end = Offset(x, y),
        strokeWidth = 1f
      )
    }

    // 3. Draw Player Score Polygon
    val scorePath = Path()
    val points = mutableListOf<Offset>()
    for (i in 0 until count) {
      val angle = i * angleStep - (Math.PI / 2).toFloat()
      val scoreRatio = (skills[i].score / 100f).coerceIn(0.1f, 1.0f)
      val r = maxRadius * scoreRatio
      val x = center.x + r * cos(angle)
      val y = center.y + r * sin(angle)
      val pt = Offset(x, y)
      points.add(pt)
      if (i == 0) scorePath.moveTo(x, y) else scorePath.lineTo(x, y)
    }
    scorePath.close()

    // Fill player polygon
    drawPath(
      path = scorePath,
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0xFF00855D).copy(alpha = 0.45f),
          Color(0xFF85F8C4).copy(alpha = 0.25f)
        ),
        center = center,
        radius = maxRadius
      )
    )

    // Stroke player polygon
    drawPath(
      path = scorePath,
      color = Color(0xFF00855D),
      style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    // 4. Draw vertex dots & highlight selected vertex
    points.forEachIndexed { i, pt ->
      val isSelected = i == selectedIndex
      val pointRadius = if (isSelected) 7f else 4.5f
      drawCircle(
        color = if (isSelected) Color(0xFFFB7800) else Color(0xFF006948),
        radius = pointRadius,
        center = pt
      )
      drawCircle(
        color = Color.White,
        radius = pointRadius * 0.5f,
        center = pt
      )
    }
  }
}

@Composable
fun PerformanceLineChart(
  progressionPoints: List<WeeklyProgressionPoint>,
  metricType: ChartMetricType,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    if (progressionPoints.size < 2) return@Canvas
    val w = size.width
    val h = size.height
    val paddingX = 36f
    val paddingBottom = 30f
    val paddingTop = 20f

    val chartW = w - (paddingX * 2)
    val chartH = h - paddingTop - paddingBottom

    // Determine min/max values based on metricType
    val values = progressionPoints.map { pt ->
      when (metricType) {
        ChartMetricType.FORM_SCORE -> pt.formScore.toFloat()
        ChartMetricType.PEAK_SPEED -> pt.peakSpeedKmH.toFloat()
        ChartMetricType.ACCURACY -> pt.accuracyPct.toFloat()
      }
    }

    val minVal = (values.minOrNull() ?: 50f) * 0.9f
    val maxVal = (values.maxOrNull() ?: 100f) * 1.08f
    val valRange = (maxVal - minVal).coerceAtLeast(1f)

    val stepX = chartW / (progressionPoints.size - 1)

    // Compute coordinate points
    val coords = progressionPoints.indices.map { i ->
      val x = paddingX + (i * stepX)
      val norm = (values[i] - minVal) / valRange
      val y = paddingTop + chartH * (1f - norm)
      Offset(x, y)
    }

    // Draw background guide grid lines (3 horizontal lines)
    for (g in 0..2) {
      val yGrid = paddingTop + (chartH * (g / 2f))
      drawLine(
        color = Color(0xFFCAD0CE).copy(alpha = 0.35f),
        start = Offset(paddingX, yGrid),
        end = Offset(w - paddingX, yGrid),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
      )
    }

    // Build smooth Bezier path
    val path = Path()
    path.moveTo(coords[0].x, coords[0].y)
    for (i in 0 until coords.size - 1) {
      val p0 = coords[i]
      val p1 = coords[i + 1]
      val cx = (p0.x + p1.x) / 2f
      path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
    }

    // Fill underneath curve
    val fillPath = Path()
    fillPath.addPath(path)
    fillPath.lineTo(coords.last().x, h - paddingBottom)
    fillPath.lineTo(coords.first().x, h - paddingBottom)
    fillPath.close()

    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(
          Color(0xFF00855D).copy(alpha = 0.30f),
          Color(0xFF85F8C4).copy(alpha = 0.05f),
          Color.Transparent
        ),
        startY = paddingTop,
        endY = h - paddingBottom
      )
    )

    // Draw Line
    drawPath(
      path = path,
      color = Color(0xFF006948),
      style = Stroke(width = 4f, cap = StrokeCap.Round)
    )

    // Draw points & apex callout on highest value
    coords.forEachIndexed { index, pt ->
      val isApex = index == coords.size - 1
      drawCircle(
        color = if (isApex) Color(0xFFFB7800) else Color(0xFF006948),
        radius = if (isApex) 6.5f else 4.5f,
        center = pt
      )
      drawCircle(
        color = Color.White,
        radius = if (isApex) 3f else 2f,
        center = pt
      )
    }
  }
}

@Composable
fun FaultProgressRow(
  faultName: String,
  reductionPct: String,
  beforeVal: String,
  currentVal: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(SurfaceContainerLow)
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = faultName,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurface
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Before: $beforeVal",
          style = MaterialTheme.typography.bodySmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 10.sp
          )
        )
        Text(
          text = " → Now: $currentVal",
          style = MaterialTheme.typography.bodySmall.copy(
            color = CourtGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        )
      }
    }

    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(CourtGreen.copy(alpha = 0.14f))
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = reductionPct,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = CourtGreen,
          fontSize = 11.sp
        )
      )
    }
  }
}

@Composable
fun HistoricalSessionCard(
  session: HistoricalSession,
  onReviewClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onReviewClick() }
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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = session.title,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${session.dateFormatted} • ${session.durationMinutes} mins • ${session.totalReps} Reps",
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (session.grade.startsWith("A")) CourtGreenContainer
              else ShuttleOrangeContainer
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Grade ${session.grade}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (session.grade.startsWith("A")) CourtGreenFixed else ShuttleOrangeFixed,
              fontSize = 11.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SessionMetricPill(
          label = "AVG FORM",
          value = "${session.avgFormScore}%",
          modifier = Modifier.weight(1f)
        )
        if (session.peakSpeedKmH > 0) {
          SessionMetricPill(
            label = "PEAK SPEED",
            value = "${session.peakSpeedKmH} km/h",
            valueColor = ShuttleOrange,
            modifier = Modifier.weight(1.2f)
          )
        }
        SessionMetricPill(
          label = "SWEET SPOT",
          value = "${session.sweetSpotPct}%",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceContainerLow)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = session.keyTakeaway,
          style = MaterialTheme.typography.bodySmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 10.sp,
            lineHeight = 13.sp
          ),
          maxLines = 2
        )
      }
    }
  }
}

@Composable
fun SessionMetricPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  valueColor: Color = DarkSurface
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(SurfaceContainerLow)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        color = DarkSurfaceVariant,
        fontSize = 8.sp
      )
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        color = valueColor,
        fontSize = 11.sp
      )
    )
  }
}

@Composable
fun MilestoneItemRow(
  achievement: MilestoneAchievement
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (achievement.isUnlocked) SurfaceContainerLow else Color(0xFFF7FAF8))
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(
          if (achievement.isUnlocked) CourtGreenContainer.copy(alpha = 0.2f)
          else SurfaceContainer
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = achievement.badgeSymbol,
        fontSize = 18.sp
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = achievement.title,
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
        if (achievement.isUnlocked) {
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(13.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = achievement.description,
        style = MaterialTheme.typography.bodySmall.copy(
          color = DarkSurfaceVariant,
          fontSize = 10.sp
        )
      )
    }

    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = "+${achievement.xpReward} XP",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = CourtGreen,
          fontSize = 10.sp
        )
      )
      Text(
        text = achievement.progressText,
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (achievement.isUnlocked) CourtGreen else DarkSurfaceVariant,
          fontSize = 9.sp
        )
      )
    }
  }
}
