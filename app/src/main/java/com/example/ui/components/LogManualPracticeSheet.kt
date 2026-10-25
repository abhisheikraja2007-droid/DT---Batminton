package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.db.AceRepository
import com.example.db.PracticeSessionEntity
import com.example.model.FirebaseAuthService
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogManualPracticeSheet(
  onDismiss: () -> Unit,
  onSessionLogged: () -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repository = remember { AceRepository.getInstance(context) }
  val authService = remember { FirebaseAuthService.getInstance(context) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val strokeTypes = listOf(
    "Forehand Jump Smash",
    "Zenith Overhead Clear",
    "Deceptive Slice Drop",
    "Drive Counter Attack",
    "Backhand High Clear",
    "Net Kill & Tumble"
  )

  var selectedStroke by remember { mutableStateOf(strokeTypes[0]) }
  var totalReps by remember { mutableFloatStateOf(40f) }
  var formScore by remember { mutableFloatStateOf(88f) }
  var smashSpeedKmH by remember { mutableFloatStateOf(215f) }
  var durationMinutes by remember { mutableFloatStateOf(20f) }
  var notes by remember { mutableStateOf("Manual court drill log with focused pronation and footwork recovery.") }
  var isSaving by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SurfaceCanvas,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 22.dp, vertical = 12.dp)
        .testTag("log_manual_practice_sheet")
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
              imageVector = Icons.Default.FitnessCenter,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Log Manual Practice",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Text(
              text = "Record real court reps & update live profile stats",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 12.sp
              )
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_log_sheet_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = DarkSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 1. Select Stroke Type
      Text(
        text = "STROKE / DRILL TYPE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = CourtGreen,
          fontSize = 10.sp,
          letterSpacing = 0.8.sp
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        strokeTypes.forEach { type ->
          val isSelected = selectedStroke == type
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) CourtGreen else SurfaceContainerHigh)
              .clickable { selectedStroke = type }
              .padding(horizontal = 14.dp, vertical = 9.dp)
          ) {
            Text(
              text = type,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else DarkSurface
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. Reps & Duration Sliders Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Total Reps Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Total Reps",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = DarkSurfaceVariant,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "${totalReps.toInt()} reps",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CourtGreen
                )
              )
            }
            Slider(
              value = totalReps,
              onValueChange = { totalReps = it },
              valueRange = 10f..200f,
              steps = 18,
              colors = SliderDefaults.colors(
                thumbColor = CourtGreen,
                activeTrackColor = CourtGreen
              )
            )
          }
        }

        // Duration Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Duration",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = DarkSurfaceVariant,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "${durationMinutes.toInt()} mins",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = ShuttleOrange
                )
              )
            }
            Slider(
              value = durationMinutes,
              onValueChange = { durationMinutes = it },
              valueRange = 5f..120f,
              steps = 22,
              colors = SliderDefaults.colors(
                thumbColor = ShuttleOrange,
                activeTrackColor = ShuttleOrange
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Form Score & Smash Speed Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Form Score %
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Form Score",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = DarkSurfaceVariant,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "${formScore.toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CourtGreen
                )
              )
            }
            Slider(
              value = formScore,
              onValueChange = { formScore = it },
              valueRange = 50f..100f,
              steps = 10,
              colors = SliderDefaults.colors(
                thumbColor = CourtGreen,
                activeTrackColor = CourtGreen
              )
            )
          }
        }

        // Peak Speed km/h
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Peak Speed",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = DarkSurfaceVariant,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "${smashSpeedKmH.toInt()} km/h",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = ShuttleOrange
                )
              )
            }
            Slider(
              value = smashSpeedKmH,
              onValueChange = { smashSpeedKmH = it },
              valueRange = 100f..350f,
              steps = 25,
              colors = SliderDefaults.colors(
                thumbColor = ShuttleOrange,
                activeTrackColor = ShuttleOrange
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Notes input field
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Session Notes / Takeaways") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CourtGreen,
          unfocusedBorderColor = SurfaceContainerHigh
        )
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 5. Submit Action Button
      Button(
        onClick = {
          if (isSaving) return@Button
          isSaving = true
          scope.launch {
            val session = PracticeSessionEntity(
              dateMs = System.currentTimeMillis(),
              strokeType = selectedStroke,
              totalReps = totalReps.toInt(),
              averageFormScore = formScore.toInt(),
              maxSmashSpeedKmH = smashSpeedKmH.toInt(),
              avgZenithReachMeters = 2.45f,
              durationSeconds = (durationMinutes * 60).toInt(),
              clipsExtractedCount = 2,
              coachSummary = notes
            )
            repository.savePracticeSession(session)
            authService.triggerCloudSync()
            Toast.makeText(context, "Practice Session Logged Successfully! 🎉", Toast.LENGTH_SHORT).show()
            isSaving = false
            onSessionLogged()
            onDismiss()
          }
        },
        enabled = !isSaving,
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_manual_practice_session_btn")
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isSaving) "Saving Session..." else "Save Practice Session",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
