package com.aima.habitual.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aima.habitual.R
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.HabitViewModel
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File

/**
 * DiaryDetailScreen provides the interface to create or edit journal entries.
 * Fully integrated with the Premium Minimal Wellness design token system.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    entryId: String?, // Nullable: If null -> New Entry, If ID exists -> Edit Mode
    isJournal: Boolean,
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val existingEntry = diaryEntries.find { it.id == entryId }
    val context = LocalContext.current

    var title by remember { mutableStateOf(existingEntry?.title ?: "") }
    var content by remember { mutableStateOf(existingEntry?.content ?: "") }
    var isLocked by remember { mutableStateOf(existingEntry?.isLocked ?: false) }

    var currentTagInput by remember { mutableStateOf("") }
    val tags = remember {
        mutableStateListOf<String>().apply {
            addAll(existingEntry?.tags ?: emptyList())
        }
    }
    
    var selectedMood by remember { mutableStateOf(existingEntry?.mood ?: "") }
    val moods = listOf("😞", "😐", "🙂", "😄")
    
    // Formatting the current date/time
    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(existingEntry?.timestamp) {
        dateFormatter.format(Date(existingEntry?.timestamp ?: System.currentTimeMillis()))
    }

    // --- ATTACHMENT STATE ---
    var photoUri by remember { mutableStateOf(existingEntry?.photoUri) }
    var audioFilePath by remember { mutableStateOf(existingEntry?.audioFilePath) }
    var locationText by remember { mutableStateOf(existingEntry?.locationText) }
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    // --- PHOTO PICKER ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Take persistable permission so the URI survives app restarts
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* Some providers don't support persistable grants */ }
            photoUri = uri.toString()
        }
    }

    // --- AUDIO RECORDING PERMISSION ---
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Start recording after permission is granted
            val file = File(context.filesDir, "diary_audio_${System.currentTimeMillis()}.m4a")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            audioFilePath = file.absolutePath
            isRecording = true
        } else {
            Toast.makeText(context, "Microphone permission is required to record audio", Toast.LENGTH_SHORT).show()
        }
    }

    // --- LOCATION PERMISSION ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            try {
                val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    locationText = String.format(Locale.US, "%.4f°N, %.4f°E", loc.latitude, loc.longitude)
                } else {
                    Toast.makeText(context, "Could not fetch location. Try again later.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    // Cleanup recorder on dispose
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                } catch (_: Exception) { }
            }
        }
    }
    
    val canSave = title.isNotBlank() && content.isNotBlank()
    val saveAction: () -> Unit = {
        // Stop recording before save if still active
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (_: Exception) { }
            mediaRecorder = null
            isRecording = false
        }

        val entry = DiaryEntry(
            id = existingEntry?.id ?: UUID.randomUUID().toString(),
            title = title,
            content = content,
            tags = tags.toList(),
            timestamp = existingEntry?.timestamp ?: System.currentTimeMillis(),
            isLocked = isLocked,
            mood = selectedMood.takeIf { it.isNotBlank() },
            photoUri = photoUri,
            audioFilePath = audioFilePath,
            locationText = locationText,
            isJournal = isJournal
        )

        if (existingEntry == null) {
            viewModel.addDiaryEntry(entry)
        } else {
            viewModel.updateDiaryEntry(entry)
        }

        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            // Using a simple Row for custom header control or CenterAlignedTopAppBar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (existingEntry != null) {
                            stringResource(R.string.diary_edit_entry)
                        } else if (isJournal) {
                            "New Journal Entry"
                        } else {
                            "New Note"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.desc_back))
                    }
                },
                actions = {
                    // Lock Toggle Button
                    IconButton(
                        onClick = { isLocked = !isLocked }
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (isLocked) "Unlock Entry" else "Lock Entry",
                            tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Save action moved to FAB, but keeping an optional icon for familiarity if desired, or remove to enforce FAB.
                    // We remove the save action here to emphasize the FAB.
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent // Transparent to show pattern
                )
            )
        },
        floatingActionButton = {
            if (canSave) {
                FloatingActionButton(
                    onClick = saveAction,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = HabitualTheme.elevation.high,
                        pressedElevation = HabitualTheme.elevation.low
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = stringResource(R.string.desc_save))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Base background for pattern
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
                    .padding(horizontal = HabitualTheme.spacing.xl)
                    .verticalScroll(rememberScrollState()) //  scrollable
            ) {

                // Date & Time Indicator
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg)) 

                // Mood Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.forEach { moodEmoji ->
                        val isSelected = selectedMood == moodEmoji
                        Box(
                            modifier = Modifier
                                .size(HabitualTheme.components.iconXl)
                                .border(
                                    width = if (isSelected) HabitualTheme.components.borderMedium else HabitualTheme.components.borderThin,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .clickable { selectedMood = moodEmoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = moodEmoji, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl))

                // A. Title Field (Reflective surface)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Give this day a title...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = HabitualTheme.components.borderThin,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle), // Subtle border
                            shape = RoundedCornerShape(HabitualTheme.radius.xl) // 20dp
                        ),
                    singleLine = true,
                    shape = RoundedCornerShape(HabitualTheme.radius.xl),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface, // Warm
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.xl)) // 20dp Rhythm

                // B. Tag Input Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md) // 12dp gap
                ) {
                    TextField(
                        value = currentTagInput,
                        onValueChange = { currentTagInput = it },
                        label = { Text(stringResource(R.string.diary_label_add_tag)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = HabitualTheme.components.borderThin,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle),
                                shape = RoundedCornerShape(HabitualTheme.radius.xl)
                            ),
                        // Soft Surface
                        singleLine = true,
                        shape = RoundedCornerShape(HabitualTheme.radius.xl),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (currentTagInput.isNotBlank()) {
                                tags.add(currentTagInput.trim())
                                currentTagInput = ""
                            }
                        }),
                        trailingIcon = {
                            // Centered Action
                            Box(
                                modifier = Modifier
                                    .padding(end = HabitualTheme.spacing.lg) // Right padding
                                    .clickable {
                                        if (currentTagInput.isNotBlank()) {
                                            tags.add(currentTagInput.trim())
                                            currentTagInput = ""
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.desc_add_tag),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    // C. Tag Display Area
                    if (tags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
                        ) {
                            tags.forEach { tag ->
                                InputChip(
                                    selected = true,
                                    onClick = { tags.remove(tag) },
                                    label = {
                                        Text(
                                            tag,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.desc_remove_tag),
                                            modifier = Modifier.size(HabitualTheme.components.iconSm)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(HabitualTheme.radius.md)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.section)) // Section Break

                // D. Content Field (Large)
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = {
                        Text(
                            "What moment stayed with you today?", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted) // Muted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(
                            width = HabitualTheme.components.borderThin,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = HabitualTheme.alpha.subtle),
                            shape = RoundedCornerShape(HabitualTheme.radius.xl)
                        ),
                    shape = RoundedCornerShape(HabitualTheme.radius.xl),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

                // Quick-Add Attachments Row (FUNCTIONAL)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- PHOTO BUTTON ---
                    IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Default.PhotoCamera, 
                            contentDescription = "Add Photo", 
                            tint = if (photoUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // --- AUDIO RECORD BUTTON ---
                    IconButton(onClick = {
                        if (isRecording) {
                            // Stop recording
                            try {
                                mediaRecorder?.stop()
                                mediaRecorder?.release()
                            } catch (_: Exception) { }
                            mediaRecorder = null
                            isRecording = false
                        } else {
                            // Check permission then start recording
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                val file = File(context.filesDir, "diary_audio_${System.currentTimeMillis()}.m4a")
                                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    MediaRecorder(context)
                                } else {
                                    @Suppress("DEPRECATION")
                                    MediaRecorder()
                                }
                                recorder.apply {
                                    setAudioSource(MediaRecorder.AudioSource.MIC)
                                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                    setOutputFile(file.absolutePath)
                                    prepare()
                                    start()
                                }
                                mediaRecorder = recorder
                                audioFilePath = file.absolutePath
                                isRecording = true
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic, 
                            contentDescription = if (isRecording) "Stop Recording" else "Record Voice Note", 
                            tint = if (isRecording) MaterialTheme.colorScheme.error 
                                   else if (audioFilePath != null) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // --- LOCATION BUTTON ---
                    IconButton(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
                            try {
                                val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                if (loc != null) {
                                    locationText = String.format(Locale.US, "%.4f°N, %.4f°E", loc.latitude, loc.longitude)
                                } else {
                                    Toast.makeText(context, "Could not fetch location", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: SecurityException) {
                                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }) {
                        Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = "Add Location", 
                            tint = if (locationText != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- ATTACHMENT PREVIEWS ---
                if (photoUri != null || audioFilePath != null || locationText != null) {
                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))
                    Column(verticalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)) {
                        photoUri?.let {
                            Surface(
                                shape = RoundedCornerShape(HabitualTheme.radius.md),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(HabitualTheme.components.iconSm), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.width(HabitualTheme.spacing.sm))
                                    Text("Photo attached", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { photoUri = null }, modifier = Modifier.size(HabitualTheme.components.iconMd)) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(HabitualTheme.components.iconXs))
                                    }
                                }
                            }
                        }
                        audioFilePath?.let {
                            Surface(
                                shape = RoundedCornerShape(HabitualTheme.radius.md),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(HabitualTheme.components.iconSm), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(Modifier.width(HabitualTheme.spacing.sm))
                                    Text(if (isRecording) "Recording..." else "Voice note attached", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(Modifier.weight(1f))
                                    if (!isRecording) {
                                        IconButton(onClick = { audioFilePath = null }, modifier = Modifier.size(HabitualTheme.components.iconMd)) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove audio", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(HabitualTheme.components.iconXs))
                                        }
                                    }
                                }
                            }
                        }
                        locationText?.let { loc ->
                            Surface(
                                shape = RoundedCornerShape(HabitualTheme.radius.md),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = HabitualTheme.spacing.md, vertical = HabitualTheme.spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(HabitualTheme.components.iconSm), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Spacer(Modifier.width(HabitualTheme.spacing.sm))
                                    Text(loc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { locationText = null }, modifier = Modifier.size(HabitualTheme.components.iconMd)) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove location", tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(HabitualTheme.components.iconXs))
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom breathing room (make space for FAB)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}