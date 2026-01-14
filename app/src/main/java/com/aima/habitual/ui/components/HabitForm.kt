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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm(viewModel: HabitViewModel, onSave: () -> Unit) {
    // Form State
    var habitName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Health", "Study", "Personal", "Work", "Wellbeing")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var targetMonths by remember { mutableFloatStateOf(1f) }

    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text("Habit Name") },
            modifier = Modifier.fillMaxWidth()
        )

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
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = { selectedCategory = category; expanded = false }
                    )
                }
            }
        }

        Column {
            Text("Repeat on", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = selectedDays.contains(index)
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(if (isSelected) Color(0xFF004D40) else Color.Transparent)
                            .border(1.dp, Color.LightGray, CircleShape)
                            .clickable { selectedDays = if (isSelected) selectedDays - index else selectedDays + index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (isSelected) Color.White else Color.Black)
                    }
                }
            }
        }

        Column {
            Text("Target Duration: ${targetMonths.toInt()} Months")
            Slider(
                value = targetMonths,
                onValueChange = { targetMonths = it },
                valueRange = 1f..12f,
                steps = 10,
                colors = SliderDefaults.colors(activeTrackColor = Color(0xFF004D40), thumbColor = Color(0xFF004D40))
            )
        }

        Button(
            onClick = {
                // Logic: Create object and Save to ViewModel
                val newHabit = Habit(
                    title = habitName,
                    category = selectedCategory,
                    repeatDays = selectedDays.toList(),
                    targetMonths = targetMonths.toInt()
                )
                viewModel.addHabit(newHabit)
                onSave() // Close the screen
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40))
        ) {
            Text("Save Ritual")
        }
    }
}