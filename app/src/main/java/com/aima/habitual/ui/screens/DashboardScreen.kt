package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.model.Habit
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.HabitCard
import com.aima.habitual.ui.components.DatePickerScroller

/**
 * DashboardScreen displays the daily rituals and habits.
 * Completed habits are automatically moved to the bottom and greyed out.
 */
@Composable
fun DashboardScreen(navController: NavHostController) {
    // State management for the habit list
    val sampleHabits = remember {
        mutableStateListOf(
            Habit(id = "1", title = "Morning Yoga", category = "Health", description = "15 mins stretch"),
            Habit(id = "2", title = "Read Kotlin Docs", category = "Study", description = "Learn State"),
            Habit(id = "3", title = "Water Plants", category = "Home", description = "Check balcony")
        )
    }

    // High Mark Requirement: Logical Sorting
    // False (incomplete) comes before True (complete) in default sorting
    val sortedHabits = sampleHabits.sortedBy { it.isCompleted }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
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

            // Horizontal date navigation
            DatePickerScroller()

            Text(
                text = "Habit List",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Vertical Scrollable List with dynamic sorting
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sortedHabits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        onCardClick = {
                            navController.navigate(Screen.HabitStats.createRoute(habit.id))
                        },
                        onCheckClick = {
                            // Find the correct habit in the source list to update state
                            val index = sampleHabits.indexOfFirst { it.id == habit.id }
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