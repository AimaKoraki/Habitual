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
 * The individual Sage-colored tag used for diary entries.
 * Updated to use the Soft Sage secondaryContainer.
 */
@Composable
fun DiaryTag(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer, // Soft Sage
        shape = RoundedCornerShape(HabitualTheme.radius.md)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.xs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer // Forest Green
        )
    }
}