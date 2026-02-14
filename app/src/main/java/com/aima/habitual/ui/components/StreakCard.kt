package com.aima.habitual.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.screens.layout.StatsLayout
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * StreakCard: Displays the user's momentum.
 * Fully responsive to dark/light mode and driven by the HabitualTheme token system.
 */
@Composable
fun StreakCard(streakCount: Int) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        // JSON Token: "card.radius": 20
        shape = RoundedCornerShape(HabitualTheme.radius.xxl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        // Premium Shadow/Border Logic
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) HabitualTheme.elevation.none else HabitualTheme.elevation.low
        ),
        border = if (isDark) BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = StatsLayout.streakCardPadding,
                    vertical = HabitualTheme.spacing.section
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md)
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = stringResource(R.string.streak_icon_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(HabitualTheme.components.iconXl)
            )

            Text(
                text = stringResource(R.string.current_streak),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = stringResource(R.string.days_count, streakCount),
                style = MaterialTheme.typography.displayMedium,
                // Notice we removed FontWeight.Bold here!
                // It now relies entirely on the 600 weight defined in your Type.kt
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}