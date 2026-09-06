package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = RosePrimary,
  onPrimary = Color.White,
  primaryContainer = RoseDeep,
  onPrimaryContainer = RoseContainer,
  secondary = RoseDark,
  onSecondary = Color.White,
  background = Color(0xFF0F172A),
  surface = Color(0xFF1E293B),
  onBackground = Color(0xFFF1F5F9),
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
  primary = RosePrimary,
  onPrimary = Color.White,
  primaryContainer = RoseContainer,
  onPrimaryContainer = RoseDeep,
  secondary = RoseDark,
  onSecondary = Color.White,
  background = Slate50,
  surface = PureWhite,
  onBackground = Slate900,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Slate200
)

@Composable
fun GolapiNewsTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

