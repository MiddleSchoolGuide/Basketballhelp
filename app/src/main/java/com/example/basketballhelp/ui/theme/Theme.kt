package com.example.basketballhelp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HoopDevColors = darkColorScheme(
    primary = Orange500,
    secondary = Sky400,
    tertiary = Green400,
    background = Navy950,
    surface = Navy900,
    surfaceVariant = Navy800,
    onPrimary = Navy950,
    onSecondary = Navy950,
    onTertiary = Navy950,
    onBackground = Slate100,
    onSurface = Slate100,
    onSurfaceVariant = Slate300,
)

@Composable
fun HoopDevTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HoopDevColors,
        typography = Typography,
        content = content,
    )
}
