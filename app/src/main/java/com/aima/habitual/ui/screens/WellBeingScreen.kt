package com.aima.habitual.ui.screens

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
        // FIX 1: Remove Color.White -> Use Theme Background
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
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Date Picker updates 'selectedDate'
            DatePickerScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Steps Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(220.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    strokeWidth = 16.dp,
                )
                CircularProgressIndicator(
                    progress = { stepProgress },
                    modifier = Modifier.size(220.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )

                // SYNC BUTTON
                IconButton(
                    onClick = { viewModel.syncSteps() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = stringResource(R.string.desc_sync_steps),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%,d", stats.stepsCount),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground // Fix text color
                    )
                    Text(
                        text = stringResource(R.string.wellbeing_steps),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Grid
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.wellbeing_daily_summary),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground // Fix text color
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        HealthStatCard(
                            label = stringResource(R.string.wellbeing_sleep),
                            value = "${stats.sleepDurationHours}h",
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

            Spacer(modifier = Modifier.height(32.dp))

            // Log Water Button
            Button(
                onClick = {
                    waterAmountInput = ""
                    showWaterDialog = true
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.LocalDrink, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.wellbeing_log_water))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- DIALOGS (Use Theme Colors) ---

    if (showSleepDialog) {
        AlertDialog(
            onDismissRequest = { showSleepDialog = false },
            title = { Text(stringResource(R.string.dialog_update_sleep)) },
            text = {
                Column {
                    Text(stringResource(R.string.dialog_sleep_prompt))
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = stats.sleepDurationHours.toFloat(),
                        onValueChange = {
                            viewModel.updateSleep(selectedDate, it.toDouble())
                        },
                        valueRange = 0f..12f,
                        steps = 23
                    )
                    Text(
                        text = String.format("%.1f", stats.sleepDurationHours) + " " + stringResource(R.string.wellbeing_sleep),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showSleepDialog = false }) { Text(stringResource(R.string.btn_done)) } },
            // FIX 2: Use Theme Surface Color
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showWaterDialog) {
        AlertDialog(
            onDismissRequest = { showWaterDialog = false },
            title = { Text(stringResource(R.string.dialog_log_water_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = waterAmountInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) waterAmountInput = it },
                        label = { Text(stringResource(R.string.wellbeing_amount_label, selectedUnit)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        unitOptions.forEach { unit ->
                            FilterChip(
                                selected = selectedUnit == unit,
                                onClick = { selectedUnit = unit },
                                label = { Text(unit) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
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
                    }
                ) { Text(stringResource(R.string.btn_add)) }
            },
            dismissButton = { TextButton(onClick = { showWaterDialog = false }) { Text(stringResource(R.string.btn_cancel)) } },
            // FIX 3: Use Theme Surface Color
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}