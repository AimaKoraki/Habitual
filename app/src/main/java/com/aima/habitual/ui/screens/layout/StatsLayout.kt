package com.aima.habitual.ui.screens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Screen-specific dimensions for HabitStatsScreen and stat components. */
object StatsLayout {
    val chartHeight: Dp = 150.dp          // ConsistencyChart height
    val calendarGridHeight: Dp = 320.dp   // HistoryCalendar grid
    val chartBarWidth: Dp = 32.dp         // Consistency chart bar width
    val streakCardPadding: Dp = 28.dp     // Generous streak card internal padding
}
