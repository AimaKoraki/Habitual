package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm() {
    var habitName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Health", "Study", "Personal", "Work")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var dailyGoal by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Field 1: Text Field (Input for Habit Name)
        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text("Habit Name") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Read for 30 mins") }
        )

        // Field 2: Exposed Dropdown Menu (Category Selection)
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
                modifier = Modifier.menuAnchor().fillMaxWidth()
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

        // Field 3: Slider (Numerical Goal)
        Column {
            Text(text = "Daily Goal: ${dailyGoal.toInt()} times", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = dailyGoal,
                onValueChange = { dailyGoal = it },
                valueRange = 1f..10f,
                steps = 9
            )
        }

        Button(
            onClick = { /* No action needed for assignment submission */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Habit")
        }
    }
}