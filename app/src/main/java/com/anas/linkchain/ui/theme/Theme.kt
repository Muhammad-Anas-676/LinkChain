package com.anas.linkchain.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = DarkBg,
    onBackground = DarkText,
    onSurface = DarkText
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    background = LightBg,
    surface = LightSurface,
    onPrimary = LightSurface,
    onBackground = LightText,
    onSurface = LightText
)

@Composable
fun LinkChainTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}