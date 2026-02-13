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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * DiaryDetailScreen provides the interface to create or edit journal entries.
 * Fully integrated with the Premium Minimal Wellness design token system.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    entryId: String?, // Nullable: If null -> New Entry, If ID exists -> Edit Mode
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    val existingEntry = viewModel.diaryEntries.find { it.id == entryId }

    var title by remember { mutableStateOf(existingEntry?.title ?: "") }
    var content by remember { mutableStateOf(existingEntry?.content ?: "") }

    var currentTagInput by remember { mutableStateOf("") }
    val tags = remember {
        mutableStateListOf<String>().apply {
            addAll(existingEntry?.tags ?: emptyList())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingEntry == null) stringResource(R.string.diary_new_entry)
                        else stringResource(R.string.diary_edit_entry),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.desc_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM"))

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
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.desc_save),
                            tint = if (title.isNotBlank() && content.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                // JSON Token: "screenPadding": 24
                .padding(horizontal = HabitualTheme.spacing.screen),
            // JSON Token: "spacing.lg": 16
            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg)
        ) {

            // JSON Token: "spacing.lg": 16 (Top breathing room)
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // A. Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.diary_label_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                // Soften the harsh square corners of default text fields
                shape = RoundedCornerShape(HabitualTheme.radius.medium)
            )

            // B. Tag Input Section
            Column(
                // JSON Token: "spacing.sm": 8
                verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
            ) {
                OutlinedTextField(
                    value = currentTagInput,
                    onValueChange = { currentTagInput = it },
                    label = { Text(stringResource(R.string.diary_label_add_tag)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(HabitualTheme.radius.medium),
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
                                contentDescription = stringResource(R.string.desc_add_tag),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // C. Tag Display Area
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        // JSON Token: "spacing.sm": 8
                        horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = true,
                                onClick = { tags.remove(tag) },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.desc_remove_tag),
                                        modifier = Modifier.size(HabitualTheme.components.iconSmall)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                // JSON Token: "radius.medium": 16
                                shape = RoundedCornerShape(HabitualTheme.radius.medium)
                            )
                        }
                    }
                }
            }

            // D. Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.diary_label_content)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Fills remaining space so the user has a large typing area
                shape = RoundedCornerShape(HabitualTheme.radius.medium)
            )

            // Bottom breathing room
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
        }
    }
}