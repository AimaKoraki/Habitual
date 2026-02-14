package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.DiaryTag
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryViewScreen(
    entryId: String,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. Find the entry. If not found (deleted), handle gracefully.
    val entry = viewModel.diaryEntries.find { it.id == entryId }

    // State for delete dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (entry == null) {
        // Fallback if entry is missing
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.diary_entry_not_found_label))
            Button(onClick = { navController.popBackStack() }) { Text(stringResource(R.string.btn_go_back)) }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Base background
        topBar = {
            TopAppBar(
                title = { }, // Empty title for a cleaner look
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.desc_back))
                    }
                },
                actions = {
                    // 2. Delete Button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.desc_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // 3. Edit Button - Navigates to the "Form" screen
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.DiaryDetail.createRoute(entry.id)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = androidx.compose.foundation.shape.CircleShape, // Standardized
                        modifier = Modifier.size(HabitualTheme.components.fabSize),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = HabitualTheme.elevation.low,
                            pressedElevation = HabitualTheme.elevation.medium
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.desc_edit_entry), modifier = Modifier.size(HabitualTheme.components.iconMd))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent // Transparent
                )
            )
        }
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = HabitualTheme.spacing.xl) // Wider reading margins
            ) {
            // 3. Date Header
            val displayDate = remember(entry.timestamp) {
                java.time.Instant.ofEpochMilli(entry.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM"))
            }
            Text(
                text = displayDate,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

            // 4. Main Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.displaySmall, // Big header
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // 5. Tags Display (Chips)
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
                ) {
                    entry.tags.forEach { tag ->
                        DiaryTag(text = tag)
                    }
                }
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = HabitualTheme.alpha.muted))

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // 6. The Content (Readable Body Text)
            Text(
                text = entry.content,
                // FIX: Changed 'height' to 'lineHeight'
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = androidx.compose.ui.unit.TextUnit(1.6f, androidx.compose.ui.unit.TextUnitType.Em)
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = HabitualTheme.alpha.high)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.components.iconXl))
        }

        // --- DELETE DIALOG ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = stringResource(R.string.dialog_delete_entry_title)) },
                text = { Text(text = stringResource(R.string.dialog_delete_entry_text)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDiaryEntry(entry.id)
                            showDeleteDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )

                    ) {
                        Text(stringResource(R.string.btn_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    }
        }
}