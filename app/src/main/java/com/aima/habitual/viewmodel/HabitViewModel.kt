package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aima.habitual.data.HabitualDatabase
import com.aima.habitual.model.*
import com.aima.habitual.model.StepSensorManager
import com.aima.habitual.model.LightSensorManager
import com.aima.habitual.utils.ReminderManager
import com.aima.habitual.utils.PasswordUtils
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.aima.habitual.data.OfflineAppRepository
import java.time.LocalDate
import com.aima.habitual.ui.theme.AppTheme

/**
 * HabitViewModel: The central brain of the app.
 * Manages Habits, Diary, Wellbeing Stats (Date-Specific), Sensors, and User Profile.
 *
 * Data persistence strategy:
 * - Room Database: Habits, HabitRecords, DiaryEntries, WellbeingStats
 * - SharedPreferences: User preferences (theme, auth, sensor state, profile image)
 */
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    // --- 2. ROOM DATABASE & REPOSITORY ---
    private val db = HabitualDatabase.getInstance(application)
    private val repository = OfflineAppRepository(db.habitDao())

    // --- 1. CORE DATA STREAMS (UI state backed by Room) ---
    val habits: StateFlow<List<Habit>> = repository.getAllHabitsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records: StateFlow<List<HabitRecord>> = repository.getAllRecordsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diaryEntries: StateFlow<List<DiaryEntry>> = repository.getAllDiaryEntriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DIARY UI STATE ---
    var isJournalTabSelected by mutableStateOf(false)

    // --- ERROR STATE ---
    var databaseError by mutableStateOf<String?>(null)
        private set

    fun clearDatabaseError() {
        databaseError = null
    }


    // --- 3. PREFERENCES (for simple key-value config only) ---
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    // --- 3b. ENCRYPTED PREFERENCES (for auth credentials) ---
    private val masterKey = MasterKey.Builder(application)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val securePrefs = EncryptedSharedPreferences.create(
        application,
        "habitual_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // --- LEVELING SYSTEM ---
    /** Count only unique (habitId, date) pairs so toggling can't inflate level. */
    private val uniqueCompletions: Int
        get() = records.value.map { it.habitId to it.timestamp }.distinct().size

    val currentLevel: Int
        get() = uniqueCompletions / 2

    val habitsForNextLevel: Int
        get() = 2 - (uniqueCompletions % 2)

    val levelProgress: Float
        get() = (uniqueCompletions % 2) / 2f

    // --- PERSISTENT SENSOR & STEP LOGIC ---
    // Keys for SharedPreferences (sensor state is transient, not suited for Room)
    private val KEY_STEPS_TODAY = "saved_steps_today"
    private val KEY_LAST_SENSOR = "last_sensor_value"
    private val KEY_LAST_DATE = "last_step_date"
    private val KEY_REWARDS = "saved_rewards_today"

    // --- USER PROFILE LOGIC ---
    var userName by mutableStateOf(
        prefs.getString("user_name", "Ritual Specialist") ?: "Ritual Specialist"
    )
        private set

    // --- THEME PREFERENCE ---
    var isDarkTheme by mutableStateOf(prefs.getBoolean("is_dark_theme", false))
        private set

    var appTheme by mutableStateOf(
        AppTheme.valueOf(prefs.getString("app_theme", AppTheme.GREEN.name) ?: AppTheme.GREEN.name)
    )
        private set

    fun toggleTheme(isDark: Boolean) {
        isDarkTheme = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun changeAppTheme(theme: AppTheme) {
        appTheme = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    fun updateUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            userName = trimmed
            prefs.edit().putString("user_name", trimmed).apply()
        }
    }

    // --- WELLBEING GOALS ---
    var stepGoal by mutableStateOf(prefs.getInt("daily_step_goal", 10000))
        private set

    var waterGoal by mutableStateOf(prefs.getInt("daily_water_goal_ml", 2000))
        private set

    fun updateStepGoal(newGoal: Int) {
        if (newGoal > 0) {
            stepGoal = newGoal
            prefs.edit().putInt("daily_step_goal", newGoal).apply()
        }
    }

    fun updateWaterGoal(newGoalMl: Int) {
        if (newGoalMl > 0) {
            waterGoal = newGoalMl
            prefs.edit().putInt("daily_water_goal_ml", newGoalMl).apply()
        }
    }

    // --- DAILY JOURNALING HABIT SETTINGS ---
    var journalHabitId by mutableStateOf<String?>(prefs.getString("journal_habit_id", null))
        private set

    var journalHabitTime by mutableStateOf<String?>(prefs.getString("journal_habit_time", null))
        private set

    fun enableDailyJournalHabit(time: String) {
        viewModelScope.launch {
            val habit = Habit(
                title = "Daily Journaling",
                description = "Reflect on your day and clear your mind.",
                category = "Mindfulness",
                priority = Priority.HIGH,
                repeatDays = listOf(0, 1, 2, 3, 4, 5, 6),
                reminderTime = time,
                isReminderEnabled = true
            )
            try {
                repository.insertHabit(habit)
                ReminderManager.scheduleReminder(getApplication(), habit)

                journalHabitId = habit.id
                journalHabitTime = time
                prefs.edit().apply {
                    putString("journal_habit_id", habit.id)
                    putString("journal_habit_time", time)
                    apply()
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to add journal habit", e)
                databaseError = "Failed to create daily journaling habit."
            }
        }
    }

    fun disableDailyJournalHabit() {
        journalHabitId?.let { deleteHabit(it) }
        journalHabitId = null
        journalHabitTime = null
        prefs.edit().apply {
            remove("journal_habit_id")
            remove("journal_habit_time")
            apply()
        }
    }

    // --- PROFILE PICTURE ---
    var profileImageUri by mutableStateOf<Uri?>(null)
        private set

    fun updateProfileImage(uri: Uri) {
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            getApplication<Application>().contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        profileImageUri = uri
        prefs.edit().putString("user_image", uri.toString()).apply()
    }

    // --- WELLBEING STATS (Date-Aware, backed by Room) ---
    private val _dailyStats = mutableStateMapOf<Long, WellbeingStats>()

    val wellbeingStats: WellbeingStats
        get() = getStatsForDate(LocalDate.now())

    fun getStatsForDate(date: LocalDate): WellbeingStats {
        return _dailyStats[date.toEpochDay()] ?: WellbeingStats(epochDay = date.toEpochDay())
    }

    // --- PERSISTENT SENSOR & STEP LOGIC ---
    private val stepSensor = StepSensorManager(application)
    private var currentSensorSteps = 0
    private var rewardSteps = 0

    // --- AMBIENT LIGHT SENSOR (Sleep Hygiene Assistant) ---
    // Measures room brightness in lux; exposed as Compose state for the sleep dialog.
    private val lightSensor = LightSensorManager(application)
    var currentLuxLevel by mutableStateOf(0f)
        private set

    init {
        // 1. Load all data from Room into reactive state lists
        loadDataFromRoom()

        // Load Profile Image from SharedPreferences
        val savedImage = prefs.getString("user_image", null)
        if (savedImage != null) {
            try {
                profileImageUri = Uri.parse(savedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        loadSleepLogs()

        // Step sensor state (transient, stays in SharedPreferences)
        val storedDate = prefs.getLong(KEY_LAST_DATE, -1L)
        val todayEpoch = LocalDate.now().toEpochDay()

        if (storedDate != todayEpoch) {
            currentSensorSteps = 0
            rewardSteps = 0
            saveStepState(0, 0, todayEpoch)
        } else {
            currentSensorSteps = prefs.getInt(KEY_STEPS_TODAY, 0)
            rewardSteps = prefs.getInt(KEY_REWARDS, 0)
        }

        // 2. Start listening for step sensor updates
        stepSensor.startListening { totalDeviceSteps ->
            handleSensorUpdate(totalDeviceSteps)
        }

        // 3. Start listening for ambient light updates (Sleep Hygiene Assistant)
        lightSensor.startListening { lux ->
            currentLuxLevel = lux
        }
    }

    /**
     * Loads all persisted data from Room into the in-memory state lists.
     * Called once during ViewModel initialization.
     */
    private fun loadDataFromRoom() {
        viewModelScope.launch {
            try {
                repository.getAllWellbeingStatsStream().collect { loadedStats ->
                    _dailyStats.clear()
                    for (stat in loadedStats) {
                        _dailyStats[stat.epochDay] = stat
                    }
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to load database data", e)
                databaseError = "Failed to load existing data. Please try restarting the app."
            }
        }
    }

    private fun handleSensorUpdate(totalDeviceSteps: Int) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val storedDate = prefs.getLong(KEY_LAST_DATE, -1L)
        val lastSensorValue = prefs.getInt(KEY_LAST_SENSOR, -2)

        if (storedDate != todayEpoch) {
            currentSensorSteps = 0
            rewardSteps = 0
            // Fix: Immediately persist reset so it's not lost on activity recreation before next sensor event
            saveStepState(0, totalDeviceSteps, todayEpoch)
        }

        var delta = 0
        if (lastSensorValue != -2) {
            delta = if (totalDeviceSteps >= lastSensorValue) {
                totalDeviceSteps - lastSensorValue
            } else {
                totalDeviceSteps
            }
        }

        if (delta > 0) {
            currentSensorSteps += delta
            prefs.edit().apply {
                putInt(KEY_STEPS_TODAY, currentSensorSteps)
                putInt(KEY_LAST_SENSOR, totalDeviceSteps)
                putLong(KEY_LAST_DATE, todayEpoch)
                apply()
            }
            updateStepsForDate(LocalDate.now())
        } else {
            if (lastSensorValue != totalDeviceSteps) {
                prefs.edit().putInt(KEY_LAST_SENSOR, totalDeviceSteps).apply()
            }
        }
    }

    fun syncSteps() {
        updateStepsForDate(LocalDate.now())
    }

    private fun updateStepsForDate(date: LocalDate) {
        val epoch = date.toEpochDay()
        val steps = currentSensorSteps + rewardSteps
        val ts = System.currentTimeMillis()
        // Optimistically keep in-memory cache current for immediate UI
        val cached = _dailyStats[epoch] ?: WellbeingStats(epochDay = epoch)
        _dailyStats[epoch] = cached.copy(stepsCount = steps, lastSyncTimestamp = ts)

        viewModelScope.launch {
            try {
                // Seed row if absent, then atomically replace only stepsCount
                val existing = repository.getStatsForDay(epoch)
                if (existing == null) {
                    repository.insertOrUpdateStats(WellbeingStats(epochDay = epoch))
                }
                repository.updateStepsForDay(epoch, steps, ts)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to update step stats", e)
                databaseError = "Failed to save steps data."
            }
        }
    }

    private fun saveStepState(steps: Int, sensorVal: Int, date: Long) {
        prefs.edit().apply {
            putInt(KEY_STEPS_TODAY, steps)
            if (sensorVal != 0) putInt(KEY_LAST_SENSOR, sensorVal)
            putLong(KEY_LAST_DATE, date)
            apply()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepSensor.stopListening()
        lightSensor.stopListening()
    }

    // --- WELLBEING ACTIONS ---

    fun logWater(date: LocalDate, amountMl: Int) {
        val epoch = date.toEpochDay()
        val ts = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                // 1. Ensure a row exists for today (seed with 0 if absent)
                val existing = repository.getStatsForDay(epoch)
                if (existing == null) {
                    repository.insertOrUpdateStats(WellbeingStats(epochDay = epoch))
                }
                // 2. Atomically increment only the water column — safe under concurrency
                repository.addWaterForDay(epoch, amountMl, ts)

                // 3. Reflect change in in-memory cache for immediate UI update
                val refreshed = repository.getStatsForDay(epoch)
                if (refreshed != null) _dailyStats[epoch] = refreshed
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to update water stats", e)
                databaseError = "Failed to save water intake."
            }
        }
    }

    fun updateSleep(date: LocalDate, hours: Double) {
        val epoch = date.toEpochDay()
        val ts = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                // Seed row if absent, then atomically update only the sleep column
                val existing = repository.getStatsForDay(epoch)
                if (existing == null) {
                    repository.insertOrUpdateStats(WellbeingStats(epochDay = epoch))
                }
                repository.updateSleepForDay(epoch, hours, ts)

                val refreshed = repository.getStatsForDay(epoch)
                if (refreshed != null) _dailyStats[epoch] = refreshed
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to update sleep stats", e)
                databaseError = "Failed to save sleep data."
            }
        }
    }

    // --- MANUAL SLEEP LOGGING ---
    private val _sleepLogs = mutableStateMapOf<Long, SleepLogEntry>()

    fun getSleepLog(date: LocalDate): SleepLogEntry? {
        return _sleepLogs[date.toEpochDay()]
    }

    fun saveSleepLog(date: LocalDate, durationMinutes: Int, quality: String) {
        val epoch = date.toEpochDay()
        val entry = SleepLogEntry(epoch, durationMinutes, quality)
        _sleepLogs[epoch] = entry

        viewModelScope.launch {
            try {
                repository.insertOrUpdateSleepLog(entry)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to save sleep log", e)
                databaseError = "Failed to save sleep log."
            }
        }

        // Also update standard sleep stats for general tracking
        updateSleep(date, durationMinutes / 60.0)
    }

    private fun loadSleepLogs() {
        viewModelScope.launch {
            try {
                repository.getAllSleepLogsStream().collect { logs ->
                    _sleepLogs.clear()
                    for (log in logs) {
                        _sleepLogs[log.dateEpoch] = log
                    }
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to load sleep logs", e)
            }
        }
    }

    fun addSteps(steps: Int) {
        rewardSteps += steps
        if (rewardSteps < 0) rewardSteps = 0
        prefs.edit().putInt(KEY_REWARDS, rewardSteps).apply()
        updateStepsForDate(LocalDate.now())
    }

    // --- HABIT CRUD LOGIC (Room-backed) ---

    suspend fun addHabit(habit: Habit): Boolean {
        return try {
            repository.insertHabit(habit)
            ReminderManager.scheduleReminder(getApplication(), habit)
            true
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to add habit", e)
            databaseError = "Failed to create new habit."
            false
        }
    }

    suspend fun updateHabit(updatedHabit: Habit): Boolean {
        return try {
            repository.updateHabit(updatedHabit)
            ReminderManager.scheduleReminder(getApplication(), updatedHabit)
            true
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to update habit", e)
            databaseError = "Failed to update habit."
            false
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                repository.deleteHabitWithRecords(habitId)
                ReminderManager.cancelReminder(getApplication(), habitId)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to delete habit", e)
                databaseError = "Failed to permanently delete habit."
            }
        }
    }

    fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        val epochDay = date.toEpochDay()
        val existingRecord = records.value.find { it.habitId == habitId && it.timestamp == epochDay }

        if (existingRecord != null) {
            viewModelScope.launch { 
                try {
                    repository.deleteRecord(existingRecord.id) 
                    // Subtract steps when un-completing a habit
                    addSteps(-300)
                } catch (e: Exception) {
                    Log.e("HabitViewModel", "Failed to delete habit record", e)
                    databaseError = "Failed to update habit progress."
                }
            }
        } else {
            val newRecord = HabitRecord(
                habitId = habitId,
                timestamp = epochDay,
                isCompleted = true
            )
            viewModelScope.launch { 
                try {
                    repository.insertRecord(newRecord) 
                    // Move addSteps logic inside so it only occurs on success
                    addSteps(300)
                } catch (e: Exception) {
                    Log.e("HabitViewModel", "Failed to create habit record", e)
                    databaseError = "Failed to register activity."
                }
            }
        }
    }

    // --- DIARY CRUD LOGIC (Room-backed) ---

    fun addDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch { 
            try {
                repository.insertDiaryEntry(entry) 
                
                // Automatic Journal Habit Completion
                if (entry.isJournal) {
                    journalHabitId?.let { hId ->
                        val today = LocalDate.now()
                        val epochDay = today.toEpochDay()
                        val isDone = records.value.any { it.habitId == hId && it.timestamp == epochDay }
                        if (!isDone) {
                            val newRecord = HabitRecord(habitId = hId, timestamp = epochDay, isCompleted = true)
                            repository.insertRecord(newRecord)
                            addSteps(300)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to add diary entry", e)
                databaseError = "Failed to save journal entry. It may not persist across restarts."
            }
        }
    }

    fun updateDiaryEntry(updatedEntry: DiaryEntry) {
        viewModelScope.launch { 
            try {
                repository.updateDiaryEntry(updatedEntry) 
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to update diary entry", e)
                databaseError = "Failed to save journal modifications."
            }
        }
    }

    fun deleteDiaryEntry(entryId: String) {
        viewModelScope.launch { 
            try {
                repository.deleteDiaryEntry(entryId) 
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to delete diary entry", e)
                databaseError = "Failed to permanently delete entry."
            }
        }
    }

    // --- AUTH LOGIC (Password hashed with SHA-256 + Salt) ---

    var isLoggedIn by mutableStateOf(prefs.getBoolean("is_logged_in", false))
        private set

    /** User preference: whether biometric quick login is enabled. */
    var isBiometricEnabled by mutableStateOf(prefs.getBoolean("biometric_login_enabled", false))
        private set

    /** Toggle the biometric login preference on/off. */
    fun toggleBiometricLogin(enabled: Boolean) {
        isBiometricEnabled = enabled
        prefs.edit().putBoolean("biometric_login_enabled", enabled).apply()
    }

    /**
     * True if ALL of these are met:
     * 1. User has explicitly enabled biometric login in settings
     * 2. User has logged in at least once before
     * 3. Device has biometric hardware enrolled
     */
    val isBiometricAvailable: Boolean
        get() {
            if (!isBiometricEnabled) return false
            val hasLoggedInBefore = prefs.getBoolean("has_authenticated_before", false)
            if (!hasLoggedInBefore) return false
            val biometricManager = BiometricManager.from(getApplication())
            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        }

    /** Mark that the user has successfully authenticated at least once (enables biometric on next visit). */
    private fun markAuthenticated() {
        prefs.edit().putBoolean("has_authenticated_before", true).apply()
    }

    var loginError by mutableStateOf<String?>(null)
        private set

    /**
     * Registers a new user. The password is hashed with a unique salt
     * before being saved to SharedPreferences. The plain-text password
     * is never stored.
     */
    fun registerUser(name: String, email: String, pass: String) {
        // FIX: Wipe all existing Room data before registering a new account.
        // This prevents data from a previous user leaking into the new user's session.
        viewModelScope.launch {
            try {
                repository.deleteAllUserData()
            } catch (e: Exception) {
                Log.w("HabitViewModel", "Could not clear prior user data before registration", e)
            }
        }
        _dailyStats.clear()
        _sleepLogs.clear()

        val salt = PasswordUtils.generateSalt()
        val hashedPassword = PasswordUtils.hashPassword(pass, salt)

        // Store auth credentials in encrypted preferences
        securePrefs.edit().apply {
            putString("user_email", email)
            putString("user_password_hash", hashedPassword)
            putString("user_password_salt", salt)
        }.apply()

        // Store non-sensitive user data in regular preferences
        prefs.edit().apply {
            putString("user_name", name)
            putBoolean("is_logged_in", true)
        }.apply()

        userName = name
        isLoggedIn = true
        loginError = null
        markAuthenticated()
    }

    fun logout() {
        isLoggedIn = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    /** Permanently delete the user's profile and all associated data. */
    fun deleteProfile() {
        // Clear both regular and encrypted preferences
        prefs.edit().clear().apply()
        securePrefs.edit().clear().apply()

        // No need to clear habits/records/diary as Flow emits from empty DB
        _dailyStats.clear()
        _sleepLogs.clear()

        viewModelScope.launch {
            try {
                repository.deleteAllUserData()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to clear profile databases", e)
                databaseError = "Profile deletion incomplete. Some database records may still exist locally."
            }
        }

        userName = "Ritual Specialist"
        profileImageUri = null
        isLoggedIn = false
        loginError = null
        currentSensorSteps = 0
        rewardSteps = 0
    }

    /**
     * Validates login by hashing the entered password with the stored salt
     * and comparing the result against the stored hash. The plain-text
     * password is never persisted or compared directly.
     */
    fun validateLogin(email: String, pass: String): Boolean {
        val savedEmail = securePrefs.getString("user_email", null)
        val savedHash = securePrefs.getString("user_password_hash", null)
        val savedSalt = securePrefs.getString("user_password_salt", null)

        return when {
            savedEmail == null -> {
                loginError = "No account found. Please register."
                false
            }
            savedEmail == email && savedHash != null && savedSalt != null
                    && PasswordUtils.verifyPassword(pass, savedSalt, savedHash) -> {
                loginError = null
                isLoggedIn = true
                prefs.edit().putBoolean("is_logged_in", true).apply()
                markAuthenticated()
                true
            }
            else -> {
                loginError = "Invalid email or password."
                false
            }
        }
    }

    /**
     * Verifies the user's password without logging them in.
     * Useful for unlocking locked entries.
     */
    fun verifyUserPassword(pass: String): Boolean {
        val savedHash = securePrefs.getString("user_password_hash", null)
        val savedSalt = securePrefs.getString("user_password_salt", null)

        return if (savedHash != null && savedSalt != null) {
            PasswordUtils.verifyPassword(pass, savedSalt, savedHash)
        } else {
            false
        }
    }

    fun clearLoginError() {
        loginError = null
    }

    // --- GOOGLE SIGN-IN (Credential Manager API) ---

    /**
     * Initiates the Google Sign-In flow using the modern Credential Manager API.
     * On success, saves the user's name and email, and sets isLoggedIn = true.
     * @param webClientId The OAuth 2.0 Web Client ID from your Google Cloud / Firebase console.
     */
    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val name = googleIdTokenCredential.displayName ?: "User"
                val email = googleIdTokenCredential.id  // email address

                // Save user info
                prefs.edit().apply {
                    putString("user_name", name)
                    putBoolean("is_logged_in", true)
                }.apply()

                securePrefs.edit().apply {
                    putString("user_email", email)
                }.apply()

                userName = name
                isLoggedIn = true
                loginError = null
                markAuthenticated()

                Log.d("HabitViewModel", "Google Sign-In successful: $email")
            } catch (e: GetCredentialCancellationException) {
                Log.d("HabitViewModel", "Google Sign-In cancelled by user")
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Google Sign-In failed", e)
                loginError = "Google Sign-In failed. Please try another method."
            }
        }
    }

    // --- BIOMETRIC AUTHENTICATION ---

    /**
     * Shows the system biometric prompt (fingerprint/face).
     * Only works if the user has previously authenticated (has_authenticated_before = true).
     * @param activity  The FragmentActivity needed by BiometricPrompt.
     * @param onSuccess Called when biometric auth succeeds.
     * @param onFailure Called when biometric auth fails or is cancelled.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isLoggedIn = true
                prefs.edit().putBoolean("is_logged_in", true).apply()
                loginError = null
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    loginError = errString.toString()
                    onFailure(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Individual attempt failed, prompt stays open for retry
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(com.aima.habitual.R.string.biometric_prompt_title))
            .setSubtitle(activity.getString(com.aima.habitual.R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(activity.getString(com.aima.habitual.R.string.biometric_prompt_cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}