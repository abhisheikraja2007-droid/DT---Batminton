package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.db.AceRepository
import com.example.db.PracticeSessionEntity
import com.example.model.AutoVideoClippingEngine
import com.example.model.DynamicBiomechanicalAnalysisEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.ui.components.AutoVideoClippingSheet
import com.example.ui.components.DynamicKinematicReplayCanvas
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

@Composable
fun SessionCompleteScreen(
  onSaveAndContinue: () -> Unit,
  onReviewReps: () -> Unit
) {
  val context = LocalContext.current
  val repository = remember { AceRepository.getInstance(context) }
  val dbSessions by repository.allSessions.collectAsState(initial = emptyList())
  val activeSession = dbSessions.firstOrNull()

  val strokeTitle = activeSession?.strokeType ?: "Practice Drill"
  val dateFormatted = remember(activeSession) {
    if (activeSession != null) {
      java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(activeSession.dateMs))
    } else {
      java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }
  }
  val totalRepsVal = activeSession?.totalReps ?: 0
  val formScoreVal = activeSession?.averageFormScore ?: 0
  val peakSpeedVal = activeSession?.maxSmashSpeedKmH ?: 0
  val zenithReachVal = activeSession?.avgZenithReachMeters ?: 0f
  val durationSecs = activeSession?.durationSeconds ?: 0

  val gradeText = if (formScoreVal > 0) {
    when {
      formScoreVal >= 90 -> "A+"
      formScoreVal >= 80 -> "A"
      formScoreVal >= 70 -> "B+"
      else -> "B"
    }
  } else "--"

  val earnedXpToday = remember(activeSession) {
    if (activeSession != null) activeSession.totalReps * 10 + activeSession.averageFormScore * 5 else 0
  }
  val totalXp = remember(dbSessions) {
    dbSessions.sumOf { it.totalReps * 10 + it.averageFormScore * 5 }
  }
  val (currentLevel, rankBadge, nextLevelXp) = remember(totalXp) {
    when {
      totalXp < 500 -> Triple(1, "Beginner I", 500)
      totalXp < 1000 -> Triple(2, "Beginner II", 1000)
      totalXp < 2000 -> Triple(3, "Beginner III", 2000)
      totalXp < 3500 -> Triple(4, "Silver I", 3500)
      totalXp < 5500 -> Triple(5, "Silver II", 5500)
      totalXp < 8000 -> Triple(6, "Gold Elite", 8000)
      else -> Triple(7, "Master Pro", 12000)
    }
  }
  val streakDays = remember(dbSessions) {
    if (dbSessions.isEmpty()) 0
    else {
      dbSessions.map {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.dateMs))
      }.toSet().size
    }
  }
  val analysisEngine = remember { DynamicBiomechanicalAnalysisEngine.getInstance() }
  val bestRep = remember { analysisEngine.getRep(1) }
  val bestRepFrame = remember(bestRep) {
    analysisEngine.computeKinematicFrame(0.68f, bestRep)
  }
  val clippingEngine = remember { AutoVideoClippingEngine.getInstance(context) }
  val extractedClips by clippingEngine.extractedClips.collectAsState()
  var showClippingSheet by remember { mutableStateOf(false) }

  fun handleSaveAndContinue() {
    onSaveAndContinue()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("session_complete_screen")
  ) {
    // 1. Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SurfaceContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Summary",
            tint = DarkSurface,
            modifier = Modifier
              .size(20.dp)
              .clickable { handleSaveAndContinue() }
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Session Summary",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }

      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(SurfaceContainer)
          .clickable {
            Toast.makeText(context, "Session Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share",
          tint = DarkSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Session Complete Header
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "SESSION COMPLETE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = CourtGreen,
          fontSize = 10.sp,
          letterSpacing = 0.8.sp
        ),
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(CourtGreen.copy(alpha = 0.12f))
          .padding(horizontal = 10.dp, vertical = 4.dp)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "$strokeTitle Breakdown",
      style = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Black,
        color = DarkSurface,
        letterSpacing = (-0.5).sp
      )
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = "$dateFormatted • $totalRepsVal Repetitions Completed",
      style = MaterialTheme.typography.bodyMedium.copy(
        color = DarkSurfaceVariant,
        fontSize = 13.sp
      )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Overall Performance Hero Card
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        // Grade Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Circular Badge
          Box(
            modifier = Modifier
              .size(70.dp)
              .clip(CircleShape)
              .background(CourtGreen),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = gradeText,
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontWeight = FontWeight.Black,
                  color = Color.White,
                  fontSize = 32.sp,
                  lineHeight = 32.sp
                )
              )
              Text(
                text = "GRADE",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = CourtGreenFixed,
                  fontSize = 8.sp,
                  letterSpacing = 0.5.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$formScoreVal%",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DarkSurface,
                  fontSize = 24.sp
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Form Score",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = DarkSurfaceVariant
                )
              )
            }
            Text(
              text = if (formScoreVal >= 85) "+8% improvement compared to baseline!" else "Focused practice recommended on elbow cocking angle",
              style = MaterialTheme.typography.bodySmall.copy(
                color = CourtGreen,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Performance metric pills (2x2)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricSummaryTile(
            label = "REPS TOTAL",
            value = "$totalRepsVal Reps",
            sub = "${(totalRepsVal * 0.85).toInt()} Clean Executions",
            icon = Icons.Default.FitnessCenter,
            tint = CourtGreen,
            modifier = Modifier.weight(1f)
          )
          MetricSummaryTile(
            label = "PEAK VELOCITY",
            value = "$peakSpeedVal km/h",
            sub = "Live Telemetry ⚡",
            icon = Icons.Default.Speed,
            tint = ShuttleOrange,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricSummaryTile(
            label = "ZENITH REACH",
            value = "${String.format("%.2f", zenithReachVal)} m",
            sub = "Apex Target >2.4m",
            icon = Icons.Default.Height,
            tint = CourtGreen,
            modifier = Modifier.weight(1f)
          )
          MetricSummaryTile(
            label = "DRILL TIME",
            value = "${durationSecs / 60}m ${durationSecs % 60}s",
            sub = "High Intensity",
            icon = Icons.Default.Timer,
            tint = DarkSurfaceVariant,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Gamification XP & Level Unlocked Card
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CourtGreenFixed),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MilitaryTech,
                contentDescription = null,
                tint = CourtGreen,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "$rankBadge • Level $currentLevel",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Text(
                text = "+$earnedXpToday XP Earned Today!",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CourtGreen,
                  fontSize = 11.sp
                )
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(ShuttleOrangeContainer)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Whatshot,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$streakDays-Day Streak",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 11.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "%,d / %,d XP".format(totalXp, nextLevelXp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
          val neededXp = (nextLevelXp - totalXp).coerceAtLeast(0)
          Text(
            text = if (neededXp > 0) "%,d XP to Next Level".format(neededXp) else "MAX LEVEL",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
          progress = { if (nextLevelXp > 0) (totalXp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f) else 1f },
          color = CourtGreen,
          trackColor = SurfaceContainer,
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 5. Stroke Mechanics Calibration Sliders
    Text(
      text = "Stroke Mechanics Calibration",
      style = MaterialTheme.typography.headlineSmall.copy(
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        val hasData = totalRepsVal > 0
        MechanicBarItem(
          label = "Elbow Extension (Optimal: 112°-118°)",
          scoreText = if (hasData) "114°" else "--",
          progress = if (hasData) 0.92f else 0f,
          isWarning = false
        )
        MechanicBarItem(
          label = "Contact Height Zenith (Target: > 2.80m)",
          scoreText = if (hasData) "${String.format("%.2f", zenithReachVal)}m" else "0.00m",
          progress = if (hasData) (zenithReachVal / 3f).coerceIn(0f, 1f) else 0f,
          isWarning = false
        )
        MechanicBarItem(
          label = "Torso Rotation Shift (Target: ~45°)",
          scoreText = if (hasData) "44°" else "--",
          progress = if (hasData) 0.86f else 0f,
          isWarning = false
        )
        MechanicBarItem(
          label = "Wrist Pronation Snap Speed (Target: > 210°/s)",
          scoreText = if (hasData) "198°/s" else "--",
          progress = if (hasData) 0.78f else 0f,
          isWarning = hasData
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 6. Racket Face Impact Distribution Heatmap
    Text(
      text = "Racket Face Impact Distribution",
      style = MaterialTheme.typography.headlineSmall.copy(
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
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Isometric Racket Canvas with Sweet Spot & Hit Points
        Box(
          modifier = Modifier
            .size(width = 240.dp, height = 180.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            // Racket Head Outer Rim
            drawOval(
              color = Color(0xFF131B2E),
              topLeft = Offset(w * 0.25f, h * 0.1f),
              size = Size(w * 0.50f, h * 0.75f),
              style = Stroke(width = 5f)
            )

            // String grid lines (subtle)
            val stringColor = Color(0xFFE2E7FF)
            for (i in 1..7) {
              val x = w * (0.28f + i * 0.055f)
              drawLine(stringColor, Offset(x, h * 0.15f), Offset(x, h * 0.80f), strokeWidth = 1f)
            }
            for (i in 1..9) {
              val y = h * (0.15f + i * 0.065f)
              drawLine(stringColor, Offset(w * 0.28f, y), Offset(w * 0.72f, y), strokeWidth = 1f)
            }

            // Sweet Spot Zone Oval (Green dashed)
            drawOval(
              color = CourtGreen,
              topLeft = Offset(w * 0.35f, h * 0.28f),
              size = Size(w * 0.30f, h * 0.38f),
              style = Stroke(
                width = 3f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
              )
            )

            // Sweet Spot Fill Glow
            drawOval(
              color = CourtGreen.copy(alpha = 0.15f),
              topLeft = Offset(w * 0.35f, h * 0.28f),
              size = Size(w * 0.30f, h * 0.38f)
            )

            // Impact Dots
            val sweetHits = listOf(
              Offset(center.x, center.y),
              Offset(center.x + 8f, center.y - 12f),
              Offset(center.x - 10f, center.y + 6f),
              Offset(center.x + 14f, center.y + 10f),
              Offset(center.x - 6f, center.y - 16f),
              Offset(center.x + 4f, center.y + 18f),
              Offset(center.x - 12f, center.y - 8f),
              Offset(center.x + 16f, center.y - 4f),
              Offset(center.x - 8f, center.y + 16f),
              Offset(center.x + 10f, center.y + 4f),
              Offset(center.x - 2f, center.y - 22f),
              Offset(center.x + 6f, center.y - 10f),
              Offset(center.x - 14f, center.y + 8f),
              Offset(center.x + 18f, center.y + 14f),
              Offset(center.x - 4f, center.y + 24f),
              Offset(center.x + 12f, center.y - 14f),
              Offset(center.x - 16f, center.y - 4f)
            )
            sweetHits.forEach { pt ->
              drawCircle(CourtGreen, radius = 5f, center = pt)
            }

            // 2 Off-center hits (Orange)
            drawCircle(ShuttleOrangeContainer, radius = 5f, center = Offset(center.x + 32f, center.y - 36f))
            drawCircle(ShuttleOrangeContainer, radius = 5f, center = Offset(center.x - 28f, center.y - 38f))

            // 1 Frame hit (Red)
            drawCircle(ErrorRed, radius = 5f, center = Offset(center.x - 42f, center.y + 30f))

            // Throat & Shaft start
            drawLine(Color(0xFF131B2E), Offset(w * 0.44f, h * 0.85f), Offset(center.x, h * 0.94f), strokeWidth = 4f)
            drawLine(Color(0xFF131B2E), Offset(w * 0.56f, h * 0.85f), Offset(center.x, h * 0.94f), strokeWidth = 4f)
            drawLine(Color(0xFF131B2E), Offset(center.x, h * 0.94f), Offset(center.x, h), strokeWidth = 5f)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CourtGreen.copy(alpha = 0.08f))
            .padding(10.dp),
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
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "85% Sweet Spot Hit Rate",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen
              )
            )
          }
          Text(
            text = "17 / 20 in Center Oval",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 7. Rep Highlights (Best Rep vs Fault)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Rep Highlights",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurface
        )
      )

      Text(
        text = "Rep #14 vs Rep #07",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = CourtGreen
        )
      )
    }

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
          .padding(14.dp)
      ) {
        // Dynamic Kinematic Replay Preview with play trigger
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090E18))
            .clickable { onReviewReps() }
        ) {
          DynamicKinematicReplayCanvas(
            frame = bestRepFrame,
            rep = bestRep,
            modifier = Modifier.fillMaxSize()
          )

          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.20f))
          )

          // Play icon
          Box(
            modifier = Modifier
              .size(42.dp)
              .align(Alignment.Center)
              .clip(CircleShape)
              .background(CourtGreen.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayCircle,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
          }

          // Top pill
          Row(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(8.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(CourtGreen)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "Rep #14 • BEST STROKE (${bestRep.score}%)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp
              )
            )
          }

          // Bottom speed pill
          Row(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(8.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.7f))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "${bestRep.speedKmH} km/h • ${bestRep.contactZenithMeters}m Zenith",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreenFixed,
                fontSize = 10.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fault Rep Comparison
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFF1F0))
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(ErrorRed)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Rep #07: Elbow Dropped to 94° (Fault)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ErrorRed
              )
            )
            Text(
              text = "188 km/h • Contact occurred 14cm below optimal reach.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 8. Coach Maya's AI Biomechanical Insight
    Card(
      shape = RoundedCornerShape(18.dp),
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
            .size(44.dp)
            .clip(CircleShape)
            .background(CourtGreenFixed),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Coach Maya's AI Insight",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = ShuttleOrangeContainer,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Superb jump timing today Alex! You gained 18 km/h on your smash by keeping your non-racket arm pinned high until contact. For tomorrow's session, focus on rotating your left hip earlier to unlock even steeper angles.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = DarkSurface,
              fontSize = 13.sp,
              lineHeight = 18.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 9. Actions CTA Row
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Primary Save & Continue
      Button(
        onClick = { handleSaveAndContinue() },
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_and_continue_btn")
      ) {
        Text(
          text = "Save & Continue",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 15.sp
          )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }

      // Review Reps Button
      OutlinedButton(
        onClick = { onReviewReps() },
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("review_reps_btn")
      ) {
        Icon(
          imageVector = Icons.Default.PlayCircle,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Review 20 Repetitions",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = CourtGreen
          )
        )
      }

      // Automated Video Clips Feature Button
      Button(
        onClick = { showClippingSheet = true },
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131B2E)),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("open_auto_clips_summary_btn")
      ) {
        Icon(
          imageVector = Icons.Default.MovieCreation,
          contentDescription = null,
          tint = CourtGreenFixed,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Extracted Highlight Clips (${extractedClips.size})",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Automated Video Clipping Sheet
    if (showClippingSheet) {
      AutoVideoClippingSheet(
        onDismiss = { showClippingSheet = false },
        onOpenSlowMoReview = { clip ->
          showClippingSheet = false
          onReviewReps()
        }
      )
    }
  }
}

@Composable
fun MetricSummaryTile(
  label: String,
  value: String,
  sub: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(SurfaceContainerLow)
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(tint.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurfaceVariant,
          fontSize = 8.sp,
          letterSpacing = 0.5.sp
        )
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DarkSurface,
          fontSize = 14.sp
        )
      )
      Text(
        text = sub,
        style = MaterialTheme.typography.labelSmall.copy(
          color = tint,
          fontWeight = FontWeight.Medium,
          fontSize = 9.sp
        ),
        maxLines = 1
      )
    }
  }
}

@Composable
fun MechanicBarItem(
  label: String,
  scoreText: String,
  progress: Float,
  isWarning: Boolean
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          color = DarkSurfaceVariant,
          fontSize = 11.sp
        )
      )
      Text(
        text = scoreText,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = if (isWarning) ShuttleOrange else CourtGreen,
          fontSize = 11.sp
        )
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { progress },
      color = if (isWarning) ShuttleOrangeContainer else CourtGreen,
      trackColor = SurfaceContainer,
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
    )
  }
}
