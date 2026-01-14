package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.Habit

/**
 * HabitCard displays the habit summary and a completion toggle.
 * * @param habit The data model for the habit.
 * @param onCardClick Action triggered when the card body is tapped (Stats).
 * @param onCheckClick Action triggered when the circular button is tapped (Toggle).
 */
@Composable
fun HabitCard(
    habit: Habit,
    onCardClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    ElevatedCard(
        onClick = onCardClick, // Tapping the card navigates to stats
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Circular Completion Button
            IconButton(
                onClick = onCheckClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (habit.isCompleted) Color(0xFF004D40)
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFF004D40),
                        shape = CircleShape
                    )
            ) {
                if (habit.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark Incomplete",
                        tint = Color.White
                    )
                }
            }
        }
    }
}