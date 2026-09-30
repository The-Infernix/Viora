package com.example.viora.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VioraDarkPrimary,
    onPrimary = VioraDarkOnPrimary,
    primaryContainer = VioraDarkPrimaryContainer,
    onPrimaryContainer = VioraDarkOnPrimaryContainer,
    secondary = VioraSecondary,
    onSecondary = VioraOnSecondary,
    secondaryContainer = VioraDarkSurfaceVariant,
    onSecondaryContainer = VioraDarkOnSurface,
    tertiary = VioraDarkTertiary,
    tertiaryContainer = VioraDarkTertiaryContainer,
    onTertiaryContainer = VioraDarkOnTertiaryContainer,
    error = VioraError,
    onError = VioraOnError,
    errorContainer = VioraErrorContainer,
    onErrorContainer = VioraOnErrorContainer,
    background = VioraDarkBackground,
    onBackground = VioraDarkOnBackground,
    surface = VioraDarkSurface,
    onSurface = VioraDarkOnSurface,
    surfaceVariant = VioraDarkSurfaceVariant,
    onSurfaceVariant = VioraDarkOnSurfaceVariant,
    outline = VioraDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = VioraPrimary,
    onPrimary = VioraOnPrimary,
    primaryContainer = VioraPrimaryContainer,
    onPrimaryContainer = VioraOnPrimaryContainer,
    secondary = VioraSecondary,
    onSecondary = VioraOnSecondary,
    secondaryContainer = VioraSecondaryContainer,
    onSecondaryContainer = VioraOnSecondaryContainer,
    tertiary = VioraTertiary,
    tertiaryContainer = VioraTertiaryContainer,
    onTertiaryContainer = VioraOnTertiaryContainer,
    error = VioraError,
    onError = VioraOnError,
    errorContainer = VioraErrorContainer,
    onErrorContainer = VioraOnErrorContainer,
    background = VioraLightBackground,
    onBackground = VioraLightOnBackground,
    surface = VioraLightSurface,
    onSurface = VioraLightOnSurface,
    surfaceVariant = VioraLightSurfaceVariant,
    onSurfaceVariant = VioraLightOnSurfaceVariant,
    outline = VioraLightOutline
)

@Composable
fun VioraTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VioraTypography,
        shapes = VioraShapes,
        content = content
    )
}
