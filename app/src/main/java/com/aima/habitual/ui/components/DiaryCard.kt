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

/**
 * DiaryCard: Visualizes an individual journal entry within the Diary list.
 * Designed with a "Paper Style" aesthetic to encourage reflection.
 */
@Composable
fun DiaryCard(
    entry: DiaryEntry,
    onClick: () -> Unit // Handle clicks to open the entry detail view
) {
    Column(
        modifier = Modifier
            .padding(vertical = HabitualTheme.spacing.md)
            .clickable { onClick() } // Makes the entire component a large, accessible touch target
    ) {
        // 1. TAG ROW: Displays user-defined categories or moods for the entry
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

        // 2. MAIN ENTRY CARD: Emulates a physical notebook page
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(HabitualTheme.radius.xl), // XL radius for a soft, premium feel
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat design consistent with M3
            // Thin border used instead of shadows to maintain clarity on high-DPI Pixel 7 screens
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(HabitualTheme.components.cardPadding)) {
                // HEADER: Date and Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date formatting logic is 'remembered' to optimize scroll performance on Android 16
                    val displayDate = remember(entry.timestamp) {
                        java.time.Instant.ofEpochMilli(entry.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM"))
                    }
                    Text(
                        text = displayDate,
                        color = MaterialTheme.colorScheme.primary, // Primary brand color used for chronological data
                        style = MaterialTheme.typography.titleMedium
                    )

                    val icon = Icons.Default.Notes

                    Icon(
                        imageVector = icon,
                        contentDescription = null, // Decorative icon
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

                // CONTENT: Title and Body
                Text(
                    text = entry.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall, // Prominent title for quick scanning
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

                Text(
                    text = entry.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Line height increased to 24sp to enhance reading comfort during reflection
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    maxLines = 4, // Truncates longer entries to keep the list scannable
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}