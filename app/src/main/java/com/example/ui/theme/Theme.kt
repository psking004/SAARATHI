package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SaarathiNavy,
    onPrimary = Color.White,
    primaryContainer = SaarathiNavyLight,
    onPrimaryContainer = Color.White,
    secondary = SaarathiTeal,
    onSecondary = Color.White,
    secondaryContainer = SaarathiTealLight,
    onSecondaryContainer = SaarathiTealDark,
    tertiary = SaarathiBlue,
    onTertiary = Color.White,
    background = SaarathiBackground,
    onBackground = TextPrimary,
    surface = SaarathiSurface,
    onSurface = TextPrimary,
    surfaceVariant = SaarathiSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = EmergencyRed,
    onError = Color.White,
    errorContainer = EmergencyRedContainer,
    onErrorContainer = EmergencyRedDark,
    outline = BorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = SaarathiTeal,
    onPrimary = SaarathiNavyDark,
    primaryContainer = SaarathiNavy,
    onPrimaryContainer = Color.White,
    secondary = SaarathiBlue,
    onSecondary = Color.White,
    secondaryContainer = SaarathiNavyLight,
    onSecondaryContainer = Color.White,
    tertiary = SaarathiTealLight,
    onTertiary = SaarathiNavyDark,
    background = SaarathiNavyDark,
    onBackground = Color(0xFFF1F5F9),
    surface = SaarathiNavy,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SaarathiNavyLight,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = EmergencyRed,
    onError = Color.White,
    errorContainer = EmergencyRedDark,
    onErrorContainer = Color(0xFFFFCDD2),
    outline = Color(0xFF334E68)
)

@Composable
fun SaarathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility wrapper
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SaarathiTheme(darkTheme = darkTheme, content = content)
}
