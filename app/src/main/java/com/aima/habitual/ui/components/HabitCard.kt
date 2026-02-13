/** HabitCard.kt **/
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.model.Habit

/**
 * HabitCard updated to reflect the Forest Green & Soft Sage theme.
 */
@Composable
fun HabitCard(
    habit: Habit,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    // 1. Visual States: Logic for completed vs. active rituals
    val contentAlpha = if (habit.isCompleted) 0.5f else 1f
    val textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    // Using theme colors for Forest Green and Soft Sage
    val forestGreen = MaterialTheme.colorScheme.primary
    val softSage = MaterialTheme.colorScheme.surfaceVariant

    val cardContainerColor = if (habit.isCompleted)
        softSage.copy(alpha = 0.6f) // Faded sage for completed rituals
    else
        softSage // Vibrant sage for active rituals

    ElevatedCard(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (habit.isCompleted) 0.dp else 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(containerColor = cardContainerColor)
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
                    textDecoration = textDecoration,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.bodyMedium,
                    // Uses Forest Green for the ritual category
                    color = forestGreen.copy(alpha = contentAlpha)
                )
            }

            // 2. Branded Completion Button with Forest Green accents
            IconButton(
                onClick = onCheckClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (habit.isCompleted) forestGreen
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = forestGreen.copy(alpha = contentAlpha),
                        shape = CircleShape
                    )
            ) {
                if (habit.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.desc_completed_ritual),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}