package com.aima.habitual.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── LIGHT COLOR SCHEME ──────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = LightAccentPrimary,
    onPrimary = Color.White,
    primaryContainer = LightAccentSoft,
    onPrimaryContainer = LightAccentPrimary,

    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    background = LightBg,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceSubtle,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = LightSurfaceTint,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerHigh = LightSurfaceContainerHigh,

    outline = LightBorderStrong,
    outlineVariant = LightBorderSubtle,


    error = DangerRed,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

// ─── DARK COLOR SCHEME ───────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentPrimary,
    onPrimary = Color(0xFF0F1110),
    primaryContainer = DarkAccentSoft,
    onPrimaryContainer = DarkTextPrimary,

    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    background = DarkBg,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = DarkSurfaceTint,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerHigh = DarkSurfaceContainerHigh,

    outline = DarkBorderStrong,
    outlineVariant = DarkBorderSubtle,

    error = DangerRed,
    onError = Color.White,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

// ─── THEME COMPOSABLE ────────────────────────────────────────────────────────
@Composable
fun HabitualTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // Safe cast — prevents crash if context isn't Activity (e.g. in previews)
            (view.context as? Activity)?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Stable token instances — not recreated on recomposition
    val spacing = remember { HabitualSpacing() }
    val radius = remember { HabitualRadius() }
    val elevation = remember { HabitualElevation() }
    val alpha = remember { HabitualAlpha() }
    val components = remember { HabitualComponents() }

    // Inject radius into MaterialTheme Shapes so all M3 components inherit our design language
    val shapes = remember(radius) {
        Shapes(
            extraSmall = RoundedCornerShape(radius.xs),   // 4dp — indicators
            small = RoundedCornerShape(radius.sm),         // 8dp — chips, tags
            medium = RoundedCornerShape(radius.md),        // 12dp — buttons, menus
            large = RoundedCornerShape(radius.lg),         // 16dp — cards
            extraLarge = RoundedCornerShape(radius.xxl)    // 24dp — dialogs, FAB
        )
    }

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalRadius provides radius,
        LocalElevation provides elevation,
        LocalAlpha provides alpha,
        LocalComponents provides components
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = shapes,
            content = content
        )
    }
}

// ─── THEME ACCESSOR ──────────────────────────────────────────────────────────
object HabitualTheme {
    val spacing: HabitualSpacing
        @Composable get() = LocalSpacing.current

    val radius: HabitualRadius
        @Composable get() = LocalRadius.current

    val elevation: HabitualElevation
        @Composable get() = LocalElevation.current

    val alpha: HabitualAlpha
        @Composable get() = LocalAlpha.current

    val components: HabitualComponents
        @Composable get() = LocalComponents.current
}