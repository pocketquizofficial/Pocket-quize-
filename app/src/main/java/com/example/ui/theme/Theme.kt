package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpleAccent,
    secondary = PurpleSecondary,
    tertiary = PurpleLight,
    background = Color(0xFF1E1B29),
    surface = Color(0xFF2A2438),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFF3E8FF),
    onSurface = Color(0xFFF3E8FF)
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = PurpleSecondary,
    tertiary = PurpleAccent,
    background = SoftBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1F1235),
    onSurface = Color(0xFF1F1235),
    surfaceVariant = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFF4C1D95)
)

@Composable
fun PocketQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Force specific requested colors (White & Purple premium theme)
    // while maintaining appropriate dark colors if system is dark
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
