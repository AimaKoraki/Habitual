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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        // 2. Main Entry Card - Paper Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(HabitualTheme.radius.xl), // Slightly less rounded for paper feel
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(HabitualTheme.components.cardPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayDate = remember(entry.timestamp) {
                        java.time.Instant.ofEpochMilli(entry.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM"))
                    }
                    Text(
                        text = displayDate,
                        color = MaterialTheme.colorScheme.primary, // Pop of color on date
                        style = MaterialTheme.typography.titleMedium // More prominent
                    )

                    val icon = Icons.Default.Notes

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

                Text(
                    text = entry.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall, // Larger title
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

                Text(
                    text = entry.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp), // Increased readability
                    maxLines = 4, // Show a bit more
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}