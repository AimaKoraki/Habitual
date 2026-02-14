package com.aima.habitual.ui.screens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * DateScrollerLayout: Centralized design tokens for the horizontal date navigation component.
 * Using a dedicated object for layout constants is a "Best Practice" that ensures
 * pixel-perfect consistency and easier maintenance across the project.
 */
object DateScrollerLayout {

    /** * The fixed width for each individual date card (e.g., "Mon 12").
     * Set to 48.dp to ensure it meets the minimum touch target requirements
     * while allowing for a 5-day visible window on standard mobile displays.
     */
    val dateItemWidth: Dp = 48.dp

    /** * The thickness of the subtle horizontal bar used to mark the current calendar day.
     * This minor detail provides a clear anchor for the user without cluttering the UI.
     */
    val indicatorHeight: Dp = 3.dp
}