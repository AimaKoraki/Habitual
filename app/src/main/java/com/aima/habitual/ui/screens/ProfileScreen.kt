package com.aima.habitual.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.runtime.collectAsState
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
import com.aima.habitual.ui.theme.AppTheme
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.ui.components.QuoteCard
import com.aima.habitual.utils.DriveBackupManager
import com.aima.habitual.viewmodel.HabitViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import androidx.core.content.FileProvider

/**
 * ProfileScreen: Manages user identity, theme preferences, and habit mastery progress.
 * Optimized for clear visual hierarchy and intuitive interaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    appTheme: AppTheme,
    onThemeChange: (Boolean) -> Unit,
    onThemeColorChange: (AppTheme) -> Unit,
    viewModel: HabitViewModel,
    onLogout: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    // 1. UI STATE: Manages visibility of overlays and temporary input data
    var showHabitsSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    // 2. PHOTO PICKER: Native Android contract for secure image selection
    var showPhotoOptions by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateProfileImage(uri)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.updateProfileImage(tempPhotoUri!!)
        }
    }

    // Name Editing State Logic
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Mastery Data observed from the ViewModel
    // Subscribe to records to force recomposition when gamification stats change
    val records by viewModel.records.collectAsState()
    val habits by viewModel.habits.collectAsState()
    
    val level = viewModel.currentLevel
    val habitsForNextLevel = viewModel.habitsForNextLevel
    val progress = viewModel.levelProgress
    val toNextLevel = viewModel.habitsForNextLevel

    // --- DRIVE BACKUP STATE ---
    val context = LocalContext.current
    val backupState = viewModel.backupState
    val lastBackupTime = viewModel.lastBackupTime

    // Track whether the pending Drive action is backup (true) or restore (false)
    var pendingDriveAction by remember { mutableStateOf(true) }

    // ActivityResultLauncher for Drive Google Sign-In authorization
    val driveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                viewModel.handleDriveSignInResult(account, isBackup = pendingDriveAction)
            } catch (e: Exception) {
                viewModel.handleDriveSignInResult(null, isBackup = pendingDriveAction)
            }
        } else {
            viewModel.handleDriveSignInResult(null, isBackup = pendingDriveAction)
        }
    }

    /**
     * Initiates Drive authorization — checks for existing permission first,
     * otherwise launches the Google Sign-In activity.
     */
    fun startDriveAction(isBackup: Boolean) {
        pendingDriveAction = isBackup
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && DriveBackupManager.isAuthorized(context)) {
            // Already authorized — proceed directly
            viewModel.handleDriveSignInResult(account, isBackup)
        } else {
            // Need authorization — launch sign-in
            val client = GoogleSignIn.getClient(context, DriveBackupManager.getSignInOptions())
            driveSignInLauncher.launch(client.signInIntent)
        }
    }

    // Refresh last backup time on screen load
    LaunchedEffect(Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && DriveBackupManager.isAuthorized(context)) {
            viewModel.refreshLastBackupTime(account)
        }
        // Fetch the daily quote when the Profile screen is opened
        viewModel.fetchDailyQuote()
    }

    // Snackbar for backup/restore feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is HabitViewModel.BackupState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearBackupState()
            }
            is HabitViewModel.BackupState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearBackupState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
            .padding(
                start = HabitualTheme.spacing.lg,
                end = HabitualTheme.spacing.lg,
                top = 0.dp
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 3. AVATAR SECTION ---
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable { showPhotoOptions = true }
        ) {
            Box(
                modifier = Modifier
                    .size(HabitualTheme.components.profileImage)
                    .shadow(HabitualTheme.elevation.medium, CircleShape) // Adds depth to the avatar
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    // Premium Detail: A subtle ring around the profile image for separation
                    .border(HabitualTheme.components.borderMedium, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(viewModel.profileImageUri ?: R.drawable.profile_placeholder)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.desc_profile_picture),
                    placeholder = androidx.compose.ui.res.painterResource(R.drawable.profile_placeholder),
                    error = androidx.compose.ui.res.painterResource(R.drawable.profile_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
            val nameInputDesc = stringResource(R.string.desc_name_input)
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
                        modifier = Modifier
                            .width(ProfileLayout.nameFieldWidth)
                            .semantics { contentDescription = nameInputDesc },
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
                        Icon(imageVector = Icons.Default.Check, tint = MaterialTheme.colorScheme.primary, contentDescription = stringResource(R.string.desc_save_name))
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

        // --- DAILY QUOTE: Online API / Offline JSON fallback ---
        QuoteCard(
            quote = viewModel.dailyQuote,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        // --- 6. SETTINGS CARD: Theme and Quick Actions ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
            border = androidx.compose.foundation.BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

                // Theme Color Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                        Text(
                            text = "Theme Color",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md)) {
                        val greenColor = Color(0xFF18463A)
                        val redColor = Color(0xFFD32F2F)

                        // Green Scheme Chip
                        val isGreen = appTheme == AppTheme.GREEN
                        Box(
                            modifier = Modifier
                                .size(HabitualTheme.components.themeSwatchOuter)
                                .clip(CircleShape)
                                .background(if (isGreen) greenColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = if (isGreen) 0.dp else HabitualTheme.components.borderThin,
                                    color = if (isGreen) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable { onThemeColorChange(AppTheme.GREEN) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(HabitualTheme.components.themeSwatchInner).clip(CircleShape).background(greenColor))
                        }

                        // Red Scheme Chip
                        val isRed = appTheme == AppTheme.RED
                        Box(
                            modifier = Modifier
                                .size(HabitualTheme.components.themeSwatchOuter)
                                .clip(CircleShape)
                                .background(if (isRed) redColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = if (isRed) 0.dp else HabitualTheme.components.borderThin,
                                    color = if (isRed) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable { onThemeColorChange(AppTheme.RED) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(HabitualTheme.components.themeSwatchInner).clip(CircleShape).background(redColor))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = HabitualTheme.spacing.lg), color = MaterialTheme.colorScheme.outlineVariant)

                // Biometric Login Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                        Text(
                            text = stringResource(R.string.profile_biometric_login),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometricLogin(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = HabitualTheme.spacing.lg), color = MaterialTheme.colorScheme.outlineVariant)

                // --- GOOGLE DRIVE BACKUP SECTION ---
                Text(
                    text = stringResource(R.string.backup_section_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary),
                    modifier = Modifier.padding(bottom = HabitualTheme.spacing.sm)
                )

                // Last backup timestamp
                Text(
                    text = if (lastBackupTime != null) {
                        val formatted = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
                            .format(Date(lastBackupTime!!))
                        stringResource(R.string.backup_last_time, formatted)
                    } else {
                        stringResource(R.string.backup_never)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted),
                    modifier = Modifier.padding(bottom = HabitualTheme.spacing.md)
                )

                val isInProgress = backupState is HabitViewModel.BackupState.InProgress

                // Backup Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HabitualTheme.radius.md))
                        .clickable(enabled = !isInProgress) { startDriveAction(isBackup = true) }
                        .padding(vertical = HabitualTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                    Text(
                        text = if (isInProgress && pendingDriveAction)
                            stringResource(R.string.backup_in_progress)
                        else
                            stringResource(R.string.backup_to_drive),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (isInProgress && pendingDriveAction) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(HabitualTheme.components.iconMd),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xs))

                // Restore Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HabitualTheme.radius.md))
                        .clickable(enabled = !isInProgress) { showRestoreDialog = true }
                        .padding(vertical = HabitualTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(HabitualTheme.spacing.lg))
                    Text(
                        text = if (isInProgress && !pendingDriveAction)
                            stringResource(R.string.restore_in_progress)
                        else
                            stringResource(R.string.restore_from_drive),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (isInProgress && !pendingDriveAction) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(HabitualTheme.components.iconMd),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = HabitualTheme.spacing.lg), color = MaterialTheme.colorScheme.outlineVariant)

                // My Rituals row
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
            border = androidx.compose.foundation.BorderStroke(HabitualTheme.components.borderThin, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.navBarPad))
    }
    } // End Scaffold

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

    // Restore Confirmation Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            icon = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        startDriveAction(isBackup = false)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.restore_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
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
    
    // Photo Options Dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Update Profile Picture") },
            text = { Text("Choose an option to update your profile picture.") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    val file = File.createTempFile("JPEG_", ".jpg", File(context.cacheDir, "images").apply { mkdirs() })
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                }) {
                    Text("Take Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    photoPickerLauncher.launch("image/*")
                }) {
                    Text("Choose from Gallery")
                }
            }
        )
    }
}
