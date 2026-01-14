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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DatePickerScroller() {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(2)) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()

    // ADD THIS LINE: This generates the list of dates for the scroller
    val days = remember(startDate) {
        (0..4).map { startDate.plusDays(it.toLong()) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { startDate = startDate.minusDays(1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }

        LazyRow(
            modifier = Modifier.weight(0.8f),
            horizontalArrangement = Arrangement.Center
        ) {
            // Now 'days' refers to the list we just created above
            items(days) { date ->
                DateItem(
                    dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    dayNumber = date.dayOfMonth.toString(),
                    isSelected = date.isEqual(selectedDate),
                    isToday = date.isEqual(today),
                    onClick = { selectedDate = date }
                )
            }
        }

        IconButton(onClick = { startDate = startDate.plusDays(1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}

@Composable
private fun DateItem(
    dayName: String,
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean, // New parameter to track the actual current date
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.8f)
        )
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )

        // REQUIREMENT: Persistent indicator for "Today"
        if (isToday) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSelected) Color.White else Color(0xFF004D40)) // Contrasts based on selection
            )
        }
    }
}