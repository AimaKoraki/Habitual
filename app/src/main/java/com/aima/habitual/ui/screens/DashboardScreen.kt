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
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.HabitCard
import com.aima.habitual.ui.components.DatePickerScroller
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(navController: NavHostController, viewModel: HabitViewModel) {
    // Current selected date state
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Logic: Determine what habits to show based on the ViewModel's state
    val filteredHabitsWithStatus = remember(selectedDate, viewModel.habits.size, viewModel.records.size) {
        val dayOfWeekIndex = selectedDate.dayOfWeek.value % 7

        viewModel.habits.filter { it.repeatDays.contains(dayOfWeekIndex) }
            .map { habit ->
                val isDone = viewModel.records.any {
                    it.habitId == habit.id && it.timestamp == selectedDate.toEpochDay()
                }
                habit.copy(isCompleted = isDone)
            }
            .sortedBy { it.isCompleted } // Move completed to the bottom
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.HabitDetail.createRoute("new")) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Daily Rituals",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredHabitsWithStatus.isEmpty()) {
                // High Mark Requirement: Handle empty states
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No rituals for today. Tap + to add one!", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredHabitsWithStatus, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onCardClick = {
                                navController.navigate(Screen.HabitStats.createRoute(habit.id))
                            },
                            onCheckClick = {
                                // Delegate logic to the ViewModel
                                viewModel.toggleHabitCompletion(habit.id, selectedDate)
                            }
                        )
                    }
                }
            }
        }
    }
}