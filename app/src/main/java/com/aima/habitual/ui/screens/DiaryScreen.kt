package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.ui.components.DiaryCard // <--- USING YOUR CARD
import com.aima.habitual.ui.components.ScreenHeader
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

@Composable
fun DiaryScreen(
    navController: NavHostController,
    viewModel: HabitViewModel,
    onEntryClick: (String) -> Unit,
    onAddClick: () -> Unit // Separate callback for FAB
) {
    val entries = viewModel.diaryEntries

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.CircleShape, // Standardized
                modifier = Modifier.size(HabitualTheme.components.fabSize),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = HabitualTheme.elevation.low,
                    pressedElevation = HabitualTheme.elevation.medium
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.desc_add_entry))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenHeader(
                title = stringResource(R.string.diary_header),
                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.lg)
            )

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.diary_empty_journal),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(HabitualTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md)
                ) {
                    items(entries) { entry ->
                        // USES YOUR DIARY CARD
                        DiaryCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) } // Pass ID back to MainScreen
                        )
                    }
                }
            }
        }
    }
}