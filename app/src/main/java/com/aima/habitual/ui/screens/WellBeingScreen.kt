package com.aima.habitual.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.ui.components.DatePickerScroller
import com.aima.habitual.ui.components.HealthStatCard
import com.aima.habitual.ui.components.ScreenHeader
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
import java.time.LocalDate

/**
 * WellBeingScreen: A centralized dashboard for tracking health metrics.
 *
 * Organized into two clearly defined visual sections:
 * - **Active Vitality**: Steps counter (dual-layer CircularProgressIndicator) + Water intake
 * - **Sleep Sanctum**: Sleep logging (interactive slider) + Ambient light nudge
 */
@Composable
fun WellBeingScreen(
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. STATE MANAGEMENT
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val stats = viewModel.getStatsForDate(selectedDate)

    var showSleepDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }

    // Goal dialog states
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var stepGoalInput by remember { mutableStateOf(viewModel.stepGoal.toString()) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var waterGoalInput by remember { mutableStateOf(viewModel.waterGoal.toString()) }

    // Water input state
    val unitOptions = listOf("ml", "Cups", "Oz")
    var waterAmountInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(unitOptions[0]) }

    // Ambient light state
    val currentLux = viewModel.currentLuxLevel
    val isRoomTooBright = currentLux > 5f

    // PERMISSION HANDLING
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    // Step progress calculation
    val stepGoal = viewModel.stepGoal
    val stepProgress = (stats.stepsCount.toFloat() / stepGoal).coerceIn(0f, 1f)
    val isStepGoalAchieved = stats.stepsCount >= stepGoal

    // Water progress calculation
    val waterGoal = viewModel.waterGoal
    val waterProgress = (stats.waterIntakeMl.toFloat() / waterGoal).coerceIn(0f, 1f)
    val isWaterGoalAchieved = stats.waterIntakeMl >= waterGoal

    // Sleep progress calculation
    val sleepProgress = (stats.sleepDurationHours.toFloat() / 8f).coerceIn(0f, 1f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ScreenHeader(
                title = stringResource(R.string.wellbeing_header),
                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.lg)
            )

            // Dynamic date selection
            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // ═══════════════════════════════════════════════════════
            // SECTION 1: ACTIVE VITALITY (Steps + Water)
            // ═══════════════════════════════════════════════════════
            SectionHeader(
                title = stringResource(R.string.wellbeing_section_active_vitality),
                icon = Icons.Default.DirectionsWalk,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // --- Step Goal Card ---
            GoalCard(
                title = "Daily Step Goal",
                goalValue = "$stepGoal steps",
                isAchieved = isStepGoalAchieved,
                onClick = { showStepGoalDialog = true }
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // --- Steps Progress Card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HabitualTheme.spacing.md),
                shape = RoundedCornerShape(HabitualTheme.radius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = HabitualTheme.elevation.none)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = HabitualTheme.spacing.lg)
                ) {
                    // Background Progress Track
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(HabitualTheme.components.progressRingSize),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        strokeWidth = HabitualTheme.components.progressTrackThick,
                    )

                    // Active Progress Arc
                    CircularProgressIndicator(
                        progress = { stepProgress },
                        modifier = Modifier.size(HabitualTheme.components.progressRingSize),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = HabitualTheme.components.progressArcThick,
                        strokeCap = StrokeCap.Round
                    )

                    // Sync button (only for today)
                    if (selectedDate == LocalDate.now()) {
                        IconButton(
                            onClick = { viewModel.syncSteps() },
                            modifier = Modifier.align(Alignment.TopEnd).padding(end = HabitualTheme.spacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = stringResource(R.string.desc_sync_steps),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(HabitualTheme.components.iconLg)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", stats.stepsCount),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.wellbeing_steps),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // --- Water Goal Card ---
            GoalCard(
                title = "Daily Water Goal",
                goalValue = "$waterGoal ml",
                isAchieved = isWaterGoalAchieved,
                onClick = { showWaterGoalDialog = true }
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // --- Water Intake Card ---
            Column(modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HealthStatCard(
                        label = stringResource(R.string.wellbeing_water),
                        value = "${stats.waterIntakeMl} ml",
                        icon = Icons.Default.LocalDrink,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { showWaterDialog = true },
                        progress = if (stats.waterIntakeMl > 0) waterProgress else null
                    )
                }

            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // Log Water Button
            Button(
                onClick = { waterAmountInput = ""; showWaterDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = HabitualTheme.spacing.lg).height(HabitualTheme.components.buttonHeight),
                shape = RoundedCornerShape(HabitualTheme.radius.md),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.LocalDrink, contentDescription = null)
                Spacer(modifier = Modifier.width(HabitualTheme.spacing.sm))
                Text(stringResource(R.string.wellbeing_log_water))
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // Section Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.xl),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // ═══════════════════════════════════════════════════════
            // SECTION 2: SLEEP SANCTUM (Sleep + Environment)
            // ═══════════════════════════════════════════════════════
            SectionHeader(
                title = stringResource(R.string.wellbeing_section_sleep_sanctum),
                icon = Icons.Default.NightsStay,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // --- Sleep Duration Card ---
            Column(modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HealthStatCard(
                        label = stringResource(R.string.wellbeing_sleep),
                        value = String.format("%.1fh", stats.sleepDurationHours),
                        icon = Icons.Default.NightsStay,
                        color = MaterialTheme.colorScheme.secondary,
                        progress = if (stats.sleepDurationHours > 0) sleepProgress else null,
                        onClick = { showSleepDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // --- Dynamic Sleep Nudge ---
            // Only appears when isRoomTooBright is true (lux > 5)
            // Uses the same darkness score indicator as SleepLogForm
            val darknessScore = ((1f - (currentLux / 50f).coerceIn(0f, 1f)) * 100).toInt()
            val scoreColor by animateColorAsState(
                targetValue = when {
                    darknessScore >= 80 -> Color(0xFF4CAF50)
                    darknessScore >= 40 -> Color(0xFFFFA726)
                    else                -> Color(0xFFEF5350)
                },
                animationSpec = androidx.compose.animation.core.tween(600),
                label = "scoreColor"
            )
            val animatedProgress by animateFloatAsState(
                targetValue = darknessScore / 100f,
                animationSpec = androidx.compose.animation.core.tween(900),
                label = "darknessProgress"
            )

            AnimatedVisibility(
                visible = isRoomTooBright,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HabitualTheme.spacing.md),
                    shape = RoundedCornerShape(HabitualTheme.radius.lg),
                    color = Color(0xFFFFF8E1),
                    tonalElevation = HabitualTheme.elevation.none
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = HabitualTheme.spacing.lg,
                            vertical = HabitualTheme.spacing.md
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg)
                    ) {
                        // Circular darkness score indicator
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                strokeWidth = 5.dp
                            )
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(56.dp),
                                color = scoreColor,
                                strokeWidth = 5.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$darknessScore",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your room is a bit bright for deep sleep.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4E2600)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Want to dim the lights?",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF795548)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format("%.1f lux", currentLux),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8D6E63)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // Log Sleep Button
            Button(
                onClick = { showSleepDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = HabitualTheme.spacing.lg).height(HabitualTheme.components.buttonHeight),
                shape = RoundedCornerShape(HabitualTheme.radius.md),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.NightsStay, contentDescription = null)
                Spacer(modifier = Modifier.width(HabitualTheme.spacing.sm))
                Text("Log Sleep")
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
        }
    }

    // ═══════════════════════════════════════════════════════
    // INTERACTIVE DIALOGS
    // ═══════════════════════════════════════════════════════

    // SLEEP DIALOG
    if (showSleepDialog) {
        val currentSleepLog = viewModel.getSleepLog(selectedDate)
        val initialDurationOpt = currentSleepLog?.durationMinutes ?: (stats.sleepDurationHours * 60).toInt()
        val initialQualityOpt = currentSleepLog?.quality ?: ""

        androidx.compose.ui.window.Dialog(onDismissRequest = { showSleepDialog = false }) {
            Surface(
                shape = RoundedCornerShape(HabitualTheme.radius.xxl),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = HabitualTheme.elevation.medium,
                shadowElevation = HabitualTheme.elevation.high,
                modifier = Modifier.padding(HabitualTheme.spacing.md)
            ) {
                com.aima.habitual.ui.components.SleepLogForm(
                    initialDurationMinutes = initialDurationOpt,
                    initialQuality = initialQualityOpt,
                    currentLux = viewModel.currentLuxLevel,
                    onSave = { duration, quality ->
                        viewModel.saveSleepLog(selectedDate, duration, quality)
                        showSleepDialog = false
                    },
                    onCancel = { showSleepDialog = false }
                )
            }
        }
    }

    // WATER DIALOG
    if (showWaterDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showWaterDialog = false }) {
            Surface(
                shape = RoundedCornerShape(HabitualTheme.radius.lg),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = HabitualTheme.elevation.medium,
                shadowElevation = HabitualTheme.elevation.high,
                modifier = Modifier.padding(HabitualTheme.spacing.md)
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(R.string.dialog_log_water_title), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
                    OutlinedTextField(
                        value = waterAmountInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) waterAmountInput = it },
                        label = { Text(stringResource(R.string.wellbeing_amount_label, selectedUnit)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(HabitualTheme.radius.xl)
                    )
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))
                    // Unit selection Chips
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)) {
                        unitOptions.forEach { unit ->
                            val isSelected = selectedUnit == unit
                            Box(
                                modifier = Modifier.weight(1f).height(HabitualTheme.components.chipSize).clip(RoundedCornerShape(HabitualTheme.radius.sm))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = HabitualTheme.alpha.muted))
                                    .clickable { selectedUnit = unit },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = unit, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showWaterDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
                        Button(onClick = {
                            val amount = waterAmountInput.toIntOrNull() ?: 0
                            if (amount > 0) {
                                val amountInMl = when (selectedUnit) {
                                    "ml" -> amount
                                    "Cups" -> amount * 250
                                    "Oz" -> amount * 30
                                    else -> amount
                                }
                                viewModel.logWater(selectedDate, amountInMl)
                                showWaterDialog = false
                            }
                        }) { Text(stringResource(R.string.btn_add)) }
                    }
                }
            }
        }
    }

    // --- STEP GOAL DIALOG ---
    if (showStepGoalDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showStepGoalDialog = false }) {
            Surface(
                shape = RoundedCornerShape(HabitualTheme.radius.lg),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(HabitualTheme.spacing.md)
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Set Daily Step Goal", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
                    OutlinedTextField(
                        value = stepGoalInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) stepGoalInput = it },
                        label = { Text("Steps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(HabitualTheme.radius.xl)
                    )
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showStepGoalDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
                        Button(onClick = {
                            val goal = stepGoalInput.toIntOrNull() ?: 10000
                            viewModel.updateStepGoal(goal)
                            showStepGoalDialog = false
                        }) { Text(stringResource(R.string.btn_save)) }
                    }
                }
            }
        }
    }

    // --- WATER GOAL DIALOG ---
    if (showWaterGoalDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showWaterGoalDialog = false }) {
            Surface(
                shape = RoundedCornerShape(HabitualTheme.radius.lg),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(HabitualTheme.spacing.md)
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Set Daily Water Goal", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
                    OutlinedTextField(
                        value = waterGoalInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) waterGoalInput = it },
                        label = { Text("Amount in ml") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(HabitualTheme.radius.xl)
                    )
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showWaterGoalDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
                        Button(onClick = {
                            val goal = waterGoalInput.toIntOrNull() ?: 2000
                            viewModel.updateWaterGoal(goal)
                            showWaterGoalDialog = false
                        }) { Text(stringResource(R.string.btn_save)) }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// SECTION HEADER COMPONENT
// ═══════════════════════════════════════════════════════

/**
 * Branded section header with an icon and a styled accent bar.
 * Used to visually separate the two primary wellbeing sections.
 */
@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HabitualTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
    ) {
        // Accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ═══════════════════════════════════════════════════════
// GOAL CARD COMPONENT
// ═══════════════════════════════════════════════════════

@Composable
private fun GoalCard(
    title: String,
    goalValue: String,
    isAchieved: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isAchieved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "GoalCardColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isAchieved) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "GoalCardContentColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HabitualTheme.spacing.md)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(HabitualTheme.radius.lg),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        border = if (isAchieved) null else androidx.compose.foundation.BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = HabitualTheme.elevation.none)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HabitualTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAchieved) "🎉 Achieved: $goalValue" else goalValue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Goal",
                modifier = Modifier.size(18.dp),
                tint = contentColor.copy(alpha = 0.5f)
            )
        }
    }
}