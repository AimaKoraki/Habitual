package com.aima.habitual.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * DiaryHeader: The primary header for the Diary screen.
 * It manages the title display and provides a trigger for the sorting menu.
 */
@Composable
fun DiaryHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = HabitualTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. SCREEN TITLE:
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 2. ACTION SLOT:
        // Allows the parent screen to inject context-specific actions (e.g., Sort Menu).
        action()
    }
}