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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm() {
    var habitName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Health", "Study", "Personal", "Work", "Wellbeing")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // Updated: State for Target Months and Circular Day Selectors
    var targetMonths by remember { mutableFloatStateOf(1f) }
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Field 1: Text Field (Habit Name)
        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text("Habit Name") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Drink Water") }
        )

        // Field 2: Exposed Dropdown Menu (Fixed Deprecation)
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

        // Field 3: NEW Circular Day Selectors (Repeat Section)
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

        // Field 4: Slider (Target Duration in Months)
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

        Button(
            onClick = { /* Action handled in parent screen */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40))
        ) {
            Text("Save Ritual")
        }
    }
}