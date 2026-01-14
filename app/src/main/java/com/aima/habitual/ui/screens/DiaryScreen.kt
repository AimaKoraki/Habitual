package com.aima.habitual.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiaryScreen() {
    // State to trigger animation
    var visible by remember { mutableStateOf(false) }

    // Launch animation when screen opens
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        // High Mark Requirement: Micro-interaction animation
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text("Your Thoughts", style = MaterialTheme.typography.headlineMedium)

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(5) { index ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text("Diary Entry #${index + 1}") },
                                supportingContent = { Text("Today I completed all my habits! Feeling great.") },
                                trailingContent = { Text("Jan 14") }
                            )
                        }
                    }
                }
            }
        }
    }
}