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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.model.Habit
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * HabitCard: The primary interaction component for ritual tracking.
 * Redesigned to reflect the Forest Green & Soft Sage theme.
 * Handles complex state changes (elevation, text decoration, and alpha) based on completion status.
 */
@Composable
fun HabitCard(
    habit: Habit,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    // 1. VISUAL STATE LOGIC:
    // We adjust transparency and text styling to give immediate visual feedback.
    // Completed rituals are "strikethrough" and faded to lower the cognitive load on the user.
    val contentAlpha = if (isCompleted) HabitualTheme.alpha.muted else 1f
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None

    // Using theme-aware colors for brand consistency
    val forestGreen = MaterialTheme.colorScheme.primary
    val softSage = MaterialTheme.colorScheme.surfaceContainer

    // Dynamic background color: Completed cards become more subtle to let active ones pop.
    val cardContainerColor = if (isCompleted)
        softSage.copy(alpha = HabitualTheme.alpha.muted + 0.1f)
    else
        softSage

    ElevatedCard(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HabitualTheme.spacing.lg, vertical = HabitualTheme.spacing.sm),
        shape = RoundedCornerShape(HabitualTheme.radius.lg),
        // 2. ADAPTIVE ELEVATION:
        // Removing elevation for completed cards makes them look "pressed" into the background.
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
            // 3. TEXT CONTENT SECTION:
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
                    // Category labels use secondary alpha to maintain clear hierarchy
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }

            // 4. INTERACTIVE COMPLETION CHECKBOX:
            // Custom-built check button using CircleShape and Forest Green accents.
            val completeDesc = stringResource(R.string.desc_complete)
            IconButton(
                onClick = onCheckClick,
                modifier = Modifier
                    .size(HabitualTheme.components.chipSize)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) forestGreen
                        else Color.Transparent
                    )
                    // Border provides a visible target even when the habit is not yet checked
                    .border(
                        width = HabitualTheme.components.borderMedium,
                        color = forestGreen.copy(alpha = if (isCompleted) 1f else HabitualTheme.alpha.secondary),
                        shape = CircleShape
                    )
                    // 5. ACCESSIBILITY (A11Y):
                    // Essential for screen readers and your automated test suite to identify the check action.
                    .semantics { contentDescription = completeDesc }
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