package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AceImages
import com.example.model.DynamicExercise
import com.example.model.WarmupFocusArea
import com.example.model.WarmupRoutinePlan
import com.example.model.WarmupRoutineRepository
import com.example.tts.TechniqueVoiceCoach
import com.example.ui.components.DynamicExerciseAnimationCanvas
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.CourtGreenFixedDim
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeContainer
import com.example.ui.theme.ShuttleOrangeFixed
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.delay

@Composable
fun WarmupRoutineScreen(
  onBack: () -> Unit,
  onStartDrillOrCourt: () -> Unit,
  initialFocusArea: WarmupFocusArea = WarmupFocusArea.WRIST_FLEXIBILITY
) {
  val context = LocalContext.current
  val voiceCoach = remember { TechniqueVoiceCoach.getInstance(context) }

  var selectedFocus by remember { mutableStateOf(initialFocusArea) }
  var currentPlan by remember(selectedFocus) {
    mutableStateOf(WarmupRoutineRepository.getRoutineForFocusArea(selectedFocus))
  }

  // Active Execution State
  var isRoutineActive by remember { mutableStateOf(false) }
  var currentExerciseIndex by remember { mutableIntStateOf(0) }
  var exerciseSecondsRemaining by remember { mutableIntStateOf(30) }
  var isTimerPaused by remember { mutableStateOf(false) }
  var isRoutineFinished by remember { mutableStateOf(false) }

  val currentExercise = currentPlan.exercises.getOrNull(currentExerciseIndex)

  // Timer Tick Coroutine
  LaunchedEffect(isRoutineActive, isTimerPaused, currentExerciseIndex, exerciseSecondsRemaining) {
    if (isRoutineActive && !isTimerPaused && exerciseSecondsRemaining > 0) {
      delay(1000L)
      exerciseSecondsRemaining -= 1
    } else if (isRoutineActive && !isTimerPaused && exerciseSecondsRemaining == 0) {
      // Completed current exercise, advance to next
      if (currentExerciseIndex < currentPlan.exercises.size - 1) {
        currentExerciseIndex += 1
        val nextEx = currentPlan.exercises[currentExerciseIndex]
        exerciseSecondsRemaining = nextEx.durationSeconds
        voiceCoach.speak("Next up: ${nextEx.name}. ${nextEx.repRecommendation}", priority = true)
      } else {
        // Finished all exercises
        isRoutineActive = false
        isRoutineFinished = true
        voiceCoach.speak("Warmup routine complete! Kinetic chain primed and injury shielded. Ready for the court!", priority = true)
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("warmup_routine_screen")
  ) {
    // 1. Header Bar
    Surface(
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(SurfaceContainerHigh)
              .clickable { onBack() }
              .testTag("warmup_back_btn"),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Dynamic Warmup",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(CourtGreen.copy(alpha = 0.15f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "INJURY SHIELD",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = CourtGreen,
                    fontSize = 9.sp
                  )
                )
              }
            }
            Text(
              text = "Targeted mobility & tendon priming",
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
          }
        }

        // Voice Coach Toggle Shortcut
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(CourtGreen.copy(alpha = 0.12f))
            .clickable {
              voiceCoach.speak("Focusing on ${selectedFocus.displayName} to prevent injuries during intense play.", priority = true)
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Voice Cues",
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    if (isRoutineFinished) {
      // Warmup Complete Celebration View
      WarmupFinishedView(
        plan = currentPlan,
        onRestart = {
          isRoutineFinished = false
          isRoutineActive = false
          currentExerciseIndex = 0
          exerciseSecondsRemaining = currentPlan.exercises[0].durationSeconds
        },
        onProceedToCourt = onStartDrillOrCourt
      )
    } else if (isRoutineActive && currentExercise != null) {
      // Active Exercise Guided Follow-Along Mode
      ActiveExerciseFollowAlongView(
        plan = currentPlan,
        exercise = currentExercise,
        exerciseIndex = currentExerciseIndex,
        totalExercises = currentPlan.exercises.size,
        secondsRemaining = exerciseSecondsRemaining,
        isPaused = isTimerPaused,
        onTogglePause = { isTimerPaused = !isTimerPaused },
        onNextExercise = {
          if (currentExerciseIndex < currentPlan.exercises.size - 1) {
            currentExerciseIndex += 1
            exerciseSecondsRemaining = currentPlan.exercises[currentExerciseIndex].durationSeconds
          } else {
            isRoutineActive = false
            isRoutineFinished = true
          }
        },
        onPreviousExercise = {
          if (currentExerciseIndex > 0) {
            currentExerciseIndex -= 1
            exerciseSecondsRemaining = currentPlan.exercises[currentExerciseIndex].durationSeconds
          }
        },
        onQuitRoutine = {
          isRoutineActive = false
          isTimerPaused = false
        }
      )
    } else {
      // Plan Selection and Routine Overview Mode
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Hero Visual Banner
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
              AsyncImage(
                model = AceImages.DRILL_POSTER,
                contentDescription = "Warmup Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.35f)
              )

              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Column {
                    Text(
                      text = "DYNAMIC MOBILITY ENGINE",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShuttleOrange,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                      )
                    )
                    Text(
                      text = "Injury Prevention Hub",
                      style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                      )
                    )
                  }

                  Box(
                    modifier = Modifier
                      .clip(CircleShape)
                      .background(CourtGreen)
                      .padding(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Text(
                      text = "${currentPlan.totalDurationMinutes} MIN",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CourtGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Shields joints against 300+ km/h smash impact shock",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color.White.copy(alpha = 0.85f),
                      fontSize = 12.sp
                    )
                  )
                }
              }
            }
          }
        }

        // Focus Area Selector Chips
        item {
          Column {
            Text(
              text = "SELECT TODAY'S FOCUS AREA",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurfaceVariant,
                letterSpacing = 0.8.sp
              )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              WarmupFocusArea.values().forEach { area ->
                val isSelected = selectedFocus == area
                val badgeColor = Color(area.badgeHex)

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) badgeColor else SurfaceContainerHigh)
                    .clickable {
                      selectedFocus = area
                      currentPlan = WarmupRoutineRepository.getRoutineForFocusArea(area)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("focus_chip_${area.id}")
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = area.iconEmoji,
                      fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = area.shortLabel,
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else DarkSurface
                      )
                    )
                  }
                }
              }
            }
          }
        }

        // Active Focus Area Information Card
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  DynamicExerciseAnimationCanvas(
                    focusArea = selectedFocus,
                    isActive = true,
                    modifier = Modifier.size(54.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = selectedFocus.displayName,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkSurface
                      )
                    )
                    Text(
                      text = "${currentPlan.totalExercises} dynamic movements • ${currentPlan.totalDurationMinutes} minutes",
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkSurfaceVariant,
                        fontSize = 12.sp
                      )
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(selectedFocus.badgeHex).copy(alpha = 0.10f))
                  .padding(12.dp)
              ) {
                Row(verticalAlignment = Alignment.Top) {
                  Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = Color(selectedFocus.badgeHex),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = selectedFocus.primaryInjuryTarget,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = DarkSurface,
                      fontSize = 12.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = currentPlan.summaryBenefit,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              Button(
                onClick = {
                  isRoutineActive = true
                  currentExerciseIndex = 0
                  exerciseSecondsRemaining = currentPlan.exercises[0].durationSeconds
                  voiceCoach.speak("Starting ${currentPlan.title}. Exercise 1: ${currentPlan.exercises[0].name}", priority = true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(selectedFocus.badgeHex)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("start_warmup_routine_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Start Guided Warmup (${currentPlan.totalDurationMinutes} Min)",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
            }
          }
        }

        // Exercises Routine Step Breakdown
        item {
          Text(
            text = "ROUTINE EXERCISES SEQUENCE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurfaceVariant,
              letterSpacing = 0.8.sp
            )
          )
        }

        itemsIndexed(currentPlan.exercises) { index, exercise ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            modifier = Modifier.fillMaxWidth().testTag("exercise_card_$index")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(exercise.focusArea.badgeHex).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${index + 1}",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(exercise.focusArea.badgeHex)
                  )
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = exercise.name,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkSurface
                  )
                )
                Text(
                  text = "${exercise.durationSeconds}s • ${exercise.repRecommendation}",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkSurfaceVariant,
                    fontSize = 11.sp
                  )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = exercise.injuryPreventionFocus,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF475569),
                    fontSize = 11.sp
                  ),
                  maxLines = 2
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(SurfaceContainerHigh)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${exercise.durationSeconds}s",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkSurface,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Active Exercise Guided Follow-Along UI Screen
 */
@Composable
fun ActiveExerciseFollowAlongView(
  plan: WarmupRoutinePlan,
  exercise: DynamicExercise,
  exerciseIndex: Int,
  totalExercises: Int,
  secondsRemaining: Int,
  isPaused: Boolean,
  onTogglePause: () -> Unit,
  onNextExercise: () -> Unit,
  onPreviousExercise: () -> Unit,
  onQuitRoutine: () -> Unit
) {
  val context = LocalContext.current
  val voiceCoach = remember { TechniqueVoiceCoach.getInstance(context) }
  val progress = 1f - (secondsRemaining.toFloat() / exercise.durationSeconds.toFloat().coerceAtLeast(1f))

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Progress Indicator & Exercise Number
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "EXERCISE ${exerciseIndex + 1} OF $totalExercises",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = Color(exercise.focusArea.badgeHex),
          letterSpacing = 1.sp
        )
      )

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceContainerHigh)
          .clickable { onQuitRoutine() }
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = "Exit",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurfaceVariant
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    LinearProgressIndicator(
      progress = { (exerciseIndex.toFloat() + progress) / totalExercises.toFloat() },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = Color(exercise.focusArea.badgeHex),
      trackColor = SurfaceContainerHigh
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Interactive Kinetic Mobility Animation Canvas
    DynamicExerciseAnimationCanvas(
      focusArea = exercise.focusArea,
      isActive = !isPaused,
      modifier = Modifier.size(160.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Exercise Name & Target Muscles
    Text(
      text = exercise.name,
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.ExtraBold,
        color = DarkSurface,
        fontSize = 20.sp,
        textAlign = TextAlign.Center
      )
    )

    Text(
      text = exercise.targetMuscles,
      style = MaterialTheme.typography.bodySmall.copy(
        color = DarkSurfaceVariant,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Big Countdown Timer Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60),
          style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color(exercise.focusArea.badgeHex),
            fontSize = 46.sp
          )
        )

        Text(
          text = exercise.repRecommendation,
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = DarkSurface
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Timer Controls Row
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(SurfaceContainerHigh)
              .clickable { onPreviousExercise() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Previous",
              tint = DarkSurface,
              modifier = Modifier.size(20.dp)
            )
          }

          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Color(exercise.focusArea.badgeHex))
              .clickable { onTogglePause() }
              .testTag("pause_resume_timer_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (isPaused) "Resume" else "Pause",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(SurfaceContainerHigh)
              .clickable { onNextExercise() }
              .testTag("skip_next_exercise_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Next",
              tint = DarkSurface,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Step-by-Step Instructions Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "EXECUTION INSTRUCTIONS",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurfaceVariant,
            letterSpacing = 0.8.sp
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        exercise.stepInstructions.forEachIndexed { i, step ->
          Row(
            modifier = Modifier.padding(vertical = 3.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = "${i + 1}.",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(exercise.focusArea.badgeHex)
              ),
              modifier = Modifier.width(20.dp)
            )
            Text(
              text = step,
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurface,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pro Tip & Injury Prevention
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFEF3C7))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = ShuttleOrange,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Pro Tip: ${exercise.proTip}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = DarkSurface,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }
  }
}

/**
 * Routine Finished Confirmation Screen
 */
@Composable
fun WarmupFinishedView(
  plan: WarmupRoutinePlan,
  onRestart: () -> Unit,
  onProceedToCourt: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .background(CourtGreen.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Success",
        tint = CourtGreen,
        modifier = Modifier.size(48.dp)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = "Warmup Complete!",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        color = DarkSurface
      )
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Kinetic chain activated & injury protection shield engaged for ${plan.focusArea.displayName}.",
      style = MaterialTheme.typography.bodyMedium.copy(
        color = DarkSurfaceVariant,
        textAlign = TextAlign.Center,
        fontSize = 13.sp
      )
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "COMPLETED DURATION",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurfaceVariant
              )
            )
            Text(
              text = "${plan.totalDurationMinutes} Minutes",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "EXERCISES CLEARED",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurfaceVariant
              )
            )
            Text(
              text = "${plan.totalExercises} Drills",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CourtGreen.copy(alpha = 0.12f))
            .padding(10.dp)
        ) {
          Text(
            text = "⚡ ${plan.recommendedForDrill}",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = CourtGreen,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onProceedToCourt,
      colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("proceed_to_court_btn")
    ) {
      Icon(
        imageVector = Icons.Default.SportsTennis,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Proceed to Court & Live Drills",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
      onClick = onRestart,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = null,
        tint = DarkSurface,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Redo Warmup",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurface
        )
      )
    }
  }
}
