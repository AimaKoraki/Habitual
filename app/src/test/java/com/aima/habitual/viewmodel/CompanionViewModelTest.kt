package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompanionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApp: Application
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockApp = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        val assetManager = mockk<android.content.res.AssetManager>(relaxed = true)
        every { mockApp.assets } returns assetManager
        val dummyJson = """{ "companions": [ { "name": "Novice", "species": "Cat", "description": "...", "requiredLevel": 0, "spriteAsset": "cat", "unlockStatus": false }, { "name": "Journeyman", "species": "Dog", "description": "...", "requiredLevel": 5, "spriteAsset": "dog", "unlockStatus": false } ] }"""
        every { assetManager.open("companions/companions_list.json") } returns java.io.ByteArrayInputStream(dummyJson.toByteArray())

        every { mockApp.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit
        every { mockPrefs.getString(any(), any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads companions and applies user level`() = runTest {
        val viewModel = CompanionViewModel(mockApp)
        
        // Let the coroutine load companions
        testScheduler.advanceUntilIdle()

        val companions = viewModel.companions.value
        // By default, lastUserLevel is 0
        // We just ensure we didn't crash and the flow has state
        assertTrue(companions.isNotEmpty())
    }

    @Test
    fun `onUserLevelChanged unlocks new companions`() = runTest {
        val viewModel = CompanionViewModel(mockApp)
        testScheduler.advanceUntilIdle()

        // Advance to level 10 to ensure we unlock level > 0 companions
        viewModel.onUserLevelChanged(10)
        
        val companions = viewModel.companions.value
        // Verify any companion with requiredLevel <= 10 is unlocked
        val unlockedCompanions = companions.filter { it.requiredLevel <= 10 }
        assertTrue(unlockedCompanions.all { it.unlockStatus })
    }

    @Test
    fun `setActive updates active companion if unlocked`() = runTest {
        val viewModel = CompanionViewModel(mockApp)
        testScheduler.advanceUntilIdle()

        // Force a high level so all are unlocked
        viewModel.onUserLevelChanged(100)

        val firstCompanion = viewModel.companions.value.first().name
        viewModel.setActive(firstCompanion)

        assertEquals(firstCompanion, viewModel.activeCompanionName.value)
        verify { mockEditor.putString("active_companion_name", firstCompanion) }
    }
}
