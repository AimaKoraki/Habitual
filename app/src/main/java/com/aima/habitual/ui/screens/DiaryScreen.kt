package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavHostController
import com.aima.habitual.R
import java.util.Calendar
import java.util.Locale
import com.aima.habitual.model.DiaryEntry
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    navController: NavHostController,
    viewModel: HabitViewModel,
    onEntryClick: (String) -> Unit,
    onAddClick: (Boolean) -> Unit
) {
    val entries by viewModel.diaryEntries.collectAsState()
    var sortMode by remember { mutableStateOf(SortMode.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Authentication State
    var entryToUnlock by remember { mutableStateOf<DiaryEntry?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var showPasswordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Time Picker State
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true
    )

    // Filter
    val currentViewEntries = remember(entries.toList(), viewModel.isJournalTabSelected) {
        entries.filter { it.isJournal == viewModel.isJournalTabSelected }
    }

    // 1. DATA PROCESSING:
    // Uses 'remember' to avoid re-sorting during every recomposition unless 'entries' or 'sortMode' change.
    val sortedEntries = remember(currentViewEntries, sortMode) {
        when (sortMode) {
            SortMode.NEWEST -> currentViewEntries.sortedByDescending { it.timestamp }
            SortMode.OLDEST -> currentViewEntries.sortedBy { it.timestamp }
            SortMode.ALPHABETICAL -> currentViewEntries.sortedBy { it.title.lowercase() }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick(viewModel.isJournalTabSelected) },
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
                    action = {
                        Box {
                            TextButton(onClick = { showSortMenu = true }) {
                                Text(
                                    text = stringResource(R.string.sort_by),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.desc_sort_options),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
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
                    }
                )
            }

            // --- TABS SECTION ---
            TabRow(
                selectedTabIndex = if (viewModel.isJournalTabSelected) 1 else 0,
                containerColor = Color.Transparent,
                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md)
            ) {
                Tab(
                    selected = !viewModel.isJournalTabSelected,
                    onClick = { viewModel.isJournalTabSelected = false },
                    text = { Text("Notes", style = MaterialTheme.typography.titleMedium) }
                )
                Tab(
                    selected = viewModel.isJournalTabSelected,
                    onClick = { viewModel.isJournalTabSelected = true },
                    text = { Text("Journal", style = MaterialTheme.typography.titleMedium) }
                )
            }
            
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

            // 3. CONTENT AREA:
            // Applies the procedural 'wavePattern' to the background to create a calm atmosphere.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .wavePattern(MaterialTheme.colorScheme.primary)
            ) {
                if (sortedEntries.isEmpty() && !viewModel.isJournalTabSelected) {
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
                                    .size(HabitualTheme.components.iconXl)
                                    .padding(bottom = HabitualTheme.spacing.md)
                            )
                            Text(
                                text = "Your notes are empty.",
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
                        // Journal Setup Header
                        if (viewModel.isJournalTabSelected) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(HabitualTheme.radius.md)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(HabitualTheme.spacing.md),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Spacer(Modifier.width(HabitualTheme.spacing.md))
                                        Column {
                                            Text(
                                                "Daily Journaling", 
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            val timeText = viewModel.journalHabitTime?.let { "Daily at $it" } ?: "Tap to set a daily reminder to journal."
                                            Text(
                                                timeText, 
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        Spacer(Modifier.weight(1f))
                                        if (viewModel.journalHabitTime != null) {
                                           IconButton(onClick = { viewModel.disableDailyJournalHabit() }) {
                                               Icon(Icons.Default.Close, contentDescription = "Disable", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                           }
                                        }
                                    }
                                }
                            }
                        }

                        if (sortedEntries.isEmpty() && viewModel.isJournalTabSelected) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(HabitualTheme.components.attachmentMaxHeight), contentAlignment = Alignment.Center) {
                                    Text("Your journal is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        items(sortedEntries, key = { it.id }) { entry ->
                            DiaryCard(
                                entry = entry,
                                onClick = {
                                    if (entry.isLocked) {
                                        entryToUnlock = entry
                                        passwordInput = ""
                                        showPasswordError = false
                                        passwordVisible = false
                                    } else {
                                        onEntryClick(entry.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- PASSWORD DIALOG ---
        if (entryToUnlock != null) {
            AlertDialog(
                onDismissRequest = { entryToUnlock = null },
                title = { Text(text = "Unlock Entry") },
                text = {
                    Column {
                        Text(text = "Please enter your password to view this entry.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                showPasswordError = false
                            },
                            label = { Text("Password") },
                            isError = showPasswordError,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                val description = if (passwordVisible) "Hide password" else "Show password"
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (showPasswordError) {
                            Text(
                                text = "Incorrect password.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = HabitualTheme.spacing.md, top = HabitualTheme.spacing.xxs)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (viewModel.verifyUserPassword(passwordInput)) {
                                val idToOpen = entryToUnlock?.id
                                entryToUnlock = null
                                if (idToOpen != null) {
                                    onEntryClick(idToOpen)
                                }
                            } else {
                                showPasswordError = true
                            }
                        }
                    ) {
                        Text("Unlock")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToUnlock = null }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
        
        // --- TIME PICKER DIALOG ---
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Set Journaling Time") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        viewModel.enableDailyJournalHabit(formattedTime)
                        showTimePicker = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
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