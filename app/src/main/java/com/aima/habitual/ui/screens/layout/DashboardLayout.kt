package com.aima.habitual.ui.screens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * DashboardLayout: Centralized design tokens for the Dashboard screen.
 * Defines animation constants and layout dimensions for the background elements.
 */
object DashboardLayout {

    /**
     * Size of the decorative leaf icons in the background pattern.
     */
    val leafSize: Dp = 38.dp

    /**
     * Vertical gap between rows in the background pattern.
     */
    val patternGap: Dp = 40.dp

    /**
     * Vertical range for the fading opacity of the pattern.
     */
    val patternFadeRange: Dp = 150.dp

    /**
     * Bottom padding for the main content to avoid fab overlap and provide breathing room.
     */
    val contentBottomPadding: Dp = 100.dp
    
    /**
     * Stroke width for the decorative leaf outlines.
     */
    val leafStrokeWidth: Dp = 1.2.dp
}
