package com.aima.habitual.ui.screens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Screen-specific dimensions for ProfileScreen. */
object ProfileLayout {
    val profileImageSmall: Dp = 80.dp     // Compact avatar (fallback)
    val editBadgeOffset: Dp = 4.dp        // Edit badge positioning
    val nameFieldWidth: Dp = 200.dp       // Name editing text field
    val progressBarWidth: Dp = 120.dp     // Level progress bar
    val progressBarHeight: Dp = 8.dp      // Level progress bar thickness
}
