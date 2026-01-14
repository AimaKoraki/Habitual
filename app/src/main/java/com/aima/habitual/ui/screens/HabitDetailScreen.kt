package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HabitDetailScreen(habitId: String?) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Habit Details")
        Text(text = "Viewing Habit ID: $habitId")
        // This is where your Form will go later
    }
}