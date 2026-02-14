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
 * Designed with a "Flat Premium" aesthetic, prioritizing crispness on high-density
 * displays like the Pixel 7 emulator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Added: Enables interactive stat editing (e.g., logging water/sleep)
) {
    Card(
        onClick = onClick, // Connects the click action to the card surface for better accessibility
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Modern flat/glassy aesthetic
        ),
        // 1. VISUAL REFINEMENT:
        // Uses a subtle 1dp border instead of heavy shadows (elevation).
        // This 'Stroke' approach is a hallmark of modern Material 3 'Surface' design.
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(HabitualTheme.radius.lg) // Follows project-wide corner radius tokens
    ) {
        Column(
            modifier = Modifier
                .padding(HabitualTheme.spacing.xl) // Generous internal padding (Airy)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            // 2. ICONOGRAPHY:
            // The tint color is passed from the parent screen to distinguish metrics at a glance.
            Icon(
                imageVector = icon,
                contentDescription = null, // Set to null as the 'label' text provides the context
                tint = color,
                modifier = Modifier.size(HabitualTheme.components.iconLg)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // 3. DATA DISPLAY:
            // Uses 'headlineSmall' for the metric value to ensure quick scanning on the dashboard.
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 4. METRIC LABEL:
            // Uses a secondary alpha (opacity) to create a visual hierarchy between the value and its unit.
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
            )
        }
    }
}