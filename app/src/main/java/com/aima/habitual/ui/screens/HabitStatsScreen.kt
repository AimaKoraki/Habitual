package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.ui.components.StreakCard
import com.aima.habitual.ui.components.ConsistencyChart
import com.aima.habitual.ui.components.HistoryCalendar
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatsScreen(
    habitId: String?,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    val habit = viewModel.habits.find { it.id == habitId }

    // Filter records specifically for this habit
    val habitRecords = viewModel.records.filter { it.habitId == habitId }

    // Logic: Calculate current streak
    val currentStreak = remember(habitRecords) {
        var streak = 0
        var checkDate = LocalDate.now()
        // Simple streak logic: count backwards from today
        while (habitRecords.any { it.timestamp == checkDate.toEpochDay() && it.isCompleted }) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        streak
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.title ?: "Habit Stats") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Streak Card
            StreakCard(streakCount = currentStreak)

            // 2. Consistency Chart (Last 7 Days)
            Text("Consistency", style = MaterialTheme.typography.titleLarge)
            ConsistencyChart(records = habitRecords)

            // 3. History Calendar (Current Month)
            Text("History", style = MaterialTheme.typography.titleLarge)
            HistoryCalendar(records = habitRecords)
        }
    }
}