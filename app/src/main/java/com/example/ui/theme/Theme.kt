package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberColorScheme = darkColorScheme(
    primary = ResistanceGreen,
    onPrimary = SyndicateDark,
    primaryContainer = ResistanceGreenDim,
    onPrimaryContainer = ResistanceGreen,
    secondary = InfoCyan,
    onSecondary = SyndicateDark,
    secondaryContainer = InfoCyanDim,
    onSecondaryContainer = InfoCyan,
    tertiary = WarningGold,
    onTertiary = SyndicateDark,
    background = SyndicateDark,
    onBackground = ConsoleText,
    surface = ConsoleSurface,
    onSurface = ConsoleText,
    error = AlertRed,
    onError = SyndicateDark,
    errorContainer = AlertRedDim,
    onErrorContainer = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force cyberpunk dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
