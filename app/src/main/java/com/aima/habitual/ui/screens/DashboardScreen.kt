package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.model.Habit
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.HabitCard
import com.aima.habitual.ui.components.DatePickerScroller

/**
 * DashboardScreen displays the daily rituals and habits.
 * It links to Stats for habit details and the Detail screen for new habits.
 */
@Composable
fun DashboardScreen(navController: NavHostController) {
    // Sample Data
    // Note: In a real app, this list would be managed by a ViewModel
    val sampleHabits = remember {
        mutableStateListOf(
            Habit(id = "1", title = "Morning Yoga", category = "Health", description = "15 mins stretch"),
            Habit(id = "2", title = "Read Kotlin Docs", category = "Study", description = "Learn State"),
            Habit(id = "3", title = "Water Plants", category = "Home", description = "Check balcony")
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to the creation form
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

            // Center-aligned date picker with arrows
            DatePickerScroller()

            Text(
                text = "Habit List",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // High Mark Requirement: Vertical Scrollable List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sampleHabits) { habit ->
                    HabitCard(
                        habit = habit,
                        onCardClick = {
                            // Tapping the card body navigates to the Statistics window
                            navController.navigate(Screen.HabitStats.createRoute(habit.id))
                        },
                        onCheckClick = {
                            // Tapping the circular button toggles completion status
                            val index = sampleHabits.indexOf(habit)
                            if (index != -1) {
                                sampleHabits[index] = habit.copy(isCompleted = !habit.isCompleted)
                            }
                        }
                    )
                }
            }
        }
    }
}