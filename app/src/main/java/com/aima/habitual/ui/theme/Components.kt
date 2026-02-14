package com.aima.habitual.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── COMPONENT TOKENS ────────────────────────────────────────────────────────
// Reusable, semantic dimensions that map to actual UI components across screens.
// Screen-specific layout constants live in per-screen layout files.
data class HabitualComponents(
    // ── Interactive ──
    val fabSize: Dp = 56.dp,            // FAB (Dashboard, Diary, DiaryView)
    val minTouchTarget: Dp = 44.dp,     // Minimum touch target
    val buttonHeight: Dp = 56.dp,       // Primary action buttons
    val chipSize: Dp = 40.dp,           // Day selector chips, unit chips
    val bottomNavHeight: Dp = 64.dp,    // Bottom navigation bar

    // ── Cards ──
    val cardPadding: Dp = 20.dp,        // Standard card internal padding

    // ── Icons ──
    val iconSm: Dp = 16.dp,            // Small icons (edit badge, close)
    val iconMd: Dp = 20.dp,            // Action icons (edit, arrows)
    val iconLg: Dp = 28.dp,            // Default-size icons (steps, nav)
    val iconXl: Dp = 48.dp,            // Large decorative icons
    val addIconSize: Dp = 24.dp,       // FAB plus icon

    // ── Progress ──
    val progressRingSize: Dp = 220.dp,  // Wellbeing step ring
    val progressTrackThick: Dp = 8.dp,  // Track background
    val progressArcThick: Dp = 16.dp,   // Arc foreground

    // ── Profile ──
    val profileImage: Dp = 120.dp,      // Profile avatar
    val editBadgeSize: Dp = 36.dp,      // Profile edit badge

    // ── Calendar ──
    val calendarCellSize: Dp = 40.dp,   // Calendar date cells

    // ── Borders ──
    val borderThin: Dp = 1.dp,          // Standard card borders
    val borderMedium: Dp = 2.dp         // Avatar ring, checkmark borders
)

val LocalComponents = staticCompositionLocalOf { HabitualComponents() }
