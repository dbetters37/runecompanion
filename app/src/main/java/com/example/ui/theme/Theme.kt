package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Ancient Nature-y Spooky Shaman Palette
val OsrsLeatherDark = Color(0xFF09120D)   // Ancient Shadow Forest Canvas
val OsrsLeatherMedium = Color(0xFF14241B) // Mossy Crypt Stone Container
val OsrsParchment = Color(0xFFDCF0E6)    // Ancient Runic Parchment Light Mint Text
val OsrsParchmentLight = Color(0xFFE8F7F0)
val OsrsGold = Color(0xFF1E6B42)          // Mystic Elder Moss Border
val OsrsGoldBright = Color(0xFF00FF9D)    // Spooky Ghostly Emerald Glow
val OsrsRedFrame = Color(0xFF1A4D36)      // Ancient Shaman Pine Frame
val OsrsTextYellow = Color(0xFF80FFE8)    // Spooky Spectral Cyan Highlight
val OsrsTextWhite = Color(0xFFF0FDF8)
val OsrsTextDark = Color(0xFF050B08)
val OsrsTextGreen = Color(0xFF00FF9D)    // Glowing Ghostly Spirit Emerald
val OsrsTextOrange = Color(0xFFFF9E3D)   // Spooky Soul Flame Amber

private val OsrsColorScheme = darkColorScheme(
    primary = OsrsGoldBright,
    onPrimary = OsrsTextDark,
    primaryContainer = OsrsLeatherMedium,
    onPrimaryContainer = OsrsParchment,
    secondary = OsrsRedFrame,
    onSecondary = OsrsTextWhite,
    background = OsrsLeatherDark,
    onBackground = OsrsTextWhite,
    surface = OsrsLeatherMedium,
    onSurface = OsrsParchment,
    surfaceVariant = Color(0xFF1D3326),
    onSurfaceVariant = OsrsParchmentLight
)

@Composable
fun OsrsPetTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = OsrsLeatherDark.toArgb()
            window.navigationBarColor = OsrsLeatherDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = OsrsColorScheme,
        typography = Typography,
        content = content
    )
}
