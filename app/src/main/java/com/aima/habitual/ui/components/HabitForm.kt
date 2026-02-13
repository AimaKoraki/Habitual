package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.model.Habit
import com.aima.habitual.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm(
    viewModel: HabitViewModel,
    initialHabit: Habit? = null,
    onSave: () -> Unit
) {
    // 1. Form State
    var habitName by remember { mutableStateOf(initialHabit?.title ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf(
        stringResource(R.string.category_health),
        stringResource(R.string.category_study),
        stringResource(R.string.category_personal),
        stringResource(R.string.category_work),
        stringResource(R.string.category_wellbeing)
    )
    var selectedCategory by remember { mutableStateOf(initialHabit?.category ?: categories[0]) }
    var targetMonths by remember { mutableFloatStateOf(initialHabit?.targetMonths?.toFloat() ?: 1f) }

    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    var selectedDays by remember { mutableStateOf(initialHabit?.repeatDays?.toSet() ?: emptySet()) }

    // Helper: Check if all days are selected
    val isEveryDaySelected = selectedDays.size == 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(HabitualTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.xl)
    ) {
        // Field 1: Habit Name
        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text(stringResource(R.string.habit_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.habit_name_placeholder)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Field 2: Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        // Field 3: Day Selection with "Every Day" Toggle
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.repeat_on_label),
                    style = MaterialTheme.typography.titleSmall
                )

                // NEW: "Every Day" Chip
                FilterChip(
                    selected = isEveryDaySelected,
                    onClick = {
                        if (isEveryDaySelected) {
                            selectedDays = emptySet() // Clear all
                        } else {
                            selectedDays = (0..6).toSet() // Select all (Sun-Sat)
                        }
                    },
                    label = { Text(stringResource(R.string.every_day)) },
                    leadingIcon = {
                        if (isEveryDaySelected) {
                            Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(HabitualTheme.components.iconSmall))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // Day Circles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = selectedDays.contains(index)
                    Box(
                        modifier = Modifier
                            .size(HabitualTheme.components.chipSize)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable {
                                // Toggle individual day
                                selectedDays = if (isSelected) selectedDays - index else selectedDays + index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Field 4: Target Duration
        Column {
            Text(
                text = stringResource(R.string.target_duration_label, targetMonths.toInt()),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = targetMonths,
                onValueChange = { targetMonths = it },
                valueRange = 1f..12f,
                steps = 10,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Field 5: Action Button
        Button(
            onClick = {
                // Ensure at least one day is selected before saving
                val daysToSave = if (selectedDays.isEmpty()) (0..6).toList() else selectedDays.toList()

                if (initialHabit == null) {
                    val newHabit = Habit(
                        title = habitName,
                        category = selectedCategory,
                        repeatDays = daysToSave,
                        targetMonths = targetMonths.toInt()
                    )
                    viewModel.addHabit(newHabit)
                } else {
                    val index = viewModel.habits.indexOfFirst { it.id == initialHabit.id }
                    if (index != -1) {
                        viewModel.habits[index] = initialHabit.copy(
                            title = habitName,
                            category = selectedCategory,
                            repeatDays = daysToSave,
                            targetMonths = targetMonths.toInt()
                        )
                    }
                }
                onSave()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitualTheme.components.buttonHeight),
            shape = RoundedCornerShape(HabitualTheme.radius.medium),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = habitName.isNotBlank()
        ) {
            Text(
                text = if (initialHabit == null)
                    stringResource(R.string.save_ritual)
                else
                    stringResource(R.string.update_ritual)
            )
        }
    }
}