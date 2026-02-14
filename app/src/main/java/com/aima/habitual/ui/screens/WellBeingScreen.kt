package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
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
 * Integrates daily step counts, sleep duration, and hydration levels
 * within the project's signature "Forest & Sage" aesthetic.
 */
@Composable
fun WellBeingScreen(
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. STATE MANAGEMENT:
    // Tracks the current date for data filtering and handles visibility for interactive logging dialogs.
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val stats = viewModel.getStatsForDate(selectedDate)

    var showSleepDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }

    // Water Logic: Managed locally during the "Draft" phase of entry
    val unitOptions = listOf("ml", "Cups", "Oz")
    var waterAmountInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(unitOptions[0]) }

    // 2. DATA VISUALIZATION LOGIC:
    // Calculates step progress relative to a 10,000-step daily goal.
    val stepGoal = 10000
    val stepProgress = (stats.stepsCount.toFloat() / stepGoal).coerceIn(0f, 1f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()) // Ensures accessibility on smaller screens
        ) {
            ScreenHeader(
                title = stringResource(R.string.wellbeing_header),
                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.lg)
            )

            // Dynamic date selection linked to the primary stats query
            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // --- 3. STEPS PROGRESS CARD ---
            // Visualizes step data using a custom-layered CircularProgressIndicator.
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
                    // Background Progress Track (Muted)
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(HabitualTheme.components.progressRingSize),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        strokeWidth = HabitualTheme.components.progressTrackThick,
                    )

                    // Active Progress Arc (Primary Forest Green)
                    CircularProgressIndicator(
                        progress = { stepProgress },
                        modifier = Modifier.size(HabitualTheme.components.progressRingSize),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = HabitualTheme.components.progressArcThick,
                        strokeCap = StrokeCap.Round // Modern rounded arc ends
                    )

                    // Manual Sync Trigger
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

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // --- 4. DAILY SUMMARY GRID ---
            Column(modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md)) {
                Text(
                    text = stringResource(R.string.wellbeing_daily_summary),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        HealthStatCard(
                            label = stringResource(R.string.wellbeing_sleep),
                            value = String.format("%.1fh", stats.sleepDurationHours),
                            icon = Icons.Default.NightsStay,
                            color = MaterialTheme.colorScheme.secondary,
                            onClick = { showSleepDialog = true }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        HealthStatCard(
                            label = stringResource(R.string.wellbeing_water),
                            value = "${stats.waterIntakeMl}ml",
                            icon = Icons.Default.LocalDrink,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showWaterDialog = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // --- 5. HYDRATION ACTION ---
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
        }
    }

    // --- 6. INTERACTIVE DIALOGS ---

    // SLEEP DIALOG: Uses a slider for precision duration entry
    if (showSleepDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSleepDialog = false }) {
            Surface(
                shape = RoundedCornerShape(HabitualTheme.radius.xxl),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = HabitualTheme.elevation.medium,
                shadowElevation = HabitualTheme.elevation.high,
                modifier = Modifier.padding(HabitualTheme.spacing.md)
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(R.string.dialog_update_sleep), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
                    Text(text = stringResource(R.string.dialog_sleep_prompt), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
                    Text(text = String.format("%.1f Hours", stats.sleepDurationHours), style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
                    Slider(
                        value = stats.sleepDurationHours.toFloat(),
                        onValueChange = { viewModel.updateSleep(selectedDate, it.toDouble()) },
                        valueRange = 0f..12f,
                        steps = 23,
                        colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
                    Button(onClick = { showSleepDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.btn_done))
                    }
                }
            }
        }
    }

    // WATER DIALOG: Handles unit conversion and numerical entry
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
                    // Unit selection Chips for internationalization support
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
                                // Logic: Convert units to a standard mL format for database storage
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
}