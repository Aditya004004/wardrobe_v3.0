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
    onSurface = GrayDark
)

private val DarkColors = darkColorScheme(
    primary = BlueLight,
    secondary = AccentYellow,
    background = GrayDark,
    surface = Color(0xFF303030),
    onPrimary = GrayDark,
    onSecondary = Color.Black,
    onBackground = GrayLight,
    onSurface = GrayLight
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
