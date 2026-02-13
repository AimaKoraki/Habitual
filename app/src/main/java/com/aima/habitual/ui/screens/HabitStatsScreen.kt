package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.ui.components.StreakCard
import com.aima.habitual.ui.components.ConsistencyChart
import com.aima.habitual.ui.components.HistoryCalendar
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate

/**
 * HabitStatsScreen visualizes progress for a specific habit using charts and a calendar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatsScreen(
    habitId: String?,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. Data Retrieval: Find the habit and its specific completion history
    val habit = viewModel.habits.find { it.id == habitId }
    val habitRecords = viewModel.records.filter { it.habitId == habitId }

    // 2. Logic: Calculate current streak (Counting backwards from today)
    val currentStreak = remember(habitRecords) {
        var streak = 0
        var checkDate = LocalDate.now()
        while (habitRecords.any { it.timestamp == checkDate.toEpochDay() && it.isCompleted }) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        streak
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = habit?.title ?: stringResource(R.string.history_header),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary // Branded Deep Teal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = HabitualTheme.spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.xxl)
        ) {
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

            // 1. Visual Highlight: The Streak Card (Peach)
            StreakCard(streakCount = currentStreak)

            // 2. Weekly Insight: Consistency Chart (Teal Bars)
            Text(
                text = stringResource(R.string.consistency_header),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            ConsistencyChart(records = habitRecords)

            // 3. Monthly Overview: History Calendar
            Text(
                text = stringResource(R.string.history_header),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            HistoryCalendar(records = habitRecords)

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // Extra padding for bottom
        }
    }
}