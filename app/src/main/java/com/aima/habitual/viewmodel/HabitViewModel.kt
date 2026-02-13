package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.aima.habitual.model.*
import com.aima.habitual.model.StepSensorManager
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

    // --- 3. USER PROFILE LOGIC ---
    // Loads saved name or defaults to "Ritual Specialist"
    var userName by mutableStateOf(
        prefs.getString("user_name", "Ritual Specialist") ?: "Ritual Specialist"
    )
        private set

    fun updateUserName(newName: String) {
        userName = newName
        prefs.edit().putString("user_name", newName).apply()
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

    // --- 5. SENSOR & STEP LOGIC ---
    private val stepSensor = StepSensorManager(application)
    private var startSteps = -1       // Sensor value at app start
    private var currentSensorSteps = 0 // Steps walked in this session
    private var rewardSteps = 0       // Steps earned from habits

    init {
        // A. Load saved reward steps for today (optional refinement)
        rewardSteps = prefs.getInt("saved_rewards_today", 0)

        // B. Start listening to hardware sensor
        stepSensor.startListening { totalDeviceSteps ->
            if (startSteps == -1) {
                startSteps = totalDeviceSteps
            }
            // Calculate active steps
            currentSensorSteps = totalDeviceSteps - startSteps

            // Sensor data always updates TODAY's record
            updateStepsForDate(LocalDate.now())
        }
    }

    /**
     * Syncs current steps and saves to storage.
     * Call this when the "Sync" button is clicked.
     */
    fun syncSteps() {
        // Force UI update
        updateStepsForDate(LocalDate.now())

        // Save total to prevent data loss
        val totalStepsToSave = currentSensorSteps + rewardSteps
        prefs.edit().apply {
            putInt("saved_rewards_today", rewardSteps) // Simplified persistence logic
            putLong("last_sync_time", System.currentTimeMillis())
            apply()
        }
    }

    private fun updateStepsForDate(date: LocalDate) {
        val epoch = date.toEpochDay()
        val currentStats = _dailyStats[epoch] ?: WellbeingStats()

        _dailyStats[epoch] = currentStats.copy(
            stepsCount = currentSensorSteps + rewardSteps,
            lastSyncTimestamp = System.currentTimeMillis()
        )
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
    }

    fun updateSleep(date: LocalDate, hours: Double) {
        val epoch = date.toEpochDay()
        val currentStats = _dailyStats[epoch] ?: WellbeingStats()

        _dailyStats[epoch] = currentStats.copy(
            sleepDurationHours = hours,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }

    fun addSteps(steps: Int) {
        rewardSteps += steps
        updateStepsForDate(LocalDate.now())
    }

    // --- 7. HABIT CRUD LOGIC ---

    fun addHabit(habit: Habit) {
        habits.add(habit)
    }

    fun updateHabit(updatedHabit: Habit) {
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
        }
    }

    fun deleteHabit(habitId: String) {
        habits.removeAll { it.id == habitId }
        records.removeAll { it.habitId == habitId }
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
    }

    // --- 8. DIARY CRUD LOGIC ---

    fun addDiaryEntry(entry: DiaryEntry) {
        diaryEntries.add(0, entry)
    }

    fun updateDiaryEntry(updatedEntry: DiaryEntry) {
        val index = diaryEntries.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            diaryEntries[index] = updatedEntry
        }
    }

    fun deleteDiaryEntry(entryId: String) {
        diaryEntries.removeAll { it.id == entryId }
    }
    // --- AUTH LOGIC ---

    // State to track if user is authenticated
    var isLoggedIn by mutableStateOf(prefs.getBoolean("is_logged_in", false))
        private set

    fun login() {
        isLoggedIn = true
        prefs.edit().putBoolean("is_logged_in", true).apply()
    }

    fun logout() {
        isLoggedIn = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }
}