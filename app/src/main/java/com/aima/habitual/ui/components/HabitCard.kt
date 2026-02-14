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
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * HabitCard updated to reflect the Forest Green & Soft Sage theme.
 */
@Composable
fun HabitCard(
    habit: Habit,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    // 1. Visual States: Logic for completed vs. active rituals
    val contentAlpha = if (isCompleted) HabitualTheme.alpha.muted else 1f
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None

    // Using theme colors for Forest Green and Soft Sage
    val forestGreen = MaterialTheme.colorScheme.primary
    val softSage = MaterialTheme.colorScheme.surfaceContainer

    val cardContainerColor = if (isCompleted)
        softSage.copy(alpha = HabitualTheme.alpha.muted + 0.1f) // Faded sage for completed rituals
    else
        softSage // Vibrant sage for active rituals

    ElevatedCard(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HabitualTheme.spacing.lg, vertical = HabitualTheme.spacing.sm),
        shape = RoundedCornerShape(HabitualTheme.radius.lg),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isCompleted) HabitualTheme.elevation.none else HabitualTheme.elevation.low
        ),
        colors = CardDefaults.elevatedCardColors(containerColor = cardContainerColor)
    ) {
        Row(
            modifier = Modifier
                .padding(HabitualTheme.components.cardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleLarge,
                    textDecoration = textDecoration,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.bodyMedium,
                    // Uses Forest Green for the ritual category
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }

            // 2. Branded Completion Button with Forest Green accents
            IconButton(
                onClick = onCheckClick,
                modifier = Modifier
                    .size(HabitualTheme.components.chipSize)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) forestGreen
                        else Color.Transparent
                    )
                    .border(
                        width = HabitualTheme.components.borderMedium,
                        color = forestGreen.copy(alpha = if (isCompleted) 1f else HabitualTheme.alpha.secondary),
                        shape = CircleShape
                    )
            ) {
                if (isCompleted) {
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
