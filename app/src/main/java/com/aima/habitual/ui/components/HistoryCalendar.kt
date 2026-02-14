package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import com.aima.habitual.R
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.ui.screens.layout.StatsLayout
import com.aima.habitual.ui.theme.HabitualTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * HistoryCalendar: A monthly heat-map visualization for ritual completion.
 * Optimized for the Forest Green & Soft Sage palette to provide a calm,
 * data-rich overview of user progress.
 */
@Composable
fun HistoryCalendar(records: List<HabitRecord>) {
    // 1. CALENDAR CALCULATIONS:
    // We use the java.time API to dynamically calculate month bounds.
    val currentMonth = YearMonth.now()
    val today = LocalDate.now()
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()

    // 2. GRID OFFSET LOGIC:
    // Ensures the 1st of the month aligns with the correct day of the week (e.g., Tuesday).
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysOfWeek = listOf(
        stringResource(R.string.day_sun),
        stringResource(R.string.day_mon),
        stringResource(R.string.day_tue),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_thu),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sat)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. BRANDED HEADER:
        // Displays the Month and Year (e.g., "February 2026") in high-contrast Forest Green.
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = HabitualTheme.spacing.section)
        )

        // 4. THE CALENDAR GRID:
        // Uses LazyVerticalGrid with 7 fixed columns to represent the days of the week.
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(StatsLayout.calendarGridHeight),
            userScrollEnabled = false, // Static display; scrolling handled by parent if needed
            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.xs)
        ) {
            // Day Labels: Sun, Mon, Tue, etc.
            items(daysOfWeek) { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = HabitualTheme.spacing.md)
                )
            }

            // Offset slots: Fill empty spaces before the 1st of the month begins.
            items(firstDayOfWeek) {
                Spacer(modifier = Modifier.size(HabitualTheme.components.calendarCellSize))
            }

            // 5. INDIVIDUAL DAY CELLS:
            items(daysInMonth) { index ->
                val dayNumber = index + 1
                val date = currentMonth.atDay(dayNumber)

                // DATA MAPPING: Checks if a completion record exists for this specific day.
                val isDone = records.any { it.timestamp == date.toEpochDay() && it.isCompleted }
                val isCurrentDay = date.isEqual(today)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // Ensures cells remain perfect circles
                        .clip(CircleShape)
                        .background(
                            when {
                                // Completed rituals use the Primary 'Forest Green' color
                                isDone -> MaterialTheme.colorScheme.primary
                                // Current day highlighted with a subtle Sage 'lift'
                                isCurrentDay -> MaterialTheme.colorScheme.surfaceContainerHigh
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isDone || isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isDone -> MaterialTheme.colorScheme.onPrimary // White text on green
                            isCurrentDay -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}