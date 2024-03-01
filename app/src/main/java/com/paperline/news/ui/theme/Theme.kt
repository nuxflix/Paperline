package com.paperline.news.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Rule,
    onPrimary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = PaperBright,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEAE1D6),
    onSurfaceVariant = InkMuted,
    outline = Line,
)

private val DarkColors = darkColorScheme(
    primary = NightRule,
    onPrimary = Night,
    background = Night,
    onBackground = NightInk,
    surface = NightRaised,
    onSurface = NightInk,
    surfaceVariant = Color(0xFF2A261F),
    onSurfaceVariant = NightMuted,
    outline = NightLine,
)

private val PaperlineShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
)

@Composable
fun PaperlineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PaperlineTypography,
        shapes = PaperlineShapes,
        content = content,
    )
}
