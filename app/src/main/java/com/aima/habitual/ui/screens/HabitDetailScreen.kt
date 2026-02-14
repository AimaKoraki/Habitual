package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.ui.components.HabitForm
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * HabitDetailScreen provides the container for the HabitForm, managing edit vs. create modes.
 * Now includes Delete functionality for existing rituals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String?,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. Logic: Determine if we are editing an existing habit
    val existingHabit = if (habitId != null && habitId != "new") {
        viewModel.habits.find { it.id == habitId }
    } else null

    // State for the Delete Confirmation Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingHabit == null)
                            stringResource(R.string.add_habit_title)
                        else
                            stringResource(R.string.edit_habit_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                // Action button for deletion (only visible if editing)
                actions = {
                    if (existingHabit != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.desc_delete_ritual),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // 2. Integration: HabitForm handles the actual data entry logic
            HabitForm(
                viewModel = viewModel,
                initialHabit = existingHabit,
                onSave = { navController.popBackStack() }
            )

            // 3. Delete Confirmation Dialog
            if (showDeleteDialog && existingHabit != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(text = stringResource(R.string.dialog_delete_title)) },
                    text = { Text(text = stringResource(R.string.dialog_delete_text, existingHabit.title)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteHabit(existingHabit.id)
                                showDeleteDialog = false
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
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