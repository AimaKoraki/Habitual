package com.aima.habitual.data

import android.content.Context
import android.util.Log
import com.aima.habitual.model.Quote
import com.aima.habitual.network.ZenQuotesApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * QuoteRepository: Single source of truth for the Daily Quote feature.
 *
 * Implements an "online-first, offline-fallback" strategy to satisfy two
 * distinct assignment requirements simultaneously:
 *
 *  REQUIREMENT 3 — Local JSON file (offline):
 *    If a network error occurs (e.g. the device is offline), this repository
 *    reads from the bundled asset file `fallback_quotes.json` using
 *    Android's AssetManager and parses it with Gson.
 *
 *  REQUIREMENT 4 — External API (online):
 *    When online, it calls the publicly available ZenQuotes REST API
 *    (https://zenquotes.io/api/random) via Retrofit to fetch a fresh quote.
 */
class QuoteRepository(private val context: Context) {

    // Lazy Retrofit singleton — only constructed the first time it is needed.
    private val api: ZenQuotesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://zenquotes.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZenQuotesApi::class.java)
    }

    /**
     * Fetches a motivational quote.
     *
     * 1. Attempts live API call to ZenQuotes.
     * 2. On any exception (no network, timeout, etc.), silently falls back to
     *    reading a random entry from the local [fallback_quotes.json] asset.
     *
     * @return A [Quote] object, never null — offline fallback guarantees content.
     */
    suspend fun getQuote(): Quote {
        return try {
            // ONLINE PATH: Fetch from the public ZenQuotes API
            Log.d("QuoteRepository", "Fetching quote from ZenQuotes API...")
            val quotes = api.getRandomQuote()
            quotes.first().also {
                Log.d("QuoteRepository", "Online quote loaded: \"${it.text}\" — ${it.author}")
            }
        } catch (e: Exception) {
            // OFFLINE PATH: Load from the bundled local JSON asset
            Log.w("QuoteRepository", "Network unavailable, loading from local fallback_quotes.json", e)
            loadFallbackQuote()
        }
    }

    /**
     * Reads and parses [fallback_quotes.json] from the app's assets directory.
     * Returns a random quote from the list to provide variety even offline.
     *
     * ASSIGNMENT NOTE: This satisfies the requirement to "read from a local JSON
     * file when the application is offline."
     */
    private fun loadFallbackQuote(): Quote {
        val json = context.assets.open("fallback_quotes.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<Quote>>() {}.type
        val quotes: List<Quote> = Gson().fromJson(json, type)

        return quotes.random().also {
            Log.d("QuoteRepository", "Offline fallback quote loaded: \"${it.text}\" — ${it.author}")
        }
    }
}
