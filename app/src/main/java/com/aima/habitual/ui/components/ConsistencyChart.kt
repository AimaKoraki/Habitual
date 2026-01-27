package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.HabitRecord
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ConsistencyChart(records: List<HabitRecord>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val last7Days = (0..6).reversed().map { LocalDate.now().minusDays(it.toLong()) }

        last7Days.forEach { date ->
            val isDone = records.any { it.timestamp == date.toEpochDay() && it.isCompleted }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Bar
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(if (isDone) 0.8f else 0.2f)
                        .background(
                            color = if (isDone) Color(0xFF004D40) else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Day Label (Mon, Tue, etc.)
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}