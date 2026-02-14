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
import androidx.compose.ui.unit.dp
import com.aima.habitual.ui.screens.layout.DateScrollerLayout
import com.aima.habitual.ui.theme.HabitualTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * DatePickerScroller allows users to navigate ritual dates using a horizontal scroll.
 * Optimized for the Forest Green & Soft Sage theme.
 */
@Composable
fun DatePickerScroller(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf(selectedDate.minusDays(2)) }
    val today = LocalDate.now()
    val days = remember(startDate) { (0..4).map { startDate.plusDays(it.toLong()) } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HabitualTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { startDate = startDate.minusDays(1) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary // Forest Green
            )
        }

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
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

        IconButton(onClick = { startDate = startDate.plusDays(1) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary // Forest Green
            )
        }
    }
}

@Composable
private fun DateItem(
    dayName: String,
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    // Theme-driven color selection to match the new Forest & Sage aesthetic
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary // Forest Green
    else
        MaterialTheme.colorScheme.secondaryContainer // Soft Sage

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier
            .padding(horizontal = HabitualTheme.spacing.xxs)
            .width(DateScrollerLayout.dateItemWidth)
            .clip(RoundedCornerShape(HabitualTheme.radius.md))
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

        // Indicator for "Today" aligned with the primary brand color
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
                            MaterialTheme.colorScheme.primary // Forest Green highlight
                    )
            )
        }
    }
}