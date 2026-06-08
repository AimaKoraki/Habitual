package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aima.habitual.data.HabitualDatabase
import com.aima.habitual.data.OfflineAppRepository
import com.aima.habitual.model.LightSensorManager
import com.aima.habitual.model.SleepLogEntry
import com.aima.habitual.model.StepSensorManager
import com.aima.habitual.model.WellbeingStats
import kotlinx.coroutines.launch
import java.time.LocalDate

class WellbeingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HabitualDatabase.getInstance(application)
    private val repository = OfflineAppRepository(db.habitDao())
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    var databaseError by mutableStateOf<String?>(null)
        private set

    fun clearDatabaseError() {
        databaseError = null
    }

    private val _dailyStats = mutableStateMapOf<Long, WellbeingStats>()

    val wellbeingStats: WellbeingStats
        get() = getStatsForDate(LocalDate.now())

    fun getStatsForDate(date: LocalDate): WellbeingStats {
        return _dailyStats[date.toEpochDay()] ?: WellbeingStats(epochDay = date.toEpochDay())
    }

    private val KEY_STEPS_TODAY = "saved_steps_today"
    private val KEY_LAST_SENSOR = "last_sensor_value"
    private val KEY_LAST_DATE = "last_step_date"
    private val KEY_REWARDS = "saved_rewards_today"

    private val stepSensor = StepSensorManager(application)
    private var currentSensorSteps = 0
    private var rewardSteps = 0

    private val lightSensor = LightSensorManager(application)
    var currentLuxLevel by mutableStateOf(0f)
        private set

    init {
        loadDataFromRoom()
        loadSleepLogs()

        val storedDate = prefs.getLong(KEY_LAST_DATE, -1L)
        val todayEpoch = LocalDate.now().toEpochDay()

        if (storedDate != todayEpoch) {
            currentSensorSteps = 0
            rewardSteps = 0
            prefs.edit().putInt(KEY_REWARDS, 0).apply()
            saveStepState(0, -2, todayEpoch)
        } else {
            currentSensorSteps = prefs.getInt(KEY_STEPS_TODAY, 0)
            rewardSteps = prefs.getInt(KEY_REWARDS, 0)
        }

        stepSensor.startListening { totalDeviceSteps ->
            handleSensorUpdate(totalDeviceSteps)
        }

        lightSensor.startListening { lux ->
            currentLuxLevel = lux
        }
    }

    fun clearCache() {
        _dailyStats.clear()
        _sleepLogs.clear()
        currentSensorSteps = 0
        rewardSteps = 0
    }

    private fun loadDataFromRoom() {
        viewModelScope.launch {
            try {
                repository.getAllWellbeingStatsStream().collect { loadedStats ->
                    for (stat in loadedStats) {
                        val current = _dailyStats[stat.epochDay]
                        if (current == null || stat.lastSyncTimestamp >= current.lastSyncTimestamp) {
                            _dailyStats[stat.epochDay] = stat
                        }
                    }
                    val todayEpoch = LocalDate.now().toEpochDay()
                    val liveSteps = currentSensorSteps + rewardSteps
                    if (liveSteps > 0) {
                        val cached = _dailyStats[todayEpoch] ?: WellbeingStats(epochDay = todayEpoch)
                        if (cached.stepsCount < liveSteps) {
                            _dailyStats[todayEpoch] = cached.copy(stepsCount = liveSteps)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WellbeingViewModel", "Failed to load database data", e)
                databaseError = "Failed to load existing data. Please try restarting the app."
            }
        }
    }

    private fun handleSensorUpdate(totalDeviceSteps: Int) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val storedDate = prefs.getLong(KEY_LAST_DATE, -1L)
        val lastSensorValue = prefs.getInt(KEY_LAST_SENSOR, -2)

        var delta = 0
        if (lastSensorValue != -2) {
            delta = if (totalDeviceSteps >= lastSensorValue) {
                totalDeviceSteps - lastSensorValue
            } else {
                totalDeviceSteps
            }
        }

        if (storedDate != todayEpoch) {
            currentSensorSteps = delta
            rewardSteps = 0
            prefs.edit().putInt(KEY_REWARDS, 0).apply()
            saveStepState(currentSensorSteps, totalDeviceSteps, todayEpoch)
            updateStepsForDate(LocalDate.now())
            return
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
        val cached = _dailyStats[epoch] ?: WellbeingStats(epochDay = epoch)
        _dailyStats[epoch] = cached.copy(stepsCount = steps, lastSyncTimestamp = ts)

        viewModelScope.launch {
            try {
                val existing = repository.getStatsForDay(epoch)
                if (existing == null) {
                    repository.insertOrUpdateStats(WellbeingStats(epochDay = epoch))
                }
                repository.updateStepsForDay(epoch, steps, ts)
            } catch (e: Exception) {
                Log.e("WellbeingViewModel", "Failed to update step stats", e)
                databaseError = "Failed to save steps data."
            }
        }
    }

    private fun saveStepState(steps: Int, sensorVal: Int, date: Long) {
        prefs.edit().apply {
            putInt(KEY_STEPS_TODAY, steps)
            putInt(KEY_LAST_SENSOR, sensorVal)
            putLong(KEY_LAST_DATE, date)
            apply()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepSensor.stopListening()
        lightSensor.stopListening()
    }

    fun logWater(date: LocalDate, amountMl: Int) {
        val epoch = date.toEpochDay()
        val ts = System.currentTimeMillis()
        val cached = _dailyStats[epoch] ?: WellbeingStats(epochDay = epoch)
        _dailyStats[epoch] = cached.copy(waterIntakeMl = cached.waterIntakeMl + amountMl, lastSyncTimestamp = ts)

        viewModelScope.launch {
            try {
                val existing = repository.getStatsForDay(epoch)
                if (existing == null) {
                    repository.insertOrUpdateStats(WellbeingStats(epochDay = epoch))
                }
                repository.addWaterForDay(epoch, amountMl, ts)

                val refreshed = repository.getStatsForDay(epoch)
                if (refreshed != null) _dailyStats[epoch] = refreshed
            } catch (e: Exception) {
                Log.e("WellbeingViewModel", "Failed to update water stats", e)
                databaseError = "Failed to save water intake."
            }
        }
    }

    fun updateSleep(date: LocalDate, hours: Double) {
        val epoch = date.toEpochDay()
        val ts = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                val existing = repository.getStatsForDay(epoch)
                if (existing == null) {
                    repository.insertOrUpdateStats(WellbeingStats(epochDay = epoch))
                }
                repository.updateSleepForDay(epoch, hours, ts)

                val refreshed = repository.getStatsForDay(epoch)
                if (refreshed != null) _dailyStats[epoch] = refreshed
            } catch (e: Exception) {
                Log.e("WellbeingViewModel", "Failed to update sleep stats", e)
                databaseError = "Failed to save sleep data."
            }
        }
    }

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
                Log.e("WellbeingViewModel", "Failed to save sleep log", e)
                databaseError = "Failed to save sleep log."
            }
        }

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
                Log.e("WellbeingViewModel", "Failed to load sleep logs", e)
            }
        }
    }

    fun addSteps(steps: Int, date: LocalDate = LocalDate.now()) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val targetEpoch = date.toEpochDay()

        if (targetEpoch == todayEpoch) {
            val storedDate = prefs.getLong(KEY_LAST_DATE, -1L)
            if (storedDate != todayEpoch) {
                currentSensorSteps = 0
                rewardSteps = 0
                prefs.edit().putInt(KEY_REWARDS, 0).apply()
                val lastSensorValue = prefs.getInt(KEY_LAST_SENSOR, -2)
                saveStepState(0, lastSensorValue, todayEpoch)
            }

            rewardSteps += steps
            if (rewardSteps < 0) rewardSteps = 0
            prefs.edit().putInt(KEY_REWARDS, rewardSteps).apply()
            updateStepsForDate(LocalDate.now())
        } else {
            val cached = _dailyStats[targetEpoch] ?: WellbeingStats(epochDay = targetEpoch)
            var newSteps = cached.stepsCount + steps
            if (newSteps < 0) newSteps = 0
            val ts = System.currentTimeMillis()
            _dailyStats[targetEpoch] = cached.copy(stepsCount = newSteps, lastSyncTimestamp = ts)

            viewModelScope.launch {
                try {
                    val existing = repository.getStatsForDay(targetEpoch)
                    if (existing == null) {
                        repository.insertOrUpdateStats(WellbeingStats(epochDay = targetEpoch))
                    }
                    repository.updateStepsForDay(targetEpoch, newSteps, ts)
                } catch (e: Exception) {
                    Log.e("WellbeingViewModel", "Failed to update step stats for past date", e)
                }
            }
        }
    }

    var voiceCommandFeedback by mutableStateOf<String?>(null)
        private set

    fun clearVoiceCommandFeedback() {
        voiceCommandFeedback = null
    }

    fun processVoiceCommand(command: String, date: LocalDate) {
        val lowerCommand = command.lowercase()
        val numberRegex = Regex("\\d+")
        val match = numberRegex.find(lowerCommand)
        var number = match?.value?.toIntOrNull()

        if (number == null) {
            val wordsToNumbers = mapOf(
                "one" to 1, "a" to 1, "two" to 2, "three" to 3, "four" to 4,
                "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10
            )
            for ((word, value) in wordsToNumbers) {
                if (lowerCommand.contains(word)) {
                    number = value
                    break
                }
            }
        }

        if (lowerCommand.contains("water")) {
            val amount = number ?: 1
            var mlToAdd = 0
            if (lowerCommand.contains("ml") || lowerCommand.contains("milliliters")) {
                mlToAdd = amount
            } else if (lowerCommand.contains("glass") || lowerCommand.contains("glasses") || lowerCommand.contains("cup") || lowerCommand.contains("cups")) {
                mlToAdd = amount * 250
            } else if (lowerCommand.contains("oz") || lowerCommand.contains("ounces")) {
                mlToAdd = amount * 30
            } else {
                mlToAdd = amount * 250
            }
            logWater(date, mlToAdd)
            voiceCommandFeedback = "Logged $mlToAdd ml of water."

        } else if (lowerCommand.contains("sleep")) {
            val amount = number ?: 8
            val hoursToAdd = if (lowerCommand.contains("minute") || lowerCommand.contains("minutes")) {
                amount / 60.0
            } else {
                amount.toDouble()
            }
            val durationMinutes = (hoursToAdd * 60).toInt()
            val existingQuality = getSleepLog(date)?.quality ?: "Good"
            saveSleepLog(date, durationMinutes, existingQuality)

            voiceCommandFeedback = "Logged ${String.format("%.1f", hoursToAdd)} hours of sleep."

        } else if (lowerCommand.contains("step")) {
            val amount = number ?: 1000
            addSteps(amount)
            voiceCommandFeedback = "Added $amount steps."
        } else {
            voiceCommandFeedback = "Could not understand command: \"$command\""
        }
    }
}
