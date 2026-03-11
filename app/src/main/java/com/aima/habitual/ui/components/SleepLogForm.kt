package com.aima.habitual.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * A stateless form for logging sleep duration and quality.
 */
@Composable
fun SleepLogForm(
    initialDurationMinutes: Int = 0,
    initialQuality: String = "",
    onSave: (durationMinutes: Int, quality: String) -> Unit,
    onCancel: () -> Unit
) {
    var sleepDurationHours by remember { mutableFloatStateOf(initialDurationMinutes / 60f) }
    var selectedQuality by remember { mutableStateOf(initialQuality) }

    val qualityOptions = listOf("Poor", "Fair", "Good", "Excellent")
    val isSaveEnabled = sleepDurationHours > 0f && selectedQuality.isNotBlank()

    Column(
        modifier = Modifier.padding(HabitualTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.dialog_update_sleep), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
        
        Text(text = stringResource(R.string.dialog_sleep_prompt), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
        
        // Duration Display
        Text(
            text = String.format("%.1f Hours", sleepDurationHours), 
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
        
        // Duration Slider
        Slider(
            value = sleepDurationHours,
            onValueChange = { sleepDurationHours = it },
            valueRange = 0f..12f,
            steps = 23, // 0.5 hour increments between 0 and 12
            colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary)
        )
        
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))
        
        // Quality Chips
        Text(
            text = "Sleep Quality",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))
        
        // FlowRow approach (simulated with Row/Wrap if needed)
        // Since we know there are exactly 4 short options, a Row, or 2x2 grid, or scrollable Row. 
        // We'll use a Row with spacing, assuming it fits (or horizontal scroll).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
        ) {
            qualityOptions.forEach { quality ->
                val isSelected = selectedQuality == quality
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(HabitualTheme.components.chipSize)
                        .clip(RoundedCornerShape(HabitualTheme.radius.sm))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                        )
                        .clickable { selectedQuality = quality },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quality,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
        
        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { 
                Text(stringResource(R.string.btn_cancel)) 
            }
            Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
            Button(
                onClick = {
                    onSave((sleepDurationHours * 60).toInt(), selectedQuality)
                },
                enabled = isSaveEnabled
            ) { 
                Text(stringResource(R.string.btn_save)) 
            }
        }
    }
}
