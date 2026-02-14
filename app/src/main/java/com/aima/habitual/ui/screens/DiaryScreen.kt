package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.ui.components.DiaryCard
import com.aima.habitual.ui.components.DiaryHeader
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

/** * SortMode: Defines the ordering logic for diary entries.
 * Enums ensure type-safe sorting transitions.
 */
private enum class SortMode { NEWEST, OLDEST, ALPHABETICAL }

/**
 * DiaryScreen: A reflective space for users to view and organize their journal entries.
 * Features a procedural background pattern and a dynamic sorting system.
 */
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

    // 1. DATA PROCESSING:
    // Uses 'remember' to avoid re-sorting during every recomposition unless 'entries' or 'sortMode' change.
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
            // --- HEADER SECTION ---
            Box(modifier = Modifier.padding(horizontal = HabitualTheme.spacing.lg)) {
                DiaryHeader(
                    title = stringResource(R.string.diary_header),
                    onSortClick = { showSortMenu = true }
                )

                // 2. SORT DROPDOWN:
                // Anchored to the header, providing an intuitive UX for organizing thoughts.
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

            // 3. CONTENT AREA:
            // Applies the procedural 'wavePattern' to the background to create a calm atmosphere.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .wavePattern(MaterialTheme.colorScheme.primary)
            ) {
                if (entries.isEmpty()) {
                    // EMPTY STATE: Provides visual guidance when no entries exist.
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = HabitualTheme.spacing.md)
                            )
                            Text(
                                text = stringResource(R.string.diary_empty_journal),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // LIST STATE: Displays scrollable entry cards.
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
}

/**
 * wavePattern: A procedural drawing modifier for background aesthetics.
 * Draws abstract, thin cubic Bézier curves at wide intervals to evoke
 * a feeling of calm and reflection.
 */
fun Modifier.wavePattern(
    baseColor: Color
): Modifier = this.drawBehind {
    val waveColor = baseColor.copy(alpha = 0.05f) // 0.05 low-opacity primary
    val strokeWidth = 1.5.dp.toPx()               // Ultra-thin line weight
    val gap = 60.dp.toPx()                        // Wide vertical spacing

    val width = size.width
    val height = size.height
    val rows = (height / gap).toInt() + 2

    for (i in 0..rows) {
        val y = i * gap
        val path = Path()
        path.moveTo(0f, y)

        // ABSTRACT GEOMETRY:
        // Generates long, lazy curves using cubic Bézier paths.
        // Alternating amplitude creates a natural, non-repeating flow.
        val amplitude = 25.dp.toPx() * (if (i % 2 == 0) 1 else -1)

        path.cubicTo(
            width * 0.35f, y + amplitude,
            width * 0.65f, y - amplitude,
            width, y
        )

        drawPath(
            path = path,
            color = waveColor,
            style = Stroke(width = strokeWidth)
        )
    }
}