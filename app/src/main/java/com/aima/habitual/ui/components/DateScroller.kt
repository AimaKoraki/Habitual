/** DateScroller.kt **/
package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

import com.aima.habitual.ui.screens.layout.DateScrollerLayout
import com.aima.habitual.ui.theme.HabitualTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * DatePickerScroller allows users to navigate ritual dates using a horizontal scroll.
 * Optimized for the Forest Green & Soft Sage theme.
 * This component manages its own horizontal window state while syncing with the parent selection.
 */
@Composable
fun DatePickerScroller(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    // 1. STATE: Maintains the "window" of dates currently visible in the scroller.
    // By default, it centers around the selected date.
    var startDate by remember { mutableStateOf(selectedDate.minusDays(2)) }
    val today = LocalDate.now()

    // 2. LOGIC: Generates a list of 5 consecutive days based on the current window.
    // 'remember' ensures we only recalculate when the startDate actually changes.
    val days = remember(startDate) { (0..4).map { startDate.plusDays(it.toLong()) } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HabitualTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Navigation: Shifts the date window back by one day
        IconButton(onClick = { startDate = startDate.minusDays(1) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Days", // Accessibility support
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 3. SCROLLABLE LIST: Displays the calculated days in a horizontal row.
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.xs, Alignment.CenterHorizontally)
        ) {
            items(days) { date ->
                DateItem(
                    dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    dayNumber = date.dayOfMonth.toString(),
                    isSelected = date.isEqual(selectedDate),
                    isToday = date.isEqual(today),
                    onClick = { onDateSelected(date) }
                )
            }
        }

        // Navigation: Shifts the date window forward by one day
        IconButton(onClick = { startDate = startDate.plusDays(1) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Days", // Accessibility support
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * DateItem: Represents a single date card within the scroller.
 * Uses conditional logic to highlight the active selection and the current "Today" marker.
 */
@Composable
private fun DateItem(
    dayName: String,
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    // 4. THEME-DRIVEN COLORS: Adapts the background and content based on selection state.
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary // Forest Green
    else
        MaterialTheme.colorScheme.surfaceContainerLow // Soft Sage

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .padding(horizontal = HabitualTheme.spacing.xxs)
            .width(DateScrollerLayout.dateItemWidth)
            .clip(RoundedCornerShape(HabitualTheme.radius.md)) // Modern rounded aesthetic
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = HabitualTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = HabitualTheme.alpha.secondary)
        )
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )

        // 5. TODAY INDICATOR: A subtle dash to anchor the user to the current date.
        if (isToday) {
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))
            Box(
                modifier = Modifier
                    .width(HabitualTheme.components.iconSm)
                    .height(DateScrollerLayout.indicatorHeight)
                    .clip(RoundedCornerShape(HabitualTheme.radius.xs))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
            )
        }
    }
}