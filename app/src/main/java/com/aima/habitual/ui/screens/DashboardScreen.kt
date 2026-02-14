package com.aima.habitual.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
                onClick = { navController.navigate(Screen.HabitDetail.createRoute("new")) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                // Premium Rule: Perfect Circle 56dp
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(HabitualTheme.components.fabSize),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = HabitualTheme.elevation.low, 
                    pressedElevation = HabitualTheme.elevation.medium
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.desc_add_ritual),
                    modifier = Modifier.size(HabitualTheme.components.addIconSize)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // JSON: "sectionSpacing": 32
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // --- 1. HEADER SECTION ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HabitualTheme.spacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: Greeting & Name
                Column {
                    val currentHour = java.time.LocalTime.now().hour
                    val greetingRes = when (currentHour) {
                        in 5..11 -> R.string.greeting_morning
                        in 12..16 -> R.string.greeting_afternoon
                        else -> R.string.greeting_evening
                    }

                    Text(
                        text = stringResource(greetingRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = HabitualTheme.alpha.secondary)
                    )
                    Text(
                        text = viewModel.userName.ifEmpty { stringResource(R.string.default_user_name) },
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // RIGHT: Date & Time
                val now = java.time.LocalDateTime.now()
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = now.format(java.time.format.DateTimeFormatter.ofPattern("d MMM")),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = HabitualTheme.alpha.secondary)
                    )
                    Text(
                        text = now.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.titleLarge, // Slightly smaller than DisplayMedium but prominent
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

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
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.xl)
            )

            // JSON: "componentSpacing": 20 (Using xl for spacing between title and list)
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // --- 3. HABIT LIST ---
            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Text(
                        text = stringResource(R.string.no_rituals_today),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = HabitualTheme.spacing.xl)
                    )
                }
            } else {
                // Filter habits to only those scheduled for the selected day AND created on/before that date
                val dayOfWeek = selectedDate.dayOfWeek.value % 7  // Mon=1..Sun=7 → 0=Sun convention
                val filteredHabits = habits.filter { habit ->
                    // Check creation date
                    val creationDate = java.time.Instant.ofEpochMilli(habit.createdAt)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    val isCreated = !selectedDate.isBefore(creationDate)

                    // Check schedule
                    val isScheduled = habit.repeatDays.isEmpty() || habit.repeatDays.contains(dayOfWeek)

                    isCreated && isScheduled
                }

                // Sort: incomplete habits first, completed habits at bottom
                val sortedHabits = filteredHabits.partition { habit ->
                    !viewModel.records.any {
                        it.habitId == habit.id && it.timestamp == selectedDate.toEpochDay() && it.isCompleted
                    }
                }.let { (incomplete, complete) -> incomplete + complete }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg),
                    contentPadding = PaddingValues(
                        bottom = 100.dp,
                        start = HabitualTheme.spacing.xl,
                        end = HabitualTheme.spacing.xl
                    )
                ) {
                    items(sortedHabits) { habit ->
                        val isCompleted = viewModel.records.any {
                            it.habitId == habit.id && it.timestamp == selectedDate.toEpochDay() && it.isCompleted
                        }
                        PremiumHabitCard(
                            habit = habit,
                            isDark = isDark,
                            isCompleted = isCompleted,
                            onClick = { navController.navigate(Screen.HabitStats.createRoute(habit.id)) },
                            onToggle = { viewModel.toggleHabitCompletion(habit.id, selectedDate) }
                        )
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
fun PremiumHabitCard(
    habit: Habit,
    isDark: Boolean,
    isCompleted: Boolean = false, // Add state parameter
    onClick: () -> Unit,
    onToggle: () -> Unit // Add toggle callback
) {
    // Animation State
    val transition = updateTransition(targetState = isCompleted, label = "CheckmarkTransition")
    
    val tint by transition.animateColor(label = "Tint") { completed ->
        if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    }

    val scale by transition.animateFloat(
        label = "Scale",
        transitionSpec = {
            if (targetState) {
                // Bounce effect when checking
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            } else {
                // Smooth shrinking when unchecking
                tween(durationMillis = 200)
            }
        }
    ) { completed ->
        if (completed) 1.2f else 1.0f // Slightly larger when checked for emphasis
    }

    // Greyed-out styling for completed habits
    val containerColor = if (isCompleted) 
        MaterialTheme.colorScheme.surfaceVariant 
        else MaterialTheme.colorScheme.surface
    
    val textAlpha = if (isCompleted) HabitualTheme.alpha.muted else 1f

    Card(
        shape = RoundedCornerShape(HabitualTheme.radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = HabitualTheme.elevation.none), // No shadow for flat feel
        // Premium Glow: 1px border with very low alpha
        border = BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.outlineVariant),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                )

                // JSON: "spacing.xs": 4
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))

                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                )
            }

            // Interactive Checkmark
            IconButton(onClick = onToggle) {
                Icon(
                    // Unchecked = Thin Outlined Circle. Checked = Filled Soft Circle.
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle, 
                    contentDescription = stringResource(if (isCompleted) R.string.desc_complete else R.string.desc_complete),
                    tint = tint,
                    modifier = Modifier
                        .size(HabitualTheme.components.minTouchTarget)
                        .scale(scale)
                )
            }
        }
    }
}