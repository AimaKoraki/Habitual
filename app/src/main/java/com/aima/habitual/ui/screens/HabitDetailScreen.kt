package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.aima.habitual.ui.components.HabitForm
import com.aima.habitual.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String?,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // Logic: If habitId is not "new", find the existing habit to edit
    val existingHabit = if (habitId != "new") {
        viewModel.habits.find { it.id == habitId }
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (existingHabit == null) "Add Ritual" else "Edit Ritual")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // pass the existingHabit to the form for pre-filling
            HabitForm(
                viewModel = viewModel,
                initialHabit = existingHabit,
                onSave = { navController.popBackStack() }
            )
        }
    }
}