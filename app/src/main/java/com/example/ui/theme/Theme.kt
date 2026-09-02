package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkForestColorScheme =
  darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = DarkForestBg,
    primaryContainer = ForestGreenAccent,
    onPrimaryContainer = PaleEmerald,
    secondary = LightEmerald,
    onSecondary = DarkForestBg,
    secondaryContainer = ForestCardBg,
    onSecondaryContainer = LightEmerald,
    tertiary = MintCyan,
    onTertiary = DarkForestBg,
    tertiaryContainer = ForestSurfaceVariant,
    onTertiaryContainer = MintCyan,
    background = DarkForestBg,
    onBackground = Color.White,
    surface = ForestSurface,
    onSurface = Color.White,
    surfaceVariant = ForestCardBg,
    onSurfaceVariant = PaleEmerald,
    outline = ForestBorder,
    outlineVariant = ForestGreenAccent
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkForestColorScheme,
    typography = Typography,
    content = content
  )
}
