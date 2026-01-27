package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.Habit
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * HabitForm handles the data entry for both creating and editing habits.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm(
    viewModel: HabitViewModel,
    initialHabit: Habit? = null, // Parameter to support editing existing habits
    onSave: () -> Unit
) {
    // 1. Form State: Initialized with existing habit data if present, otherwise defaults
    var habitName by remember { mutableStateOf(initialHabit?.title ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Health", "Study", "Personal", "Work", "Wellbeing")
    var selectedCategory by remember { mutableStateOf(initialHabit?.category ?: categories[0]) }

    // Slider state (Float) converted from the Int in our Model
    var targetMonths by remember { mutableFloatStateOf(initialHabit?.targetMonths?.toFloat() ?: 1f) }

    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    // Convert List from model to a Set for easier toggling in the UI
    var selectedDays by remember { mutableStateOf(initialHabit?.repeatDays?.toSet() ?: emptySet()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Field 1: Habit Name
        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text("Habit Name") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Drink Water") }
        )

        // Field 2: Category Dropdown (Exposed Dropdown Menu)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
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

        // Field 3: Circular Day Selectors (Repeat Days)
        Column {
            Text("Repeat on", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = selectedDays.contains(index)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF004D40) else Color.Transparent)
                            .border(1.dp, Color.LightGray, CircleShape)
                            .clickable {
                                // Toggle logic: Add if absent, remove if present
                                selectedDays = if (isSelected) selectedDays - index else selectedDays + index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color.Black,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Field 4: Target Duration Slider
        Column {
            Text(
                text = "Target Duration: ${targetMonths.toInt()} ${if (targetMonths.toInt() == 1) "Month" else "Months"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = targetMonths,
                onValueChange = { targetMonths = it },
                valueRange = 1f..12f,
                steps = 10,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF004D40),
                    thumbColor = Color(0xFF004D40)
                )
            )
        }

        // Field 5: Save/Update Button
        Button(
            onClick = {
                if (initialHabit == null) {
                    // Logic for Creating New Habit
                    val newHabit = Habit(
                        title = habitName,
                        category = selectedCategory,
                        repeatDays = selectedDays.toList(),
                        targetMonths = targetMonths.toInt()
                    )
                    viewModel.addHabit(newHabit)
                } else {
                    // Logic for Updating Existing Habit
                    val index = viewModel.habits.indexOfFirst { it.id == initialHabit.id }
                    if (index != -1) {
                        viewModel.habits[index] = initialHabit.copy(
                            title = habitName,
                            category = selectedCategory,
                            repeatDays = selectedDays.toList(),
                            targetMonths = targetMonths.toInt()
                        )
                    }
                }
                onSave() // Navigate back to Dashboard
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
            enabled = habitName.isNotBlank() // Validation: Prevent saving empty habits
        ) {
            Text(if (initialHabit == null) "Save Ritual" else "Update Ritual")
        }
    }
}