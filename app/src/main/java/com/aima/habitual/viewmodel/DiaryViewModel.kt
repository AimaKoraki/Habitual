package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aima.habitual.data.HabitualDatabase
import com.aima.habitual.data.OfflineAppRepository
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.Priority
import com.aima.habitual.utils.ReminderManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HabitualDatabase.getInstance(application)
    private val repository = OfflineAppRepository(db.habitDao())
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    var databaseError by mutableStateOf<String?>(null)
        private set

    fun clearDatabaseError() {
        databaseError = null
    }

    val diaryEntries: StateFlow<List<DiaryEntry>> = repository.getAllDiaryEntriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isJournalTabSelected by mutableStateOf(false)

    var journalHabitId by mutableStateOf<String?>(prefs.getString("journal_habit_id", null))
        private set

    var journalHabitTime by mutableStateOf<String?>(prefs.getString("journal_habit_time", null))
        private set

    var onAddSteps: ((Int, LocalDate) -> Unit)? = null

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
                Log.e("DiaryViewModel", "Failed to add journal habit", e)
                databaseError = "Failed to create daily journaling habit."
            }
        }
    }

    fun disableDailyJournalHabit() {
        journalHabitId?.let {
            viewModelScope.launch {
                try {
                    repository.deleteHabitWithRecords(it)
                    ReminderManager.cancelReminder(getApplication(), it)
                } catch (e: Exception) {
                    Log.e("DiaryViewModel", "Failed to delete journal habit", e)
                }
            }
        }
        journalHabitId = null
        journalHabitTime = null
        prefs.edit().apply {
            remove("journal_habit_id")
            remove("journal_habit_time")
            apply()
        }
    }

    fun addDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            try {
                repository.insertDiaryEntry(entry)

                if (entry.isJournal) {
                    journalHabitId?.let { hId ->
                        val today = LocalDate.now()
                        val epochDay = today.toEpochDay()
                        val records = repository.getAllRecordsStream().firstOrNull() ?: emptyList()
                        val isDone = records.any { it.habitId == hId && it.timestamp == epochDay }
                        if (!isDone) {
                            val newRecord = HabitRecord(habitId = hId, timestamp = epochDay, isCompleted = true)
                            repository.insertRecord(newRecord)
                            onAddSteps?.invoke(300, today)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Failed to add diary entry", e)
                databaseError = "Failed to save journal entry. It may not persist across restarts."
            }
        }
    }

    fun updateDiaryEntry(updatedEntry: DiaryEntry) {
        viewModelScope.launch {
            try {
                repository.updateDiaryEntry(updatedEntry)
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Failed to update diary entry", e)
                databaseError = "Failed to save journal modifications."
            }
        }
    }

    fun deleteDiaryEntry(entryId: String) {
        viewModelScope.launch {
            try {
                repository.deleteDiaryEntry(entryId)
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Failed to delete diary entry", e)
                databaseError = "Failed to permanently delete entry."
            }
        }
    }
}
