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
    viewModel: HabitViewModel // Added ViewModel parameter
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == "new") "Add Habit" else "Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Passing the ViewModel into the form
            HabitForm(
                viewModel = viewModel,
                onSave = { navController.popBackStack() }
            )
        }
    }
}