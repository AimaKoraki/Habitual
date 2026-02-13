package com.aima.habitual.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.model.Habit
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.DatePickerScroller
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    val habits = viewModel.habits
    val isDark = isSystemInDarkTheme()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.HabitDetail.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                // JSON: "fab.radius": 24
                shape = RoundedCornerShape(HabitualTheme.radius.extraLarge),
                // JSON: "fab.size": 56
                modifier = Modifier.size(HabitualTheme.components.fabSize)
            ) {
                // JSON: "fab.iconSize": 20
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.desc_add_ritual),
                    modifier = Modifier.size(HabitualTheme.components.iconMedium)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // JSON: "screen.horizontalPadding": 24
                .padding(horizontal = HabitualTheme.spacing.screen)
        ) {
            // JSON: "sectionSpacing": 32
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // --- 1. HEADER SECTION ---
            Text(
                text = stringResource(R.string.greeting_morning),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = viewModel.userName.ifEmpty { stringResource(R.string.default_user_name) },
                style = MaterialTheme.typography.displayMedium, // Using new premium typography
                color = MaterialTheme.colorScheme.onBackground
            )

            // JSON: "sectionSpacing": 32
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // --- 2. DATE SELECTOR ---
            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            // JSON: "sectionSpacing": 32
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            Text(
                text = stringResource(R.string.todays_rituals),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // JSON: "componentSpacing": 20 (Using xl for spacing between title and list)
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // --- 3. HABIT LIST ---
            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Text(
                        text = stringResource(R.string.no_rituals_today),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    // JSON: "spacing.lg": 16 (Space between cards)
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg),
                    // Adding bottom padding so the FAB doesn't cover the last item
                    contentPadding = PaddingValues(bottom = HabitualTheme.spacing.listBottom)
                ) {
                    items(habits) { habit ->
                        PremiumHabitCard(habit = habit, isDark = isDark)
                    }
                }
            }
        }
    }
}

/**
 * Premium Card using centralized design tokens for shadow, radius, and padding.
 */
@Composable
fun PremiumHabitCard(habit: Habit, isDark: Boolean) {
    Card(
        // JSON: "card.radius": 20
        shape = RoundedCornerShape(HabitualTheme.radius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            // Premium Rule: Soft shadow in light mode, NO shadow in dark mode
            defaultElevation = if (isDark) 0.dp else 4.dp
        ),
        // Premium Rule: Subtle 1px white border in dark mode
        border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                // JSON: "card.padding": 20
                .padding(HabitualTheme.components.cardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // JSON: "spacing.xs": 4
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))

                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.desc_complete),
                tint = MaterialTheme.colorScheme.primary,
                // Premium Rule: Min touch target sizing
                modifier = Modifier.size(HabitualTheme.components.minTouchTarget)
            )
        }
    }
}