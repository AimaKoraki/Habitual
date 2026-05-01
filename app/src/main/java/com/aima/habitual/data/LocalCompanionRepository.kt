package com.aima.habitual.data

import android.content.Context
import android.util.Log
import com.aima.habitual.model.CompanionListResponse
import com.aima.habitual.model.VirtualCompanion
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalCompanionRepository(private val context: Context) {

    suspend fun loadCompanions(): List<VirtualCompanion> = withContext(Dispatchers.IO) {
        try {
            context.assets.open("companions/companions_list.json").use { stream ->
                val text = stream.bufferedReader().readText()
                Gson().fromJson(text, CompanionListResponse::class.java).companions
            }
        } catch (e: Exception) {
            Log.e("LocalCompanionRepository", "Failed to load companions_list.json", e)
            emptyList()
        }
    }
}
