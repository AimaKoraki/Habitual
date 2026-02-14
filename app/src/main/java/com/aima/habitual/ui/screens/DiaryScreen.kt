package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.ui.components.DiaryCard
import com.aima.habitual.ui.components.DiaryHeader
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

/** Available sort modes for diary entries. */
private enum class SortMode { NEWEST, OLDEST, ALPHABETICAL }

@Composable
fun DiaryScreen(
    navController: NavHostController,
    viewModel: HabitViewModel,
    onEntryClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val entries = viewModel.diaryEntries
    var sortMode by remember { mutableStateOf(SortMode.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Apply sort
    val sortedEntries = remember(entries.toList(), sortMode) {
        when (sortMode) {
            SortMode.NEWEST -> entries.sortedByDescending { it.timestamp }
            SortMode.OLDEST -> entries.sortedBy { it.timestamp }
            SortMode.ALPHABETICAL -> entries.sortedBy { it.title.lowercase() }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.CircleShape,
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
            // --- DiaryHeader with Sort ---
            Box(modifier = Modifier.padding(horizontal = HabitualTheme.spacing.lg)) {
                DiaryHeader(
                    title = stringResource(R.string.diary_header),
                    onSortClick = { showSortMenu = true }
                )

                // Sort Dropdown anchored to the header
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    SortMode.entries.forEach { mode ->
                        val label = when (mode) {
                            SortMode.NEWEST -> stringResource(R.string.sort_newest)
                            SortMode.OLDEST -> stringResource(R.string.sort_oldest)
                            SortMode.ALPHABETICAL -> stringResource(R.string.sort_alphabetical)
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (sortMode == mode)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                sortMode = mode
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortMode == mode) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(HabitualTheme.components.iconSm)
                                    )
                                }
                            }
                        )
                    }
                }
            }

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
                    items(sortedEntries, key = { it.id }) { entry ->
                        DiaryCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}