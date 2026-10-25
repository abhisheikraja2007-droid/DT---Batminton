package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.CommonBiomechanicalFault
import com.example.model.DrillCategory
import com.example.model.DrillDifficulty
import com.example.model.DrillStepInstruction
import com.example.model.StructuredDrill
import com.example.model.StructuredDrillCatalog
import com.example.model.TargetSkillMetric
import com.example.tts.TechniqueVoiceCoach
import com.example.ui.components.DailyGoalReminderSheet
import com.example.db.BadmintonDrillModel
import com.example.db.BadmintonDrillRepository
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.collectAsState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.ShuttleOrangeContainer
import com.example.ui.theme.ShuttleOrangeFixed
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun DrillsLibraryScreen(
  onBack: () -> Unit,
  onStartLiveCameraDrill: (String) -> Unit,
  onOpenReminders: () -> Unit = {}
) {
  val context = LocalContext.current
  val voiceCoach = remember { TechniqueVoiceCoach.getInstance(context) }

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(DrillCategory.ALL) }
  var selectedDifficulty by remember { mutableStateOf(DrillDifficulty.ALL) }
  var selectedDrillForDetail by remember { mutableStateOf<StructuredDrill?>(null) }
  var showReminderSheet by remember { mutableStateOf(false) }
  var showAddFirestoreDrillModal by remember { mutableStateOf(false) }

  val drillRepo = remember { BadmintonDrillRepository.getInstance(context) }
  val firestoreDrills by drillRepo.drills.collectAsState()

  // Filter drills based on search query, category, and difficulty
  val filteredDrills = remember(searchQuery, selectedCategory, selectedDifficulty) {
    StructuredDrillCatalog.drills.filter { drill ->
      val matchesCategory = selectedCategory == DrillCategory.ALL || drill.category == selectedCategory
      val matchesDifficulty = selectedDifficulty == DrillDifficulty.ALL || drill.difficulty == selectedDifficulty
      val matchesQuery = if (searchQuery.isBlank()) {
        true
      } else {
        val q = searchQuery.trim().lowercase()
        drill.title.lowercase().contains(q) ||
            drill.subtitle.lowercase().contains(q) ||
            drill.summary.lowercase().contains(q) ||
            drill.category.label.lowercase().contains(q) ||
            drill.targetSkillMetrics.any { it.name.lowercase().contains(q) || it.targetValue.lowercase().contains(q) } ||
            drill.steps.any { it.title.lowercase().contains(q) || it.instruction.lowercase().contains(q) }
      }
      matchesCategory && matchesDifficulty && matchesQuery
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .testTag("drills_library_screen")
  ) {
    // 1. Top Bar Header
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(SurfaceContainer)
              .clickable { onBack() }
              .testTag("drills_library_back_btn"),
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
              text = "Drills Library",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Text(
              text = "${filteredDrills.size} structured badminton training drills",
              style = MaterialTheme.typography.bodySmall.copy(
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
          // Action: Add Firestore Drill
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(ShuttleOrange.copy(alpha = 0.15f))
              .clickable { showAddFirestoreDrillModal = true }
              .testTag("add_firestore_drill_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Create Drill Schema in Firestore",
              tint = ShuttleOrange,
              modifier = Modifier.size(20.dp)
            )
          }

          // Action: Daily Reminders Icon
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(CourtGreen.copy(alpha = 0.12f))
              .clickable { showReminderSheet = true }
              .testTag("drills_library_reminders_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = "Daily Goal Reminders",
              tint = CourtGreen,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // 2. Real-Time Search Bar
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("drill_search_input"),
        placeholder = {
          Text(
            "Search drills, techniques, or metrics (e.g. smash, 240, pronation)...",
            fontSize = 13.sp,
            color = DarkSurfaceVariant.copy(alpha = 0.7f)
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear search",
                tint = DarkSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = SurfaceContainerLowest,
          unfocusedContainerColor = SurfaceContainerLowest,
          focusedBorderColor = CourtGreen,
          unfocusedBorderColor = SurfaceContainerHigh
        )
      )
    }

    // 3. Category Filter Chips (Horizontal Scroll)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      DrillCategory.entries.forEach { category ->
        val isSelected = selectedCategory == category
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) CourtGreen else SurfaceContainerLowest)
            .border(
              1.dp,
              if (isSelected) CourtGreen else SurfaceContainerHigh,
              RoundedCornerShape(20.dp)
            )
            .clickable { selectedCategory = category }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("category_filter_${category.name}"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(category.icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = category.label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else DarkSurface,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 4. Difficulty Level Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      DrillDifficulty.entries.forEach { diff ->
        val isSelected = selectedDifficulty == diff
        val diffColor = getDifficultyColor(diff)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) diffColor.copy(alpha = 0.18f) else SurfaceContainerLowest)
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) diffColor else SurfaceContainerHigh,
              shape = RoundedCornerShape(14.dp)
            )
            .clickable { selectedDifficulty = diff }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("difficulty_filter_${diff.name}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = diff.label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) diffColor else DarkSurfaceVariant,
              fontSize = 10.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 5. Drills List
    if (filteredDrills.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = DarkSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No Drills Found",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DarkSurface
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try adjusting your search keywords or switching filters.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant
            )
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              searchQuery = ""
              selectedCategory = DrillCategory.ALL
              selectedDifficulty = DrillDifficulty.ALL
            },
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreen)
          ) {
            Text("Reset All Filters")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(filteredDrills, key = { it.id }) { drill ->
          StructuredDrillCard(
            drill = drill,
            onClick = { selectedDrillForDetail = drill },
            onStartCameraDrill = { onStartLiveCameraDrill(drill.id) }
          )
        }
      }
    }
  }

  // 6. Drill Detailed Inspector Modal Bottom Sheet
  if (selectedDrillForDetail != null) {
    DrillDetailModal(
      drill = selectedDrillForDetail!!,
      onDismiss = { selectedDrillForDetail = null },
      onStartCamera = {
        val id = selectedDrillForDetail!!.id
        selectedDrillForDetail = null
        onStartLiveCameraDrill(id)
      },
      voiceCoach = voiceCoach
    )
  }

  // 7. Daily Goal Reminder Sheet Modal
  if (showReminderSheet) {
    DailyGoalReminderSheet(onDismiss = { showReminderSheet = false })
  }

  // 8. Add Drill Firestore Schema Modal
  if (showAddFirestoreDrillModal) {
    AddDrillFirestoreModal(
      onDismiss = { showAddFirestoreDrillModal = false },
      onAddDrill = { name, diff, stroke, cat, dur, reps, sum ->
        drillRepo.addDrillToFirestore(
          drillName = name,
          difficulty = diff,
          targetStroke = stroke,
          category = cat,
          durationMinutes = dur,
          targetReps = reps,
          summary = sum
        )
      }
    )
  }
}

@Composable
fun StructuredDrillCard(
  drill: StructuredDrill,
  onClick: () -> Unit,
  onStartCameraDrill: () -> Unit
) {
  val diffColor = getDifficultyColor(drill.difficulty)

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("drill_card_${drill.id}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Image Banner with Overlay badges
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        AsyncImage(
          model = drill.imageUrl,
          contentDescription = drill.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Difficulty Badge (Top Left)
        Box(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopStart)
            .clip(RoundedCornerShape(8.dp))
            .background(diffColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = drill.difficulty.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              fontSize = 9.sp
            )
          )
        }

        // Hot Badge if available (Top Right)
        if (drill.badgeLabel != null) {
          Box(
            modifier = Modifier
              .padding(10.dp)
              .align(Alignment.TopEnd)
              .clip(RoundedCornerShape(8.dp))
              .background(ShuttleOrange)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = drill.badgeLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 9.sp
              )
            )
          }
        }

        // Duration & Reps Bottom Scrim Bar
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(drill.category.icon, fontSize = 11.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = drill.category.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 10.sp
                )
              )
            }

            Text(
              text = "${drill.durationMinutes} mins • ${drill.targetReps} reps • ~${drill.estimatedCalories} kcal",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp
              )
            )
          }
        }
      }

      // Card Body
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = drill.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface,
            fontSize = 16.sp
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = drill.subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            color = DarkSurfaceVariant,
            fontSize = 12.sp
          ),
          maxLines = 2
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Target Skill Metrics Highlight Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val keyMetric = drill.targetSkillMetrics.firstOrNull()
          if (keyMetric != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = CourtGreen,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${keyMetric.name}: ${keyMetric.targetValue} ${keyMetric.unit}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface,
                  fontSize = 11.sp
                )
              )
            }
          }

          Text(
            text = "${drill.steps.size} Steps",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = CourtGreen,
              fontSize = 10.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row: View Steps & Launch Camera Drill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
          ) {
            Text("Instructions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onStartCameraDrill,
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1.3f)
              .height(42.dp)
              .testTag("start_camera_drill_${drill.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Start Camera Drill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillDetailModal(
  drill: StructuredDrill,
  onDismiss: () -> Unit,
  onStartCamera: () -> Unit,
  voiceCoach: TechniqueVoiceCoach
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val diffColor = getDifficultyColor(drill.difficulty)

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
        .padding(horizontal = 20.dp, vertical = 6.dp)
        .testTag("drill_detail_modal")
    ) {
      // Top Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(diffColor)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = drill.difficulty.label.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 10.sp
              )
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${drill.category.icon} ${drill.category.label}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = DarkSurfaceVariant
            )
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = drill.title,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurface
        )
      )

      Text(
        text = drill.subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = DarkSurfaceVariant
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Quick Stats Card (Duration, Reps, Calories, Personal Best)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          DrillStatItem(label = "DURATION", value = "${drill.durationMinutes}m")
          DrillStatItem(label = "TARGET REPS", value = "${drill.targetReps}")
          DrillStatItem(label = "CALORIES", value = "${drill.estimatedCalories}")
          DrillStatItem(label = "BEST FORM", value = drill.personalBest.take(10))
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Equipment Required Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        drill.equipment.forEach { eq ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(SurfaceContainer)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "• $eq",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = DarkSurface
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 1. Target Skill Metrics Section
      Text(
        text = "TARGET SKILL METRICS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = CourtGreen,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )
      )
      Spacer(modifier = Modifier.height(8.dp))

      drill.targetSkillMetrics.forEach { metric ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                text = metric.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Text(
                text = metric.description,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp
                )
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${metric.targetValue} ${metric.unit}",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = CourtGreen
                )
              )
              Text(
                text = metric.benchmarkComparison,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = ShuttleOrange,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 2. Step-by-Step Instructions Section
      Text(
        text = "STEP-BY-STEP INSTRUCTIONS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = CourtGreen,
          letterSpacing = 1.sp,
          fontSize = 11.sp
        )
      )
      Spacer(modifier = Modifier.height(8.dp))

      drill.steps.forEach { step ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(CourtGreen),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${step.stepNumber}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = step.title,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = step.instruction,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = DarkSurface,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Biomechanical focus note
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceContainer)
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = DarkSurfaceVariant,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "FOCUS: ${step.biomechanicalFocus}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium
                )
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Coach Vocal Cue with TTS play button
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CourtGreenContainer.copy(alpha = 0.25f))
                .clickable {
                  voiceCoach.speak(step.coachCue, priority = true)
                  Toast.makeText(context, "Speaking vocal cue...", Toast.LENGTH_SHORT).show()
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.VolumeUp,
                  contentDescription = "Listen to verbal cue",
                  tint = CourtGreen,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "AUDIO CUE: \"${step.coachCue}\"",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CourtGreen,
                    fontSize = 10.sp
                  )
                )
              }
              Text(
                text = "Tap to hear",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = CourtGreen,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. Common Biomechanical Faults & Warnings
      if (drill.commonFaults.isNotEmpty()) {
        Text(
          text = "COMMON BIOMECHANICAL FAULTS",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = ShuttleOrange,
            letterSpacing = 1.sp,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        drill.commonFaults.forEach { fault ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShuttleOrangeContainer.copy(alpha = 0.15f)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = ShuttleOrange,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = fault.faultTitle,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkSurface
                  )
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = fault.description,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Coach Warning: \"${fault.voiceCorrectionCue}\"",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = ShuttleOrange,
                  fontSize = 10.sp
                )
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }

      // Bottom Primary Action: Start Camera Drill with Live AI Voice Coach
      Button(
        onClick = onStartCamera,
        colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("launch_camera_drill_from_detail_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Videocam,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Start Live Camera Drill With Voice Coach",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun DrillStatItem(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        color = DarkSurface
      )
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        color = DarkSurfaceVariant,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold
      )
    )
  }
}

private fun getDifficultyColor(diff: DrillDifficulty): Color {
  return when (diff) {
    DrillDifficulty.BEGINNER -> CourtGreen
    DrillDifficulty.INTERMEDIATE -> Color(0xFF2563EB) // Royal blue
    DrillDifficulty.ADVANCED -> ShuttleOrange
    DrillDifficulty.ELITE_PRO -> Color(0xFF7C3AED) // Purple
    DrillDifficulty.ALL -> DarkSurfaceVariant
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrillFirestoreModal(
  onDismiss: () -> Unit,
  onAddDrill: (String, String, String, String, Int, Int, String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var drillName by remember { mutableStateOf("") }
  var difficulty by remember { mutableStateOf("Intermediate") }
  var targetStroke by remember { mutableStateOf("Jump Smash") }
  var category by remember { mutableStateOf("Smash & Power") }
  var durationText by remember { mutableStateOf("20") }
  var targetRepsText by remember { mutableStateOf("20") }
  var summary by remember { mutableStateOf("") }

  val difficultyOptions = listOf("Beginner", "Intermediate", "Advanced", "Elite Pro")
  val strokeOptions = listOf(
    "Jump Smash",
    "Fast Slice Drop Shot",
    "High Defensive Clear",
    "Flat Drive Counter",
    "Reverse Slice Drop",
    "Steep Stick Smash",
    "Defensive Smash Block",
    "Net Spin & Tumbling"
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SurfaceContainerLowest
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Create New Firestore Drill Schema",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = DarkSurface
          )
        )
        IconButton(onClick = onDismiss) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = drillName,
        onValueChange = { drillName = it },
        label = { Text("Drill Name") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("new_drill_name_input"),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Target Stroke",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DarkSurface)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        strokeOptions.forEach { stroke ->
          val isSel = targetStroke == stroke
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSel) CourtGreen else SurfaceContainer)
              .clickable { targetStroke = stroke }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = stroke,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) Color.White else DarkSurface
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Difficulty Level",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DarkSurface)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        difficultyOptions.forEach { diff ->
          val isSel = difficulty == diff
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSel) CourtGreen else SurfaceContainer)
              .clickable { difficulty = diff }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = diff,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) Color.White else DarkSurface,
                fontSize = 11.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = durationText,
          onValueChange = { durationText = it },
          label = { Text("Duration (Mins)") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
          value = targetRepsText,
          onValueChange = { targetRepsText = it },
          label = { Text("Target Reps") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = summary,
        onValueChange = { summary = it },
        label = { Text("Drill Biomechanical Summary") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 2
      )

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = {
          if (drillName.isNotBlank()) {
            val dur = durationText.toIntOrNull() ?: 20
            val reps = targetRepsText.toIntOrNull() ?: 20
            onAddDrill(
              drillName,
              difficulty,
              targetStroke,
              category,
              dur,
              reps,
              summary.ifBlank { "Custom $targetStroke drill focusing on $difficulty technique execution." }
            )
            onDismiss()
          }
        },
        enabled = drillName.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("submit_new_firestore_drill_btn")
      ) {
        Text("Save & Initialize in Firestore", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
