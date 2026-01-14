package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.model.Habit
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.HabitCard
import com.aima.habitual.ui.components.DatePickerScroller // Import the scroller

/**
 * DashboardScreen displays the daily rituals and habits.
 * It features a horizontal date scroller and a vertical list of habits.
 */
@Composable
fun DashboardScreen(navController: NavHostController) {
    // Sample Data with descriptions to match the Habit model
    val sampleHabits = listOf(
        Habit(id = "1", title = "Morning Yoga", category = "Health", description = "15 mins stretch"),
        Habit(id = "2", title = "Read Kotlin Docs", category = "Study", description = "Learn State"),
        Habit(id = "3", title = "Water Plants", category = "Home", description = "Check balcony")
    )

    // Using Scaffold to manage the Floating Action Button (FAB) position
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to details with a "new" ID for creation
                    navController.navigate(Screen.HabitDetail.createRoute("new"))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Habit"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Text(
                text = "Daily Rituals",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )

            // Horizontal Scrollable List for Date Selection
            DatePickerScroller()

            // Sub-header for the Habit List
            Text(
                text = "Habit List",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            //Vertical Scrollable List (LazyColumn)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Prevents FAB from overlapping last item
            ) {
                items(sampleHabits) { habit ->
                    HabitCard(
                        habit = habit,
                        onClick = {
                            // Navigate to details for editing
                            navController.navigate(Screen.HabitDetail.createRoute(habit.id))
                        }
                    )
                }
            }
        }
    }
}