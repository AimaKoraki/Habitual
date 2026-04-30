package com.aima.habitual.network

import com.aima.habitual.model.Quote
import retrofit2.http.GET

/**
 * ZenQuotesApi: Retrofit interface for the public ZenQuotes API.
 *
 * Base URL: https://zenquotes.io/
 * Endpoint: GET /api/random
 *
 * The API returns a JSON array with a single object, e.g.:
 * [{"q": "The journey of a thousand miles...", "a": "Lao Tzu", "h": "..."}]
 *
 * ASSIGNMENT NOTE: This satisfies the requirement to "connect to the internet
 * to get data from a publicly available API."
 */
interface ZenQuotesApi {
    @GET("api/random")
    suspend fun getRandomQuote(): List<Quote>
}
