package com.aima.habitual.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aima.habitual.R
import com.aima.habitual.ui.screens.layout.ProfileLayout
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    viewModel: HabitViewModel,
    onLogout: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    var showHabitsSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> 
            if (uri != null) {
                viewModel.updateProfileImage(uri)
            }
        }
    )

    // Name Editing State
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val habits = viewModel.habits
    val level = viewModel.currentLevel
    val progress = viewModel.levelProgress
    val toNextLevel = viewModel.habitsForNextLevel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HabitualTheme.spacing.lg)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(HabitualTheme.components.iconXl))

        // --- 1. AVATAR SECTION ---
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
        ) {
            Box(
                modifier = Modifier
                    .size(HabitualTheme.components.profileImage)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    // Premium: Subtle Avatar Ring
                    .border(HabitualTheme.components.borderMedium, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.profileImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(viewModel.profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.desc_profile_picture),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(ProfileLayout.profileImageSmall),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Edit Badge (Camera Icon)
            Box(
                modifier = Modifier
                    .size(HabitualTheme.components.editBadgeSize)
                    .offset(x = ProfileLayout.editBadgeOffset, y = ProfileLayout.editBadgeOffset)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(HabitualTheme.components.borderMedium, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(HabitualTheme.components.iconSm)
                )
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

        // --- 2. EDITABLE NAME SECTION ---
        if (isEditingName) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = {
                            tempName = it
                            nameError = false
                        },
                        singleLine = true,
                        isError = nameError,
                        modifier = Modifier.width(ProfileLayout.nameFieldWidth),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (tempName.trim().isBlank()) {
                                nameError = true
                            } else {
                                viewModel.updateUserName(tempName)
                                isEditingName = false
                                nameError = false
                                focusManager.clearFocus()
                            }
                        })
                    )
                    IconButton(onClick = {
                        if (tempName.trim().isBlank()) {
                            nameError = true
                        } else {
                            viewModel.updateUserName(tempName)
                            isEditingName = false
                            nameError = false
                            focusManager.clearFocus()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.desc_save_name),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (nameError) {
                    Text(
                        text = stringResource(R.string.profile_name_empty_error),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = HabitualTheme.spacing.xs)
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        tempName = viewModel.userName
                        isEditingName = true
                    }
                    .padding(HabitualTheme.spacing.sm)
            ) {
                Text(
                    text = viewModel.userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(HabitualTheme.spacing.sm))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.desc_edit_name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted),
                    modifier = Modifier.size(HabitualTheme.components.iconMd)
                )
            }
        }

        // Space between Name and Mastery Level increased
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg)) 

        Text(
            text = stringResource(R.string.profile_mastery_level, level),
            style = MaterialTheme.typography.titleLarge, // Bolder
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

        // Level Progress Bar
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(ProfileLayout.progressBarWidth)
                    .height(ProfileLayout.progressBarHeight)
                    .clip(RoundedCornerShape(HabitualTheme.radius.xs)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))
            Text(
                text = stringResource(R.string.profile_level_progress, toNextLevel, level + 1),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- 3. SETTINGS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(HabitualTheme.spacing.lg)) {

                // Theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                        Text(
                            text = stringResource(R.string.profile_dark_mode),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = HabitualTheme.spacing.lg), color = MaterialTheme.colorScheme.outlineVariant)

                // Habits List Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHabitsSheet = true }
                        .padding(vertical = HabitualTheme.spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                        Text(
                            text = stringResource(R.string.profile_my_rituals),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- 4. LOG OUT BUTTON ---
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitualTheme.components.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(HabitualTheme.radius.md)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
            Text(
                text = stringResource(R.string.profile_log_out),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

        // --- 5. DELETE ACCOUNT BUTTON ---
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitualTheme.components.buttonHeight),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(
                HabitualTheme.components.borderThin,
                MaterialTheme.colorScheme.error.copy(alpha = HabitualTheme.alpha.secondary)
            ),
            shape = RoundedCornerShape(HabitualTheme.radius.md)
        ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
            Text(
                text = stringResource(R.string.profile_delete_account),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.profile_delete_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.profile_delete_text),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteProfile()
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

    // --- HABITS SHEET ---
    if (showHabitsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHabitsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = HabitualTheme.spacing.lg)
                    .padding(bottom = HabitualTheme.spacing.section)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.profile_your_rituals),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = HabitualTheme.spacing.lg)
                )

                if (habits.isEmpty()) {
                    Text(stringResource(R.string.profile_no_rituals), style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                        contentPadding = PaddingValues(bottom = HabitualTheme.spacing.lg)
                    ) {
                        items(habits) { habit ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = MaterialTheme.shapes.medium,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(HabitualTheme.spacing.md).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                                        Column {
                                            Text(
                                                text = habit.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = habit.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteHabit(habit.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.desc_delete),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}