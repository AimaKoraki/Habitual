package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * The individual Sage-colored tag used for diary entries.
 * Updated to use the Soft Sage secondaryContainer.
 */
@Composable
fun DiaryTag(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer, // Soft Sage
        shape = RoundedCornerShape(HabitualTheme.radius.medium)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.xs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer // Forest Green
        )
    }
}

/**
 * The header component containing the title and "Sort by" dropdown.
 * Optimized for the Forest Green brand identity.
 */
@Composable
fun DiaryHeader(
    title: String,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = HabitualTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary // Forest Green
        )

        TextButton(onClick = onSortClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.sort_by), // Localized "Sort by"
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}