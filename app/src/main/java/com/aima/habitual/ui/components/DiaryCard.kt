package com.aima.habitual.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.ui.theme.HabitualTheme

@Composable
fun DiaryCard(
    entry: DiaryEntry,
    onClick: () -> Unit // <--- ADDED: To handle clicks
) {
    Column(
        modifier = Modifier
             .padding(vertical = HabitualTheme.spacing.md)
            .clickable { onClick() } // <--- ADDED: Makes the whole area clickable
    ) {
        // 1. Tag Row
        if (entry.tags.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(bottom = HabitualTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
            ) {
                entry.tags.forEach { tag ->
                    DiaryTag(text = tag)
                }
            }
        }

        // 2. Main Entry Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(HabitualTheme.radius.xxl),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh

            )
        ) {
            Column(modifier = Modifier.padding(HabitualTheme.components.cardPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.date,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )

                    // Helper to safely check if 'categoryIcon' exists on your model
                    // (Assuming your model has this field, if not, remove this block)
                    val icon = Icons.Default.Notes

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

                Text(
                    text = entry.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

                Text(
                    text = entry.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}