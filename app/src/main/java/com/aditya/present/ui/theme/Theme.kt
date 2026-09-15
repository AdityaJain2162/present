package com.aditya.present.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

@Suppress("unused")
private fun buildLightScheme(p: AccentPreset) = lightColorScheme(
    primary = p.primary,
    onPrimary = Color_OnPrimary,
    primaryContainer = p.primaryContainer,
    onPrimaryContainer = Color_OnPrimaryContainer,
    secondary = p.secondary,
    onSecondary = Color_OnSecondary,
    secondaryContainer = p.secondaryContainer,
    onSecondaryContainer = Color_OnSecondaryContainer,
    tertiary = p.tertiary,
    onTertiary = Color_OnTertiary,
    tertiaryContainer = p.tertiaryContainer,
    onTertiaryContainer = Color_OnTertiaryContainer,
    error = Color_Error,
    onError = Color_OnError,
    errorContainer = Color_ErrorContainer,
    onErrorContainer = Color_OnErrorContainer,
    background = Color_Background,
    onBackground = Color_OnBackground,
    surface = Color_Surface,
    onSurface = Color_OnSurface,
    surfaceVariant = Color_SurfaceVariant,
    onSurfaceVariant = Color_OnSurfaceVariant,
    surfaceContainer = Color_SurfaceContainer,
)

@Suppress("unused")
private fun buildDarkScheme(p: AccentPreset) = darkColorScheme(
    primary = p.primary.copy(alpha = 0.85f).compositeOver(Color(0xFFE0E0E0)),
    onPrimary = Color_DarkOnPrimary,
    primaryContainer = p.primary.copy(alpha = 0.3f),
    onPrimaryContainer = p.primaryContainer,
    secondary = p.secondary.copy(alpha = 0.8f),
    onSecondary = Color_DarkOnSecondary,
    secondaryContainer = p.secondary.copy(alpha = 0.25f),
    onSecondaryContainer = p.secondaryContainer,
    tertiary = p.tertiary.copy(alpha = 0.8f),
    onTertiary = Color_DarkOnTertiary,
    tertiaryContainer = p.tertiary.copy(alpha = 0.25f),
    onTertiaryContainer = p.tertiaryContainer,
    error = Color_DarkError,
    onError = Color_DarkOnError,
    errorContainer = Color_DarkErrorContainer,
    onErrorContainer = Color_DarkOnErrorContainer,
    background = Color_DarkBackground,
    onBackground = Color_DarkOnBackground,
    surface = Color_DarkSurface,
    onSurface = Color_DarkOnSurface,
    surfaceVariant = Color_DarkSurfaceVariant,
    onSurfaceVariant = Color_DarkOnSurfaceVariant,
    surfaceContainer = Color_DarkSurfaceContainer,
)

private val AmoledOverride = darkColorScheme(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceVariant = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF111111),
)

val LocalAccentPreset = compositionLocalOf { AccentPresets[0] }
val LocalAnimationsEnabled = compositionLocalOf { true }

@Composable
fun PresentTheme(
    themeMode: com.aditya.present.domain.ThemeMode = com.aditya.present.domain.ThemeMode.SYSTEM,
    accentName: String = "Teal",
    dynamicColor: Boolean = true,
    animationsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val preset = accentPresetByName(accentName)

    val colorScheme = when (themeMode) {
        com.aditya.present.domain.ThemeMode.SYSTEM -> {
            val isDark = isSystemInDarkTheme()
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) buildDarkScheme(preset) else buildLightScheme(preset)
            }
        }
        com.aditya.present.domain.ThemeMode.LIGHT -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                dynamicLightColorScheme(context) else buildLightScheme(preset)
        }
        com.aditya.present.domain.ThemeMode.DARK -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                dynamicDarkColorScheme(context) else buildDarkScheme(preset)
        }
        com.aditya.present.domain.ThemeMode.AMOLED -> {
            val base = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                dynamicDarkColorScheme(context) else buildDarkScheme(preset)
            base.copy(
                background = Color(0xFF000000),
                surface = Color(0xFF000000),
                surfaceVariant = Color(0xFF0A0A0A),
                surfaceContainer = Color(0xFF111111),
            )
        }
    }

    CompositionLocalProvider(
        LocalAccentPreset provides preset,
        LocalAnimationsEnabled provides animationsEnabled,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PresentTypography,
            content = content,
        )
    }
}

// Composite helper (simplified)
private fun Color.compositeOver(background: Color): Color {
    val a = alpha + background.alpha * (1f - alpha)
    val r = (red * alpha + background.red * background.alpha * (1f - alpha)) / a
    val g = (green * alpha + background.green * background.alpha * (1f - alpha)) / a
    val b = (blue * alpha + background.blue * background.alpha * (1f - alpha)) / a
    return Color(r, g, b, a)
}
