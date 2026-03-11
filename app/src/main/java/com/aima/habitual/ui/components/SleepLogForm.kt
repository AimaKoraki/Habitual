package com.aima.habitual.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

// ------------------------------------------------------------------
// Darkness Score thresholds (lux)
// > 5 lux → room is too bright for deep sleep (nudge shown)
// 0–50 lux maps the full score range (50+ lux = 0/100, 0 lux = 100/100)
// ------------------------------------------------------------------
private const val LUX_NUDGE_THRESHOLD = 5f          // lux: show the banner above this
private const val LUX_FULL_BRIGHTNESS = 50f          // lux: score bottoms out here

/**
 * A stateless form for logging sleep duration, quality, and ambient room brightness.
 *
 * @param currentLux  Live lux reading from the ambient light sensor; used to compute
 *                    the Darkness Score and optionally show the sleep hygiene nudge.
 */
@Composable
fun SleepLogForm(
    initialDurationMinutes: Int = 0,
    initialQuality: String = "",
    currentLux: Float = 0f,
    onSave: (durationMinutes: Int, quality: String) -> Unit,
    onCancel: () -> Unit
) {
    var sleepDurationHours by remember { mutableFloatStateOf(initialDurationMinutes / 60f) }
    var selectedQuality by remember { mutableStateOf(initialQuality) }

    val qualityOptions = listOf("Poor", "Fair", "Good", "Excellent")
    val isSaveEnabled = sleepDurationHours > 0f && selectedQuality.isNotBlank()

    // --- Darkness Score computation ---
    // Maps lux to a 0-100 score: 0 lux → 100 (pitch dark), 50+ lux → 0 (too bright)
    val darknessScore = ((1f - (currentLux / LUX_FULL_BRIGHTNESS).coerceIn(0f, 1f)) * 100).toInt()
    val isTooBright = currentLux > LUX_NUDGE_THRESHOLD

    // Animate the score colour: green ≥ 80, amber 40–79, red < 40
    val scoreColor by animateColorAsState(
        targetValue = when {
            darknessScore >= 80 -> Color(0xFF4CAF50)   // Forest green
            darknessScore >= 40 -> Color(0xFFFFA726)   // Amber
            else                -> Color(0xFFEF5350)   // Soft red
        },
        animationSpec = tween(600),
        label = "scoreColor"
    )

    // Animate progress from 0 → darknessScore/100 on first composition
    val animatedProgress by animateFloatAsState(
        targetValue = darknessScore / 100f,
        animationSpec = tween(900),
        label = "darknessProgress"
    )

    Column(
        modifier = Modifier.padding(HabitualTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.dialog_update_sleep), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

        Text(text = stringResource(R.string.dialog_sleep_prompt), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // ── Duration Display ─────────────────────────────────────
        Text(
            text = String.format("%.1f Hours", sleepDurationHours),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

        // ── Duration Slider ──────────────────────────────────────
        Slider(
            value = sleepDurationHours,
            onValueChange = { sleepDurationHours = it },
            valueRange = 0f..12f,
            steps = 23, // 0.5 hour increments between 0 and 12
            colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        // ── Quality Chips ────────────────────────────────────────
        Text(
            text = "Sleep Quality",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

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

        // ── Darkness Score Card ──────────────────────────────────
        // Shows the ambient light reading as a bedroom-readiness score.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(HabitualTheme.radius.lg),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = HabitualTheme.elevation.none)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HabitualTheme.spacing.lg, vertical = HabitualTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.lg)
            ) {
                // Circular progress ring showing darkness score
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        strokeWidth = 6.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(64.dp),
                        color = scoreColor,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "$darknessScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }

                // Score description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Darkness Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            darknessScore >= 80 -> "Great for sleep 🌑"
                            darknessScore >= 40 -> "Could be darker 🌒"
                            else               -> "Too bright for deep sleep ☀️"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format("%.1f lux", currentLux),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Nudge Banner (only when room is too bright) ──────────
        if (isTooBright) {
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(HabitualTheme.radius.md),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8E1) // Warm amber tint
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = HabitualTheme.elevation.none)
            ) {
                Row(
                    modifier = Modifier.padding(HabitualTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbTwilight,
                        contentDescription = null,
                        tint = Color(0xFFE65100), // Deep amber
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Your room is a bit bright for deep sleep. " +
                                "Want to dim the lights for better melatonin production?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4E2600) // Dark amber text for contrast
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // ── Action Buttons ───────────────────────────────────────
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
