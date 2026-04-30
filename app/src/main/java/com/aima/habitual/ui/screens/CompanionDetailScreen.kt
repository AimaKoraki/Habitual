package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * CompanionDetailScreen (DETAIL VIEW):
 * Displays full information for a specific virtual companion selected from the master list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionDetailScreen(
    companionName: String,
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    // Find the specific companion from the pre-fetched list
    val companion = viewModel.companions.find { it.name == companionName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Companion Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        if (companion == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Companion not found.", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = HabitualTheme.spacing.xl)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = companion.photoUrl,
                contentDescription = "${companion.name} Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square image
                    .clip(RoundedCornerShape(HabitualTheme.radius.xl))
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            Text(
                text = companion.name,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = companion.species,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = HabitualTheme.spacing.lg)
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

            Text(
                text = "Born: ${companion.birthYear}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!companion.favoriteFoods.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
                Text(
                    text = "Favorite Foods:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                companion.favoriteFoods.forEach { food ->
                    Text(
                        text = "• $food",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = HabitualTheme.spacing.md, top = HabitualTheme.spacing.xs)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
        }
    }
}
