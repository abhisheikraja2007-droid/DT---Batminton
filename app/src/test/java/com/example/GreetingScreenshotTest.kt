package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun app_renders_home_screen_successfully() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("home_screen_content").assertExists()
    composeTestRule.onNodeWithTag("start_live_coaching_card").assertExists()
  }

  @Test
  fun app_navigation_to_training_flow() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("start_live_coaching_card").performClick()
    composeTestRule.onNodeWithTag("camera_pose_screen").assertExists()
  }

  @Test
  fun app_navigation_to_camera_pose_screen_via_live_view() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("open_live_camera_preview_btn").performClick()
    composeTestRule.onNodeWithTag("camera_pose_screen").assertExists()
  }

  @Test
  fun app_navigation_to_profile_screen_via_bottom_nav() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("nav_profile").performClick()
    composeTestRule.onNodeWithTag("user_profile_screen").assertExists()
  }

  @Test
  fun app_navigation_to_profile_screen_via_top_bar() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("top_bar_profile_btn").performClick()
    composeTestRule.onNodeWithTag("user_profile_screen").assertExists()
  }

  @Test
  fun app_navigation_to_optical_calibration_flow() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("court_camera_ready_card").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("optical_alignment_screen").assertExists()
  }

  @Test
  fun app_navigation_to_warmup_routine_flow() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("open_warmup_routine_card").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("warmup_routine_screen").assertExists()
    composeTestRule.onNodeWithTag("start_warmup_routine_btn").assertExists()
  }

  @Test
  fun app_navigation_to_ai_coach_chat_flow() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AceAppRoot()
      }
    }

    composeTestRule.onNodeWithTag("nav_ai_chat").performClick()
    composeTestRule.onNodeWithTag("ai_coach_chat_screen").assertExists()
    composeTestRule.onNodeWithTag("ai_chat_input_field").assertExists()
    composeTestRule.onNodeWithTag("ai_chat_send_btn").assertExists()
  }
}
