package com.aima.habitual.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light Mode Configuration:
 * Focuses on a clean, nature-inspired "Forest Green" and "Soft Sage" palette.
 */
private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = White,

    // Core Layout colors
    background = White,
    surface = White,
    onBackground = ForestGreen,
    onSurface = ForestGreen,

    // Component/Card colors
    surfaceVariant = SoftSage,
    onSurfaceVariant = Color(0xFF1B5E20),

    secondary = SoftSage,
    onSecondary = ForestGreen
)

/**
 * Dark Mode Configuration:
 * Swaps to high-contrast "LimeSage" for primary actions and deep greys for eye comfort.
 */
private val DarkColorScheme = darkColorScheme(
    primary = LimeSageAccent,
    onPrimary = Color(0xFF1B5E20),
    secondary = DarkCardGrey,

    background = DarkBackground,
    surface = DarkBackground,
    onSurface = White,

    surfaceVariant = DarkCardGrey,
    onSurfaceVariant = DarkTextSecondary
)

/**
 * HabitualTheme: The root styling engine for the application.
 * Manages Dynamic Colors, Typography, and System UI (Status/Navigation Bars).
 */
@Composable
fun HabitualTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    // System UI logic (Status Bar and Navigation Bar colors)
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Adjusts status bar icons: Dark icons on Light backgrounds, Light icons on Dark backgrounds
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}