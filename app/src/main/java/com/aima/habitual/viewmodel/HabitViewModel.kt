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
import com.aima.habitual.model.*
import com.aima.habitual.model.StepSensorManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

/**
 * HabitViewModel: The central brain of the app.
 * Manages Habits, Diary, Wellbeing Stats (Date-Specific), Sensors, and User Profile.
 */
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    // --- 1. CORE DATA LISTS ---
    val habits = mutableStateListOf<Habit>()
    val records = mutableStateListOf<HabitRecord>()
    val diaryEntries = mutableStateListOf<DiaryEntry>()

    // --- 2. PREFERENCES (Storage) ---
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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

    // --- 5. PERSISTENT SENSOR & STEP LOGIC ---
    // Keys for SharedPreferences
    private val KEY_STEPS_TODAY = "saved_steps_today"
    private val KEY_LAST_SENSOR = "last_sensor_value"
    private val KEY_LAST_DATE = "last_step_date"
    private val KEY_REWARDS = "saved_rewards_today"

    // --- 3. USER PROFILE LOGIC ---
    // Loads saved name or defaults to "Ritual Specialist"
    var userName by mutableStateOf(
        prefs.getString("user_name", "Ritual Specialist") ?: "Ritual Specialist"
    )
        private set

    fun updateUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            userName = trimmed
            prefs.edit().putString("user_name", trimmed).apply()
        }
    }

    // --- 3.1 PROFILE PICTURE ---
    var profileImageUri by mutableStateOf<Uri?>(null)
        private set

    fun updateProfileImage(uri: Uri) {
        // 1. Take persistable permission so we can read this later
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            getApplication<Application>().contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            e.printStackTrace() // Handle potential failure (e.g. if URI is not from document provider)
        }

        // 2. Save
        profileImageUri = uri
        prefs.edit().putString("user_image", uri.toString()).apply()
    }

    // --- 4. WELLBEING STATS (Date-Aware) ---
    // Stores stats for each specific day (Key = EpochDay)
    private val _dailyStats = mutableStateMapOf<Long, WellbeingStats>()

    /**
     * Helper to get stats for "Today" (Backward compatibility)
     */
    val wellbeingStats: WellbeingStats
        get() = getStatsForDate(LocalDate.now())

    /**
     * Retrieves stats for any specific date. Returns empty stats if none exist.
     */
    fun getStatsForDate(date: LocalDate): WellbeingStats {
        return _dailyStats[date.toEpochDay()] ?: WellbeingStats()
    }

    // --- 5. PERSISTENT SENSOR & STEP LOGIC ---
    
    private val stepSensor = StepSensorManager(application)
    
    // In-memory definition, but backed by Prefs
    private var currentSensorSteps = 0 
    private var rewardSteps = 0

    init {
        // 1. Initialize State from Storage
        loadData()
        
        // Load Profile Image
        val savedImage = prefs.getString("user_image", null)
        if (savedImage != null) {
            try {
                profileImageUri = Uri.parse(savedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val storedDate = prefs.getLong(KEY_LAST_DATE, -1L)
        val todayEpoch = LocalDate.now().toEpochDay()

        if (storedDate != todayEpoch) {
            // New Day: Reset counters
            currentSensorSteps = 0
            rewardSteps = 0
            saveStepState(0, 0, todayEpoch) 
            // Note: We don't reset last_sensor_value yet, we wait for the first reading
        } else {
            // Same Day: Load counters
            currentSensorSteps = prefs.getInt(KEY_STEPS_TODAY, 0)
            rewardSteps = prefs.getInt(KEY_REWARDS, 0)
        }

        // 2. Start Listening
        stepSensor.startListening { totalDeviceSteps ->
            handleSensorUpdate(totalDeviceSteps)
        }
    }

    private fun handleSensorUpdate(totalDeviceSteps: Int) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val storedDate = prefs.getLong(KEY_LAST_DATE, -1L)
        
        // Use -2 as "uninitialized" marker for last sensor value
        val lastSensorValue = prefs.getInt(KEY_LAST_SENSOR, -2)

        // Day Change Check (in case app was open overnight)
        if (storedDate != todayEpoch) {
            currentSensorSteps = 0
            rewardSteps = 0 // Optional: Reset rewards too? Yes, usually daily.
            // valid lastSensorValue is still relevant for delta calculation if no reboot occurred
        }

        var delta = 0
        if (lastSensorValue != -2) {
            if (totalDeviceSteps >= lastSensorValue) {
                // Normal case: user walked
                delta = totalDeviceSteps - lastSensorValue
            } else {
                // Reboot case: sensor reset to 0
                // We assume all steps since boot (totalDeviceSteps) are new
                delta = totalDeviceSteps
            }
        } 
        // If lastSensorValue IS -2 (First run), we assume delta = 0 to establish baseline
        // Alternatively, if we want to count steps walked BEFORE app install as 0, this is correct.

        if (delta > 0) {
            currentSensorSteps += delta
            
            // Commit to Storage
            prefs.edit().apply {
                putInt(KEY_STEPS_TODAY, currentSensorSteps)
                putInt(KEY_LAST_SENSOR, totalDeviceSteps)
                putLong(KEY_LAST_DATE, todayEpoch)
                apply()
            }
            
            // Update UI
            updateStepsForDate(LocalDate.now())
        } else {
            // Even if no delta (standing still), we update the baseline
            if (lastSensorValue != totalDeviceSteps) {
                 prefs.edit().putInt(KEY_LAST_SENSOR, totalDeviceSteps).apply()
            }
        }
    }

    fun syncSteps() {
        // Now just a visual force-refresh, as persistence is automatic
        updateStepsForDate(LocalDate.now())
    }

    private fun updateStepsForDate(date: LocalDate) {
        val epoch = date.toEpochDay()
        val currentStats = _dailyStats[epoch] ?: WellbeingStats()

        _dailyStats[epoch] = currentStats.copy(
            stepsCount = currentSensorSteps + rewardSteps,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        saveDailyStats()
    }

    private fun saveStepState(steps: Int, sensorVal: Int, date: Long) {
         prefs.edit().apply {
            putInt(KEY_STEPS_TODAY, steps)
            if (sensorVal != 0) putInt(KEY_LAST_SENSOR, sensorVal) // specific case logic
            putLong(KEY_LAST_DATE, date)
            apply()
        }
    }


    override fun onCleared() {
        super.onCleared()
        stepSensor.stopListening()
    }

    // --- 6. WELLBEING ACTIONS ---

    fun logWater(date: LocalDate, amountMl: Int) {
        val epoch = date.toEpochDay()
        val currentStats = _dailyStats[epoch] ?: WellbeingStats()

        _dailyStats[epoch] = currentStats.copy(
            waterIntakeMl = currentStats.waterIntakeMl + amountMl,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        saveDailyStats()
    }

    fun updateSleep(date: LocalDate, hours: Double) {
        val epoch = date.toEpochDay()
        val currentStats = _dailyStats[epoch] ?: WellbeingStats()

        _dailyStats[epoch] = currentStats.copy(
            sleepDurationHours = hours,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        saveDailyStats()
    }

    fun addSteps(steps: Int) {
        rewardSteps += steps
        // Persist rewards immediately
        prefs.edit().putInt(KEY_REWARDS, rewardSteps).apply()
        updateStepsForDate(LocalDate.now())
    }

    // --- 7. HABIT CRUD LOGIC ---

    fun addHabit(habit: Habit) {
        habits.add(habit)
        saveHabits()
    }

    fun updateHabit(updatedHabit: Habit) {
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
            saveHabits()
        }
    }

    fun deleteHabit(habitId: String) {
        habits.removeAll { it.id == habitId }
        records.removeAll { it.habitId == habitId }
        saveHabits()
        saveRecords()
    }

    fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        val epochDay = date.toEpochDay()
        val existingRecord = records.find { it.habitId == habitId && it.timestamp == epochDay }

        if (existingRecord != null) {
            // Toggle OFF
            records.remove(existingRecord)
        } else {
            // Toggle ON
            records.add(
                HabitRecord(
                    habitId = habitId,
                    timestamp = epochDay,
                    isCompleted = true
                )
            )
            // Reward: Add 300 steps
            addSteps(300)
        }
        saveRecords()
    }

    // --- 8. DIARY CRUD LOGIC ---

    fun addDiaryEntry(entry: DiaryEntry) {
        diaryEntries.add(0, entry)
        saveDiary()
    }

    fun updateDiaryEntry(updatedEntry: DiaryEntry) {
        val index = diaryEntries.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            diaryEntries[index] = updatedEntry
            saveDiary()
        }
    }

    fun deleteDiaryEntry(entryId: String) {
        diaryEntries.removeAll { it.id == entryId }
        saveDiary()
    }

    // --- PERSISTENCE HELPER METHODS ---

    private fun saveHabits() {
        val json = gson.toJson(habits)
        prefs.edit().putString("habits_data", json).apply()
    }

    private fun saveRecords() {
        val json = gson.toJson(records)
        prefs.edit().putString("records_data", json).apply()
    }

    private fun saveDiary() {
        val json = gson.toJson(diaryEntries)
        prefs.edit().putString("diary_data", json).apply()
    }

    private fun saveDailyStats() {
        // Convert mutableStateMap to a plain Map<Long, WellbeingStats> for Gson
        val statsMap: Map<Long, WellbeingStats> = _dailyStats.toMap()
        val json = gson.toJson(statsMap)
        prefs.edit().putString("daily_stats_data", json).apply()
    }

    private fun loadData() {
        // Load Habits
        val habitsJson = prefs.getString("habits_data", null)
        if (habitsJson != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            val loadedHabits: List<Habit> = gson.fromJson(habitsJson, type)
            habits.clear()
            habits.addAll(loadedHabits)
        }

        // Load Records
        val recordsJson = prefs.getString("records_data", null)
        if (recordsJson != null) {
            val type = object : TypeToken<List<HabitRecord>>() {}.type
            val loadedRecords: List<HabitRecord> = gson.fromJson(recordsJson, type)
            records.clear()
            records.addAll(loadedRecords)
        }

        // Load Diary
        val diaryJson = prefs.getString("diary_data", null)
        if (diaryJson != null) {
            val type = object : TypeToken<List<DiaryEntry>>() {}.type
            val loadedDiary: List<DiaryEntry> = gson.fromJson(diaryJson, type)
            diaryEntries.clear()
            diaryEntries.addAll(loadedDiary)
        }

        // Load Daily Wellbeing Stats
        val statsJson = prefs.getString("daily_stats_data", null)
        if (statsJson != null) {
            val type = object : TypeToken<Map<Long, WellbeingStats>>() {}.type
            val loadedStats: Map<Long, WellbeingStats> = gson.fromJson(statsJson, type)
            _dailyStats.clear()
            _dailyStats.putAll(loadedStats)
        }
    }
    // --- AUTH LOGIC ---
// --- AUTHENTICATION LOGIC ---

    var isLoggedIn by mutableStateOf(prefs.getBoolean("is_logged_in", false))
        private set

    // State to hold error messages for the Login Screen
    var loginError by mutableStateOf<String?>(null)
        private set

    /**
     * Saves user credentials locally during Registration.
     */
    fun registerUser(name: String, email: String, pass: String) {
        prefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_password", pass) // Note: Real apps encrypt this!
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

        userName = "Ritual Specialist"
        profileImageUri = null
        isLoggedIn = false
        loginError = null
        currentSensorSteps = 0
        rewardSteps = 0
    }
    /**
     * Checks if entered credentials match the locally saved ones.
     */
    fun validateLogin(email: String, pass: String): Boolean {
        val savedEmail = prefs.getString("user_email", null)
        val savedPass = prefs.getString("user_password", null)

        return when {
            savedEmail == null -> {
                loginError = "No account found. Please register."
                false
            }
            savedEmail == email && savedPass == pass -> {
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

    fun clearLoginError() {
        loginError = null
    }
}