package com.aima.habitual.data

import android.util.Log
import com.aima.habitual.model.Companion
import com.aima.habitual.network.CompanionApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * CompanionRepository: Handles fetching the raw external JSON file from the internet.
 */
class CompanionRepository {

    // Lazy Retrofit singleton pointing to the raw GitHubusercontent domain.
    private val api: CompanionApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CompanionApi::class.java)
    }

    /**
     * Fetches the list of virtual companions by reading the external JSON file.
     *
     * @return A list of [Companion] objects. If the network request fails, returns an empty list.
     */
    suspend fun getCompanions(): List<Companion> {
        return try {
            Log.d("CompanionRepository", "Fetching external JSON file for companions...")
            val response = api.getCompanionsJsonFile()
            Log.d("CompanionRepository", "Successfully fetched ${response.pets.size} companions from JSON.")
            response.pets
        } catch (e: Exception) {
            Log.e("CompanionRepository", "Failed to fetch companions JSON file.", e)
            emptyList()
        }
    }
}
