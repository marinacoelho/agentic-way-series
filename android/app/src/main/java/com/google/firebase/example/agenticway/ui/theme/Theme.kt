package com.google.firebase.example.agenticway.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = TranslateButtonBackground,
    onPrimary = TranslateButtonContent,
    background = IdiomScreenBackground,
    onBackground = TextPrimary,
    surface = InputCardBackground,
    onSurface = TextPrimary
)

@Composable
fun AgenticWayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}