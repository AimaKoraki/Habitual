package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

/**
 * Quote: Represents a single motivational quote.
 *
 * Maps to both the external ZenQuotes API JSON response and the
 * local fallback_quotes.json asset file. Using @SerializedName to handle
 * the ZenQuotes API's compact field names ("q" = quote, "a" = author).
 *
 * Data source evidence:
 *  - ONLINE: Fetched from https://zenquotes.io/api/random via Retrofit
 *  - OFFLINE: Parsed from app/src/main/assets/fallback_quotes.json
 */
data class Quote(
    @SerializedName("q") val text: String,
    @SerializedName("a") val author: String
)
