package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AceImages
import com.example.model.CameraCalibrationManager
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
fun CalibrationHubScreen(
  onBack: () -> Unit,
  onOpenOpticalAlignment: () -> Unit,
  onOpenLiveCameraPose: () -> Unit = {}
) {
  val context = LocalContext.current
  val calibrationManager = remember { CameraCalibrationManager.getInstance(context) }
  val calibrationTelemetry by calibrationManager.telemetry.collectAsState()
  val isCalibrationLocked by calibrationManager.isCalibrationLocked.collectAsState()
  val selectedPreset by calibrationManager.selectedPreset.collectAsState()
  var stringTension by remember { mutableFloatStateOf(25.8f) }
  var sensitivity by remember { mutableStateOf("High") }
  var voiceAudioCues by remember { mutableStateOf(true) }
  var autoClipKeyReps by remember { mutableStateOf(true) }
  var ghostOverlayModel by remember { mutableStateOf("Viktor Axelsen Smash Model") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("calibration_hub_screen")
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
        Text(
          text = "Kinetic Calibration",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }

      Text(
        text = "SYNCED",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = CourtGreen,
          fontSize = 10.sp
        ),
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(CourtGreen.copy(alpha = 0.12f))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Athlete Biometric Profile Hero Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(62.dp)
              .clip(CircleShape)
          ) {
            AsyncImage(
              model = AceImages.CALIBRATION_ALEX_AVATAR,
              contentDescription = "Alex Tan Avatar",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = "Alex Tan",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DarkSurface,
                fontSize = 19.sp
              )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Attacking Singles Specialist",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(CourtGreen)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Silver I • Level 14",
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

        // Biometrics Grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          BioPill(label = "HEIGHT", value = "182 cm", modifier = Modifier.weight(1f))
          BioPill(label = "WINGSPAN", value = "188 cm", modifier = Modifier.weight(1f))
          BioPill(label = "HAND", value = "Right", modifier = Modifier.weight(1f))
          BioPill(label = "APE INDEX", value = "+6 cm (1.03)", modifier = Modifier.weight(1.2f))
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 3. Equipment Locker (Racket & Stringing Specs)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.SportsTennis,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Equipment Locker",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }

      Text(
        text = "ACTIVE SETUP",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = ShuttleOrange,
          fontSize = 10.sp
        ),
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(ShuttleOrangeContainer.copy(alpha = 0.15f))
          .padding(horizontal = 8.dp, vertical = 3.dp)
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
          .padding(16.dp)
      ) {
        Text(
          text = "Yonex Astrox 88D Pro",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = DarkSurface,
            fontSize = 17.sp
          )
        )
        Text(
          text = "3U (88g) • G5 Grip Size • Head-Heavy Flex",
          style = MaterialTheme.typography.bodySmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 12.sp
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // String tension slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "String Setup: BG80 Power",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = DarkSurface
            )
          )
          Text(
            text = "%.1f lbs".format(stringTension),
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = CourtGreen
            )
          )
        }

        Slider(
          value = stringTension,
          onValueChange = { stringTension = it },
          valueRange = 22f..32f,
          colors = SliderDefaults.colors(
            thumbColor = CourtGreen,
            activeTrackColor = CourtGreen,
            inactiveTrackColor = SurfaceContainer
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          BioPill(label = "GRIP MODEL", value = "AC102EX Super Grap", modifier = Modifier.weight(1f))
          BioPill(label = "BALANCE PT", value = "305 mm", modifier = Modifier.weight(1f))
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 4. Camera & Court Calibration Card
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CameraAlt,
          contentDescription = null,
          tint = CourtGreen,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Court Optical Calibration",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
      }
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
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(width = 68.dp, height = 54.dp)
              .clip(RoundedCornerShape(8.dp))
          ) {
            AsyncImage(
              model = AceImages.COURT_CAMERA_READY,
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isCalibrationLocked) "${selectedPreset.displayName} [LOCKED]" else selectedPreset.displayName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface,
                fontSize = 14.sp
              )
            )
            Text(
              text = "Tilt: ${"%.1f".format(calibrationTelemetry.rollDeg)}° • Pitch: ${"%.1f".format(calibrationTelemetry.pitchDeg)}° • Dist: ${"%.1f".format(calibrationTelemetry.estimatedDistanceM)}m",
              style = MaterialTheme.typography.bodySmall.copy(
                color = if (isCalibrationLocked) CourtGreen else DarkSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { onOpenLiveCameraPose() },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
            modifier = Modifier
              .weight(1.1f)
              .height(44.dp)
              .testTag("hub_open_live_camera_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Live Camera Preview",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }

          Button(
            onClick = { onOpenOpticalAlignment() },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("launch_optical_alignment_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Court Grid",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 5. AI Feedback Preferences
    Text(
      text = "AI Coaching Preferences",
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
        // Sensitivity Selector
        Column {
          Text(
            text = "Biomechanical Fault Sensitivity",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = DarkSurface
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("Low", "Medium", "High (Strict)").forEach { item ->
              val isSelected = sensitivity.startsWith(item.take(4))
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) CourtGreen else SurfaceContainer)
                  .clickable { sensitivity = item }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = item,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else DarkSurfaceVariant,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }
        }

        // Audio Voice Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Live Voice Audio Cues",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = DarkSurface
              )
            )
            Text(
              text = "Provides spoken audio feedback during active drill swings",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }

          Switch(
            checked = voiceAudioCues,
            onCheckedChange = { voiceAudioCues = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = CourtGreen
            )
          )
        }

        // Auto Clip Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Auto-Clip Key Reps (240 FPS)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = DarkSurface
              )
            )
            Text(
              text = "Saves slow-motion video snippets of best & fault executions",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }

          Switch(
            checked = autoClipKeyReps,
            onCheckedChange = { autoClipKeyReps = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = CourtGreen
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 6. Save Button
    Button(
      onClick = {
        Toast.makeText(context, "Calibration Profile Saved!", Toast.LENGTH_SHORT).show()
        onBack()
      },
      shape = RoundedCornerShape(26.dp),
      colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("save_calibration_profile_btn")
    ) {
      Icon(
        imageVector = Icons.Default.Save,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Save Calibration Profile",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      )
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
fun BioPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(SurfaceContainerLow)
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        color = DarkSurfaceVariant,
        fontSize = 8.sp,
        letterSpacing = 0.4.sp
      )
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        color = DarkSurface,
        fontSize = 11.sp
      ),
      maxLines = 1
    )
  }
}
