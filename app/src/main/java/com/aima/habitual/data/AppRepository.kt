package com.aima.habitual.data

import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.SleepLogEntry
import com.aima.habitual.model.WellbeingStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides access to all App data.
 */
interface AppRepository {
    // --- Habits ---
    fun getAllHabitsStream(): Flow<List<Habit>>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)

    // --- Habit Records ---
    fun getAllRecordsStream(): Flow<List<HabitRecord>>
    suspend fun insertRecord(record: HabitRecord)
    suspend fun deleteRecord(recordId: String)
    suspend fun findRecord(habitId: String, epochDay: Long): HabitRecord?

    // --- Diary Entries ---
    fun getAllDiaryEntriesStream(): Flow<List<DiaryEntry>>
    suspend fun insertDiaryEntry(entry: DiaryEntry)
    suspend fun updateDiaryEntry(entry: DiaryEntry)
    suspend fun deleteDiaryEntry(entryId: String)

    // --- Wellbeing Stats ---
    fun getAllWellbeingStatsStream(): Flow<List<WellbeingStats>>
    suspend fun insertOrUpdateStats(stats: WellbeingStats)
    suspend fun getStatsForDay(epochDay: Long): WellbeingStats?
    /** Atomically add water for a day (no overwrite risk). */
    suspend fun addWaterForDay(epochDay: Long, amountMl: Int, ts: Long)
    /** Atomically replace sleep hours for a day. */
    suspend fun updateSleepForDay(epochDay: Long, hours: Double, ts: Long)
    /** Atomically replace step count for a day. */
    suspend fun updateStepsForDay(epochDay: Long, steps: Int, ts: Long)

    // --- Sleep Logs ---
    fun getAllSleepLogsStream(): Flow<List<SleepLogEntry>>
    suspend fun insertOrUpdateSleepLog(entry: SleepLogEntry)

    // --- Bulk Operations ---
    suspend fun deleteHabitWithRecords(habitId: String)
    suspend fun deleteAllUserData()

    // --- Backup Snapshots ---
    suspend fun getAllHabitsSnapshot(): List<Habit>
    suspend fun getAllRecordsSnapshot(): List<HabitRecord>
    suspend fun getAllDiaryEntriesSnapshot(): List<DiaryEntry>
    suspend fun getAllWellbeingStatsSnapshot(): List<WellbeingStats>
    suspend fun getAllSleepLogsSnapshot(): List<SleepLogEntry>

    // --- Restore Bulk Inserts ---
    suspend fun insertAllHabits(habits: List<Habit>)
    suspend fun insertAllRecords(records: List<HabitRecord>)
    suspend fun insertAllDiaryEntries(entries: List<DiaryEntry>)
    suspend fun insertAllWellbeingStats(stats: List<WellbeingStats>)
    suspend fun insertAllSleepLogs(logs: List<SleepLogEntry>)
}

class OfflineAppRepository(private val habitDao: HabitDao) : AppRepository {
    override fun getAllHabitsStream() = habitDao.getAllHabits()
    override suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    override suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    override suspend fun deleteHabit(habitId: String) = habitDao.deleteHabit(habitId)

    override fun getAllRecordsStream() = habitDao.getAllRecords()
    override suspend fun insertRecord(record: HabitRecord) = habitDao.insertRecord(record)
    override suspend fun deleteRecord(recordId: String) = habitDao.deleteRecord(recordId)
    override suspend fun findRecord(habitId: String, epochDay: Long) = habitDao.findRecord(habitId, epochDay)

    override fun getAllDiaryEntriesStream() = habitDao.getAllDiaryEntries()
    override suspend fun insertDiaryEntry(entry: DiaryEntry) = habitDao.insertDiaryEntry(entry)
    override suspend fun updateDiaryEntry(entry: DiaryEntry) = habitDao.updateDiaryEntry(entry)
    override suspend fun deleteDiaryEntry(entryId: String) = habitDao.deleteDiaryEntry(entryId)

    override fun getAllWellbeingStatsStream() = habitDao.getAllWellbeingStats()
    override suspend fun insertOrUpdateStats(stats: WellbeingStats) = habitDao.insertOrUpdateStats(stats)
    override suspend fun getStatsForDay(epochDay: Long) = habitDao.getStatsForDay(epochDay)
    override suspend fun addWaterForDay(epochDay: Long, amountMl: Int, ts: Long) = habitDao.addWaterForDay(epochDay, amountMl, ts)
    override suspend fun updateSleepForDay(epochDay: Long, hours: Double, ts: Long) = habitDao.updateSleepForDay(epochDay, hours, ts)
    override suspend fun updateStepsForDay(epochDay: Long, steps: Int, ts: Long) = habitDao.updateStepsForDay(epochDay, steps, ts)

    override fun getAllSleepLogsStream() = habitDao.getAllSleepLogs()
    override suspend fun insertOrUpdateSleepLog(entry: SleepLogEntry) = habitDao.insertOrUpdateSleepLog(entry)

    override suspend fun deleteHabitWithRecords(habitId: String) = habitDao.deleteHabitWithRecords(habitId)
    override suspend fun deleteAllUserData() = habitDao.deleteAllUserData()

    // --- Backup ---
    override suspend fun getAllHabitsSnapshot() = habitDao.getAllHabitsSnapshot()
    override suspend fun getAllRecordsSnapshot() = habitDao.getAllRecordsSnapshot()
    override suspend fun getAllDiaryEntriesSnapshot() = habitDao.getAllDiaryEntriesSnapshot()
    override suspend fun getAllWellbeingStatsSnapshot() = habitDao.getAllWellbeingStatsSnapshot()
    override suspend fun getAllSleepLogsSnapshot() = habitDao.getAllSleepLogsSnapshot()

    // --- Restore ---
    override suspend fun insertAllHabits(habits: List<Habit>) = habitDao.insertAllHabits(habits)
    override suspend fun insertAllRecords(records: List<HabitRecord>) = habitDao.insertAllRecords(records)
    override suspend fun insertAllDiaryEntries(entries: List<DiaryEntry>) = habitDao.insertAllDiaryEntries(entries)
    override suspend fun insertAllWellbeingStats(stats: List<WellbeingStats>) = habitDao.insertAllWellbeingStats(stats)
    override suspend fun insertAllSleepLogs(logs: List<SleepLogEntry>) = habitDao.insertAllSleepLogs(logs)
}

