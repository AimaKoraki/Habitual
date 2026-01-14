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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.Habit

@Composable
fun HabitCard(
    habit: Habit,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    // Determine colors based on completion to avoid the "white shadow" alpha glitch
    val contentAlpha = if (habit.isCompleted) 0.5f else 1f
    val textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    ElevatedCard(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        // Use a flatter look for completed items to reduce shadow artifacts
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (habit.isCompleted) 0.dp else 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (habit.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
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
                    textDecoration = textDecoration, // Professional strike-through
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)
                )
            }

            // Circular Completion Button
            IconButton(
                onClick = onCheckClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (habit.isCompleted) Color(0xFF004D40).copy(alpha = 0.6f)
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFF004D40).copy(alpha = contentAlpha),
                        shape = CircleShape
                    )
            ) {
                if (habit.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}