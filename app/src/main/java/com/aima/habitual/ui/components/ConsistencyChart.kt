package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.ui.theme.HabitualTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * ConsistencyChart visualizes ritual completion over the last 7 days.
 * Updated to match the Forest Green & Soft Sage palette.
 */
@Composable
fun ConsistencyChart(records: List<HabitRecord>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = HabitualTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Calculate the last 7 days including today
        val last7Days = (0..6).reversed().map { LocalDate.now().minusDays(it.toLong()) }

        last7Days.forEach { date ->
            // Check if a completion record exists for this specific day
            val isDone = records.any { it.timestamp == date.toEpochDay() && it.isCompleted }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Bar Visualization with branded colors
                Box(
                    modifier = Modifier
                        .width(HabitualTheme.components.iconDefault) // Adjusted for better visual balance
                        .fillMaxHeight(if (isDone) 0.8f else 0.2f)
                        .background(
                            color = if (isDone)
                                MaterialTheme.colorScheme.primary // Forest Green
                            else
                                MaterialTheme.colorScheme.secondaryContainer, // Soft Sage
                            shape = RoundedCornerShape(HabitualTheme.radius.tag) // Fully rounded bars for a modern look
                        )
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

                // Day Label pulling from localized system settings
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Muted text for labels
                )
            }
        }
    }
}