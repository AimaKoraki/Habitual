package com.aima.habitual.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightAccentPrimary,
    onPrimary = LightSurface,
    primaryContainer = LightAccentSoft,
    onPrimaryContainer = LightAccentPrimary,

    background = LightBg,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceSubtle,
    onSurfaceVariant = LightTextSecondary,

    outline = LightBorderStrong,
    outlineVariant = LightBorderSubtle,

    error = DangerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentPrimary,
    onPrimary = DarkBg,
    primaryContainer = DarkAccentSoft,
    onPrimaryContainer = DarkAccentPrimary,

    background = DarkBg,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkBorderStrong,
    outlineVariant = DarkBorderSubtle,

    error = DangerRed
)

@Composable
fun HabitualTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // NEW: Provide our custom design tokens to the app
    CompositionLocalProvider(
        LocalSpacing provides HabitualSpacing(),
        LocalRadius provides HabitualRadius(),
        LocalComponents provides HabitualComponents(),
        LocalAlpha provides HabitualAlpha()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// NEW: Create a custom object to easily access these tokens anywhere!
object HabitualTheme {
    val spacing: HabitualSpacing
        @Composable
        get() = LocalSpacing.current
    
    val radius: HabitualRadius
        @Composable
        get() = LocalRadius.current
        
    val components: HabitualComponents
        @Composable
        get() = LocalComponents.current

    val alpha: HabitualAlpha
        @Composable
        get() = LocalAlpha.current
}