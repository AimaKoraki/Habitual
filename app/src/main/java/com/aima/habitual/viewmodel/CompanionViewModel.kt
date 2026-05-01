package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aima.habitual.data.LocalCompanionRepository
import com.aima.habitual.model.VirtualCompanion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalCompanionRepository(application)
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    private val _companions = MutableStateFlow<List<VirtualCompanion>>(emptyList())
    val companions: StateFlow<List<VirtualCompanion>> = _companions.asStateFlow()

    private val _activeCompanionName = MutableStateFlow(prefs.getString(KEY_ACTIVE, null))
    val activeCompanionName: StateFlow<String?> = _activeCompanionName.asStateFlow()

    private var lastUserLevel: Int = 0

    init {
        viewModelScope.launch {
            val raw = repository.loadCompanions()
            _companions.value = raw.map { it.copy(unlockStatus = lastUserLevel >= it.requiredLevel) }
        }
    }

    fun onUserLevelChanged(userLevel: Int) {
        if (userLevel == lastUserLevel && _companions.value.isNotEmpty()) return
        lastUserLevel = userLevel
        _companions.update { current ->
            current.map { c ->
                val unlocked = userLevel >= c.requiredLevel
                if (unlocked && prefs.getString(unlockKey(c.name), null) == null) {
                    prefs.edit().putString(unlockKey(c.name), Instant.now().toString()).apply()
                }
                c.copy(unlockStatus = unlocked)
            }
        }
        val active = _activeCompanionName.value
        if (active != null && _companions.value.firstOrNull { it.name == active }?.unlockStatus != true) {
            setActive(null)
        }
    }

    fun setActive(name: String?) {
        if (name != null) {
            val target = _companions.value.firstOrNull { it.name == name }
            if (target == null || !target.unlockStatus) return
        }
        prefs.edit().apply {
            if (name == null) remove(KEY_ACTIVE) else putString(KEY_ACTIVE, name)
        }.apply()
        _activeCompanionName.value = name
    }

    private fun unlockKey(name: String) = "companion_unlocked_$name"

    companion object {
        private const val KEY_ACTIVE = "active_companion_name"
    }
}
