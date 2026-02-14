package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * HabitForm: A comprehensive input form for creating or editing rituals.
 * It utilizes local state for draft management and integrates with the ViewModel for persistence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm(
    viewModel: HabitViewModel,
    initialHabit: Habit? = null, // If provided, the form enters "Edit Mode"
    onSave: () -> Unit           // Callback to handle navigation after a successful save
) {
    // 1. FORM STATE MANAGEMENT:
    // Tracks user input before it is committed to the database.
    var habitName by remember { mutableStateOf(initialHabit?.title ?: "") }
    var expanded by remember { mutableStateOf(false) } // State for Category Dropdown

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

    /** Helper: SectionLabel creates consistent, accessible subtitles for form sections. */
    @Composable
    fun SectionLabel(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
            ),
            modifier = Modifier.padding(bottom = HabitualTheme.spacing.md)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(HabitualTheme.spacing.lg)
    ) {
        // --- VISUAL HIERARCHY: HEADER ---
        // Uses an 'Eyebrow' label for a premium, structured look.
        Text(
            text = stringResource(R.string.add_habit_title).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))

        Text(
            text = if (initialHabit == null) stringResource(R.string.add_habit_header) else stringResource(R.string.edit_habit_header),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- FIELD 1: HABIT NAME ---
        SectionLabel(stringResource(R.string.habit_name_label))

        TextField(
            value = habitName,
            onValueChange = { habitName = it },
            placeholder = { Text(stringResource(R.string.habit_name_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)) },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = HabitualTheme.components.borderThin,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle),
                    shape = RoundedCornerShape(HabitualTheme.radius.xl) // Custom pill-shape
                ),
            shape = RoundedCornerShape(HabitualTheme.radius.xl),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent, // Removes the standard bottom line for a cleaner look
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

        // --- FIELD 2: CATEGORY SELECTION ---
        SectionLabel(stringResource(R.string.category_label))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true, // Prevents keyboard entry to ensure data integrity
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .border(
                        width = HabitualTheme.components.borderThin,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle),
                        shape = RoundedCornerShape(HabitualTheme.radius.xl)
                    ),
                shape = RoundedCornerShape(HabitualTheme.radius.xl),
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
                    .clip(RoundedCornerShape(HabitualTheme.radius.md))
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = category,
                                modifier = Modifier.padding(vertical = HabitualTheme.spacing.xs)
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

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- FIELD 3: REPEAT SCHEDULE (Day Selection) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(stringResource(R.string.repeat_on_label))

            // "Every Day" Quick-Select Toggle
            TextButton(
                onClick = {
                    selectedDays = if (isEveryDaySelected) emptySet() else (0..6).toSet()
                }
            ) {
                Text(stringResource(R.string.every_day), color = MaterialTheme.colorScheme.primary)
            }
        }

        // Custom Selection Chips for Days of the Week
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
                            if (isSelected) MaterialTheme.colorScheme.primary // Forest Green
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                        )
                        .clickable {
                            selectedDays = if (isSelected) selectedDays - index else selectedDays + index
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- FIELD 4: DURATION (Target Months) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(stringResource(R.string.target_duration_label_only))
            Text(
                text = "${targetMonths.toInt()} Months",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
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

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- SUBMIT ACTION ---
        Button(
            onClick = {
                // Default to "Every Day" if no specific days are selected
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
                    viewModel.updateHabit(
                        initialHabit.copy(
                            title = habitName,
                            category = selectedCategory,
                            repeatDays = daysToSave,
                            targetMonths = targetMonths.toInt()
                        )
                    )
                }
                onSave()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitualTheme.components.buttonHeight),
            shape = RoundedCornerShape(HabitualTheme.radius.md),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            // Input Validation: Ensures a habit cannot be saved without a name
            enabled = habitName.isNotBlank()
        ) {
            Text(
                text = if (initialHabit == null) stringResource(R.string.save_ritual) else stringResource(R.string.update_ritual),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}