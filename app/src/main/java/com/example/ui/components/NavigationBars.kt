package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AceImages
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.DarkSurface

@Composable
fun AceTopBar(
  onProfileClick: () -> Unit = {},
  actionContent: (@Composable () -> Unit)? = null
) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    modifier = Modifier
      .fillMaxWidth()
      .height(64.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onProfileClick() }
      ) {
        AsyncImage(
          model = AceImages.LOGO,
          contentDescription = "AceAI Shuttle Logo",
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(verticalArrangement = Arrangement.Center) {
          Text(
            text = "AceAI",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 19.sp,
              color = DarkSurface
            )
          )
          Text(
            text = "BADMINTON COACH",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = CourtGreen,
              letterSpacing = 1.sp,
              fontSize = 9.sp
            )
          )
        }
      }

      if (actionContent != null) {
        actionContent()
      } else {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CourtGreen)
            .clickable { onProfileClick() }
            .testTag("top_bar_profile_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile and Settings",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

enum class NavTab {
  HOME, TRAIN, AI_CHAT, PROGRESS, PROFILE
}

@Composable
fun AceBottomNavBar(
  currentTab: NavTab,
  onTabSelected: (NavTab) -> Unit
) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    shadowElevation = 8.dp,
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavBarItem(
        label = "Home",
        icon = Icons.Default.Home,
        selected = currentTab == NavTab.HOME,
        onClick = { onTabSelected(NavTab.HOME) },
        testTag = "nav_home"
      )
      NavBarItem(
        label = "Train",
        icon = Icons.Default.SportsTennis,
        selected = currentTab == NavTab.TRAIN,
        onClick = { onTabSelected(NavTab.TRAIN) },
        testTag = "nav_train"
      )
      NavBarItem(
        label = "AI Coach",
        icon = Icons.Default.Psychology,
        selected = currentTab == NavTab.AI_CHAT,
        onClick = { onTabSelected(NavTab.AI_CHAT) },
        testTag = "nav_ai_chat"
      )
      NavBarItem(
        label = "Progress",
        icon = Icons.Default.Insights,
        selected = currentTab == NavTab.PROGRESS,
        onClick = { onTabSelected(NavTab.PROGRESS) },
        testTag = "nav_progress"
      )
      NavBarItem(
        label = "Profile",
        icon = Icons.Default.Person,
        selected = currentTab == NavTab.PROFILE,
        onClick = { onTabSelected(NavTab.PROFILE) },
        testTag = "nav_profile"
      )
    }
  }
}

@Composable
private fun NavBarItem(
  label: String,
  icon: ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  val backgroundModifier = if (selected) {
    Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(CourtGreen.copy(alpha = 0.12f))
      .padding(horizontal = 12.dp, vertical = 4.dp)
  } else {
    Modifier
      .clip(RoundedCornerShape(20.dp))
      .padding(horizontal = 12.dp, vertical = 4.dp)
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clickable { onClick() }
      .testTag(testTag)
      .then(backgroundModifier)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (selected) CourtGreen else Color(0xFF6D7A72),
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) CourtGreen else Color(0xFF6D7A72),
        fontSize = 10.sp
      )
    )
  }
}
