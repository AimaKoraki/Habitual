package com.aima.habitual.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aima.habitual.model.DiaryEntry
import com.aima.habitual.model.Habit
import com.aima.habitual.model.HabitRecord
import com.aima.habitual.model.WellbeingStats

/**
 * The Room Database for the Habitual app.
 * Manages all four entities and provides a singleton instance.
 */
@Database(
    entities = [Habit::class, HabitRecord::class, DiaryEntry::class, WellbeingStats::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitualDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN mood TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN photoUri TEXT")
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN audioFilePath TEXT")
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN locationText TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN isJournal INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: HabitualDatabase? = null

        /**
         * Thread-safe singleton accessor.
         * Uses double-checked locking to avoid multiple database instances.
         */
        fun getInstance(context: Context): HabitualDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitualDatabase::class.java,
                    "habitual_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
