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

@Composable
fun DashboardScreen(navController: NavHostController) {
    val sampleHabits = listOf(
        Habit(id = "1", title = "Morning Yoga", category = "Health", description = "15 mins stretch"),
        Habit(id = "2", title = "Read Kotlin Docs", category = "Study", description = "Learn State"),
        Habit(id = "3", title = "Water Plants", category = "Home", description = "Check balcony")
    )

    // Using Scaffold to provide the FAB slot
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigates to the Detail screen with ID "new" to show a blank form
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
        // Apply innerPadding so the content isn't covered by bars or the FAB
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Daily Habits",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            // Scrollable List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp) // Extra padding so FAB doesn't block last item
            ) {
                items(sampleHabits) { habit ->
                    HabitCard(
                        habit = habit,
                        onClick = {
                            navController.navigate(Screen.HabitDetail.createRoute(habit.id))
                        }
                    )
                }
            }
        }
    }
}