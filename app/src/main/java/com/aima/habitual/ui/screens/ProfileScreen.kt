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
import androidx.compose.ui.draw.shadow
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

/**
 * ProfileScreen: Manages user identity, theme preferences, and habit mastery progress.
 * Optimized for clear visual hierarchy and intuitive interaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    viewModel: HabitViewModel,
    onLogout: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    // 1. UI STATE: Manages visibility of overlays and temporary input data
    var showHabitsSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 2. PHOTO PICKER: Native Android contract for secure image selection
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateProfileImage(uri)
            }
        }
    )

    // Name Editing State Logic
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Mastery Data observed from the ViewModel
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

        // --- 3. AVATAR SECTION ---
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
        ) {
            Box(
                modifier = Modifier
                    .size(HabitualTheme.components.profileImage)
                    .shadow(4.dp, CircleShape) // Adds depth to the avatar
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    // Premium Detail: A subtle ring around the profile image for separation
                    .border(HabitualTheme.components.borderMedium, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.profileImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(viewModel.profileImageUri)
                            .crossfade(true) // Smooth transition after selection
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
            // Edit Badge: Anchored to the bottom-right of the avatar using ProfileLayout tokens
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

        // --- 4. EDITABLE NAME SECTION ---
        // Toggle between static Text and OutlinedTextField based on isEditingName state
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
                        shape = RoundedCornerShape(HabitualTheme.radius.xl),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle),
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (tempName.trim().isBlank()) {
                                nameError = true
                            } else {
                                viewModel.updateUserName(tempName)
                                isEditingName = false
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
                            focusManager.clearFocus()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Check, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
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

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        // --- 5. GAMIFICATION: Mastery Level & Progress ---
        Text(
            text = stringResource(R.string.profile_mastery_level, level),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(ProfileLayout.progressBarWidth)
                    .height(ProfileLayout.progressBarHeight)
                    .clip(RoundedCornerShape(HabitualTheme.radius.xs)),
                color = MaterialTheme.colorScheme.primary, // Forest Green fill
                trackColor = MaterialTheme.colorScheme.surfaceVariant, // Soft Sage track
            )
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))
            Text(
                text = stringResource(R.string.profile_level_progress, toNextLevel, level + 1),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- 6. SETTINGS CARD: Theme and Quick Actions ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(HabitualTheme.spacing.lg)) {
                // Theme Toggle: Real-time Dark/Light mode switching
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

                // Ritual Management: Triggers the ModalBottomSheet
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
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- 7. DESTRUCTIVE ACTIONS: Logout and Account Deletion ---
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(HabitualTheme.components.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(HabitualTheme.radius.md)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
            Text(text = stringResource(R.string.profile_log_out), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth().height(HabitualTheme.components.buttonHeight),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = androidx.compose.foundation.BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(HabitualTheme.radius.md)
        ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
            Text(text = stringResource(R.string.profile_delete_account), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
    }

    // --- DIALOGS & OVERLAYS ---

    // Deletion Warning: Critical confirmation before permanent data loss
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.profile_delete_title)) },
            text = { Text(stringResource(R.string.profile_delete_text)) },
            confirmButton = {
                TextButton(onClick = { onDeleteProfile() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    // Habits Bottom Sheet: Provides a secondary view for managing the ritual library
    if (showHabitsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHabitsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(HabitualTheme.spacing.lg).padding(bottom = HabitualTheme.spacing.section).fillMaxWidth()) {
                Text(text = stringResource(R.string.profile_your_rituals), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = HabitualTheme.spacing.lg))

                if (habits.isEmpty()) {
                    Text(stringResource(R.string.profile_no_rituals), style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm), contentPadding = PaddingValues(bottom = HabitualTheme.spacing.lg)) {
                        items(habits) { habit ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(HabitualTheme.spacing.md).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = habit.title, style = MaterialTheme.typography.titleMedium)
                                        Text(text = habit.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                    IconButton(onClick = { viewModel.deleteHabit(habit.id) }) {
                                        Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = null)
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