package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.navigation.Screen
import com.aima.habitual.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryViewScreen(
    entryId: String,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. Find the entry. If not found (deleted), handle gracefully.
    val entry = viewModel.diaryEntries.find { it.id == entryId }

    if (entry == null) {
        // Fallback if entry is missing
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Entry not found")
            Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface, // Soft Sage
        topBar = {
            TopAppBar(
                title = { }, // Empty title for a cleaner look
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // 2. Edit Button - Navigates to the "Form" screen
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.DiaryDetail.createRoute(entry.id)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp).padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Entry", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp) // Wider reading margins
                .verticalScroll(rememberScrollState())
        ) {
            // 3. Date Header
            Text(
                text = entry.date,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Main Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.displaySmall, // Big, bold header
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Tags Display (Chips)
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(24.dp))

            // 6. The Content (Readable Body Text)
// 6. The Content (Readable Body Text)
            Text(
                text = entry.content,
                // FIX: Changed 'height' to 'lineHeight'
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = androidx.compose.ui.unit.TextUnit(1.6f, androidx.compose.ui.unit.TextUnitType.Em)
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}