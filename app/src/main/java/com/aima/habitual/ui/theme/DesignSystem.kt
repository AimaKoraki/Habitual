package com.aima.habitual.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 1. Define Spacing Tokens
data class HabitualSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val section: Dp = 32.dp,
    val screen: Dp = 24.dp // Standard horizontal screen padding
)

// 2. Define Radius Tokens
data class HabitualRadius(
    val small: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 20.dp, // Card radius
    val extraLarge: Dp = 24.dp, // FAB radius
    val full: Dp = 999.dp // Perfect circles
)

// 3. Define Component Specifics (from your JSON)
data class HabitualComponents(
    val fabSize: Dp = 56.dp,
    val minTouchTarget: Dp = 44.dp,
    val bottomNavHeight: Dp = 64.dp,
    val cardPadding: Dp = 20.dp,
    val iconLarge: Dp = 48.dp
)

// 4. Create "Locals" so Compose can pass these down the UI tree
val LocalSpacing = staticCompositionLocalOf { HabitualSpacing() }
val LocalRadius = staticCompositionLocalOf { HabitualRadius() }
val LocalComponents = staticCompositionLocalOf { HabitualComponents() }