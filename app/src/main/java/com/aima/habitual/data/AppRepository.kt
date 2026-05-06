package com.aima.habitual.data

import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.SleepLogEntry
import com.aima.habitual.model.WellbeingStats
import kotlinx.coroutines.flow.Flow

/**
 * Central data-access contract for the app.
 *
 * Most callers use this instead of talking to the DAO directly so the rest of the
 * app stays decoupled from the storage layer.
 */
interface AppRepository {
    // Habit data is exposed as a Flow so UI layers can react to changes automatically.
    fun getAllHabitsStream(): Flow<List<Habit>>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: String)

    // Habit records track day-by-day completion state and support lookup by habit/date.
    fun getAllRecordsStream(): Flow<List<HabitRecord>>
    suspend fun insertRecord(record: HabitRecord)
    suspend fun deleteRecord(recordId: String)
    suspend fun findRecord(habitId: String, epochDay: Long): HabitRecord?

    // Diary entries are stored separately from habits so notes can be edited independently.
    fun getAllDiaryEntriesStream(): Flow<List<DiaryEntry>>
    suspend fun insertDiaryEntry(entry: DiaryEntry)
    suspend fun updateDiaryEntry(entry: DiaryEntry)
    suspend fun deleteDiaryEntry(entryId: String)

    // Wellbeing stats use atomic helpers to avoid overwriting concurrent updates.
    fun getAllWellbeingStatsStream(): Flow<List<WellbeingStats>>
    suspend fun insertOrUpdateStats(stats: WellbeingStats)
    suspend fun getStatsForDay(epochDay: Long): WellbeingStats?
    /** Atomically add water for a day (no overwrite risk). */
    suspend fun addWaterForDay(epochDay: Long, amountMl: Int, ts: Long)
    /** Atomically replace sleep hours for a day. */
    suspend fun updateSleepForDay(epochDay: Long, hours: Double, ts: Long)
    /** Atomically replace step count for a day. */
    suspend fun updateStepsForDay(epochDay: Long, steps: Int, ts: Long)

    // Sleep logs are normalized records that can be updated without duplicating entries.
    fun getAllSleepLogsStream(): Flow<List<SleepLogEntry>>
    suspend fun insertOrUpdateSleepLog(entry: SleepLogEntry)

    // Bulk operations are used when deleting a habit or clearing the app's local data.
    suspend fun deleteHabitWithRecords(habitId: String)
    suspend fun deleteAllUserData()

    // Snapshots capture a point-in-time view for export/backup.
    suspend fun getAllHabitsSnapshot(): List<Habit>
    suspend fun getAllRecordsSnapshot(): List<HabitRecord>
    suspend fun getAllDiaryEntriesSnapshot(): List<DiaryEntry>
    suspend fun getAllWellbeingStatsSnapshot(): List<WellbeingStats>
    suspend fun getAllSleepLogsSnapshot(): List<SleepLogEntry>

    // Restore methods rehydrate the database from a saved snapshot.
    suspend fun insertAllHabits(habits: List<Habit>)
    suspend fun insertAllRecords(records: List<HabitRecord>)
    suspend fun insertAllDiaryEntries(entries: List<DiaryEntry>)
    suspend fun insertAllWellbeingStats(stats: List<WellbeingStats>)
    suspend fun insertAllSleepLogs(logs: List<SleepLogEntry>)
}

/**
 * Offline implementation that forwards each call to the DAO.
 *
 * Keeping this layer in place makes it easy to swap the backing store later
 * without changing the rest of the app.
 */
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

