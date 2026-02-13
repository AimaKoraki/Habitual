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
    val screen: Dp = 24.dp, // Standard horizontal screen padding
    val listBottom: Dp = 100.dp // Bottom padding for scrollable lists
)

// 2. Define Radius Tokens
data class HabitualRadius(
    val tag: Dp = 8.dp,        // Tag/chip corner radius
    val input: Dp = 12.dp,     // Text field corner radius
    val small: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 20.dp,     // Card radius
    val extraLarge: Dp = 24.dp, // FAB radius
    val full: Dp = 999.dp      // Perfect circles
)

// 3. Define Component Specifics (from your JSON)
data class HabitualComponents(
    val fabSize: Dp = 56.dp,
    val minTouchTarget: Dp = 44.dp,
    val bottomNavHeight: Dp = 64.dp,
    val cardPadding: Dp = 20.dp,
    val iconLarge: Dp = 48.dp,
    val iconDefault: Dp = 28.dp,   // Standard icon
    val iconMedium: Dp = 20.dp,    // Action icons
    val iconSmall: Dp = 16.dp,
    val buttonHeight: Dp = 56.dp,  // Standard button height
    val chipSize: Dp = 40.dp,      // Day selector chips
    val profileImage: Dp = 120.dp,
    val profileImageSmall: Dp = 80.dp,
    val calendarCellSize: Dp = 40.dp,
    val progressRingSize: Dp = 220.dp,
    val progressStrokeWidth: Dp = 16.dp
)

// 4. Create "Locals" so Compose can pass these down the UI tree
val LocalSpacing = staticCompositionLocalOf { HabitualSpacing() }
val LocalRadius = staticCompositionLocalOf { HabitualRadius() }
val LocalComponents = staticCompositionLocalOf { HabitualComponents() }