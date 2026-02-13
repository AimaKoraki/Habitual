package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val isEveryDaySelected = selectedDays.size == 7

    // Helper for Section Labels (14sp, Medium, Muted)
    @Composable
    fun SectionLabel(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.medium)
            ),
            modifier = Modifier.padding(bottom = HabitualTheme.spacing.md) // 12dp spacing (Rhythm)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(HabitualTheme.spacing.lg) 
            // Theme surface is used by parent Surface/Scaffold usually. 
            // If explicit warmth needed: .background(MaterialTheme.colorScheme.surface)
    ) {
        // --- HIERARCHY ---
        // 1. Subtle Back Label / Eyebrow
        Text(
            text = stringResource(R.string.add_habit_title).uppercase(), // "ADD HABIT"
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        )
        
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs)) // Small gap

        // 2. Dominant Title
        Text(
            text = if (initialHabit == null) stringResource(R.string.add_habit_header) else stringResource(R.string.edit_habit_header),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // 32dp (Section Gap)

        // --- FIELD 1: NAME ---
        SectionLabel(stringResource(R.string.habit_name_label))
        
        TextField(
            value = habitName,
            onValueChange = { habitName = it },
            placeholder = { Text(stringResource(R.string.habit_name_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.inputBorder),
                    shape = RoundedCornerShape(HabitualTheme.radius.input) // 20dp
                ),
            shape = RoundedCornerShape(HabitualTheme.radius.input),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl)) // 20dp (Input Gap)

        // --- FIELD 2: CATEGORY (Refined Dropdown) ---
        SectionLabel(stringResource(R.string.category_label))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.inputBorder),
                        shape = RoundedCornerShape(HabitualTheme.radius.input)
                    ),
                shape = RoundedCornerShape(HabitualTheme.radius.input),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .clip(RoundedCornerShape(HabitualTheme.radius.medium)) // Rounded Menu
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = category, 
                                modifier = Modifier.padding(vertical = 4.dp) // More vertical padding
                            ) 
                        },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // 32dp (Section Gap)

        // --- FIELD 3: DAYS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label with Rhythm
            Column {
                SectionLabel(stringResource(R.string.repeat_on_label))
            }
            
            // "Every Day" Toggle
            TextButton(
                onClick = {
                    if (isEveryDaySelected) selectedDays = emptySet() else selectedDays = (0..6).toSet() 
                }
            ) {
                Text(stringResource(R.string.every_day), color = MaterialTheme.colorScheme.primary)
            }
        }

        // Day Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayLabels.forEachIndexed { index, label ->
                val isSelected = selectedDays.contains(index)
                Box(
                    modifier = Modifier
                        .size(HabitualTheme.components.chipSize) // 40dp
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { selectedDays = if (isSelected) selectedDays - index else selectedDays + index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // 32dp

        // --- FIELD 4: DURATION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(stringResource(R.string.target_duration_label_only))
            Text(
                text = "${targetMonths.toInt()} Months",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = targetMonths,
            onValueChange = { targetMonths = it },
            valueRange = 1f..12f,
            steps = 10,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                thumbColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(vertical = HabitualTheme.spacing.sm)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // 32dp

        // --- ACTION BUTTON ---
        Button(
            onClick = {
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
            shape = RoundedCornerShape(HabitualTheme.radius.medium), // 16dp
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = habitName.isNotBlank()
        ) {
            Text(
                text = if (initialHabit == null) stringResource(R.string.save_ritual) else stringResource(R.string.update_ritual),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}