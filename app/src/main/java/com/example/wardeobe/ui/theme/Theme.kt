package com.example.wardeobe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = AccentPink,
    background = GrayLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = GrayDark,
    onSurface = GrayDark,
    primaryContainer = BlueLight,
    onPrimaryContainer = BlueDark,
    secondaryContainer = Color(0xFFFFCDD2),
    onSecondaryContainer = Color(0xFFC62828),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = GrayDark,
    surfaceContainer = GrayLight,
    surfaceContainerHigh = Color.White
)

private val DarkColors = darkColorScheme(
    primary = BlueLight,
    secondary = AccentYellow,
    background = GrayDark,
    surface = Color(0xFF303030),
    onPrimary = GrayDark,
    onSecondary = Color.Black,
    onBackground = GrayLight,
    onSurface = GrayLight,
    primaryContainer = BlueDark,
    onPrimaryContainer = BlueLight,
    secondaryContainer = Color(0xFFC62828),
    onSecondaryContainer = Color(0xFFFFCDD2),
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = GrayLight,
    surfaceContainer = GrayDark,
    surfaceContainerHigh = Color(0xFF303030)
)

@Composable
fun WardrobeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
