package com.aima.habitual.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
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
            // Using a simple Row for custom header control or CenterAlignedTopAppBar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (existingEntry == null) stringResource(R.string.diary_new_entry)
                        else stringResource(R.string.diary_edit_entry),
                        style = MaterialTheme.typography.titleLarge
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
                            val entry = DiaryEntry(
                                id = existingEntry?.id ?: UUID.randomUUID().toString(),
                                title = title,
                                content = content,
                                tags = tags.toList(),
                                timestamp = existingEntry?.timestamp ?: System.currentTimeMillis()
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
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent // Transparent to show pattern
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Base background for pattern
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .wavePattern(MaterialTheme.colorScheme.primary) // Apply pattern here
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = HabitualTheme.spacing.xl)
                    .verticalScroll(rememberScrollState()) // Make scrollable
            ) {

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // Extra breathing room below header

                // A. Title Field (Soft Surface Style)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.diary_label_title)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = HabitualTheme.components.borderThin,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle), // Subtle border
                            shape = RoundedCornerShape(HabitualTheme.radius.xl) // 20dp
                        ),
                    singleLine = true,
                    shape = RoundedCornerShape(HabitualTheme.radius.xl),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface, // Warm
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl)) // 20dp Rhythm

                // B. Tag Input Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md) // 12dp gap
                ) {
                    TextField(
                        value = currentTagInput,
                        onValueChange = { currentTagInput = it },
                        label = { Text(stringResource(R.string.diary_label_add_tag)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = HabitualTheme.components.borderThin,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle),
                                shape = RoundedCornerShape(HabitualTheme.radius.xl)
                            ),
                        singleLine = true,
                        shape = RoundedCornerShape(HabitualTheme.radius.xl),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (currentTagInput.isNotBlank()) {
                                tags.add(currentTagInput.trim())
                                currentTagInput = ""
                            }
                        }),
                        trailingIcon = {
                            // Centered Action
                            Box(
                                modifier = Modifier
                                    .padding(end = HabitualTheme.spacing.lg) // Right padding
                                    .clickable {
                                        if (currentTagInput.isNotBlank()) {
                                            tags.add(currentTagInput.trim())
                                            currentTagInput = ""
                                        }
                                    }
                            ) {
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
                            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
                        ) {
                            tags.forEach { tag ->
                                InputChip(
                                    selected = true,
                                    onClick = { tags.remove(tag) },
                                    label = {
                                        Text(
                                            tag,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.desc_remove_tag),
                                            modifier = Modifier.size(HabitualTheme.components.iconSm)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(HabitualTheme.radius.md)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // Section Break

                // D. Content Field (Large)
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(stringResource(R.string.diary_label_content)) },
                    placeholder = {
                        Text(
                            "Write your thoughts...", // Hardcoded for now to avoid resource error if missing
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted) // Muted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(
                            width = HabitualTheme.components.borderThin,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle),
                            shape = RoundedCornerShape(HabitualTheme.radius.xl)
                        ),
                    shape = RoundedCornerShape(HabitualTheme.radius.xl),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Bottom breathing room
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
            }
        }
    }
}