package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.ui.screens.layout.StatsLayout
import com.aima.habitual.ui.theme.HabitualTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * ConsistencyChart visualizes ritual completion over the last 7 days.
 * Redesigned with softer inactive bars and precise spacing for high-density
 * displays like the Pixel 7.
 */
@Composable
fun ConsistencyChart(records: List<HabitRecord>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(StatsLayout.chartHeight) // Uses standardized layout tokens for consistency
            .padding(horizontal = HabitualTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // 1. Generate the date range for the last week (Today back to 6 days ago)
        val last7Days = (0..6).reversed().map { LocalDate.now().minusDays(it.toLong()) }

        last7Days.forEach { date ->
            // 2. Check the data records to see if the habit was completed on this specific date
            val isDone = records.any { it.timestamp == date.toEpochDay() && it.isCompleted }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // 3. Visual Bar Logic
                // Completed days are taller (70%) and use the primary theme color.
                // Incomplete days are shorter (25%) and use a subtle muted tone.
                Box(
                    modifier = Modifier
                        .width(StatsLayout.chartBarWidth)
                        .fillMaxHeight(if (isDone) 0.7f else 0.25f)
                        .background(
                            color = if (isDone)
                                MaterialTheme.colorScheme.primary
                            else
                            // Uses high-surface container color with alpha for a "soft" feel
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                                    alpha = HabitualTheme.alpha.subtle
                                ),
                            shape = RoundedCornerShape(
                                topStart = HabitualTheme.radius.md,
                                topEnd = HabitualTheme.radius.md
                            )
                        )
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

                // 4. Day Label (e.g., "Mon", "Tue")
                // Localized based on the user's phone settings.
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}