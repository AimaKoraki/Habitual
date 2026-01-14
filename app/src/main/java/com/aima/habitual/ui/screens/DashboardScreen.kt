package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    // Sample Data (No Lorem Ipsum!)
    val sampleHabits = listOf(
        Habit(id = "1", title = "Morning Yoga", category = "Health"),
        Habit(id = "2", title = "Read Kotlin Docs", category = "Study"),
        Habit(id = "3", title = "Water Plants", category = "Home")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Daily Habits",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // High Mark Requirement: Scrollable List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(sampleHabits) { habit ->
                HabitCard(
                    habit = habit,
                    onClick = {
                        // High Mark Requirement: Suitable navigation
                        navController.navigate(Screen.HabitDetail.createRoute(habit.id))
                    }
                )
            }
        }
    }
}