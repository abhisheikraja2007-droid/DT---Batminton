package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.model.TermsAndPrivacyManager
import com.example.ui.components.TermsAndPrivacyModal
import com.example.ui.components.AceBottomNavBar
import com.example.ui.components.AceTopBar
import com.example.ui.components.NavTab
import com.example.ui.screens.AICoachChatScreen
import com.example.ui.screens.CalibrationHubScreen
import com.example.ui.screens.CameraPoseScreen
import com.example.ui.screens.DrillDetailScreen
import com.example.ui.screens.DrillsLibraryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OpticalAlignmentScreen
import com.example.ui.screens.SessionCompleteScreen
import com.example.ui.screens.SessionReviewScreen
import com.example.ui.screens.TrainScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.WarmupRoutineScreen
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.screens.LiveVoiceCoachScreen
import com.example.ui.screens.LoginScreen

enum class Screen {
  HOME,
  LOGIN,
  TRAIN,
  AI_CHAT,
  CAMERA_POSE,
  DRILL_DETAIL,
  DRILLS_LIBRARY,
  SESSION_COMPLETE,
  SESSION_REVIEW,
  CALIBRATION_HUB,
  OPTICAL_ALIGNMENT,
  PROFILE,
  WARMUP_ROUTINE,
  LIVE_VOICE_COACH
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }
  }
}

@Composable
fun AceAppRoot() {
  val context = LocalContext.current
  var hasAcceptedTerms by remember { mutableStateOf(TermsAndPrivacyManager.hasAcceptedTerms(context)) }

  if (!hasAcceptedTerms) {
    TermsAndPrivacyModal(
      onAccept = {
        hasAcceptedTerms = true
      }
    )
  }

  var currentScreen by remember { mutableStateOf(Screen.HOME) }
  var currentTab by remember { mutableStateOf(NavTab.HOME) }

  // Determines if we show standard top bar & bottom bar
  val showTopBar = currentScreen == Screen.HOME
  val showBottomBar = currentScreen == Screen.HOME || currentScreen == Screen.PROFILE || currentScreen == Screen.AI_CHAT

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      if (showTopBar) {
        AceTopBar(
          onProfileClick = {
            currentScreen = Screen.PROFILE
            currentTab = NavTab.PROFILE
          }
        )
      }
    },
    bottomBar = {
      if (showBottomBar) {
        AceBottomNavBar(
          currentTab = currentTab,
          onTabSelected = { tab ->
            currentTab = tab
            when (tab) {
              NavTab.HOME -> currentScreen = Screen.HOME
              NavTab.TRAIN -> currentScreen = Screen.TRAIN
              NavTab.AI_CHAT -> currentScreen = Screen.AI_CHAT
              NavTab.PROGRESS -> currentScreen = Screen.SESSION_COMPLETE
              NavTab.PROFILE -> currentScreen = Screen.PROFILE
            }
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
      ) { targetScreen ->
        when (targetScreen) {
          Screen.HOME -> {
            HomeScreen(
              onStartLiveCoaching = { currentScreen = Screen.CAMERA_POSE },
              onOpenDrillDetail = { drillId -> currentScreen = Screen.DRILL_DETAIL },
              onOpenOpticalAlignment = { currentScreen = Screen.OPTICAL_ALIGNMENT },
              onOpenCalibrationHub = { currentScreen = Screen.CALIBRATION_HUB },
              onOpenProfile = {
                currentScreen = Screen.PROFILE
                currentTab = NavTab.PROFILE
              },
              onOpenLiveCameraPose = { currentScreen = Screen.CAMERA_POSE },
              onOpenDrillsLibrary = { currentScreen = Screen.DRILLS_LIBRARY },
              onOpenWarmupRoutine = { currentScreen = Screen.WARMUP_ROUTINE },
              onOpenAIChat = {
                currentScreen = Screen.AI_CHAT
                currentTab = NavTab.AI_CHAT
              },
              onOpenLiveVoiceCoach = {
                currentScreen = Screen.LIVE_VOICE_COACH
              },
              onOpenLogin = {
                currentScreen = Screen.LOGIN
              }
            )
          }

          Screen.DRILLS_LIBRARY -> {
            DrillsLibraryScreen(
              onBack = { currentScreen = Screen.HOME },
              onStartLiveCameraDrill = { drillId ->
                currentScreen = Screen.CAMERA_POSE
              }
            )
          }

          Screen.CAMERA_POSE -> {
            CameraPoseScreen(
              onBack = { currentScreen = Screen.HOME },
              onOpenReview = { currentScreen = Screen.SESSION_REVIEW },
              onOpenWarmupRoutine = { currentScreen = Screen.WARMUP_ROUTINE }
            )
          }

          Screen.TRAIN -> {
            TrainScreen(
              onStopTraining = {
                currentScreen = Screen.SESSION_COMPLETE
                currentTab = NavTab.PROGRESS
              },
              onCalibrationClick = { currentScreen = Screen.OPTICAL_ALIGNMENT }
            )
          }

          Screen.AI_CHAT -> {
            AICoachChatScreen(
              onBack = {
                currentScreen = Screen.HOME
                currentTab = NavTab.HOME
              },
              onOpenSessionReview = {
                currentScreen = Screen.SESSION_REVIEW
              },
              onOpenDrillDetail = { drillId ->
                currentScreen = Screen.DRILL_DETAIL
              }
            )
          }

          Screen.DRILL_DETAIL -> {
            DrillDetailScreen(
              onBack = { currentScreen = Screen.HOME },
              onStartCameraDrill = { currentScreen = Screen.TRAIN },
              onOpenCalibration = { currentScreen = Screen.CALIBRATION_HUB }
            )
          }

          Screen.SESSION_COMPLETE -> {
            SessionCompleteScreen(
              onSaveAndContinue = {
                currentScreen = Screen.HOME
                currentTab = NavTab.HOME
              },
              onReviewReps = { currentScreen = Screen.SESSION_REVIEW }
            )
          }

          Screen.SESSION_REVIEW -> {
            SessionReviewScreen(
              onBack = { currentScreen = Screen.SESSION_COMPLETE },
              onOpenAIChat = {
                currentScreen = Screen.AI_CHAT
                currentTab = NavTab.AI_CHAT
              }
            )
          }

          Screen.CALIBRATION_HUB -> {
            CalibrationHubScreen(
              onBack = { currentScreen = Screen.HOME },
              onOpenOpticalAlignment = { currentScreen = Screen.OPTICAL_ALIGNMENT },
              onOpenLiveCameraPose = { currentScreen = Screen.CAMERA_POSE }
            )
          }

          Screen.OPTICAL_ALIGNMENT -> {
            OpticalAlignmentScreen(
              onDismiss = { currentScreen = Screen.HOME },
              onStartTracking = { currentScreen = Screen.CAMERA_POSE }
            )
          }

          Screen.PROFILE -> {
            UserProfileScreen(
              onBack = {
                currentScreen = Screen.HOME
                currentTab = NavTab.HOME
              },
              onOpenCalibration = {
                currentScreen = Screen.CALIBRATION_HUB
              },
              onOpenSessionReview = { sessionId ->
                currentScreen = Screen.SESSION_REVIEW
              },
              onOpenLogin = {
                currentScreen = Screen.LOGIN
              }
            )
          }

          Screen.WARMUP_ROUTINE -> {
            WarmupRoutineScreen(
              onBack = { currentScreen = Screen.HOME },
              onStartDrillOrCourt = { currentScreen = Screen.CAMERA_POSE }
            )
          }

          Screen.LIVE_VOICE_COACH -> {
            LiveVoiceCoachScreen(
              onBack = { currentScreen = Screen.HOME }
            )
          }

          Screen.LOGIN -> {
            LoginScreen(
              onLoginSuccess = {
                currentScreen = Screen.HOME
                currentTab = NavTab.HOME
              },
              onBack = {
                currentScreen = Screen.HOME
                currentTab = NavTab.HOME
              }
            )
          }
        }
      }
    }
  }
}
