package com.aima.habitual.data

import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.SleepLogEntry
import com.aima.habitual.model.WellbeingStats

/**
 * Represents the full backup payload serialized to/from JSON.
 * All Room entities are included so a single file captures the entire user state.
 *
 * @property version Schema version for forward-compatibility; the app can
 *   decide how to handle older backup formats if the schema changes later.
 * @property timestamp Epoch millis when the backup was created.
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val habits: List<Habit> = emptyList(),
    val records: List<HabitRecord> = emptyList(),
    val diaryEntries: List<DiaryEntry> = emptyList(),
    val wellbeingStats: List<WellbeingStats> = emptyList(),
    val sleepLogs: List<SleepLogEntry> = emptyList()
)
