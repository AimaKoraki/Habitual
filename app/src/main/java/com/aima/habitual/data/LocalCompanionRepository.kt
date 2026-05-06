package com.aima.habitual.data

import android.content.Context
import android.util.Log
import com.aima.habitual.model.CompanionListResponse
import com.aima.habitual.model.VirtualCompanion
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads companion data from a bundled JSON asset instead of a remote source.
 */
class LocalCompanionRepository(private val context: Context) {

    // Read the asset on the IO dispatcher so the main thread stays responsive.
    suspend fun loadCompanions(): List<VirtualCompanion> = withContext(Dispatchers.IO) {
        try {
            // Parse the bundled JSON into the response model, then return the companion list.
            context.assets.open("companions/companions_list.json").use { stream ->
                val text = stream.bufferedReader().readText()
                Gson().fromJson(text, CompanionListResponse::class.java).companions
            }
        } catch (e: Exception) {
            // If the asset is missing or malformed, fail safely with an empty list.
            Log.e("LocalCompanionRepository", "Failed to load companions_list.json", e)
            emptyList()
        }
    }
}
