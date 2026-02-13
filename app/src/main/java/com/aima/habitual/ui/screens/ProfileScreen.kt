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
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    viewModel: HabitViewModel,
    onLogout: () -> Unit // <--- ADDED: Callback for logout logic
) {
    var showHabitsSheet by remember { mutableStateOf(false) }

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
        Spacer(modifier = Modifier.height(HabitualTheme.components.iconLarge))

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
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
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
                        modifier = Modifier.size(HabitualTheme.components.profileImageSmall),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Edit Badge (Camera Icon)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = 4.dp, y = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(HabitualTheme.components.iconSmall)
                )
            }
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xxl))

        // --- 2. EDITABLE NAME SECTION ---
        if (isEditingName) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    modifier = Modifier.width(200.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.updateUserName(tempName)
                        isEditingName = false
                        focusManager.clearFocus()
                    })
                )
                IconButton(onClick = {
                    viewModel.updateUserName(tempName)
                    isEditingName = false
                    focusManager.clearFocus()
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.desc_save_name),
                        tint = MaterialTheme.colorScheme.primary
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(HabitualTheme.components.iconMedium)
                )
            }
        }

        // Space between Name and Mastery Level increased
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg)) 

        Text(
            text = stringResource(R.string.profile_mastery_level, level),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

        // Level Progress Bar
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(120.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$toNextLevel habits to Level ${level + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- 3. SETTINGS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = MaterialTheme.shapes.large
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
                            tint = MaterialTheme.colorScheme.primary
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
                            tint = MaterialTheme.colorScheme.primary
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
        // Satisfies the Logout requirement and fixes the missing parameter error
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitualTheme.components.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(HabitualTheme.radius.medium)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
            Text(
                text = stringResource(R.string.profile_log_out),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))
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
                    color = MaterialTheme.colorScheme.primary,
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
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = MaterialTheme.shapes.medium
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
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = habit.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
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