package com.aima.habitual.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aima.habitual.data.HabitualDatabase
import com.aima.habitual.data.OfflineAppRepository
import com.aima.habitual.data.QuoteRepository
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.Quote
import com.aima.habitual.utils.ConnectivityStatus
import com.aima.habitual.utils.NetworkConnectivityObserver
import com.aima.habitual.utils.ReminderManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HabitualDatabase.getInstance(application)
    private val repository = OfflineAppRepository(db.habitDao())
    private val quoteRepository = QuoteRepository(application)

    var dailyQuote by mutableStateOf<Quote?>(null)
        private set

    fun fetchDailyQuote() {
        viewModelScope.launch {
            try {
                dailyQuote = quoteRepository.getQuote()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Failed to fetch daily quote", e)
            }
        }
    }

    val habits: StateFlow<List<Habit>> = repository.getAllHabitsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records: StateFlow<List<HabitRecord>> = repository.getAllRecordsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var databaseError by mutableStateOf<String?>(null)
        private set

    fun clearDatabaseError() {
        databaseError = null
    }

    private val connectivityObserver = NetworkConnectivityObserver(application)
    val networkStatus: StateFlow<ConnectivityStatus> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectivityStatus.Available)

    private val uniqueCompletions: Int
        get() = records.value.map { it.habitId to it.timestamp }.distinct().size

    val currentLevel: Int
        get() = uniqueCompletions / 2

    val habitsForNextLevel: Int
        get() = 2 - (uniqueCompletions % 2)

    val levelProgress: Float
        get() = (uniqueCompletions % 2) / 2f

    // Callback for WellbeingViewModel
    var onAddSteps: ((Int, LocalDate) -> Unit)? = null

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
                    onAddSteps?.invoke(-300, date)
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
                    onAddSteps?.invoke(300, date)
                } catch (e: Exception) {
                    Log.e("HabitViewModel", "Failed to create habit record", e)
                    databaseError = "Failed to register activity."
                }
            }
        }
    }
}