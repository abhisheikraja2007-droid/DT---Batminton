package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AuthState
import com.example.model.AuthUser
import com.example.model.CloudSyncStatus
import com.example.model.FirebaseAuthService
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch

/**
 * CloudSyncBanner displays real-time Firebase Authentication & Sync state across athlete devices.
 */
@Composable
fun CloudSyncBanner(
  authState: AuthState,
  syncStatus: CloudSyncStatus,
  onOpenAuthDialog: () -> Unit,
  onTriggerSync: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("cloud_sync_banner")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (syncStatus.isCloudConnected) CourtGreenContainer else SurfaceContainer),
          contentAlignment = Alignment.Center
        ) {
          if (syncStatus.isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.5.dp,
              color = CourtGreen
            )
          } else {
            Icon(
              imageVector = if (syncStatus.isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
              contentDescription = "Cloud Status",
              tint = if (syncStatus.isCloudConnected) CourtGreen else DarkSurfaceVariant,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (syncStatus.isCloudConnected) "Firebase Sync Active" else "Offline Training Mode",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface,
                fontSize = 13.sp
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (syncStatus.isCloudConnected) CourtGreen else ShuttleOrange)
            )
          }

          Text(
            text = when (authState) {
              is AuthState.Authenticated -> "Signed in as ${authState.user.email ?: authState.user.displayName} • ${syncStatus.lastSyncedTime}"
              is AuthState.Loading -> "Connecting to Firebase..."
              else -> "Sign in to sync stroke telemetry across devices"
            },
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            ),
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (authState is AuthState.Authenticated) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(SurfaceContainer)
              .clickable { onTriggerSync() }
              .padding(horizontal = 10.dp, vertical = 7.dp)
              .testTag("sync_now_btn"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = CourtGreen,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Sync",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = CourtGreen,
                  fontSize = 11.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (authState is AuthState.Authenticated) SurfaceContainerHigh else CourtGreen)
            .clickable { onOpenAuthDialog() }
            .padding(horizontal = 11.dp, vertical = 7.dp)
            .testTag("auth_action_btn"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (authState is AuthState.Authenticated) "Account" else "Sign In",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (authState is AuthState.Authenticated) DarkSurface else Color.White,
              fontSize = 11.sp
            )
          )
        }
      }
    }
  }
}

/**
 * FirebaseAuthDialog provides a clean Material 3 modal for signing in, registering, or managing account sync.
 */
@Composable
fun FirebaseAuthDialog(
  authService: FirebaseAuthService,
  authState: AuthState,
  onDismiss: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var isRegisterMode by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var displayName by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isProcessing by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
        .testTag("firebase_auth_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (authState is AuthState.Authenticated) {
          // Account Management View
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(CourtGreenContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Cloud Sync Connected",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DarkSurface
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Your 24 sessions and stroke telemetry are securely backed up with Firebase.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Text(
                text = "ATHLETE ACCOUNT",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurfaceVariant,
                  fontSize = 10.sp
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = authState.user.displayName ?: "Athlete",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
              Text(
                text = authState.user.email ?: "No Email Registered",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = CourtGreen,
                  fontWeight = FontWeight.Medium
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              authService.triggerCloudSync()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("dialog_sync_now_btn")
          ) {
            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sync Telemetry Now",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = {
              authService.signOut()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("dialog_sign_out_btn")
          ) {
            Text(
              text = "Sign Out",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ErrorRed
              )
            )
          }
        } else {
          // Sign In / Register Form View
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(CourtGreenContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.VpnKey,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = if (isRegisterMode) "Create AceAI Cloud Account" else "Sign In to AceAI Cloud",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DarkSurface,
              fontSize = 18.sp
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Keep your smash velocities, form progression, and court calibration in sync across all phones & tablets.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = DarkSurfaceVariant,
              fontSize = 11.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          if (isRegisterMode) {
            OutlinedTextField(
              value = displayName,
              onValueChange = { displayName = it },
              label = { Text("Athlete Full Name") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CourtGreen) },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_input_name")
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CourtGreen) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_input_email")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CourtGreen) },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = null
                )
              }
            },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_input_password")
          )

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = errorMessage!!,
              style = MaterialTheme.typography.bodySmall.copy(
                color = ErrorRed,
                fontWeight = FontWeight.Medium
              )
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Button(
            onClick = {
              if (email.isBlank() || password.isBlank()) {
                errorMessage = "Please enter both email and password."
                return@Button
              }
              isProcessing = true
              errorMessage = null
              scope.launch {
                if (isRegisterMode) {
                  val result = authService.signUpWithEmail(email, password, displayName)
                  if (result.isSuccess) {
                    onDismiss()
                  } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed."
                  }
                } else {
                  val result = authService.signInWithEmail(email, password)
                  if (result.isSuccess) {
                    onDismiss()
                  } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Sign in failed."
                  }
                }
                isProcessing = false
              }
            },
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("auth_submit_btn")
          ) {
            if (isProcessing) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
              )
            } else {
              Text(
                text = if (isRegisterMode) "Create Account & Sync" else "Sign In & Sync Telemetry",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceContainerHigh)
            Text(
              text = "  OR  ",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurfaceVariant,
                fontSize = 11.sp
              )
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceContainerHigh)
          }

          Button(
            onClick = {
              isProcessing = true
              scope.launch {
                authService.signInWithGoogle(if (email.isBlank()) "abhisheikraja2007@gmail.com" else email)
                isProcessing = false
                onDismiss()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("auth_google_btn")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF4285F4)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "G",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 13.sp
                  )
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Sign in with Google",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DarkSurface
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Switch between Sign In and Register
          TextButton(
            onClick = {
              isRegisterMode = !isRegisterMode
              errorMessage = null
            },
            modifier = Modifier.testTag("auth_toggle_mode_btn")
          ) {
            Text(
              text = if (isRegisterMode) "Already have an account? Sign In" else "New athlete? Create an account",
              style = MaterialTheme.typography.labelMedium.copy(
                color = CourtGreen,
                fontWeight = FontWeight.Bold
              )
            )
          }

          // Guest / Anonymous Sign In Option
          TextButton(
            onClick = {
              isProcessing = true
              scope.launch {
                authService.signInAnonymously()
                isProcessing = false
                onDismiss()
              }
            },
            modifier = Modifier.testTag("auth_guest_btn")
          ) {
            Text(
              text = "Continue as Guest Athlete",
              style = MaterialTheme.typography.labelSmall.copy(
                color = DarkSurfaceVariant
              )
            )
          }
        }
      }
    }
  }
}
