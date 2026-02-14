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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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

/**
 * DashboardScreen: The main landing page displaying daily rituals,
 * personalized greetings, and chronological navigation.
 */
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    val habits = viewModel.habits
    val isDark = isSystemInDarkTheme()

    // 1. CHRONOLOGICAL STATE: Tracks the currently viewed day
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.HabitDetail.createRoute("new")) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // --- HEADER SECTION: Greeting and Dynamic Time ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HabitualTheme.spacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                val now = java.time.LocalDateTime.now()
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = now.format(java.time.format.DateTimeFormatter.ofPattern("d MMM")),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = HabitualTheme.alpha.secondary)
                    )
                    Text(
                        text = now.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // --- DATE SELECTOR: Syncs scroller with screen state ---
            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            Text(
                text = stringResource(R.string.todays_rituals),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.xl)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // 2. BACKGROUND TEXTURE: Applies the fading leaf pattern to the list area
            val leafColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.17f)
            Box(modifier = Modifier
                .fillMaxSize()
                .subtleLeafPattern(leafColor)
            ) {
                // --- HABIT LIST LOGIC ---
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
                    // Logic: Filters rituals based on selected date and creation timestamp
                    val dayOfWeek = selectedDate.dayOfWeek.value % 7
                    val filteredHabits = habits.filter { habit ->
                        val creationDate = java.time.Instant.ofEpochMilli(habit.createdAt)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        val isCreated = !selectedDate.isBefore(creationDate)
                        val isScheduled = habit.repeatDays.isEmpty() || habit.repeatDays.contains(dayOfWeek)
                        isCreated && isScheduled
                    }

                    // Sorting: Pushes completed rituals to the bottom of the list
                    val sortedHabits = filteredHabits.partition { habit ->
                        !viewModel.records.any {
                            it.habitId == habit.id && it.timestamp == selectedDate.toEpochDay() && it.isCompleted
                        }
                    }.let { (incomplete, complete) -> incomplete + complete }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg),
                        contentPadding = PaddingValues(bottom = 100.dp, start = HabitualTheme.spacing.xl, end = HabitualTheme.spacing.xl)
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
}

/**
 * PremiumHabitCard: Individual ritual item with micro-interactions.
 * Utilizes updateTransition for high-performance animation during state toggles.
 */
@Composable
fun PremiumHabitCard(
    habit: Habit,
    isDark: Boolean,
    isCompleted: Boolean = false,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    // 3. ANIMATION: Smoothly transitions color and scale during completion
    val transition = updateTransition(targetState = isCompleted, label = "CheckmarkTransition")

    val tint by transition.animateColor(label = "Tint") { completed ->
        if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    }

    val scale by transition.animateFloat(
        label = "Scale",
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            } else {
                tween(durationMillis = 200)
            }
        }
    ) { completed ->
        if (completed) 1.2f else 1.0f
    }

    val containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val textAlpha = if (isCompleted) HabitualTheme.alpha.muted else 1f

    Card(
        shape = RoundedCornerShape(HabitualTheme.radius.lg),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(HabitualTheme.components.cardPadding).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                )
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))
                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha)
                )
            }

            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = stringResource(R.string.desc_complete),
                    tint = tint,
                    modifier = Modifier.size(HabitualTheme.components.minTouchTarget).scale(scale)
                )
            }
        }
    }
}

/**
 * subtleLeafPattern: A procedural drawing modifier.
 * Uses a vertical fade mask to ensure the pattern disappears near the header for better readability.
 */
@Composable
fun Modifier.subtleLeafPattern(
    baseColor: Color
): Modifier = this.drawBehind {
    val leafSize = 38.dp.toPx()
    val gap = 40.dp.toPx()
    val rows = (size.height / gap).toInt() + 2
    val cols = (size.width / gap).toInt() + 2

    for (r in 0..rows) {
        val rowY = r * gap

        // 4. FADE MASK: Calculates alpha based on vertical position
        val fadeFactor = ((rowY - 0f) / (150.dp.toPx() - 0f)).coerceIn(0f, 1f)

        for (c in 0..cols) {
            val seed = (r * 31 + c).toLong()
            val random = java.util.Random(seed)

            val offsetX = random.nextFloat() * (gap * 0.7f)
            val offsetY = random.nextFloat() * (gap * 0.7f)
            val randomRotation = random.nextFloat() * 360f

            drawContext.canvas.save()
            drawContext.transform.translate(c * gap + offsetX, rowY + offsetY)
            drawContext.transform.rotate(randomRotation)

            val leafPath = Path().apply {
                moveTo(0f, 0f)
                quadraticBezierTo(leafSize / 2, -leafSize / 3, leafSize, 0f)
                quadraticBezierTo(leafSize / 2, leafSize / 3, 0f, 0f)
                moveTo(0f, 0f)
                lineTo(leafSize * 0.75f, 0f)
            }

            drawPath(
                path = leafPath,
                color = baseColor.copy(alpha = baseColor.alpha * fadeFactor),
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawContext.canvas.restore()
        }
    }
}