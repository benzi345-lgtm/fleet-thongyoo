package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FleetColorScheme =
  lightColorScheme(
    primary = NavyPrimary,
    onPrimary = NavyOnPrimary,
    primaryContainer = NavyContainer,
    onPrimaryContainer = NavyOnContainer,
    secondary = AmberAccent,
    onSecondary = AmberOnAccent,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberOnContainer,
    tertiary = StatusInfoBlue,
    onTertiary = NavyOnPrimary,
    tertiaryContainer = StatusInfoContainer,
    onTertiaryContainer = StatusInfoOnContainer,
    background = BackgroundLight,
    onBackground = DarkGrayText,
    surface = SurfaceLight,
    onSurface = DarkGrayText,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = DarkGrayText,
    error = StatusIssueRed,
    onError = NavyOnPrimary,
    errorContainer = StatusIssueContainer,
    onErrorContainer = StatusIssueOnContainer,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent high contrast fleet branding
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = FleetColorScheme,
    typography = Typography,
    content = content,
  )
}

