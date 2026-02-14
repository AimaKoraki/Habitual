package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        color = MaterialTheme.colorScheme.primary, // Forest Green
        modifier = modifier.padding(vertical = HabitualTheme.spacing.lg)
    )
}

/**
 * A reusable primary button styled with the app's theme.
 * Used for "Save Ritual" or "Update Ritual" actions.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(HabitualTheme.components.buttonHeight),
        shape = RoundedCornerShape(HabitualTheme.radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary, // Forest Green
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = enabled
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * HealthStatCard (formerly InfoCard) specifically styled for Wellbeing metrics.
 * Now supports dynamic icon coloring to match your Health Insights grid.
 */
