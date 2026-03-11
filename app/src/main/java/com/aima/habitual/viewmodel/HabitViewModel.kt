package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.aima.habitual.data.HabitualDatabase
import com.aima.habitual.model.*
import com.aima.habitual.model.StepSensorManager
import com.aima.habitual.utils.ReminderManager
import com.aima.habitual.utils.PasswordUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * HabitViewModel: The central brain of the app.
 * Manages Habits, Diary, Wellbeing Stats (Date-Specific), Sensors, and User Profile.
 *
 * Data persistence strategy:
 * - Room Database: Habits, HabitRecords, DiaryEntries, WellbeingStats
 * - SharedPreferences: User preferences (theme, auth, sensor state, profile image)
 */
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    // --- 1. CORE DATA LISTS (UI state backed by Room) ---
    val habits = mutableStateListOf<Habit>()
    val records = mutableStateListOf<HabitRecord>()
    val diaryEntries = mutableStateListOf<DiaryEntry>()

    // --- ERROR STATE ---
    var databaseError by mutableStateOf<String?>(null)
        private set

    fun clearDatabaseError() {
        databaseError = null
    }

    // --- 2. ROOM DATABASE ---
    private val db = HabitualDatabase.getInstance(application)
    private val dao = db.habitDao()

    // --- 3. PREFERENCES (for simple key-value config only) ---
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    // --- LEVELING SYSTEM ---
    /** Count only unique (habitId, date) pairs so toggling can't inflate level. */
    private val uniqueCompletions: Int
        get() = records.map { it.habitId to it.timestamp }.distinct().size

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

    fun toggleTheme(isDark: Boolean) {
        isDarkTheme = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun updateUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            userName = trimmed
            prefs.edit().putString("user_name", trimmed).apply()
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

        // 2. Start Listening for step sensor updates
        stepSensor.startListening { totalDeviceSteps ->
            handleSensorUpdate(totalDeviceSteps)
        }
    }

    /**
     * Loads all persisted data from Room into the in-memory state lists.
     * Called once during ViewModel initialization.
     */
    private fun loadDataFromRoom() {
        viewModelScope.launch {
            try {
                // Load Habits
                val loadedHabits = dao.getAllHabits()
                habits.clear()
                habits.addAll(loadedHabits)

                // Load Records
                val loadedRecords = dao.getAllRecords()
                records.clear()
                records.addAll(loadedRecords)

                // Load Diary Entries
                val loadedDiary = dao.getAllDiaryEntries()
                diaryEntries.clear()
                diaryEntries.addAll(loadedDiary)

                // Load Wellbeing Stats
                val loadedStats = dao.getAllWellbeingStats()
                _dailyStats.clear()
                for (stat in loadedStats) {
                    _dailyStats[stat.epochDay] = stat
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
        val currentStats = _dailyStats[epoch] ?: WellbeingStats(epochDay = epoch)

        val updatedStats = currentStats.copy(
            stepsCount = currentSensorSteps + rewardSteps,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _dailyStats[epoch] = updatedStats
        viewModelScope.launch {
            try {
                dao.insertOrUpdateStats(updatedStats)
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
    }

    // --- WELLBEING ACTIONS ---

    fun logWater(date: LocalDate, amountMl: Int) {
        val epoch = date.toEpochDay()
        val currentStats = _dailyStats[epoch] ?: WellbeingStats(epochDay = epoch)

        val updatedStats = currentStats.copy(
            waterIntakeMl = currentStats.waterIntakeMl + amountMl,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _dailyStats[epoch] = updatedStats
        viewModelScope.launch { 
            try {
                dao.insertOrUpdateStats(updatedStats) 
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to update water stats", e)
                databaseError = "Failed to save water intake."
            }
        }
    }

    fun updateSleep(date: LocalDate, hours: Double) {
        val epoch = date.toEpochDay()
        val currentStats = _dailyStats[epoch] ?: WellbeingStats(epochDay = epoch)

        val updatedStats = currentStats.copy(
            sleepDurationHours = hours,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _dailyStats[epoch] = updatedStats
        viewModelScope.launch { 
            try {
                dao.insertOrUpdateStats(updatedStats) 
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to update sleep stats", e)
                databaseError = "Failed to save sleep data."
            }
        }
    }

    // --- MANUAL SLEEP LOGGING ---
    private val KEY_SLEEP_LOGS = "saved_sleep_logs_json"
    private val gson = Gson()
    private val _sleepLogs = mutableStateMapOf<Long, SleepLogEntry>()

    fun getSleepLog(date: LocalDate): SleepLogEntry? {
        return _sleepLogs[date.toEpochDay()]
    }

    fun saveSleepLog(date: LocalDate, durationMinutes: Int, quality: String) {
        val epoch = date.toEpochDay()
        val entry = SleepLogEntry(epoch, durationMinutes, quality)
        _sleepLogs[epoch] = entry

        try {
            val json = gson.toJson(_sleepLogs.toMap())
            prefs.edit().putString(KEY_SLEEP_LOGS, json).apply()
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to serialize sleep logs", e)
        }

        // Also update standard sleep stats for DB legacy/general tracking
        updateSleep(date, durationMinutes / 60.0)
    }

    private fun loadSleepLogs() {
        val json = prefs.getString(KEY_SLEEP_LOGS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<Map<Long, SleepLogEntry>>() {}.type
                val logs: Map<Long, SleepLogEntry> = gson.fromJson(json, type)
                _sleepLogs.clear()
                _sleepLogs.putAll(logs)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to parse sleep logs", e)
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
        habits.add(habit)
        return try {
            dao.insertHabit(habit)
            ReminderManager.scheduleReminder(getApplication(), habit)
            true
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to add habit", e)
            habits.remove(habit)
            databaseError = "Failed to create new habit."
            false
        }
    }

    suspend fun updateHabit(updatedHabit: Habit): Boolean {
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index == -1) return false
        val previous = habits[index]
        habits[index] = updatedHabit
        return try {
            dao.updateHabit(updatedHabit)
            ReminderManager.scheduleReminder(getApplication(), updatedHabit)
            true
        } catch (e: Exception) {
            Log.e("HabitViewModel", "Failed to update habit", e)
            habits[index] = previous
            databaseError = "Failed to update habit."
            false
        }
    }

    fun deleteHabit(habitId: String) {
        habits.removeAll { it.id == habitId }
        records.removeAll { it.habitId == habitId }
        viewModelScope.launch {
            try {
                dao.deleteHabit(habitId)
                dao.deleteRecordsByHabitId(habitId)
                ReminderManager.cancelReminder(getApplication(), habitId)
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to delete habit", e)
                databaseError = "Failed to permanently delete habit."
            }
        }
    }

    fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        val epochDay = date.toEpochDay()
        val existingRecord = records.find { it.habitId == habitId && it.timestamp == epochDay }

        if (existingRecord != null) {
            records.remove(existingRecord)
            viewModelScope.launch { 
                try {
                    dao.deleteRecord(existingRecord.id) 
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
            records.add(newRecord)
            viewModelScope.launch { 
                try {
                    dao.insertRecord(newRecord) 
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
        diaryEntries.add(0, entry)
        viewModelScope.launch { 
            try {
                dao.insertDiaryEntry(entry) 
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to add diary entry", e)
                databaseError = "Failed to save journal entry. It may not persist across restarts."
            }
        }
    }

    fun updateDiaryEntry(updatedEntry: DiaryEntry) {
        val index = diaryEntries.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            diaryEntries[index] = updatedEntry
            viewModelScope.launch { 
                try {
                    dao.updateDiaryEntry(updatedEntry) 
                } catch (e: Exception) {
                    Log.e("HabitViewModel", "Failed to update diary entry", e)
                    databaseError = "Failed to save journal modifications."
                }
            }
        }
    }

    fun deleteDiaryEntry(entryId: String) {
        diaryEntries.removeAll { it.id == entryId }
        viewModelScope.launch { 
            try {
                dao.deleteDiaryEntry(entryId) 
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to delete diary entry", e)
                databaseError = "Failed to permanently delete entry."
            }
        }
    }

    // --- AUTH LOGIC (Password hashed with SHA-256 + Salt) ---

    var isLoggedIn by mutableStateOf(prefs.getBoolean("is_logged_in", false))
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    /**
     * Registers a new user. The password is hashed with a unique salt
     * before being saved to SharedPreferences. The plain-text password
     * is never stored.
     */
    fun registerUser(name: String, email: String, pass: String) {
        val salt = PasswordUtils.generateSalt()
        val hashedPassword = PasswordUtils.hashPassword(pass, salt)

        prefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_password_hash", hashedPassword)
            putString("user_password_salt", salt)
            putBoolean("is_logged_in", true)
        }.apply()

        userName = name
        isLoggedIn = true
        loginError = null
    }

    fun logout() {
        isLoggedIn = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    /** Permanently delete the user's profile and all associated data. */
    fun deleteProfile() {
        prefs.edit().clear().apply()

        habits.clear()
        records.clear()
        diaryEntries.clear()
        _dailyStats.clear()

        viewModelScope.launch {
            try {
                dao.deleteAllHabits()
                dao.deleteAllRecords()
                dao.deleteAllDiaryEntries()
                dao.deleteAllStats()
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
        val savedEmail = prefs.getString("user_email", null)
        val savedHash = prefs.getString("user_password_hash", null)
        val savedSalt = prefs.getString("user_password_salt", null)

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
        val savedHash = prefs.getString("user_password_hash", null)
        val savedSalt = prefs.getString("user_password_salt", null)

        return if (savedHash != null && savedSalt != null) {
            PasswordUtils.verifyPassword(pass, savedSalt, savedHash)
        } else {
            false
        }
    }

    fun clearLoginError() {
        loginError = null
    }
}