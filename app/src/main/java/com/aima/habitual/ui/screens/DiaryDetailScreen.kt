package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * DiaryDetailScreen provides the interface to create or edit journal entries.
 * Integrated with Forest Green & Soft Sage branding.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    entryId: String?, // Nullable: If null -> New Entry, If ID exists -> Edit Mode
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. Logic: Check if we are editing an existing entry
    // We use a safe call in case the list is empty or ID is null
    val existingEntry = viewModel.diaryEntries.find { it.id == entryId }

    // 2. State: Fields
    var title by remember { mutableStateOf(existingEntry?.title ?: "") }
    var content by remember { mutableStateOf(existingEntry?.content ?: "") }

    // 3. State: Tags
    var currentTagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>().apply {
        addAll(existingEntry?.tags ?: emptyList())
    }}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingEntry == null) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // SAVE BUTTON
                    IconButton(
                        onClick = {
                            val todayDate = LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("dd MMM"))

                            val entry = DiaryEntry(
                                id = existingEntry?.id ?: UUID.randomUUID().toString(),
                                title = title,
                                content = content,
                                date = existingEntry?.date ?: todayDate,
                                tags = tags.toList()
                            )

                            if (existingEntry == null) {
                                viewModel.addDiaryEntry(entry)
                            } else {
                                viewModel.updateDiaryEntry(entry)
                            }

                            navController.popBackStack()
                        },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // A. Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // B. Tag Input Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentTagInput,
                    onValueChange = { currentTagInput = it },
                    label = { Text("Add Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (currentTagInput.isNotBlank()) {
                            tags.add(currentTagInput.trim())
                            currentTagInput = ""
                        }
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (currentTagInput.isNotBlank()) {
                                tags.add(currentTagInput.trim())
                                currentTagInput = ""
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Tag",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // C. Tag Display Area
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = true,
                                onClick = { tags.remove(tag) },
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }

            // D. Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("How was your day?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Fills remaining space
                // FIX: Removed 'textAlign' parameter (it does not exist in OutlinedTextField)
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}