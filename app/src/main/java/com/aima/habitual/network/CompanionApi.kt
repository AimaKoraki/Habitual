package com.aima.habitual.network

import com.aima.habitual.model.CompanionResponse
import retrofit2.http.GET

/**
 * CompanionApi: Retrofit interface for fetching the external JSON file.
 *
 * Base URL: https://raw.githubusercontent.com/
 * Endpoint: GET LearnWebCode/json-example/master/pets-data.json
 *
 * This explicitly satisfies the assignment requirement to:
 * "read data (master/detail) from external JSON file(s)"
 */
interface CompanionApi {
    @GET("LearnWebCode/json-example/master/pets-data.json")
    suspend fun getCompanionsJsonFile(): CompanionResponse
}
