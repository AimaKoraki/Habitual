package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.model.WellbeingStats
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * WellbeingStatsGrid: Organizes individual health metrics into a structured, branded layout.
 * It utilizes a combination of full-width and half-width cards to create a visual
 * hierarchy of health data.
 */
@Composable
fun WellbeingStatsGrid(stats: WellbeingStats) {
    // 1. MAIN CONTAINER:
    // Uses a Column with standardized 'lg' (Large) spacing to prevent UI crowding.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HabitualTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg)
    ) {
        // 2. PRIMARY METRIC (STEPS):
        // Displayed as a full-width card to emphasize its importance as a daily goal.
        // Formats the step count with commas (e.g., "10,000") for better readability.
        HealthStatCard(
            label = stringResource(R.string.steps_label),
            value = String.format("%,d", stats.stepsCount),
            icon = Icons.Default.DirectionsRun,
            color = MaterialTheme.colorScheme.primary, // Forest Green accent
            modifier = Modifier.fillMaxWidth()
        )

        // 3. SECONDARY METRICS ROW:
        // Uses a Row with weight-based modifiers to split the screen 50/50.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg)
        ) {
            // SLEEP CARD:
            // Uses 'weight(1f)' to ensure it takes up exactly half of the available width.
            HealthStatCard(
                label = stringResource(R.string.sleep_label),
                value = "${stats.sleepDurationHours}h",
                icon = Icons.Default.NightsStay,
                color = MaterialTheme.colorScheme.secondary, // Muted Sage accent
                modifier = Modifier.weight(1f)
            )

            // WATER CARD:
            // Dynamically extracts the last word from the "Log Water" string to use as a label.
            // This is a clever way to reuse existing string resources.
            HealthStatCard(
                label = stringResource(R.string.log_water).split(" ").last(), // Becomes "Water"
                value = "${stats.waterIntakeMl}ml",
                icon = Icons.Default.LocalDrink,
                color = MaterialTheme.colorScheme.primary, // Forest Green accent
                modifier = Modifier.weight(1f)
            )
        }
    }
}