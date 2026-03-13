package com.aima.habitual.data

import androidx.room.TypeConverter

/**
 * Room TypeConverters for complex data types that cannot be stored directly in SQLite.
 * Converts List<Int> and List<String> to/from comma-separated Strings.
 *
 * Null handling is symmetric: null lists are stored as SQL NULL and
 * returned as null, preserving the distinction between null and empty.
 */
class Converters {

    // --- List<Int> (used by Habit.repeatDays) ---

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value == null) return null
        if (value.isBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    // --- List<String> (used by DiaryEntry.tags) ---

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString("|||")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        if (value.isBlank()) return emptyList()
        return value.split("|||")
    }
}
