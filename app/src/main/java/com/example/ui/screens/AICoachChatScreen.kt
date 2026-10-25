package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AICoachService
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.DynamicBiomechanicalAnalysisEngine
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun AICoachChatScreen(
  onBack: () -> Unit,
  onOpenSessionReview: (String) -> Unit = {},
  onOpenDrillDetail: (String) -> Unit = {}
) {
  val coachService = remember { AICoachService.getInstance() }
  val messages by coachService.messages.collectAsState()
  val isGenerating by coachService.isGenerating.collectAsState()

  val analysisEngine = remember { DynamicBiomechanicalAnalysisEngine.getInstance() }
  val reps by analysisEngine.sessionReps.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
  val listState = rememberLazyListState()

  // Auto-scroll to bottom when messages update
  LaunchedEffect(messages.size, isGenerating) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SurfaceCanvas)
      .navigationBarsPadding()
      .imePadding()
      .testTag("ai_coach_chat_screen")
  ) {
    // 1. Top Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SurfaceContainerLowest)
        .padding(horizontal = 16.dp, vertical = 12.dp),
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

        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(CourtGreen),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Coach Maya AI",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(CourtGreen)
            )
          }
          Text(
            text = "Multi-turn Gemini Biomechanics Coach",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 10.sp
            )
          )
        }
      }
    }

    // 2. Model Selection Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SurfaceContainerLow)
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Model:",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = DarkSurfaceVariant,
          fontSize = 10.sp
        )
      )

      listOf(
        "gemini-3.5-flash" to "Flash (General)",
        "gemini-3.1-pro-preview" to "Pro (Complex)",
        "gemini-3.1-flash-lite-preview" to "Lite (Fast)"
      ).forEach { (modelId, label) ->
        val isSelected = selectedModel == modelId
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CourtGreen else SurfaceContainer)
            .clickable { selectedModel = modelId }
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color.White else DarkSurface,
              fontSize = 10.sp
            )
          )
        }
      }
    }

    // 3. Message Thread
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item { Spacer(modifier = Modifier.height(8.dp)) }

      items(messages, key = { it.id }) { msg ->
        ChatMessageBubble(
          message = msg,
          onOpenSessionReview = onOpenSessionReview,
          onOpenDrillDetail = onOpenDrillDetail
        )
      }

      if (isGenerating) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = CourtGreen,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Coach Maya is analyzing with $selectedModel...",
              style = MaterialTheme.typography.labelSmall.copy(color = DarkSurfaceVariant)
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    // 4. Quick Prompts
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(
        "Why did Rep #7 fault?",
        "How do I reach 250 km/h?",
        "Design a 10-min smash drill",
        "Explain my wrist pronation"
      ).forEach { prompt ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainer)
            .clickable { coachService.sendUserMessage(prompt) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = prompt,
            style = MaterialTheme.typography.labelSmall.copy(
              color = DarkSurface,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    // 5. Input Field
    Surface(
      color = SurfaceContainerLowest,
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text("Ask Coach Maya...") },
          modifier = Modifier
            .weight(1f)
            .testTag("ai_chat_input_field"),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CourtGreen,
            unfocusedBorderColor = SurfaceContainer
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(
            onSend = {
              if (inputText.isNotBlank()) {
                coachService.sendUserMessage(inputText)
                inputText = ""
              }
            }
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (inputText.isNotBlank()) CourtGreen else SurfaceContainer)
            .clickable {
              if (inputText.isNotBlank()) {
                coachService.sendUserMessage(inputText)
                inputText = ""
              }
            }
            .testTag("ai_chat_send_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Message",
            tint = if (inputText.isNotBlank()) Color.White else DarkSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onOpenSessionReview: (String) -> Unit,
  onOpenDrillDetail: (String) -> Unit
) {
  val isUser = message.sender == ChatSender.USER

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(CourtGreen),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      modifier = Modifier.fillMaxWidth(0.85f),
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
      Card(
        shape = RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isUser) 16.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        colors = CardDefaults.cardColors(
          containerColor = if (isUser) CourtGreen else SurfaceContainerLowest
        )
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (isUser) Color.White else DarkSurface,
              fontSize = 13.sp,
              lineHeight = 18.sp
            )
          )

          if (message.suggestedDrillId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = { onOpenDrillDetail(message.suggestedDrillId) },
              colors = ButtonDefaults.buttonColors(containerColor = ShuttleOrange),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Open Recommended Drill",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = message.timestamp,
        style = MaterialTheme.typography.labelSmall.copy(
          color = DarkSurfaceVariant,
          fontSize = 9.sp
        )
      )
    }
  }
}
