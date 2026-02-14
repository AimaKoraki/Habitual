package com.aima.habitual.ui.components

import androidx.compose.foundation.BorderStroke
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
 * HealthStatCard with click support for editing stats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Added: Default empty click action
) {
    Card(
        onClick = onClick, // Added: Connects the click action
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Glassy/Flat
        ),
        // Premium: Subtle alpha border
        border = BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.onSurface.copy(alpha = HabitualTheme.alpha.low)),
        shape = RoundedCornerShape(HabitualTheme.radius.extraLarge)
    ) {
        Column(
            modifier = Modifier
                .padding(HabitualTheme.spacing.cardInternalLg) // Airy padding
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(HabitualTheme.components.iconDefault)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.nearFull)
            )
        }
    }
}