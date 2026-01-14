package com.aima.habitual.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light Color Scheme optimized for a calm habit tracking experience.
 */
private val LightColorScheme = lightColorScheme(
    primary = DeepTeal,               // Calm, professional buttons
    onPrimary = Color.White,          // White text on deep teal
    secondary = SageGreen,            // Accent color
    surfaceVariant = SoftSage,        // Calm habit card color
    onSurfaceVariant = DeepTeal,      // High contrast text on cards
    background = Color(0xFFFDFDFD),   // Off-white background to reduce glare
    onBackground = Color(0xFF1A1C1E)  // Soft black text
)

/**
 * Dark Color Scheme designed to reduce eye strain in low-light environments.
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkSage,               // Soft green buttons
    onPrimary = Color(0xFF00390A),    // Dark text on light green buttons
    secondary = MutedSlate,
    surfaceVariant = DarkCard,        // Dark forest-toned cards
    onSurfaceVariant = Color.White,   // Clear white text on dark cards
    background = DarkBackground,      // Near black for less strain
    onBackground = DarkText           // Off-white text to prevent "haloing"
)

/**
 * HabitualTheme manages the global look and feel, including system UI colors.
 */
@Composable
fun HabitualTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    // Modern System UI Management Logic
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Note: Strikethrough is expected in modern Android Studio.
            // This is the correct way to apply custom solid colors to bars.
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)

            // High Mark Requirement: Accessibility & Contrast
            // We want white icons (isAppearanceLightStatusBars = false)
            // because our status bar is a dark Deep Teal.
            insetsController.isAppearanceLightStatusBars = false

            // Navigation bar matches the app background.
            // In Light Mode, we need Dark icons (true).
            // In Dark Mode, we need Light icons (false).
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Defined in your Type.kt file
        content = content
    )
}