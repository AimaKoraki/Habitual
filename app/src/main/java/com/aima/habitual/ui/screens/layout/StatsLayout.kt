package com.aima.habitual.ui.screens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * StatsLayout: Dedicated design tokens for data visualization components.
 * This object ensures that charts and calendars remain proportional and
 * accessible, providing a consistent "Dashboard" experience.
 */
object StatsLayout {

    /** * The fixed vertical space for the 7-day consistency bar chart.
     * 150.dp is optimized to show clear progress peaks without requiring
     * the user to scroll to see the entire week.
     */
    val chartHeight: Dp = 150.dp

    /** * The allocated height for the monthly habit-tracking grid.
     * 320.dp provides enough vertical room for 5-6 rows of day cells plus
     * the day-of-week labels.
     */
    val calendarGridHeight: Dp = 320.dp

    /** * The thickness of individual bars in the 7-day consistency chart.
     * 32.dp ensures that bars are wide enough to be "tappable" for potential
     * tooltips while remaining distinct on smaller screens.
     */
    val chartBarWidth: Dp = 32.dp

    /** * Specific internal padding for the Streak display.
     * At 28.dp, this provides "Airy" negative space around the fire icon and
     * count, emphasizing the user's momentum.
     */
    val streakCardPadding: Dp = 28.dp
}