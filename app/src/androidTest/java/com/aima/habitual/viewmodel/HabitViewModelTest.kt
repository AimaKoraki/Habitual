package com.aima.habitual.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aima.habitual.model.Habit
import com.aima.habitual.model.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HabitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HabitViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = HabitViewModel(app)
    }

    @After
    fun tearDown() {
        // Clean up database so it doesn't pollute subsequent tests
        viewModel.deleteProfile() 
        Dispatchers.resetMain()
    }

    @Test
    fun testLevelingLogic() = runTest {
        testScheduler.advanceUntilIdle()

        val habit = Habit(
            title = "Level Up Habit",
            category = "Health",
            priority = Priority.HIGH,
            repeatDays = listOf(0, 1, 2, 3, 4, 5, 6)
        )
        viewModel.addHabit(habit)
        testScheduler.advanceUntilIdle()

        val initialLevel = viewModel.currentLevel

        val today = LocalDate.now()
        viewModel.toggleHabitCompletion(habit.id, today)
        testScheduler.advanceUntilIdle()

        viewModel.toggleHabitCompletion(habit.id, today.plusDays(1))
        testScheduler.advanceUntilIdle()

        // 2 unique completions = 1 level up
        assertEquals(initialLevel + 1, viewModel.currentLevel)
    }

    @Test
    fun testWellbeingWaterSync() = runTest {
        testScheduler.advanceUntilIdle()
        val today = LocalDate.now()
        
        val initialWater = viewModel.getStatsForDate(today).waterIntakeMl
        viewModel.logWater(today, 250)
        testScheduler.advanceUntilIdle()
        
        val newWater = viewModel.getStatsForDate(today).waterIntakeMl
        assertEquals(initialWater + 250, newWater)
    }

    @Test
    fun testAddStepsUpdatesRewardSteps() = runTest {
        testScheduler.advanceUntilIdle()
        
        val today = LocalDate.now()
        val initialStats = viewModel.getStatsForDate(today)
        val initialSteps = initialStats.stepsCount
        
        viewModel.addSteps(300)
        testScheduler.advanceUntilIdle()
        
        val newStats = viewModel.getStatsForDate(today)
        assertEquals(initialSteps + 300, newStats.stepsCount)
    }
}
