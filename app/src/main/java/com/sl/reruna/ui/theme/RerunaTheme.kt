package com.sl.reruna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RerunaBackground = Color(0xFF090B10)
val RerunaSurface = Color(0xFF111620)
val RerunaGrid = Color(0xFF273040)
val RerunaText = Color(0xFFF4F7FF)
val RerunaMuted = Color(0xFF8994A8)
val RerunaCyan = Color(0xFF59E8FF)
val RerunaViolet = Color(0xFF9A7CFF)
val RerunaDanger = Color(0xFFFF5C7A)

private val RerunaColors = darkColorScheme(
    primary = RerunaCyan,
    secondary = RerunaViolet,
    background = RerunaBackground,
    surface = RerunaSurface,
    onPrimary = RerunaBackground,
    onBackground = RerunaText,
    onSurface = RerunaText,
    error = RerunaDanger,
)

@Composable
fun RerunaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RerunaColors,
        content = content,
    )
}
