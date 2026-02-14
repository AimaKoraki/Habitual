package com.aima.habitual.ui.screens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ProfileLayout: A centralized repository of design tokens for the Profile screen.
 * By isolating these constants, we maintain a strict separation between
 * UI structure and screen logic, simplifying future layout adjustments.
 */
object ProfileLayout {

    /** * The size of the circular user avatar when displayed in compact layouts.
     * 80.dp provides a clear focal point without dominating the upper screen real estate.
     */
    val profileImageSmall: Dp = 80.dp

    /** * Controls the precise 'overlap' of the Edit Icon badge on the profile picture.
     * Small offsets like 4.dp create a polished, layered look common in premium social apps.
     */
    val editBadgeOffset: Dp = 4.dp

    /** * The fixed width for the user name input field during edit mode.
     * Ensuring a set width prevents the UI from "jumping" or shifting when the user taps to edit.
     */
    val nameFieldWidth: Dp = 200.dp

    /** * The horizontal length of the experience/level indicator.
     * Balanced at 120.dp to fit comfortably alongside profile labels or statistics.
     */
    val progressBarWidth: Dp = 120.dp

    /** * The vertical thickness of the level progress bar.
     * 8.dp is thick enough to show a clear color contrast but thin enough to remain elegant.
     */
    val progressBarHeight: Dp = 8.dp
}