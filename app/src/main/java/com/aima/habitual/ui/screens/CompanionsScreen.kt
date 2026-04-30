package com.aima.habitual.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aima.habitual.model.Companion
import com.aima.habitual.ui.components.ScreenHeader
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * CompanionsScreen (MASTER VIEW):
 * Displays a scrollable list of virtual companions fetched from an external JSON file.
 * Clicking a companion navigates to the detailed view.
 */
@Composable
fun CompanionsScreen(
    viewModel: HabitViewModel,
    onCompanionClick: (String) -> Unit
) {
    // Fetch JSON data when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchCompanions()
    }

    val companions = viewModel.companions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HabitualTheme.spacing.lg)
    ) {
        ScreenHeader(title = "Virtual Companions")

        Text(
            text = "Discover your habit companions, fetched from an external JSON file.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = HabitualTheme.spacing.md)
        )

        if (companions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md),
                contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
            ) {
                items(companions) { companion ->
                    CompanionCard(
                        companion = companion,
                        onClick = { onCompanionClick(companion.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun CompanionCard(companion: Companion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(HabitualTheme.radius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = HabitualTheme.elevation.low)
    ) {
        Row(
            modifier = Modifier.padding(HabitualTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = companion.photoUrl,
                contentDescription = "${companion.name} Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(HabitualTheme.radius.md))
            )
            
            Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
            
            Column {
                Text(
                    text = companion.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = companion.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
