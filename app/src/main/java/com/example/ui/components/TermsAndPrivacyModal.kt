package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.TermsAndPrivacyManager
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun TermsAndPrivacyModal(
  onAccept: () -> Unit
) {
  val context = LocalContext.current
  var isChecked by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = { /* Modal cannot be dismissed without accepting */ },
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(SurfaceCanvas)
        .padding(16.dp),
      color = SurfaceCanvas
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
          .testTag("terms_privacy_onboarding_modal")
      ) {
        // Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(CourtGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "Terms & Privacy",
              tint = CourtGreen,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = "Welcome to Ace AI",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen,
                fontSize = 14.sp
              )
            )
            Text(
              text = "Terms & Privacy Consent",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                color = DarkSurface
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terms Scrollable Card
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = SurfaceContainerLowest,
          shadowElevation = 4.dp,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(18.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CourtGreen.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.PrivacyTip,
                contentDescription = null,
                tint = CourtGreen,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "REQUIRED BEFORE FIRST USE",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = CourtGreen,
                  letterSpacing = 0.8.sp
                )
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = TermsAndPrivacyManager.termsAndPrivacySummary,
              style = MaterialTheme.typography.bodySmall.copy(
                color = DarkSurface,
                fontSize = 13.sp,
                lineHeight = 20.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checkbox Agreement
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainer)
            .clickable { isChecked = !isChecked }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("terms_agreement_checkbox_row")
        ) {
          Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            colors = CheckboxDefaults.colors(checkedColor = CourtGreen),
            modifier = Modifier.testTag("terms_agreement_checkbox")
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "I have read and agree to the Terms of Service & Privacy Policy.",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = DarkSurface,
              fontSize = 12.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Button(
          onClick = {
            TermsAndPrivacyManager.setAcceptedTerms(context, true)
            onAccept()
          },
          enabled = isChecked,
          colors = ButtonDefaults.buttonColors(
            containerColor = CourtGreen,
            disabledContainerColor = DarkSurfaceVariant.copy(alpha = 0.3f)
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("accept_terms_continue_btn")
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Accept & Continue to Ace AI",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )
        }
      }
    }
  }
}

@Composable
fun TermsAndPrivacyDetailSheet(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(SurfaceCanvas)
        .padding(16.dp),
      color = SurfaceCanvas
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Gavel,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Terms & Privacy Policy",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )
          }

          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SurfaceContainer)
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center
          ) {
            Text(text = "✕", fontWeight = FontWeight.Bold, color = DarkSurface)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = SurfaceContainerLowest,
          shadowElevation = 2.dp,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(18.dp)
          ) {
            Text(
              text = "Accepted: ${
                if (TermsAndPrivacyManager.hasAcceptedTerms(context)) "Yes" else "Pending"
              }",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CourtGreen
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = TermsAndPrivacyManager.termsAndPrivacySummary,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = DarkSurface,
                fontSize = 13.sp,
                lineHeight = 20.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Text("Close Terms", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
