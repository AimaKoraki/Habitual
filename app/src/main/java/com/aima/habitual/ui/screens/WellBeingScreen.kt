package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WellBeingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Health Insights", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Example of "Another Component Type": Circular Progress for Steps
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 0.65f },
                modifier = Modifier.size(200.dp),
                strokeWidth = 12.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("6,540", style = MaterialTheme.typography.displaySmall)
                Text("Steps Today", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Example of "Component Variety": Assist Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { },
                label = { Text("7h Sleep") },
                leadingIcon = { Icon(Icons.Default.Nightlight, contentDescription = null) }
            )
            AssistChip(
                onClick = { },
                label = { Text("5km Walked") },
                leadingIcon = { Icon(Icons.Default.DirectionsWalk, contentDescription = null) }
            )
        }
    }
}