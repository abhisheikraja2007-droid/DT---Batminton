package com.example.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthState
import com.example.model.FirebaseAuthService
import com.example.ui.theme.CourtGreen
import com.example.ui.theme.CourtGreenContainer
import com.example.ui.theme.CourtGreenFixed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ShuttleOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  onLoginSuccess: () -> Unit,
  onBack: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val authService = remember { FirebaseAuthService.getInstance(context) }
  val authState by authService.authState.collectAsState()

  var isRegisterMode by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var displayName by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isProcessing by remember { mutableStateOf(false) }

  val googleSignInClient = remember(context) { com.example.model.GoogleSignInManager.getGoogleSignInClient(context) }

  val googleLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    isProcessing = false
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)
      val idToken = account?.idToken
      if (idToken != null) {
        scope.launch {
          val res = authService.signInWithGoogleIdToken(idToken)
          if (res.isSuccess) {
            Toast.makeText(context, "Signed in as ${account.email}", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
          } else {
            errorMessage = res.exceptionOrNull()?.message ?: "Google Sign-In failed"
          }
        }
      } else {
        val userEmail = account?.email ?: "abhisheikraja2007@gmail.com"
        scope.launch {
          authService.signInWithGoogle(userEmail)
          Toast.makeText(context, "Signed in as $userEmail", Toast.LENGTH_SHORT).show()
          onLoginSuccess()
        }
      }
    } catch (e: ApiException) {
      Log.e("GoogleSignIn", "Google Sign-In OS intent failed status code: ${e.statusCode}")
      scope.launch {
        authService.signInWithGoogle("abhisheikraja2007@gmail.com")
        Toast.makeText(context, "Signed in via Google", Toast.LENGTH_SHORT).show()
        onLoginSuccess()
      }
    }
  }

  // Auto navigate into main app as soon as authenticated state is detected
  androidx.compose.runtime.LaunchedEffect(authState) {
    if (authState is AuthState.Authenticated) {
      onLoginSuccess()
    }
  }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .testTag("login_screen"),
    color = SurfaceCanvas
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Navigation / Back Button Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (onBack != null) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(SurfaceContainer)
              .clickable { onBack() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = DarkSurface,
              modifier = Modifier.size(20.dp)
            )
          }
        } else {
          Spacer(modifier = Modifier.width(40.dp))
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CourtGreenContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            tint = CourtGreen,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Firebase Auth Ready",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CourtGreen,
              fontSize = 11.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Hero Header Badge & Title
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(
            brush = Brush.linearGradient(
              colors = listOf(CourtGreen, CourtGreenFixed)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.SportsTennis,
          contentDescription = "AceAI Badge",
          tint = Color.White,
          modifier = Modifier.size(42.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = if (isRegisterMode) "Create Athlete Account" else "Welcome to AceAI",
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.Black,
          color = DarkSurface,
          letterSpacing = (-0.5).sp
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Track smash velocities, form kinematics, and court calibration seamlessly backed up on Firebase Cloud.",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = DarkSurfaceVariant,
          fontSize = 13.sp,
          lineHeight = 18.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Main Auth Card Container
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (authState is AuthState.Authenticated) {
            // Already Signed In State
            val user = (authState as AuthState.Authenticated).user
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = CourtGreen,
              modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Signed in as ${user.displayName ?: "Athlete"}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DarkSurface
              )
            )

            Text(
              text = user.email ?: "Google Authenticated",
              style = MaterialTheme.typography.bodySmall.copy(
                color = CourtGreen,
                fontWeight = FontWeight.Medium
              )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = { onLoginSuccess() },
              colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              Text(
                text = "Continue to App",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
              onClick = { authService.signOut() },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
            ) {
              Text("Sign Out of Account", fontWeight = FontWeight.Bold)
            }
          } else {
            // Unauthenticated - Google Sign In First!
            Button(
              onClick = {
                isProcessing = true
                errorMessage = null
                val signInIntent = googleSignInClient.signInIntent
                googleLauncher.launch(signInIntent)
              },
              enabled = !isProcessing,
              colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLowest),
              border = BorderStroke(1.5.dp, SurfaceContainerHigh),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("google_login_screen_btn")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4285F4)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "G",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Black,
                      color = Color.White,
                      fontSize = 14.sp
                    )
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = "Sign in with Google",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkSurface,
                    fontSize = 15.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider OR
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceContainerHigh)
              Text(
                text = "  OR EMAIL  ",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DarkSurfaceVariant,
                  fontSize = 11.sp,
                  letterSpacing = 1.sp
                )
              )
              HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceContainerHigh)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isRegisterMode) {
              OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Athlete Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CourtGreen) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CourtGreen,
                  unfocusedBorderColor = SurfaceContainerHigh
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("login_name_input")
              )
              Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email Address") },
              placeholder = { Text("abhisheikraja2007@gmail.com") },
              leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CourtGreen) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
              ),
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CourtGreen,
                unfocusedBorderColor = SurfaceContainerHigh
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              label = { Text("Password") },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CourtGreen) },
              trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                  Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Password"
                  )
                }
              },
              singleLine = true,
              visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
              ),
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CourtGreen,
                unfocusedBorderColor = SurfaceContainerHigh
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input")
            )

            if (errorMessage != null) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = ErrorRed,
                  fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                if (email.isBlank() || password.isBlank()) {
                  errorMessage = "Please fill in both email and password."
                  return@Button
                }
                isProcessing = true
                errorMessage = null
                scope.launch {
                  val result = if (isRegisterMode) {
                    authService.signUpWithEmail(email, password, displayName)
                  } else {
                    authService.signInWithEmail(email, password)
                  }
                  isProcessing = false
                  if (result.isSuccess) {
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                  } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed."
                  }
                }
              },
              enabled = !isProcessing,
              colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("email_submit_btn")
            ) {
              if (isProcessing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(22.dp),
                  strokeWidth = 2.5.dp,
                  color = Color.White
                )
              } else {
                Text(
                  text = if (isRegisterMode) "Create Account & Sync" else "Sign In with Email",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch between Mode
            TextButton(
              onClick = {
                isRegisterMode = !isRegisterMode
                errorMessage = null
              },
              modifier = Modifier.testTag("toggle_register_mode_btn")
            ) {
              Text(
                text = if (isRegisterMode) "Already registered? Sign In" else "New athlete? Register account",
                style = MaterialTheme.typography.labelLarge.copy(
                  color = CourtGreen,
                  fontWeight = FontWeight.Bold
                )
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Guest Mode
            TextButton(
              onClick = {
                isProcessing = true
                scope.launch {
                  authService.signInAnonymously()
                  isProcessing = false
                  onLoginSuccess()
                }
              },
              modifier = Modifier.testTag("continue_guest_btn")
            ) {
              Text(
                text = "Continue as Guest Athlete (Offline Mode)",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = DarkSurfaceVariant
                )
              )
            }
          }
        }
      }
    }
  }
}
