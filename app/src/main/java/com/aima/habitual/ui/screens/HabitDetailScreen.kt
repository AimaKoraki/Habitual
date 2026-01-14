package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(habitId: String?, navController: NavHostController) {
    // Form States
    var title by remember { mutableStateOf("") }
    var targetMonths by remember { mutableFloatStateOf(1f) }

    // Dropdown States
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Health", "Study", "Personal", "Work", "Wellbeing")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // Circular Day Selector States
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == "new") "Add Habit" else "Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Habit Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Habit Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Category Dropdown (Exposed Dropdown Menu)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true, // Prevents keyboard from appearing
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
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Repeat Days (Circular Selector)
            Text("Repeat on", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = selectedDays.contains(index)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Target Months Slider
            Text(
                text = "Target Duration: ${targetMonths.toInt()} Months",
                style = MaterialTheme.typography.bodyLarge
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

            Spacer(modifier = Modifier.height(32.dp))

            // Save Action
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40))
            ) {
                Text("Save Habit")
            }
        }
    }
}