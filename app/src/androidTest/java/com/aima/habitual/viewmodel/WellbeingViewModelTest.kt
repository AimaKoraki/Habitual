package com.aima.habitual.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class WellbeingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WellbeingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = WellbeingViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
