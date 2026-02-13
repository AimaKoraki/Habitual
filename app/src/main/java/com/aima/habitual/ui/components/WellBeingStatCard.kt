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

/**
 * WellbeingStatsGrid organizes health metrics into a branded Forest & Sage layout.
 */
@Composable
fun WellbeingStatsGrid(stats: WellbeingStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1: Steps - Uses Forest Green (Primary)
        HealthStatCard(
            label = stringResource(R.string.steps_label),
            value = String.format("%,d", stats.stepsCount),
            icon = Icons.Default.DirectionsRun,
            color = MaterialTheme.colorScheme.primary, // Forest Green
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Row 2, Card 1: Sleep - Uses Secondary (Sage)
            HealthStatCard(
                label = stringResource(R.string.sleep_label),
                value = "${stats.sleepDurationHours}h",
                icon = Icons.Default.NightsStay,
                color = MaterialTheme.colorScheme.secondary, // Muted Sage
                modifier = Modifier.weight(1f)
            )

            // Row 2, Card 2: Water - Uses Forest Green with localized string
            HealthStatCard(
                label = stringResource(R.string.log_water).split(" ").last(), // "Water"
                value = "${stats.waterIntakeMl}ml",
                icon = Icons.Default.LocalDrink,
                color = MaterialTheme.colorScheme.primary, // Forest Green
                modifier = Modifier.weight(1f)
            )
        }
    }
}