package com.aima.habitual.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.Priority
import com.aima.habitual.model.WellbeingStats
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    private lateinit var db: HabitualDatabase
    private lateinit var dao: HabitDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HabitualDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
        dao = db.habitDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHabit() = runBlocking {
        val habit = Habit(
            id = "habit_1",
            title = "Test Habit",
            description = "A habit for testing",
            category = "Health",
            priority = Priority.HIGH,
            repeatDays = listOf(1, 3, 5),
            reminderTime = "08:00",
            isReminderEnabled = true,
            createdAt = System.currentTimeMillis()
        )
        dao.insertHabit(habit)
        
        val retrieved = dao.getHabitById("habit_1")
        assertNotNull(retrieved)
        assertEquals("Test Habit", retrieved?.title)
    }

    @Test
    fun deleteHabitWithRecords() = runBlocking {
        // Insert Habit
        val habit = Habit(id = "habit_2", title = "To Delete", category = "General")
        dao.insertHabit(habit)

        // Insert Record for this habit
        val record = HabitRecord(id = "rec_1", habitId = "habit_2", timestamp = 12345L, isCompleted = true)
        dao.insertRecord(record)

        // Verify insertion
        val habitFromDb = dao.getHabitById("habit_2")
        assertNotNull(habitFromDb)
        val records = dao.getAllRecords().first()
        assertEquals(1, records.size)

        // Delete habit using transaction
        dao.deleteHabitWithRecords("habit_2")

        // Verify habit is deleted
        assertNull(dao.getHabitById("habit_2"))
    }

    @Test
    fun insertOrUpdateStats() = runBlocking {
        val stats = WellbeingStats(epochDay = 1000L, stepsCount = 5000, waterIntakeMl = 1000)
        dao.insertOrUpdateStats(stats)

        var retrieved = dao.getStatsForDay(1000L)
        assertNotNull(retrieved)
        assertEquals(5000, retrieved?.stepsCount)

        // Add water atomically
        val newTs = System.currentTimeMillis()
        dao.addWaterForDay(1000L, 500, newTs)

        retrieved = dao.getStatsForDay(1000L)
        assertEquals(1500, retrieved?.waterIntakeMl)
    }
}
