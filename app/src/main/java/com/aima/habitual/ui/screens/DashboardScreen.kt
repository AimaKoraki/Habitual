/** DasshboardScreen.kt **/

package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.HabitCard
import com.aima.habitual.ui.components.DatePickerScroller
import com.aima.habitual.ui.components.ScreenHeader
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * DashboardScreen: The main hub for your rituals.
 * Connects ScreenHeader, DatePickerScroller, and HabitCard.
 */
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: HabitViewModel) {
    // 1. State: Manage the currently selected date
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 2. Logic: Filter habits based on repeat days and creation date
    val filteredHabitsWithStatus = remember(selectedDate, viewModel.habits.size, viewModel.records.size) {
        val dayOfWeekIndex = selectedDate.dayOfWeek.value % 7 // 0=Sun, 1=Mon...
        val selectedEpochDay = selectedDate.toEpochDay()

        viewModel.habits.filter { habit ->
            // Only show if:
            // a) It is scheduled for this day of the week
            // b) The selected date is NOT before the habit was created
            val isRepeatDay = habit.repeatDays.contains(dayOfWeekIndex)
            val creationDate = Instant.ofEpochMilli(habit.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            isRepeatDay && !selectedDate.isBefore(creationDate)
        }
            .map { habit ->
                // Check if it's completed for this specific date
                val isDone = viewModel.records.any {
                    it.habitId == habit.id && it.timestamp == selectedEpochDay
                }
                habit.copy(isCompleted = isDone)
            }
            .sortedBy { it.isCompleted } // Move completed items to the bottom
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.HabitDetail.createRoute("new")) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_habit_content_desc)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Respects Status Bar & Nav Bar automatically
        ) {
            // 3. Header: Uses your CommonComponent 'ScreenHeader'
            ScreenHeader(
                title = stringResource(R.string.dashboard_header),
                modifier = Modifier.padding(horizontal = 16.dp) // Adds side padding
            )

            // 4. Date Scroller
            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Content Area
            if (filteredHabitsWithStatus.isEmpty()) {
                // Empty State: Lotus Icon + Encouraging Text
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_habits_msg),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                ) {
                    items(filteredHabitsWithStatus, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onCardClick = {
                                // Navigate to Stats/History
                                navController.navigate(Screen.HabitStats.createRoute(habit.id))
                            },
                            onCheckClick = {
                                viewModel.toggleHabitCompletion(habit.id, selectedDate)
                            }
                        )
                    }
                }
            }
        }
    }
}