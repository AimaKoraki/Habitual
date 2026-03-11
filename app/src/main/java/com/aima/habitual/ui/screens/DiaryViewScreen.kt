package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.DiaryTag
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryViewScreen(
    entryId: String,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    // 1. Find the entry. If not found (deleted), handle gracefully.
    val entry = viewModel.diaryEntries.find { it.id == entryId }

    // State for delete dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (entry == null) {
        // Fallback if entry is missing
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.diary_entry_not_found_label))
            Button(onClick = { navController.popBackStack() }) { Text(stringResource(R.string.btn_go_back)) }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Base background
        topBar = {
            TopAppBar(
                title = { }, // Empty title for a cleaner look
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.desc_back))
                    }
                },
                actions = {
                    // 2. Delete Button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.desc_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // 3. Edit Button - Reduce size from FAB to standard IconButton for balance
                    IconButton(
                        onClick = { navController.navigate(Screen.DiaryDetail.createRoute(entry.id)) },
                        modifier = Modifier
                            .padding(end = HabitualTheme.spacing.sm)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = stringResource(R.string.desc_edit_entry), 
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent // Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .wavePattern(MaterialTheme.colorScheme.primary) // Apply pattern here
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = HabitualTheme.spacing.xl) // Wider reading margins
            ) {
            // 3. Date Header
            val displayDate = remember(entry.timestamp) {
                java.time.Instant.ofEpochMilli(entry.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy • h:mm a"))
            }
            Text(
                text = displayDate,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

            // 4. Main Title & Mood
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold), 
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (entry.mood != null) {
                    Text(
                        text = entry.mood,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(start = HabitualTheme.spacing.md)
                    )
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // 5. Tags Display (Chips) & Meta Metadata
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
                ) {
                    entry.tags.forEach { tag ->
                        // Inline bright chip implementation replacing DiaryTag for better visibility mapping
                        Surface(
                            shape = RoundedCornerShape(HabitualTheme.radius.md),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.sm)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
            }

            // Word Count / Reading Time Estimate
            val wordCount = entry.content.split(Regex("\\s+")).count { it.isNotBlank() }
            val readTimeMins = maxOf(1, wordCount / 200) // Avg reading speed 200 wpm
            Text(
                text = "$wordCount words • $readTimeMins min read",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = HabitualTheme.alpha.subtle))
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

            // 6. The Content (Readable Body Text)
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp, // Improved line spacing for readability
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) // Softer contrast
            )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
            
            // 7. Real Attachments (conditional based on entry data)
            val hasAnyAttachment = entry.photoUri != null || entry.audioFilePath != null || entry.locationText != null
            if (hasAnyAttachment) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = HabitualTheme.alpha.subtle))
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
                Text(
                    text = "Attached Media",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
                
                Column(verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md)) {
                    // Photo Attachment
                    entry.photoUri?.let { uriStr ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(HabitualTheme.radius.lg))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            // Display attached photo label with icon
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(HabitualTheme.spacing.sm))
                                Text("Photo attached", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Audio Attachment with Playback
                    entry.audioFilePath?.let { filePath ->
                        val context = LocalContext.current
                        var isPlaying by remember { mutableStateOf(false) }
                        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
                        
                        // Cleanup player on dispose
                        androidx.compose.runtime.DisposableEffect(filePath) {
                            onDispose {
                                mediaPlayer?.release()
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(HabitualTheme.radius.lg),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(HabitualTheme.spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            mediaPlayer?.pause()
                                            isPlaying = false
                                        } else {
                                            if (mediaPlayer == null) {
                                                mediaPlayer = MediaPlayer().apply {
                                                    setDataSource(filePath)
                                                    prepare()
                                                    setOnCompletionListener {
                                                        isPlaying = false
                                                    }
                                                }
                                            }
                                            mediaPlayer?.start()
                                            isPlaying = true
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(Modifier.width(HabitualTheme.spacing.sm))
                                Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(HabitualTheme.spacing.sm))
                                Text("Voice Note", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }

                    // Location Attachment
                    entry.locationText?.let { loc ->
                        Surface(
                            shape = RoundedCornerShape(HabitualTheme.radius.md),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(Modifier.width(HabitualTheme.spacing.sm))
                                Text(loc, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))
        }

        // --- DELETE DIALOG ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = stringResource(R.string.dialog_delete_entry_title)) },
                text = { Text(text = stringResource(R.string.dialog_delete_entry_text)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDiaryEntry(entry.id)
                            showDeleteDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )

                    ) {
                        Text(stringResource(R.string.btn_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    }
        }
}