package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.model.AutoClippingConfig
import com.example.model.AutoVideoClippingEngine
import com.example.model.ExtractedVideoClip
import com.example.model.MomentTriggerReason
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeContainer
import com.example.ui.theme.ShuttleOrangeFixed
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest

/**
 * Modal Bottom Sheet presenting the automated video clipping reel,
 * filtering by biomechanical efficiency thresholds, slow-mo playback inspection,
 * and one-tap video extraction to device storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoVideoClippingSheet(
  onDismiss: () -> Unit,
  onOpenSlowMoReview: ((ExtractedVideoClip) -> Unit)? = null
) {
  val context = LocalContext.current
  val clippingEngine = remember { AutoVideoClippingEngine.getInstance(context) }
  val clips by clippingEngine.extractedClips.collectAsState()
  val config by clippingEngine.autoConfig.collectAsState()
  val isClippingInProgress by clippingEngine.isClippingInProgress.collectAsState()

  var selectedFilterReason by remember { mutableStateOf<MomentTriggerReason?>(null) }
  var showConfigOptions by remember { mutableStateOf(false) }
  var previewingClip by remember { mutableStateOf<ExtractedVideoClip?>(null) }

  val filteredClips = remember(clips, selectedFilterReason) {
    if (selectedFilterReason == null) clips
    else clips.filter { it.triggerReason == selectedFilterReason }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = SurfaceCanvas,
    modifier = Modifier.testTag("auto_video_clipping_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .navigationBarsPadding()
    ) {
      // 1. Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(CourtGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MovieCreation,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Automated Video Clips",
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
                  text = "AI CLIPPED",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 8.sp
                  )
                )
              }
            }
            Text(
              text = "Significant moments based on biomechanical efficiency scores",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          IconButton(
            onClick = { showConfigOptions = !showConfigOptions },
            modifier = Modifier.testTag("clipping_settings_toggle_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Clipping Settings",
              tint = if (showConfigOptions) CourtGreen else DarkSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = DarkSurfaceVariant)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Expandable Threshold Tuning Settings
      AnimatedVisibility(visible = showConfigOptions) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "EXTRACTION THRESHOLDS & SETTINGS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DarkSurfaceVariant,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp
              )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Efficiency Threshold Slider
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Min Biomechanical Efficiency",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = DarkSurface)
              )
              Text(
                text = "${config.minEfficiencyThreshold}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CourtGreen)
              )
            }

            Slider(
              value = config.minEfficiencyThreshold.toFloat(),
              onValueChange = { newScore ->
                clippingEngine.updateConfig(config.copy(minEfficiencyThreshold = newScore.toInt()))
              },
              valueRange = 70f..95f,
              steps = 4,
              colors = SliderDefaults.colors(
                thumbColor = CourtGreen,
                activeTrackColor = CourtGreen
              ),
              modifier = Modifier.testTag("efficiency_threshold_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fault clips toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Include Kinematic Faults for Review",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = DarkSurface)
                )
                Text(
                  text = "Clip strokes with dropped elbow for corrective analysis",
                  style = MaterialTheme.typography.bodySmall.copy(color = DarkSurfaceVariant, fontSize = 10.sp)
                )
              }
              Switch(
                checked = config.includeFaultClips,
                onCheckedChange = { checked ->
                  clippingEngine.updateConfig(config.copy(includeFaultClips = checked))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CourtGreen)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Re-analyze session button
            Button(
              onClick = {
                clippingEngine.runAutomatedSessionClipping()
                Toast.makeText(context, "Re-scanned session with updated thresholds", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("rescan_session_clips_btn")
            ) {
              Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Re-extract Session Highlights", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }

      // 3. Filter category chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = selectedFilterReason == null,
          onClick = { selectedFilterReason = null },
          label = { Text("All (${clips.size})", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CourtGreen,
            selectedLabelColor = Color.White
          )
        )

        FilterChip(
          selected = selectedFilterReason == MomentTriggerReason.PEAK_EFFICIENCY,
          onClick = { selectedFilterReason = MomentTriggerReason.PEAK_EFFICIENCY },
          label = { Text("Apex Form (95%+)", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CourtGreen,
            selectedLabelColor = Color.White
          )
        )

        FilterChip(
          selected = selectedFilterReason == MomentTriggerReason.MAX_VELOCITY,
          onClick = { selectedFilterReason = MomentTriggerReason.MAX_VELOCITY },
          label = { Text("Max Speed", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ShuttleOrange,
            selectedLabelColor = Color.White
          )
        )

        FilterChip(
          selected = selectedFilterReason == MomentTriggerReason.KINEMATIC_FAULT,
          onClick = { selectedFilterReason = MomentTriggerReason.KINEMATIC_FAULT },
          label = { Text("Faults", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ErrorRed,
            selectedLabelColor = Color.White
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. Clips List
      if (isClippingInProgress) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(color = CourtGreen)
        }
      } else if (filteredClips.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No clips match the current threshold filter.",
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkSurfaceVariant)
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredClips, key = { it.id }) { clip ->
            ClipItemCard(
              clip = clip,
              onExport = {
                clippingEngine.exportClipToStorage(clip.id) { success, path ->
                  if (success) {
                    Toast.makeText(context, "Exported: $path", Toast.LENGTH_SHORT).show()
                  }
                }
              },
              onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_SUBJECT, "AceAI Biomechanical Swing Clip - ${clip.strokeType} Rep #${clip.repIndex}")
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out my badminton swing analysis clip from AceAI!\n" +
                        "Stroke: ${clip.strokeType} (Rep #${clip.repIndex})\n" +
                        "Efficiency Score: ${clip.efficiencyScore}%\n" +
                        "Smash Speed: ${clip.smashSpeedKmH} km/h\n" +
                        "Elbow Angle: ${clip.elbowAngleDeg}°\n" +
                        "Apex Reach: ${clip.zenithReachMeters}m\n" +
                        "Biomechanics: ${clip.biomechanicalSummary}"
                  )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Swing Clip"))
              },
              onInspectSlowMo = {
                previewingClip = clip
                onOpenSlowMoReview?.invoke(clip)
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Batch Export All Significant Clips Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = {
            var count = 0
            filteredClips.forEach { c ->
              clippingEngine.exportClipToStorage(c.id) { ok, _ -> if (ok) count++ }
            }
            Toast.makeText(context, "Exported ${filteredClips.size} slow-mo clips to device movies", Toast.LENGTH_SHORT).show()
          },
          shape = RoundedCornerShape(22.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("export_all_clips_btn")
        ) {
          Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Export All (${filteredClips.size})", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }

        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(22.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
        ) {
          Text("Done", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
fun ClipItemCard(
  clip: ExtractedVideoClip,
  onExport: () -> Unit,
  onShare: () -> Unit,
  onInspectSlowMo: () -> Unit
) {
  val badgeColor = Color(clip.triggerReason.badgeColorHex)
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded }
      .testTag("clip_card_rep_${clip.repIndex}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Video Thumbnail Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .background(Color.Black)
      ) {
        AsyncImage(
          model = clip.previewThumbnailUrl,
          contentDescription = "Slow-mo clip preview",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient Scrim
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.45f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.75f)
                )
              )
            )
        )

        // Top Badges
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(badgeColor)
              .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = when (clip.triggerReason) {
                MomentTriggerReason.MAX_VELOCITY -> Icons.Default.Speed
                MomentTriggerReason.KINEMATIC_FAULT -> Icons.Default.Close
                else -> Icons.Default.AutoAwesome
              },
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = clip.highlightLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp
              )
            )
          }

          // Slow-Mo Duration Pill
          Text(
            text = clip.simulatedVideoDuration,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 9.sp
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        // Center Play Action Trigger
        Box(
          modifier = Modifier
            .size(44.dp)
            .align(Alignment.Center)
            .clip(CircleShape)
            .background(CourtGreen.copy(alpha = 0.9f))
            .clickable { onInspectSlowMo() }
            .testTag("play_clip_rep_${clip.repIndex}"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Clip Slow-Mo",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        // Bottom metrics pill
        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "${clip.smashSpeedKmH} km/h",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = ShuttleOrangeFixed,
              fontSize = 11.sp
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.7f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          )
          Text(
            text = "Elbow: ${clip.elbowAngleDeg.toInt()}°",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CourtGreenFixed,
              fontSize = 11.sp
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.7f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          )
          Text(
            text = "Reach: ${String.format("%.2f", clip.zenithReachMeters)}m",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 11.sp
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.7f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      // Clip Details & Actions
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${clip.strokeType} • Rep #${clip.repIndex}",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Text(
              text = "Biomechanical Efficiency: ${clip.efficiencyScore}%",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (clip.efficiencyScore >= 90) CourtGreen else if (clip.efficiencyScore < 70) ErrorRed else ShuttleOrange,
                fontSize = 12.sp
              )
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Share Button
            IconButton(
              onClick = onShare,
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Clip",
                tint = DarkSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }

            // Export to device button
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (clip.isExported) CourtGreenContainer else SurfaceContainerLow)
                .clickable { onExport() }
                .testTag("export_clip_btn_${clip.repIndex}"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (clip.isExported) Icons.Default.DownloadDone else Icons.Default.FileDownload,
                contentDescription = "Export Clip to Storage",
                tint = if (clip.isExported) CourtGreen else DarkSurface,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = clip.biomechanicalSummary,
          style = MaterialTheme.typography.bodySmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 15.sp
          )
        )

        // Expanded Coaching Insight
        AnimatedVisibility(visible = isExpanded) {
          Column(modifier = Modifier.padding(top = 8.dp)) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CourtGreen.copy(alpha = 0.08f))
                .padding(10.dp)
            ) {
              Row(verticalAlignment = Alignment.Top) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = CourtGreen,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = clip.coachingFeedback,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = DarkSurface,
                    fontSize = 11.sp
                  )
                )
              }
            }

            if (clip.exportedFilePath != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Saved to: ${clip.exportedFilePath}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = CourtGreen,
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
