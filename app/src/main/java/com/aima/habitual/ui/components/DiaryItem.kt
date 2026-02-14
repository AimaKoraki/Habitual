package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * DiaryTag: A small, labeled indicator used to categorize diary entries by mood or activity.
 * Designed with a low-contrast "Soft Sage" background to ensure it complements rather than
 * distracts from the entry content.
 */
@Composable
fun DiaryTag(text: String) {
    // 1. CONTAINER:
    // Uses a 'Surface' instead of a 'Box' to automatically handle elevation and
    // basic Material background behaviors.
    Surface(
        // Set to secondaryContainer for the Sage-toned aesthetic.
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(HabitualTheme.radius.md) // Rounded corners for a friendly UI
    ) {
        // 2. LABEL:
        // Uses 'labelMedium' typography for a compact but highly readable tag.
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = HabitualTheme.spacing.md,
                vertical = HabitualTheme.spacing.xs
            ),
            style = MaterialTheme.typography.labelMedium,
            // Uses onSecondaryContainer (Forest Green) to ensure high-contrast readability
            // against the Sage background.
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}