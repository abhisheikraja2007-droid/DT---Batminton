package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoVideoClippingEngine
import com.example.model.DynamicBiomechanicalAnalysisEngine
import com.example.model.DynamicRepAnalysis
import com.example.model.FirebaseAuthService
import com.example.ui.components.AutoVideoClippingSheet
import com.example.ui.components.DynamicKinematicReplayCanvas
import com.example.ui.components.DynamicKineticChainCurveCanvas
import com.example.ui.components.DynamicSweetSpotCanvas
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
import kotlinx.coroutines.delay

@Composable
fun SessionReviewScreen(
  onBack: () -> Unit,
  onOpenAIChat: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val authService = remember { FirebaseAuthService.getInstance(context) }
  val syncStatus by authService.syncStatus.collectAsState()

  val analysisEngine = remember { DynamicBiomechanicalAnalysisEngine.getInstance() }
  val reps by analysisEngine.sessionReps.collectAsState()

  var selectedRepNum by remember { mutableIntStateOf(14) }
  val currentRep = remember(selectedRepNum, reps) {
    analysisEngine.getRep(selectedRepNum)
  }

  // Comparison Rep (Defaults to Rep 7 for Fault vs Best comparison)
  var comparisonRepNum by remember { mutableIntStateOf(7) }
  val comparisonRep = remember(comparisonRepNum, reps) {
    analysisEngine.getRep(comparisonRepNum)
  }
  var showComparisonSection by remember { mutableStateOf(true) }

  // Slow-Mo Kinematic Frame Scrubbing State
  var isPlaying by remember { mutableStateOf(false) }
  var playbackProgress by remember { mutableFloatStateOf(0.68f) } // Defaults to impact apex
  var slowMoSpeed by remember { mutableFloatStateOf(0.25f) } // 0.25x, 0.5x, 1.0x

  // Auto Video Clipping
  val clippingEngine = remember { AutoVideoClippingEngine.getInstance(context) }
  val extractedClips by clippingEngine.extractedClips.collectAsState()
  var showClippingSheet by remember { mutableStateOf(false) }

  // Slow-Mo Replay Loop
  LaunchedEffect(isPlaying, slowMoSpeed) {
    while (isPlaying) {
      delay((33 / slowMoSpeed).toLong())
      playbackProgress = (playbackProgress + 0.02f)
      if (playbackProgress > 1.0f) {
        playbackProgress = 0.0f
      }
    }
  }

  val activeFrame = remember(playbackProgress, currentRep) {
    analysisEngine.computeKinematicFrame(playbackProgress, currentRep)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("session_review_screen")
  ) {
    // 1. Top Bar
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
            .size(36.dp)
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
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Dynamic Session Analysis",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
          Text(
            text = "${currentRep.strokeType} • Rep #${currentRep.repNumber} of ${reps.size}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .clickable {
              authService.triggerCloudSync()
              Toast.makeText(context, "Syncing telemetry session to Firebase Cloud...", Toast.LENGTH_SHORT).show()
            },
          contentAlignment = Alignment.Center
        ) {
          if (syncStatus.isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = CourtGreen
            )
          } else {
            Icon(
              imageVector = if (syncStatus.isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
              contentDescription = "Sync Telemetry",
              tint = if (syncStatus.isCloudConnected) CourtGreen else ShuttleOrange,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Text(
          text = when {
            currentRep.isBest -> "Best Rep ⭐"
            currentRep.isFault -> "Fault Rep ⚠️"
            else -> "Clean Rep"
          },
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = when {
              currentRep.isBest -> ShuttleOrange
              currentRep.isFault -> ErrorRed
              else -> CourtGreen
            },
            fontSize = 10.sp
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
              when {
                currentRep.isBest -> ShuttleOrangeContainer.copy(alpha = 0.15f)
                currentRep.isFault -> ErrorContainer
                else -> CourtGreen.copy(alpha = 0.12f)
              }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Dynamic Rep Carousel Selector (1 to 20)
    Text(
      text = "SELECT REPETITION TIMELINE",
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        color = DarkSurfaceVariant,
        fontSize = 9.sp,
        letterSpacing = 0.8.sp
      )
    )

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      reps.forEach { rep ->
        val isSelected = rep.repNumber == selectedRepNum
        Box(
          modifier = Modifier
            .width(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              when {
                isSelected -> CourtGreen
                rep.isBest -> ShuttleOrangeContainer.copy(alpha = 0.18f)
                rep.isFault -> ErrorContainer
                else -> SurfaceContainerLow
              }
            )
            .clickable {
              selectedRepNum = rep.repNumber
              analysisEngine.selectRep(rep.repNumber)
            }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "#${rep.repNumber}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else DarkSurface,
                fontSize = 12.sp
              )
            )
            Text(
              text = "${rep.score}%",
              style = MaterialTheme.typography.labelSmall.copy(
                color = when {
                  isSelected -> CourtGreenFixed
                  rep.isBest -> ShuttleOrange
                  rep.isFault -> ErrorRed
                  else -> DarkSurfaceVariant
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Dynamic High-Precision Kinematic Replay Frame
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(Color(0xFF090E18))
        ) {
          // Dynamic Kinematic Vector Canvas
          DynamicKinematicReplayCanvas(
            frame = activeFrame,
            rep = currentRep,
            modifier = Modifier.fillMaxSize()
          )

          // Top Telemetry Header HUD
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (currentRep.isFault) ErrorRed else CourtGreenFixed)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${(playbackProgress * 1.0f).let { String.format("%.2fs", it) }} • ${activeFrame.phaseName}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 10.sp
                )
              )
            }

            Text(
              text = "Elbow: ${activeFrame.elbowAngle.toInt()}° (Target: 114°)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (currentRep.isFault && activeFrame.elbowAngle < 100f) Color(0xFFFFDAD6) else CourtGreenFixed,
                fontSize = 10.sp
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (currentRep.isFault && activeFrame.elbowAngle < 100f) ErrorRed else CourtGreenContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          // Bottom Dynamic Speed & Reach Overlay
          Row(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(10.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.75f))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${activeFrame.headSpeedKmH} km/h • Reach: ${currentRep.contactZenithMeters}m",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (currentRep.isFault) Color(0xFFFFB4AB) else ShuttleOrangeFixed,
                fontSize = 10.sp
              )
            )
          }
        }

        // Scrubbing Slider & Controls
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131B2E))
            .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Slider(
            value = playbackProgress,
            onValueChange = {
              playbackProgress = it
              isPlaying = false
            },
            colors = SliderDefaults.colors(
              thumbColor = CourtGreenFixed,
              activeTrackColor = CourtGreen,
              inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // -1 frame
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.12f))
                  .clickable {
                    playbackProgress = (playbackProgress - 0.05f).coerceAtLeast(0f)
                    isPlaying = false
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "-1f",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }

              // Play/Pause
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(CourtGreen)
                  .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isPlaying) "Pause" else "Play",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }

              // +1 frame
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.12f))
                  .clickable {
                    playbackProgress = (playbackProgress + 0.05f).coerceAtMost(1f)
                    isPlaying = false
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "+1f",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
            }

            // Speed Selector Toggle (0.25x / 0.5x / 1.0x)
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .padding(2.dp),
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              listOf(0.25f, 0.5f, 1.0f).forEach { spd ->
                val isSelected = slowMoSpeed == spd
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) CourtGreen else Color.Transparent)
                    .clickable { slowMoSpeed = spd }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${spd}x",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Dynamic Kinetic Telemetry 2x2
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      TelemetryMetricCard(
        label = "ELBOW EXTENSION",
        value = "${currentRep.elbowAngleDeg.toInt()}°",
        desc = if (currentRep.elbowAngleDeg < 100f) "Elbow dropped (-${(114 - currentRep.elbowAngleDeg).toInt()}°)" else "Optimal high-lever reach",
        isWarning = currentRep.elbowAngleDeg < 100f,
        modifier = Modifier.weight(1f)
      )
      TelemetryMetricCard(
        label = "CONTACT ZENITH",
        value = "${currentRep.contactZenithMeters}m",
        desc = if (currentRep.contactZenithMeters < 2.75f) "-${((2.88f - currentRep.contactZenithMeters) * 100).toInt()}cm below apex" else "Peak vertical jump impact",
        isWarning = currentRep.contactZenithMeters < 2.75f,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      TelemetryMetricCard(
        label = "HIP UNCOIL LEAD",
        value = "${currentRep.hipUncoilLeadMs}ms",
        desc = if (currentRep.hipUncoilLeadMs < 30) "Late core engagement" else "Ideal kinetic uncoiling lead",
        isWarning = currentRep.hipUncoilLeadMs < 30,
        modifier = Modifier.weight(1f)
      )
      TelemetryMetricCard(
        label = "SWEET SPOT OFFSET",
        value = "${currentRep.sweetSpotOffsetMm}mm",
        desc = if (currentRep.sweetSpotOffsetMm > 15f) "Off-center rim strike" else "Center string sweet spot",
        isWarning = currentRep.sweetSpotOffsetMm > 15f,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 5. Dynamic Interactive Sweet Spot & Kinetic Energy Wave Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Sweet Spot Impact Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.weight(1f)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "STRING BED IMPACT",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DarkSurfaceVariant,
              fontSize = 9.sp,
              letterSpacing = 0.5.sp
            )
          )
          Spacer(modifier = Modifier.height(6.dp))

          DynamicSweetSpotCanvas(
            rep = currentRep,
            modifier = Modifier
              .size(110.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "${currentRep.sweetSpotOffsetMm}mm off-center",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (currentRep.sweetSpotOffsetMm > 15f) ErrorRed else CourtGreen,
              fontSize = 11.sp
            )
          )
        }
      }

      // Kinetic Chain Acceleration Curve Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.weight(1f)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "KINETIC ENERGY WAVE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DarkSurfaceVariant,
              fontSize = 9.sp,
              letterSpacing = 0.5.sp
            )
          )
          Spacer(modifier = Modifier.height(8.dp))

          DynamicKineticChainCurveCanvas(
            rep = currentRep,
            currentProgress = playbackProgress,
            modifier = Modifier
              .fillMaxWidth()
              .height(90.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Pronation: ${currentRep.pronationSpeedDegPerSec}°/s",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = ShuttleOrange,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 6. Dynamic AI Biomechanical Coach Diagnosis
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
            .size(36.dp)
            .clip(CircleShape)
            .background(CourtGreen.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "AceAI Kinematic Takeaway",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen
              )
            )

            Text(
              text = "Rep #${currentRep.repNumber}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurfaceVariant
              )
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = currentRep.aiDiagnosis,
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurface,
              lineHeight = 17.sp
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Correction Cue Action Chip
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(CourtGreen.copy(alpha = 0.10f))
              .padding(horizontal = 8.dp, vertical = 5.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Cue: ${currentRep.correctionCue}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CourtGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 7. Dynamic Comparative Biomechanical Analysis Table (Rep A vs Rep B)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Side-by-Side Comparison",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurface
        )
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { showComparisonSection = !showComparisonSection }
      ) {
        Icon(
          imageVector = Icons.Default.CompareArrows,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (showComparisonSection) "Hide" else "Show",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = CourtGreen
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    AnimatedVisibility(visible = showComparisonSection) {
      Card(
        shape = RoundedCornerShape(16.dp),
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
          // Select Comparison Rep Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "COMPARING:",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DarkSurfaceVariant,
                fontSize = 9.sp
              )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Primary Rep Badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(CourtGreen.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "Rep #${currentRep.repNumber} (${currentRep.score}%)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtGreen,
                    fontSize = 10.sp
                  )
                )
              }

              // Comparator Rep Picker Dropdown/Cycle
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (comparisonRep.isFault) ErrorContainer else ShuttleOrangeContainer.copy(alpha = 0.18f))
                  .clickable {
                    // Cycle comparison rep through other key reps (7, 11, 14, 1, 18)
                    comparisonRepNum = when (comparisonRepNum) {
                      7 -> 11
                      11 -> 14
                      14 -> 1
                      1 -> 18
                      else -> 7
                    }
                  }
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "vs Rep #${comparisonRep.repNumber} (${comparisonRep.score}%) ↻",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (comparisonRep.isFault) ErrorRed else ShuttleOrange,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }

          // Table Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "METRIC",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DarkSurfaceVariant,
                fontSize = 9.sp
              ),
              modifier = Modifier.weight(1.3f)
            )
            Text(
              text = "REP #${currentRep.repNumber}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = CourtGreen,
                fontSize = 9.sp
              ),
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "REP #${comparisonRep.repNumber}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (comparisonRep.isFault) ErrorRed else ShuttleOrange,
                fontSize = 9.sp
              ),
              modifier = Modifier.weight(1f)
            )
          }

          ComparisonRow(
            label = "Elbow Extension",
            bestVal = "${currentRep.elbowAngleDeg.toInt()}°",
            faultVal = "${comparisonRep.elbowAngleDeg.toInt()}°",
            isFaultBad = comparisonRep.elbowAngleDeg < currentRep.elbowAngleDeg
          )
          ComparisonRow(
            label = "Contact Zenith",
            bestVal = "${currentRep.contactZenithMeters}m",
            faultVal = "${comparisonRep.contactZenithMeters}m",
            isFaultBad = comparisonRep.contactZenithMeters < currentRep.contactZenithMeters
          )
          ComparisonRow(
            label = "Smash Speed",
            bestVal = "${currentRep.speedKmH} km/h",
            faultVal = "${comparisonRep.speedKmH} km/h",
            isFaultBad = comparisonRep.speedKmH < currentRep.speedKmH
          )
          ComparisonRow(
            label = "Pronation Rate",
            bestVal = "${currentRep.pronationSpeedDegPerSec}°/s",
            faultVal = "${comparisonRep.pronationSpeedDegPerSec}°/s",
            isFaultBad = comparisonRep.pronationSpeedDegPerSec < currentRep.pronationSpeedDegPerSec
          )
          ComparisonRow(
            label = "Sweet Spot Offset",
            bestVal = "${currentRep.sweetSpotOffsetMm}mm",
            faultVal = "${comparisonRep.sweetSpotOffsetMm}mm",
            isFaultBad = comparisonRep.sweetSpotOffsetMm > currentRep.sweetSpotOffsetMm
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Ask Coach Maya AI Button
    if (onOpenAIChat != null) {
      OutlinedButton(
        onClick = onOpenAIChat,
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = CourtGreen
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("ask_ai_coach_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Ask Coach Maya AI About Rep #${currentRep.repNumber}",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = CourtGreen
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
    }

    // 8. Auto-Clipped Moments CTA Button
    Button(
      onClick = { showClippingSheet = true },
      shape = RoundedCornerShape(26.dp),
      colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("view_auto_clips_btn")
    ) {
      Icon(
        imageVector = Icons.Default.MovieCreation,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "View Auto-Clipped Highlights (${extractedClips.size})",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }

  // Auto Video Clipping Bottom Sheet
  if (showClippingSheet) {
    AutoVideoClippingSheet(
      onDismiss = { showClippingSheet = false }
    )
  }
}

@Composable
private fun TelemetryMetricCard(
  label: String,
  value: String,
  desc: String,
  isWarning: Boolean,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DarkSurfaceVariant,
          fontSize = 9.sp,
          letterSpacing = 0.5.sp
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = if (isWarning) ErrorRed else DarkSurface,
          fontSize = 20.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(
          color = if (isWarning) ErrorRed else DarkSurfaceVariant,
          fontSize = 11.sp,
          lineHeight = 14.sp
        )
      )
    }
  }
}

@Composable
private fun ComparisonRow(
  label: String,
  bestVal: String,
  faultVal: String,
  isFaultBad: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Medium,
        color = DarkSurface,
        fontSize = 12.sp
      ),
      modifier = Modifier.weight(1.3f)
    )
    Text(
      text = bestVal,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        color = CourtGreen,
        fontSize = 12.sp
      ),
      modifier = Modifier.weight(1f)
    )
    Text(
      text = faultVal,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        color = if (isFaultBad) ErrorRed else ShuttleOrange,
        fontSize = 12.sp
      ),
      modifier = Modifier.weight(1f)
    )
  }
}
