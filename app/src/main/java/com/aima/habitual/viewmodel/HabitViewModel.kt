package com.aima.habitual.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import java.time.LocalDate

/**
 * HabitViewModel acts as the single source of truth for the application state.
 */
class HabitViewModel : ViewModel() {

    // Master list of all created habit goals
    val habits = mutableStateListOf<Habit>()

    // Master list of all historical completion records
    val records = mutableStateListOf<HabitRecord>()

    /**
     * Adds a new habit to the list.
     * Called when the 'Save' button in HabitForm is clicked.
     */
    fun addHabit(habit: Habit) {
        habits.add(habit)
    }

    /**
     * Toggles the completion status for a specific habit on a specific date.
     * Uses HabitRecord to track history independently of the Habit goal.
     */
    fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        val epochDay = date.toEpochDay()
        val existingRecord = records.find {
            it.habitId == habitId && it.timestamp == epochDay
        }

        if (existingRecord != null) {
            records.remove(existingRecord) // Uncheck the habit
        } else {
            records.add(
                HabitRecord(
                    habitId = habitId,
                    timestamp = epochDay,
                    isCompleted = true
                )
            )
        }
    }
}