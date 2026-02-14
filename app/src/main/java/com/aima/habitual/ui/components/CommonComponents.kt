package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * A standardized header for all screens to ensure visual consistency.
 * Pulls from the primary Forest Green color.
 */
@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(vertical = HabitualTheme.spacing.lg)
    )
}
