package com.aima.habitual.data

import androidx.room.*
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.WellbeingStats

/**
 * Data Access Object for all database operations.
 * Provides type-safe, compile-time verified SQL queries for all four entities.
 */
@Dao
interface HabitDao {

    // ─── HABITS ─────────────────────────────────────────────

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    suspend fun getAllHabits(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: String)

    // ─── HABIT RECORDS ──────────────────────────────────────

    @Query("SELECT * FROM habit_records")
    suspend fun getAllRecords(): List<HabitRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HabitRecord)

    @Query("DELETE FROM habit_records WHERE id = :recordId")
    suspend fun deleteRecord(recordId: String)

    @Query("DELETE FROM habit_records WHERE habitId = :habitId")
    suspend fun deleteRecordsByHabitId(habitId: String)

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND timestamp = :epochDay LIMIT 1")
    suspend fun findRecord(habitId: String, epochDay: Long): HabitRecord?

    // ─── DIARY ENTRIES ──────────────────────────────────────

    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    suspend fun getAllDiaryEntries(): List<DiaryEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntry)

    @Update
    suspend fun updateDiaryEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries WHERE id = :entryId")
    suspend fun deleteDiaryEntry(entryId: String)

    // ─── WELLBEING STATS ────────────────────────────────────

    @Query("SELECT * FROM wellbeing_stats")
    suspend fun getAllWellbeingStats(): List<WellbeingStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: WellbeingStats)

    @Query("SELECT * FROM wellbeing_stats WHERE epochDay = :epochDay LIMIT 1")
    suspend fun getStatsForDay(epochDay: Long): WellbeingStats?

    @Query("DELETE FROM wellbeing_stats")
    suspend fun deleteAllStats()

    // ─── BULK DELETE (for profile wipe) ─────────────────────

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_records")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM diary_entries")
    suspend fun deleteAllDiaryEntries()
}
