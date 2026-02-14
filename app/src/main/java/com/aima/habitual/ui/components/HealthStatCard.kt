package com.aima.habitual.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import com.aima.habitual.ui.theme.HabitualTheme

/**
 * HealthStatCard: A specialized card for displaying health metrics like Sleep and Water.
 * Redesigned for a "Premium" look with a flat elevation and subtle borders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Added: Enables interactive stat editing
) {
    Card(
        onClick = onClick, // Connects the click action to the card surface
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Modern flat/glassy aesthetic
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(HabitualTheme.radius.lg) // Standardized radius from design tokens
    ) {
        Column(
            modifier = Modifier
                .padding(HabitualTheme.spacing.xl) // 2. AIRY PADDING: Provides a spacious, high-end feel
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            // 1. ICONOGRAPHY:
            // Uses the color passed from the screen (e.g., Primary for Water, Secondary for Sleep).
            Icon(
                imageVector = icon,
                contentDescription = null, // Visual decoration only
                tint = color,
                modifier = Modifier.size(HabitualTheme.components.iconLg)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // 2. VALUE DISPLAY:
            // High-contrast headline text for quick readability.
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 3. LABEL:
            // Uses a secondary alpha to create a clear visual hierarchy.
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
            )
        }
    }
}