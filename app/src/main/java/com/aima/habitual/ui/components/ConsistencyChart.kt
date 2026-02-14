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
import com.aima.habitual.ui.theme.HabitualTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * ConsistencyChart visualizes ritual completion over the last 7 days.
 * Redesigned with softer inactive bars and precise spacing.
 */
@Composable
fun ConsistencyChart(records: List<HabitRecord>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HabitualTheme.components.chartHeight)
            .padding(horizontal = HabitualTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val last7Days = (0..6).reversed().map { LocalDate.now().minusDays(it.toLong()) }

        last7Days.forEach { date ->
            val isDone = records.any { it.timestamp == date.toEpochDay() && it.isCompleted }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Bar — wider with rounded top corners, softer inactive style
                Box(
                    modifier = Modifier
                        .width(HabitualTheme.components.chartBarWidth)
                        .fillMaxHeight(if (isDone) 0.8f else 0.2f)
                        .background(
                            color = if (isDone)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = HabitualTheme.alpha.subtle
                                ),
                            shape = RoundedCornerShape(
                                topStart = HabitualTheme.radius.medium,
                                topEnd = HabitualTheme.radius.medium
                            )
                        )
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

                // Day Label
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}