package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * DiaryHeader: The primary header for the Diary screen.
 * It manages the title display and provides a trigger for the sorting menu.
 */
@Composable
fun DiaryHeader(
    title: String,
    onSortClick: () -> Unit,
    // Flexible modifier allows the parent (DiaryScreen) to control external padding
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Consistent vertical spacing aligned with the app's typography scale
            .padding(vertical = HabitualTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. SCREEN TITLE:
        // Uses 'headlineSmall' for prominent branding that remains readable on Pixel 7 displays.
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground // Adapts to Forest Green (Light) or Cream (Dark)
        )

        // 2. SORT ACTION:
        // A TextButton provides a larger, accessible touch target for opening the SortMenu.
        TextButton(onClick = onSortClick) {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Sage Green accent
            )

            // 3. VISUAL CUE:
            // The downward arrow indicates that clicking will reveal a dropdown menu.
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.desc_sort_options), // Accessibility description for Screen Readers
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}