package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.aima.habitual.R
import com.aima.habitual.ui.theme.RitualOrange // Import your custom orange

/**
 * StreakCard updated for the Forest & Sage theme with an orange momentum icon.
 */
@Composable
fun StreakCard(streakCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // Uses SoftSage/SecondaryContainer for the background
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                // Using the specific orange from your Color.kt
                tint = RitualOrange,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = stringResource(R.string.current_streak),
                style = MaterialTheme.typography.bodyMedium,
                // Uses ForestGreen/OnSecondaryContainer for text contrast
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = stringResource(R.string.days_count, streakCount),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                // Uses the main text color defined in your theme
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}