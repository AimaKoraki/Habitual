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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.theme.HabitualTheme
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
            Text(stringResource(R.string.diary_entry_not_found_label))
            Button(onClick = { navController.popBackStack() }) { Text(stringResource(R.string.btn_go_back)) }
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
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.desc_back))
                    }
                },
                actions = {
                    // 2. Edit Button - Navigates to the "Form" screen
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.DiaryDetail.createRoute(entry.id)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = androidx.compose.foundation.shape.CircleShape, // Standardized
                        modifier = Modifier.size(HabitualTheme.components.fabSize),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = HabitualTheme.components.fabElevation,
                            pressedElevation = HabitualTheme.components.fabPressedElevation
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.desc_edit_entry), modifier = Modifier.size(HabitualTheme.components.iconMedium))
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
                .padding(horizontal = HabitualTheme.spacing.xxl) // Wider reading margins
                .verticalScroll(rememberScrollState())
        ) {
            // 3. Date Header
            Text(
                text = entry.date,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = HabitualTheme.alpha.secondary)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

            // 4. Main Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.displaySmall, // Big, bold header
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // 5. Tags Display (Chips)
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
                ) {
                    entry.tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(HabitualTheme.radius.tag)
                        ) {
                            Text(
                                text = stringResource(R.string.tag_prefix, tag),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.tagVertical)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xxl))
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = HabitualTheme.alpha.muted))

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xxl))

            // 6. The Content (Readable Body Text)
// 6. The Content (Readable Body Text)
            Text(
                text = entry.content,
                // FIX: Changed 'height' to 'lineHeight'
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = androidx.compose.ui.unit.TextUnit(1.6f, androidx.compose.ui.unit.TextUnitType.Em)
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = HabitualTheme.alpha.bodyText)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.components.iconLarge))
        }
    }
}