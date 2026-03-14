package com.aima.habitual.data

import androidx.room.*
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.SleepLogEntry
import com.aima.habitual.model.WellbeingStats
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for all database operations.
 * Provides type-safe, compile-time verified SQL queries for all four entities.
 */
@Dao
interface HabitDao {

    // ─── HABITS ─────────────────────────────────────────────

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: String)

    // ─── HABIT RECORDS ──────────────────────────────────────

    @Query("SELECT * FROM habit_records")
    fun getAllRecords(): Flow<List<HabitRecord>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecord(record: HabitRecord)

    @Query("DELETE FROM habit_records WHERE id = :recordId")
    suspend fun deleteRecord(recordId: String)

    @Query("DELETE FROM habit_records WHERE habitId = :habitId")
    suspend fun deleteRecordsByHabitId(habitId: String)

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND timestamp = :epochDay LIMIT 1")
    suspend fun findRecord(habitId: String, epochDay: Long): HabitRecord?

    // ─── DIARY ENTRIES ──────────────────────────────────────

    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    fun getAllDiaryEntries(): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDiaryEntry(entry: DiaryEntry)

    @Update
    suspend fun updateDiaryEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries WHERE id = :entryId")
    suspend fun deleteDiaryEntry(entryId: String)

    // ─── WELLBEING STATS ────────────────────────────────────

    @Query("SELECT * FROM wellbeing_stats")
    fun getAllWellbeingStats(): Flow<List<WellbeingStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: WellbeingStats)

    @Query("SELECT * FROM wellbeing_stats WHERE epochDay = :epochDay LIMIT 1")
    suspend fun getStatsForDay(epochDay: Long): WellbeingStats?

    @Query("DELETE FROM wellbeing_stats")
    suspend fun deleteAllStats()

    // ─── SLEEP LOG ENTRIES ───────────────────────────────

    @Query("SELECT * FROM sleep_log_entries")
    fun getAllSleepLogs(): Flow<List<SleepLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSleepLog(entry: SleepLogEntry)

    @Query("SELECT * FROM sleep_log_entries WHERE dateEpoch = :epochDay LIMIT 1")
    suspend fun getSleepLogForDay(epochDay: Long): SleepLogEntry?

    @Query("DELETE FROM sleep_log_entries")
    suspend fun deleteAllSleepLogs()

    // ─── BULK DELETE (for profile wipe) ─────────────────────

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_records")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM diary_entries")
    suspend fun deleteAllDiaryEntries()

    // ─── TRANSACTIONAL OPERATIONS ────────────────────────

    @Transaction
    suspend fun deleteHabitWithRecords(habitId: String) {
        deleteRecordsByHabitId(habitId)
        deleteHabit(habitId)
    }

    @Transaction
    suspend fun deleteAllUserData() {
        deleteAllRecords()
        deleteAllHabits()
        deleteAllDiaryEntries()
        deleteAllSleepLogs()
        deleteAllStats()
    }
}
