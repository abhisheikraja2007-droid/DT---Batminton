package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = CourtGreen,
  onPrimary = Color.White,
  primaryContainer = CourtGreenContainer,
  onPrimaryContainer = OnCourtGreenContainer,
  inversePrimary = CourtGreenFixedDim,
  secondary = ShuttleOrange,
  onSecondary = Color.White,
  secondaryContainer = ShuttleOrangeContainer,
  onSecondaryContainer = Color.White,
  tertiary = AmberWarning,
  onTertiary = Color.White,
  tertiaryContainer = AmberWarningContainer,
  onTertiaryContainer = Color.White,
  background = SurfaceCanvas,
  onBackground = DarkSurface,
  surface = SurfaceCanvas,
  onSurface = DarkSurface,
  surfaceVariant = SurfaceContainerHighest,
  onSurfaceVariant = DarkSurfaceVariant,
  inverseSurface = InverseSurface,
  inverseOnSurface = InverseOnSurface,
  outline = OutlineColor,
  outlineVariant = OutlineVariant,
  error = ErrorRed,
  onError = Color.White,
  errorContainer = ErrorContainer,
  onErrorContainer = OnErrorContainer
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
