package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.core.content.ContextCompat
import com.example.notification.FitnessGoal
import com.example.notification.ReminderSettings
import com.example.notification.TrainingGoalReminderManager
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeContainer
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGoalReminderSheet(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val reminderManager = remember { TrainingGoalReminderManager.getInstance(context) }
  val goals by reminderManager.goals.collectAsState()
  val settings by reminderManager.settings.collectAsState()

  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
    if (isGranted) {
      Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Notifications disabled. Daily goal reminders cannot be delivered.", Toast.LENGTH_LONG).show()
    }
  }

  val activeGoal = goals.find { it.id == settings.selectedGoalId } ?: goals.first()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SurfaceCanvas,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .testTag("daily_goal_reminder_sheet")
    ) {
      // Header with close button
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
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Daily Drill Reminders",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Text(
              text = "Tied to your active badminton fitness targets",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant
              )
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = DarkSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Permission Warning Card if needed
      if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ShuttleOrangeContainer.copy(alpha = 0.2f)),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ShuttleOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsOff,
              contentDescription = null,
              tint = ShuttleOrange,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Notifications Disabled",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Text(
                text = "Grant notification permission to receive your daily fitness drill alerts.",
                style = MaterialTheme.typography.bodySmall.copy(color = DarkSurfaceVariant)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
              colors = ButtonDefaults.buttonColors(containerColor = ShuttleOrange),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("grant_notification_btn")
            ) {
              Text("Enable", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // 1. Master Toggle & Streak Status Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (settings.isEnabled) CourtGreenContainer else SurfaceContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (settings.isEnabled) Icons.Default.AlarmOn else Icons.Default.Alarm,
                contentDescription = null,
                tint = if (settings.isEnabled) CourtGreen else DarkSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (settings.isEnabled) "Reminders Active" else "Reminders Paused",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Whatshot,
                  contentDescription = null,
                  tint = ShuttleOrange,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${settings.daysStreak}-day streak protected",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ShuttleOrange
                  )
                )
              }
            }
          }

          Switch(
            checked = settings.isEnabled,
            onCheckedChange = { isChecked ->
              reminderManager.updateSettings(settings.copy(isEnabled = isChecked))
              if (isChecked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = CourtGreen
            ),
            modifier = Modifier.testTag("reminder_toggle_switch")
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. Select Active Fitness Goal Section
      Text(
        text = "TARGET FITNESS GOAL",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = CourtGreen,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )
      )
      Spacer(modifier = Modifier.height(8.dp))

      goals.forEach { goal ->
        val isSelected = goal.id == settings.selectedGoalId
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CourtGreen.copy(alpha = 0.08f) else SurfaceContainerLowest
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) CourtGreen else SurfaceContainerHigh,
              shape = RoundedCornerShape(14.dp)
            )
            .clickable { reminderManager.selectActiveGoal(goal.id) }
            .testTag("goal_item_${goal.id}")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = goal.iconSymbol,
                  fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = goal.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = DarkSurface
                    )
                  )
                  Text(
                    text = goal.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = DarkSurfaceVariant,
                      fontSize = 10.sp
                    )
                  )
                }
              }

              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Selected Goal",
                  tint = CourtGreen,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar & metrics
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                  .weight(1f)
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = if (isSelected) CourtGreen else ShuttleOrange,
                trackColor = SurfaceContainer
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "${goal.currentValue} / ${goal.targetValue} ${goal.unit}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isSelected) CourtGreen else DarkSurface
                )
              )
            }

            if (isSelected) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = goal.motivationalQuote,
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

      // 3. Preferred Daily Reminder Time Selection
      Text(
        text = "DAILY REMINDER TIME",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = CourtGreen,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )
      )
      Spacer(modifier = Modifier.height(8.dp))

      val timeSlots = listOf(
        Triple("07:30 AM", 7, 30),
        Triple("12:30 PM", 12, 30),
        Triple("06:30 PM", 18, 30),
        Triple("08:00 PM", 20, 0)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        timeSlots.forEach { (label, hour, minute) ->
          val isTimeSelected = settings.hour == hour && settings.minute == minute
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isTimeSelected) CourtGreen else SurfaceContainerLowest)
              .border(
                1.dp,
                if (isTimeSelected) CourtGreen else SurfaceContainerHigh,
                RoundedCornerShape(12.dp)
              )
              .clickable {
                reminderManager.updateSettings(
                  settings.copy(hour = hour, minute = minute)
                )
              }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isTimeSelected) Color.White else DarkSurface,
                fontSize = 11.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 4. Test Notification Action Button
      Button(
        onClick = {
          if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          } else {
            reminderManager.triggerTestNotification(activeGoal.id)
            Toast.makeText(
              context,
              "Daily drill reminder posted for '${activeGoal.title}'!",
              Toast.LENGTH_SHORT
            ).show()
          }
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("send_test_reminder_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Send Test Goal Reminder Now",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Motivational preview footer
      Text(
        text = "💡 AceAI aligns reminder notifications dynamically to your current training backlog, encouraging focused biomechanical improvement.",
        style = MaterialTheme.typography.bodySmall.copy(
          color = DarkSurfaceVariant,
          fontSize = 11.sp
        ),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
