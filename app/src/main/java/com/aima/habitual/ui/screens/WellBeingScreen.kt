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

@Composable
fun WellBeingScreen(
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. STATE: Selected Date
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 2. LOGIC: Fetch stats strictly for the selected date
    val stats = viewModel.getStatsForDate(selectedDate)

    // Dialog States
    var showSleepDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }

    // Water Logic constants
    val unitOptions = listOf("ml", "Cups", "Oz")
    var waterAmountInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(unitOptions[0]) }

    val stepGoal = 10000
    val stepProgress = (stats.stepsCount.toFloat() / stepGoal).coerceIn(0f, 1f)

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

            // Date Picker updates 'selectedDate'
            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // Steps Circle
            // ─── Steps Section Card ───
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HabitualTheme.spacing.md), // Matches horizontal padding of cards below
                shape = RoundedCornerShape(HabitualTheme.radius.lg), // Rounded corners like your other cards
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), // Subtle background color
                tonalElevation = 1.dp // Optional: adds a slight "lifted" look
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = HabitualTheme.spacing.lg)
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

                    // Sync Button (Repositioned slightly for the new card layout)
                    IconButton(
                        onClick = { viewModel.syncSteps() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = HabitualTheme.spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = stringResource(R.string.desc_sync_steps),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(HabitualTheme.components.iconLg)
                        )
                    }

                    // Center Text Labels
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", stats.stepsCount),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
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
            // Stats Grid
            Column(modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md)) {
                Text(
                    text = stringResource(R.string.wellbeing_daily_summary),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground // Fix text color
                )
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        HealthStatCard(
                            label = stringResource(R.string.wellbeing_sleep),
                            value = String.format("%.1fh", stats.sleepDurationHours), // Fix: Rounds to "4.0h"
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

            // Log Water Button
            Button(
                onClick = {
                    waterAmountInput = ""
                    showWaterDialog = true
                },
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

    // --- DIALOGS (Use Theme Colors) ---

    if (showSleepDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSleepDialog = false }) {
            Surface(
                shape = RoundedCornerShape(HabitualTheme.radius.xxl), // 24dp
                color = MaterialTheme.colorScheme.surface, // Warm white
                tonalElevation = HabitualTheme.elevation.medium,
                shadowElevation = HabitualTheme.elevation.high, // Diffused shadow
                modifier = Modifier.padding(HabitualTheme.spacing.md)
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl), // 24dp Internal Padding
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Header
                    Text(
                        text = stringResource(R.string.dialog_update_sleep),
                        style = MaterialTheme.typography.titleLarge, // 22sp
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.md)) // 12dp

                    // 2. Subtitle (Muted)
                    Text(
                        text = stringResource(R.string.dialog_sleep_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // 32dp

                    // 3. Value Label (Strong Emphasis)
                    Text(
                        text = String.format("%.1f Hours", stats.sleepDurationHours),
                        style = MaterialTheme.typography.headlineSmall, // Strong
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl)) // 20dp

                    // 4. Slider (Visual Balance)
                    Slider(
                        value = stats.sleepDurationHours.toFloat(),
                        onValueChange = {
                            viewModel.updateSleep(selectedDate, it.toDouble())
                        },
                        valueRange = 0f..12f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            thumbColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=HabitualTheme.alpha.muted)
                        )
                    )

                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // 32dp

                    // 5. Action
                    Button(
                        onClick = { showSleepDialog = false },
                        shape = RoundedCornerShape(HabitualTheme.radius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.btn_done),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }

    if (showWaterDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showWaterDialog = false }) {
            Surface(
                shape = RoundedCornerShape(HabitualTheme.radius.lg),
                // Premium: Softer surface tone & shadow
                color = MaterialTheme.colorScheme.surface, // Use surface (warm) or surfaceContainerLow if available. 
                // Using surface as base, but maybe add tonal elevation
                tonalElevation = HabitualTheme.elevation.medium, 
                shadowElevation = HabitualTheme.elevation.high,
                modifier = Modifier.padding(HabitualTheme.spacing.md)
            ) {
                Column(
                    modifier = Modifier.padding(HabitualTheme.spacing.xl), // Generous internal padding
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Header
                    Text(
                        text = stringResource(R.string.dialog_log_water_title),
                        style = MaterialTheme.typography.titleLarge, // Larger
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

                    // 2. Input
                    OutlinedTextField(
                        value = waterAmountInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) waterAmountInput = it },
                        label = { Text(stringResource(R.string.wellbeing_amount_label, selectedUnit)) },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(HabitualTheme.radius.xl), // 20dp
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle), // Softer border
                        )
                    )

                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

                    // 3. Unit Chips (Borderless)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        unitOptions.forEach { unit ->
                            val isSelected = selectedUnit == unit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(HabitualTheme.components.chipSize)
                                    .clip(RoundedCornerShape(HabitualTheme.radius.sm))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                                    )
                                    .clickable { selectedUnit = unit },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

                    // 4. Actions (Clean Baseline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showWaterDialog = false },
                            shape = RoundedCornerShape(HabitualTheme.radius.md)
                        ) {
                            Text(stringResource(R.string.btn_cancel), style = MaterialTheme.typography.labelLarge)
                        }
                        
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
                        
                        Button(
                            onClick = {
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
                            },
                            shape = RoundedCornerShape(HabitualTheme.radius.md),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.btn_add), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}