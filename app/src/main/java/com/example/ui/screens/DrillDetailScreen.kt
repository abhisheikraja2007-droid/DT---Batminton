package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AceImages
import com.example.ui.theme.AmberFixedDim
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningContainer
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
fun DrillDetailScreen(
  onBack: () -> Unit,
  onStartCameraDrill: () -> Unit,
  onOpenCalibration: () -> Unit
) {
  val context = LocalContext.current
  var isBookmarked by remember { mutableStateOf(true) }
  var playbackSpeed by remember { mutableStateOf("0.25x") }
  var isPlaying by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .testTag("drill_detail_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(top = 10.dp, bottom = 90.dp)
    ) {
      // 1. Top Navigation & Drill Meta Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
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
              contentDescription = "Back to Drills",
              tint = DarkSurface,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Drills",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SurfaceContainer)
              .clickable {
                isBookmarked = !isBookmarked
                Toast.makeText(
                  context,
                  if (isBookmarked) "Drill saved to bookmarks" else "Removed from bookmarks",
                  Toast.LENGTH_SHORT
                ).show()
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (isBookmarked) CourtGreen else DarkSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SurfaceContainer)
              .clickable {
                Toast.makeText(context, "Drill link copied to clipboard!", Toast.LENGTH_SHORT).show()
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
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Tags & Title
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "LEVEL: INTERMEDIATE",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = CourtGreen,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CourtGreen.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "⚡ Power & Angle",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = ShuttleOrange,
            fontSize = 11.sp
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ShuttleOrangeContainer.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Forehand Overhead Smash",
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DarkSurface,
          letterSpacing = (-0.5).sp
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Biomechanical kinetic chain analysis with precision shuttle steepness telemetry.",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = DarkSurfaceVariant,
          fontSize = 13.sp
        )
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Video Player & Slow-Mo Video Section
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Video Poster Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(16f / 9f)
              .background(Color.Black)
          ) {
            AsyncImage(
              model = AceImages.DRILL_POSTER,
              contentDescription = "Forehand smash jump execution",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            // Scrim
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  brush = Brush.verticalGradient(
                    colors = listOf(
                      Color.Black.copy(alpha = 0.5f),
                      Color.Transparent,
                      Color.Black.copy(alpha = 0.75f)
                    )
                  )
                )
            )

            // Top overlay controls
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
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color.Black.copy(alpha = 0.6f))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Videocam,
                  contentDescription = null,
                  tint = CourtGreenFixed,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Angle: Side Profile | 45° Iso",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontSize = 10.sp
                  )
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(14.dp))
                  .background(CourtGreenContainer)
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Hd,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "1080p 60fps",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 10.sp
                  )
                )
              }
            }

            // Center Play Button
            Box(
              modifier = Modifier
                .size(54.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
                .clickable { isPlaying = !isPlaying },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }

            // Zenith Overlay Indicator
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 60.dp)
            ) {
              Text(
                text = "Zenith: 2.88m",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF002114),
                  fontSize = 10.sp
                ),
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(CourtGreenFixed)
                  .padding(horizontal = 7.dp, vertical = 3.dp)
              )
              Spacer(modifier = Modifier.height(3.dp))
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(ShuttleOrangeContainer)
              )
            }

            // Bottom timestamp
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(ErrorRed)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "01:24 / 03:45",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontSize = 10.sp
                )
              )
            }
          }

          // Scrubbing Timeline Panel
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF131B2E))
              .padding(14.dp)
          ) {
            // Scrubber Bar
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(Color.White.copy(alpha = 0.15f))
              )

              Box(
                modifier = Modifier
                  .fillMaxWidth(0.6f)
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(
                    brush = Brush.horizontalGradient(
                      colors = listOf(CourtGreenContainer, CourtGreenFixed)
                    )
                  )
              )

              // Thumb
              Box(
                modifier = Modifier
                  .padding(start = 180.dp)
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(Color.White)
              )
            }

            // Keypoint Markers Labels
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "01. Prep", style = MaterialTheme.typography.labelSmall.copy(color = CourtGreenFixed, fontSize = 9.sp))
              Text(text = "02. Racket Drop", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp))
              Text(text = "03. Impact (Zenith)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ShuttleOrangeFixed, fontSize = 9.sp))
              Text(text = "04. Pronation", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Speed Selector Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SpeedButton(label = "1.0x Regular", selected = playbackSpeed == "1.0x") { playbackSpeed = "1.0x" }
                SpeedButton(label = "0.5x Slow", selected = playbackSpeed == "0.5x") { playbackSpeed = "0.5x" }
                SpeedButton(label = "0.25x Frame Scrub", selected = playbackSpeed == "0.25x", isOrange = true) { playbackSpeed = "0.25x" }
              }

              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Fullscreen,
                  contentDescription = "Fullscreen",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 3. Kinetic Angle Checkpoints 2x2 Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Kinetic Angle Checkpoints",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
        }

        Text(
          text = "AI Verified 98% Match",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = CourtGreen,
            fontSize = 10.sp
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CourtGreen.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2x2 Telemetry Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Metric 1: Elbow Extension
        KineticMetricCard(
          title = "ELBOW EXTENSION",
          value = "114°",
          subValue = "/ 110°-118°",
          desc = "Optimal high-reach biomechanical lever",
          barProgress = 0.82f,
          isPrimary = true,
          modifier = Modifier.weight(1f)
        )

        // Metric 2: Non-Racket Arm
        KineticMetricCard(
          title = "NON-RACKET ARM",
          value = "Pointing Up",
          subValue = "",
          desc = "Tracking birdie zenith & chest balance",
          barProgress = 0.95f,
          isPrimary = true,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Metric 3: Wrist Pronation
        KineticMetricCard(
          title = "WRIST PRONATION",
          value = "218°/s",
          subValue = "+18% Pwr",
          desc = "Rapid inward forearm whipping snap",
          barProgress = 0.88f,
          isOrange = true,
          modifier = Modifier.weight(1f)
        )

        // Metric 4: Hip Rotation
        KineticMetricCard(
          title = "HIP ROTATION",
          value = "45° Shift",
          subValue = "",
          desc = "Kinetic torso uncoil before shuttle strike",
          barProgress = 0.90f,
          isPrimary = true,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 4. Stroke Phase Breakdown (3 Stages)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Stroke Phase Breakdown",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
        Text(
          text = "3 Stages",
          style = MaterialTheme.typography.labelSmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 11.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Stage 1
      StrokeStageCard(
        stepNumber = "1",
        title = "Prep & Split-Step Coiling",
        timeRange = "0.0s – 0.6s • Base loading & racket cocking",
        stageLabel = "Stage 1",
        desc = "Push back into a staggered stance with the dominant foot anchored rearward. Racket drops behind the upper back, cocking wrist into full supination while non-racket arm sights the flight path.",
        footerLabel = "Center of Gravity:",
        footerValue = "Rear 65% | Knees flexed 28°"
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Stage 2 (Active Focus / Crucial)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
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
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(CourtGreen),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "2",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Contact Zenith Point",
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontWeight = FontWeight.Bold,
                      color = DarkSurface
                    )
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(ShuttleOrangeContainer)
                  )
                }
                Text(
                  text = "0.8s • Maximum Reach & Impact",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = CourtGreen,
                    fontSize = 11.sp
                  )
                )
              }
            }

            Text(
              text = "Crucial",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen,
                fontSize = 10.sp
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(CourtGreen.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Fully uncoil hips and shoulder. Drive the elbow upward and snap the forearm inward through pronation. Meet the shuttle 20-30cm in front of the hitting shoulder at the absolute pinnacle of your jump.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = DarkSurfaceVariant,
              lineHeight = 19.sp
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Column(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(CourtGreen.copy(alpha = 0.08f))
                .padding(10.dp)
            ) {
              Text(
                text = "Target Contact Height",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = CourtGreen,
                  fontSize = 10.sp
                )
              )
              Text(
                text = "2.85m – 2.92m",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DarkSurface,
                  fontSize = 15.sp
                )
              )
            }

            Column(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(ShuttleOrangeContainer.copy(alpha = 0.1f))
                .padding(10.dp)
            ) {
              Text(
                text = "Head Speed",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = ShuttleOrange,
                  fontSize = 10.sp
                )
              )
              Text(
                text = "210+ km/h",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DarkSurface,
                  fontSize = 15.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Stage 3
      StrokeStageCard(
        stepNumber = "3",
        title = "Follow-Through & Court Recovery",
        timeRange = "1.2s • Deceleration & Base Repositioning",
        stageLabel = "Stage 3",
        desc = "Allow the racket to naturally follow across diagonally toward the opposite hip without abruptly locking the joint. Scissor kick lands dominant foot forward for instant pushback to mid-court base.",
        footerLabel = "Recovery Time Target:",
        footerValue = "< 0.42s to center T-line"
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 5. Common Biomechanical Faults Callout Card (Amber)
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8EE)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = AmberWarning,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Common Biomechanical Faults",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Avoid these high-frequency errors detected by AceAI sensors during intermediate sessions:",
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 12.sp
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Fault 1
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(SurfaceContainerLowest)
              .padding(10.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.ReportProblem,
              contentDescription = null,
              tint = ErrorRed,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Leading Elbow Drops Below Shoulder Level",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Text(
                text = "Flattens trajectory, reducing smash steepness by ~14° and causing frequent net errors.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Fault 2
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(SurfaceContainerLowest)
              .padding(10.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.ReportProblem,
              contentDescription = null,
              tint = ErrorRed,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Striking Shuttle Behind the Head",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Text(
                text = "Severely limits forearm pronation speed and risks shoulder rotator cuff impingement.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }
    }

    // 6. Sticky Bottom Floating CTA Bar
    Surface(
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
      shadowElevation = 12.dp,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(SurfaceContainerHigh)
            .clickable { onOpenCalibration() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Calibrate Ghost Pose",
            tint = DarkSurface,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
          onClick = { onStartCameraDrill() },
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("start_camera_drill_btn")
        ) {
          Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Start AI Camera Drill",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun SpeedButton(
  label: String,
  selected: Boolean,
  isOrange: Boolean = false,
  onClick: () -> Unit
) {
  val bgColor = when {
    selected && isOrange -> ShuttleOrangeContainer
    selected -> CourtGreenContainer
    else -> Color.White.copy(alpha = 0.1f)
  }
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = Color.White,
        fontSize = 10.sp
      )
    )
  }
}

@Composable
fun KineticMetricCard(
  title: String,
  value: String,
  subValue: String,
  desc: String,
  barProgress: Float,
  isPrimary: Boolean = false,
  isOrange: Boolean = false,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurfaceVariant,
            fontSize = 9.sp,
            letterSpacing = 0.4.sp
          )
        )
        if (isOrange && subValue.isNotEmpty()) {
          Text(
            text = subValue,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = ShuttleOrange,
              fontSize = 9.sp
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(ShuttleOrangeContainer.copy(alpha = 0.18f))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          )
        } else {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = if (isOrange) ShuttleOrange else if (isPrimary && title.contains("ELBOW")) CourtGreen else DarkSurface,
            fontSize = 20.sp
          )
        )
        if (!isOrange && subValue.isNotEmpty()) {
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = subValue,
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 10.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(
          color = DarkSurfaceVariant,
          fontSize = 10.sp,
          lineHeight = 13.sp
        ),
        maxLines = 2,
        minLines = 2
      )

      Spacer(modifier = Modifier.height(8.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(SurfaceContainer)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(barProgress)
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isOrange) ShuttleOrangeContainer else CourtGreen)
        )
      }
    }
  }
}

@Composable
fun StrokeStageCard(
  stepNumber: String,
  title: String,
  timeRange: String,
  stageLabel: String,
  desc: String,
  footerLabel: String,
  footerValue: String
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
              .size(30.dp)
              .clip(CircleShape)
              .background(SurfaceContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = stepNumber,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen
              )
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Text(
              text = timeRange,
              style = MaterialTheme.typography.labelSmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        Text(
          text = stageLabel,
          style = MaterialTheme.typography.labelSmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 11.sp
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = desc,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = DarkSurfaceVariant,
          fontSize = 13.sp,
          lineHeight = 18.sp
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceContainerLow)
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = footerLabel,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = footerValue,
          style = MaterialTheme.typography.labelSmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 11.sp
          )
        )
      }
    }
  }
}
