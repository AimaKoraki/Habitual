package com.aima.habitual.data

import androidx.room.TypeConverter

/**
 * Room TypeConverters for complex data types that cannot be stored directly in SQLite.
 * Converts List<Int> and List<String> to/from comma-separated Strings.
 */
class Converters {

    // --- List<Int> (used by Habit.repeatDays) ---

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    // --- List<String> (used by DiaryEntry.tags) ---

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString("|||")  // Use a delimiter unlikely to appear in tag text
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|||")
    }
}
