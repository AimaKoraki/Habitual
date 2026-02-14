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
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.ui.theme.HabitualTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * HistoryCalendar visualizes ritual completion over a full month.
 * Optimized for the Forest Green & Soft Sage palette.
 */
@Composable
fun HistoryCalendar(records: List<HabitRecord>) {
    val currentMonth = YearMonth.now()
    val today = LocalDate.now()
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()

    // Offset calculation: ensure the grid starts on the correct day of the week
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
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Branded Month Header in Forest Green
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = HabitualTheme.spacing.xxl)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(HabitualTheme.components.calendarGridHeight),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.xs)
        ) {
            // Day Labels using muted Sage text
            items(daysOfWeek) { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = HabitualTheme.spacing.md)
                )
            }

            // Empty slots for the start of the month
            items(firstDayOfWeek) {
                Spacer(modifier = Modifier.size(HabitualTheme.components.calendarCellSize))
            }

            // Individual Day Cells
            items(daysInMonth) { index ->
                val dayNumber = index + 1
                val date = currentMonth.atDay(dayNumber)
                val isDone = records.any { it.timestamp == date.toEpochDay() && it.isCompleted }
                val isCurrentDay = date.isEqual(today)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> MaterialTheme.colorScheme.primary // Forest Green
                                isCurrentDay -> MaterialTheme.colorScheme.primaryContainer // Consistent accent
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
                            isDone -> MaterialTheme.colorScheme.onPrimary
                            isCurrentDay -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface.copy(
                                alpha = HabitualTheme.alpha.calendarInactive
                            )
                        }
                    )
                }
            }
        }
    }
}