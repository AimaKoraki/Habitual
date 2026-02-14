package com.aima.habitual.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── SPACING ─────────────────────────────────────────────────────────────────
// Consistent 4-point grid. Used for padding, margins, gaps.
data class HabitualSpacing(
    val xxs: Dp = 4.dp,       // Tight internal gaps
    val xs: Dp = 8.dp,        // Small gaps, chip padding
    val sm: Dp = 12.dp,       // Inter-element spacing
    val md: Dp = 16.dp,       // Standard padding
    val lg: Dp = 20.dp,       // Card padding, section gaps
    val xl: Dp = 24.dp,       // Generous padding, screen horizontal
    val section: Dp = 32.dp   // Between major sections
)

// ─── RADIUS ──────────────────────────────────────────────────────────────────
// Named scale for corner rounding.
data class HabitualRadius(
    val xs: Dp = 4.dp,        // Progress bars, indicators
    val sm: Dp = 8.dp,        // Tags, chips
    val md: Dp = 12.dp,       // Buttons, menus, small cards
    val lg: Dp = 16.dp,       // Standard cards, habit cards
    val xl: Dp = 20.dp,       // Input fields, large cards
    val xxl: Dp = 24.dp,      // FAB, hero cards, dialogs
    val full: Dp = 999.dp     // Perfect circles
)

// ─── ELEVATION ───────────────────────────────────────────────────────────────
// Semantic elevation tokens for depth hierarchy.
data class HabitualElevation(
    val none: Dp = 0.dp,      // Flat cards
    val low: Dp = 2.dp,       // Subtle lift — cards, FAB resting
    val medium: Dp = 4.dp,    // Active cards, FAB pressed, dialogs tonal
    val high: Dp = 8.dp       // Dialogs shadow, nav bar
)

// ─── ALPHA ───────────────────────────────────────────────────────────────────
// Opacity scale for tinting, borders, and emphasis.
data class HabitualAlpha(
    val low: Float = 0.05f,        // Barely visible — card borders
    val subtle: Float = 0.3f,      // Soft backgrounds, input borders, avatar rings
    val muted: Float = 0.5f,       // Inactive chips, placeholders, calendar inactive
    val secondary: Float = 0.7f,   // Secondary text, labels, near-full content
    val high: Float = 1.0f,        // Full opacity
    val disabled: Float = 0.38f    // Material disabled state
)

// ─── COMPOSITION LOCALS ──────────────────────────────────────────────────────
val LocalSpacing = staticCompositionLocalOf { HabitualSpacing() }
val LocalRadius = staticCompositionLocalOf { HabitualRadius() }
val LocalElevation = staticCompositionLocalOf { HabitualElevation() }
val LocalAlpha = staticCompositionLocalOf { HabitualAlpha() }
