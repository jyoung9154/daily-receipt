package com.dailyreceipt.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AccentBrown,
    onPrimary = Color.White,
    surface = CreamBackground,
    onSurface = TextPrimary,
    background = CreamBackground,
    onBackground = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary
)

@Composable
fun DailyReceiptTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColorScheme, content = content)
}
