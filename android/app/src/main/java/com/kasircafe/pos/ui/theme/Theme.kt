package com.kasircafe.pos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = CafeGreen,
    secondary = WarmOrange,
    surface = SoftCream
)

private val DarkColors = darkColorScheme(
    primary = CafeGreen,
    secondary = WarmOrange
)

@Composable
fun KasirCafeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
