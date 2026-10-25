package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeminiLiveVoiceEngine
import com.example.model.LiveVoiceState
import com.example.model.LyriaModelType
import com.example.model.LyriaMusicGenerator
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun LiveVoiceCoachScreen(
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val voiceEngine = remember { GeminiLiveVoiceEngine.getInstance(context) }
  val voiceState by voiceEngine.state.collectAsState()
  val liveTranscript by voiceEngine.liveTranscript.collectAsState()

  val musicGenerator = remember { LyriaMusicGenerator.getInstance(context) }
  val isGeneratingMusic by musicGenerator.isGenerating.collectAsState()
  val isPlayingMusic by musicGenerator.isPlaying.collectAsState()
  val currentTrack by musicGenerator.currentTrack.collectAsState()
  val musicStatus by musicGenerator.statusMessage.collectAsState()

  var musicPrompt by remember { mutableStateOf("High-energy 145 BPM badminton court warmup beat with driving bass and energetic synth loops") }
  var selectedLyriaModel by remember { mutableStateOf(LyriaModelType.CLIP_PREVIEW) }

  val audioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    voiceEngine.startLiveListening()
  }

  // Pulsing mic animation for live voice mode
  val infiniteTransition = rememberInfiniteTransition()
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    )
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("live_voice_coach_screen")
  ) {
    // 1. Top Header
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

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = "Gemini 3.8 Live & Lyria AI Studio",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
          Text(
            text = "Real-Time Voice Coaching & AI Music Generation",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 10.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 2. Gemini 3.8 Live Voice Coaching Section
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Gemini 3.8 Live Voice Conversation",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mic Button with Pulse
        Box(
          modifier = Modifier
            .size(100.dp)
            .scale(if (voiceState == LiveVoiceState.LISTENING) pulseScale else 1f)
            .clip(CircleShape)
            .background(
              when (voiceState) {
                LiveVoiceState.LISTENING -> ShuttleOrange
                LiveVoiceState.SPEAKING -> CourtGreen
                else -> DarkSurface
              }
            )
            .clickable {
              if (voiceState == LiveVoiceState.LISTENING) {
                voiceEngine.stopListening()
              } else {
                val hasPerm = ContextCompat.checkSelfPermission(
                  context,
                  Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPerm) {
                  audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                  voiceEngine.startLiveListening()
                }
              }
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (voiceState == LiveVoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Tap to talk",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = when (voiceState) {
            LiveVoiceState.LISTENING -> "Listening... Speak your badminton question!"
            LiveVoiceState.PROCESSING -> "Processing via Gemini 3.8 Live API..."
            LiveVoiceState.SPEAKING -> "Coach Maya speaking..."
            else -> "Tap microphone to begin real-time voice coaching"
          },
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = if (voiceState == LiveVoiceState.LISTENING) ShuttleOrange else DarkSurface,
            textAlign = TextAlign.Center
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = liveTranscript,
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurface,
              fontSize = 12.sp,
              lineHeight = 16.sp
            ),
            modifier = Modifier.padding(12.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 3. Lyria AI Court Music Generator
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = ShuttleOrange,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Lyria AI Court Music Generator",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lyria Model Selection
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LyriaModelType.values().forEach { modelType ->
            val isSelected = selectedLyriaModel == modelType
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) CourtGreen else SurfaceContainer)
                .clickable { selectedLyriaModel = modelType }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (modelType == LyriaModelType.CLIP_PREVIEW) "Lyria Clip (30s)" else "Lyria Pro (Track)",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else DarkSurface,
                  fontSize = 11.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = musicPrompt,
          onValueChange = { musicPrompt = it },
          label = { Text("Court Workout Music Prompt") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = {
            musicGenerator.generateCourtMusic(musicPrompt, selectedLyriaModel)
          },
          enabled = !isGeneratingMusic,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("generate_lyria_music_btn")
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isGeneratingMusic) "Generating with Lyria AI..." else "Generate Court Workout Soundtrack",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }

        if (musicStatus != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = musicStatus ?: "",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            )
          )
        }

        currentTrack?.let { track ->
          Spacer(modifier = Modifier.height(14.dp))
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Track Generated (${track.modelUsed.label})",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtGreen
                  )
                )
                Text(
                  text = track.prompt,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkSurfaceVariant,
                    fontSize = 10.sp
                  ),
                  maxLines = 1
                )
              }

              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(CourtGreen)
                  .clickable {
                    if (isPlayingMusic) {
                      musicGenerator.stopPlayback()
                    } else {
                      musicGenerator.playTrack(track)
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isPlayingMusic) Icons.Default.Stop else Icons.Default.PlayArrow,
                  contentDescription = "Play/Stop Track",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
