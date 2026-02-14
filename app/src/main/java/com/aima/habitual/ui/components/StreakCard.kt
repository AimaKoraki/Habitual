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
 * StreakCard: A specialized visualization component that displays the user's momentum.
 * This component is fully responsive to Dark/Light mode and is driven by the
 * HabitualTheme design token system for maximum consistency.
 */
@Composable
fun StreakCard(streakCount: Int) {
    // 1. THEME ADAPTATION:
    // We check the system theme to toggle between Elevation (Light) and Borders (Dark).
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        // JSON Token: "card.radius": 20 (Defined in HabitualTheme.radius.xxl)
        shape = RoundedCornerShape(HabitualTheme.radius.xxl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        // 2. PREMIUM DEPTH LOGIC:
        // In Light Mode, we use a subtle low elevation shadow.
        // In Dark Mode, we remove shadows and use a thin 'outlineVariant' border to
        // define the card shape against the dark background.
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) HabitualTheme.elevation.none else HabitualTheme.elevation.low
        ),
        border = if (isDark) BorderStroke(
            width = HabitualTheme.components.borderThin,
            color = MaterialTheme.colorScheme.outlineVariant
        ) else null
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = StatsLayout.streakCardPadding,
                    vertical = HabitualTheme.spacing.section // 32dp vertical rhythm
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md)
        ) {
            // 3. ICONOGRAPHY:
            // Uses the specialized 'StreakFire' color defined in your Theme.kt
            // to ensure the flame icon pops across all backgrounds.
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = stringResource(R.string.streak_icon_desc),
                tint = com.aima.habitual.ui.theme.StreakFire,
                modifier = Modifier.size(HabitualTheme.components.iconXl)
            )

            // 4. TEXT HIERARCHY:
            // Secondary label to provide context to the large number below.
            Text(
                text = stringResource(R.string.current_streak),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 5. STREAK COUNTER:
            // Utilizes 'displayMedium' which leverages the 600-weight semi-bold font
            // defined in your Type.kt. No hardcoded bolding is required.
            Text(
                text = stringResource(R.string.days_count, streakCount),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}