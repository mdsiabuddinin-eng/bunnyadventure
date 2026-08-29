package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DeepOrange,
    onPrimary = Color.White,
    primaryContainer = SunnyOrange,
    onPrimaryContainer = Color.White,
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = CloudBlue,
    onSecondaryContainer = DeepSkyBlue,
    tertiary = GrassGreen,
    onTertiary = Color.White,
    background = OffWhite,
    onBackground = DarkText,
    surface = CardBackground,
    onSurface = DarkText,
    surfaceVariant = CloudBlue,
    onSurfaceVariant = MutedText
)

private val DarkColors = darkColorScheme(
    primary = SunnyOrange,
    onPrimary = DarkText,
    primaryContainer = DeepOrange,
    onPrimaryContainer = Color.White,
    secondary = SkyBlue,
    onSecondary = DarkText,
    tertiary = MintGreen,
    onTertiary = DarkText,
    background = Color(0xFF1A1F2C),
    onBackground = OffWhite,
    surface = Color(0xFF262D3D),
    onSurface = OffWhite
)

@Composable
fun BunnyAdventureTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
