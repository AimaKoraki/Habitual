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

@Composable
fun DiaryHeader(
    title: String,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier // Step 1: Add the modifier parameter here
) {
    Row(
        // Step 2: Apply the modifier to the top-level Row
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HabitualTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground // Forest Green
        )

        TextButton(onClick = onSortClick) {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Forest Green
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.desc_sort_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}