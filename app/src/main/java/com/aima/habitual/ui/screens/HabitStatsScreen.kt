package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.StreakCard
import com.aima.habitual.ui.components.ConsistencyChart
import com.aima.habitual.ui.components.HistoryCalendar
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate

/**
 * HabitStatsScreen — Premium visualization of habit progress.
 * Uses section cards with warm off-white background for depth and cohesion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatsScreen(
    habitId: String?,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    val habit = viewModel.habits.find { it.id == habitId }
    val habitRecords = viewModel.records.filter { it.habitId == habitId }

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = habit?.title ?: stringResource(R.string.history_header),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        if (habitId != null) {
                            navController.navigate(Screen.HabitDetail.createRoute(habitId))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.desc_edit_name),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = HabitualTheme.spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.section)
        ) {
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

            // 1. Streak Card — Hero section
            StreakCard(streakCount = currentStreak)

            // 2. Consistency Chart — wrapped in a section card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(HabitualTheme.radius.xxl),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = HabitualTheme.elevation.low
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl)
                ) {
                    Text(
                        text = stringResource(R.string.consistency_header),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))
                    ConsistencyChart(records = habitRecords)
                }
            }

            // 3. History Calendar — wrapped in a section card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(HabitualTheme.radius.xxl),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = HabitualTheme.elevation.low
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl)
                ) {
                    Text(
                        text = stringResource(R.string.history_header),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))
                    HistoryCalendar(records = habitRecords)
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
        }
    }
}