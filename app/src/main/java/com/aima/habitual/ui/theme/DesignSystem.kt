package com.aima.habitual.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 1. Define Spacing Tokens
data class HabitualSpacing(
    val xxs: Dp = 2.dp,       // Micro padding (e.g. DateScroller horizontal)
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val section: Dp = 32.dp,
    val sectionLg: Dp = 40.dp,       // Large gap between major sections
    val screen: Dp = 24.dp,        // Standard horizontal screen padding
    val listBottom: Dp = 100.dp,   // Bottom padding for scrollable lists
    val cardInternalLg: Dp = 24.dp, // Airy internal padding for summary cards
    val tagVertical: Dp = 6.dp     // Tag chip vertical padding
)

// 2. Define Radius Tokens
data class HabitualRadius(
    val indicator: Dp = 2.dp,   // Indicator dots/bars
    val xs: Dp = 4.dp,          // Tiny radius (progress bars)
    val tag: Dp = 8.dp,         // Tag/chip corner radius
    val input: Dp = 20.dp,      // Softer input fields
    val small: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 20.dp,      // Card radius
    val extraLarge: Dp = 24.dp,  // FAB radius
    val full: Dp = 999.dp       // Perfect circles
)

// 3. Define Component Specifics (from your JSON)
data class HabitualComponents(
    val fabSize: Dp = 56.dp,
    val minTouchTarget: Dp = 44.dp,
    val bottomNavHeight: Dp = 64.dp,
    val cardPadding: Dp = 20.dp,
    val iconLarge: Dp = 48.dp,
    val iconDefault: Dp = 28.dp,       // Standard icon
    val iconMedium: Dp = 20.dp,        // Action icons
    val iconSmall: Dp = 16.dp,
    val addIconSize: Dp = 24.dp,       // FAB add icon
    val buttonHeight: Dp = 56.dp,      // Standard button height
    val chipSize: Dp = 40.dp,          // Day selector chips
    val profileImage: Dp = 120.dp,
    val profileImageSmall: Dp = 80.dp,
    val calendarCellSize: Dp = 40.dp,
    val progressRingSize: Dp = 220.dp,
    val progressTrackThick: Dp = 8.dp,  // Thinner background track
    val progressArcThick: Dp = 16.dp,   // Thicker progress arc
    val fabElevation: Dp = 2.dp,
    val fabPressedElevation: Dp = 4.dp,
    // Border widths
    val borderThin: Dp = 1.dp,          // Standard thin border
    val borderMedium: Dp = 2.dp,        // Medium border (avatar ring, checkmark)
    // Component-specific sizes
    val indicatorHeight: Dp = 3.dp,     // DateScroller today indicator
    val dateItemWidth: Dp = 62.dp,      // DateScroller item width
    val chartHeight: Dp = 150.dp,       // ConsistencyChart height
    val calendarGridHeight: Dp = 320.dp, // HistoryCalendar grid
    val editBadgeSize: Dp = 36.dp,      // Profile edit badge
    val editBadgeOffset: Dp = 4.dp,     // Profile edit badge offset
    val nameFieldWidth: Dp = 200.dp,    // Profile name editing field
    val progressBarWidth: Dp = 120.dp,  // Profile level progress bar width
    val progressBarHeight: Dp = 8.dp,   // Profile level progress bar height
    // Elevation tokens
    val dialogTonalElevation: Dp = 6.dp,
    val dialogShadowElevation: Dp = 8.dp,
    val navBarElevation: Dp = 8.dp,     // Bottom nav bar tonal elevation
    val cardElevationNone: Dp = 0.dp,   // Flat cards
    val cardElevationLight: Dp = 2.dp,  // Light elevation
    val cardElevationMedium: Dp = 4.dp,  // Medium elevation
    // Stats screen specifics
    val streakCardPadding: Dp = 28.dp,  // Generous internal streak card padding
    val chartBarWidth: Dp = 32.dp       // Consistency chart bar width
)

// 4. Define Alpha Tokens
data class HabitualAlpha(
    val low: Float = 0.05f,        // Subtle borders
    val avatarRing: Float = 0.1f,  // Avatar ring border
    val inputBorder: Float = 0.15f, // Slightly visible border for inputs
    val subtle: Float = 0.3f,      // Subtle tracks/backgrounds
    val disabled: Float = 0.38f,
    val muted: Float = 0.5f,       // Muted elements (placeholders, inactive chips)
    val medium: Float = 0.7f,      // Secondary text
    val secondary: Float = 0.7f,   // Secondary content (alias for medium)
    val nearFull: Float = 0.8f,    // Near-full opacity
    val bodyText: Float = 0.9f,    // Body text
    val high: Float = 1.0f,
    val calendarInactive: Float = 0.45f // Softer inactive calendar dates
)

// 5. Create "Locals" so Compose can pass these down the UI tree
val LocalSpacing = staticCompositionLocalOf { HabitualSpacing() }
val LocalRadius = staticCompositionLocalOf { HabitualRadius() }
val LocalComponents = staticCompositionLocalOf { HabitualComponents() }
val LocalAlpha = staticCompositionLocalOf { HabitualAlpha() }