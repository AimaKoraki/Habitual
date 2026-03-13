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
import com.aima.habitual.model.SleepLogEntry
import com.aima.habitual.model.WellbeingStats

/**
 * The Room Database for the Habitual app.
 * Manages all four entities and provides a singleton instance.
 */
@Database(
    entities = [Habit::class, HabitRecord::class, DiaryEntry::class, WellbeingStats::class, SleepLogEntry::class],
    version = 7,
    exportSchema = true
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM habit_records WHERE habitId NOT IN (SELECT id FROM habits)")
                database.execSQL("""
                    CREATE TABLE habit_records_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        habitId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        FOREIGN KEY (habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("INSERT INTO habit_records_new SELECT * FROM habit_records")
                database.execSQL("DROP TABLE habit_records")
                database.execSQL("ALTER TABLE habit_records_new RENAME TO habit_records")
                database.execSQL("CREATE INDEX index_habit_records_habitId ON habit_records(habitId)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP INDEX IF EXISTS index_habit_records_habitId")
                database.execSQL("CREATE INDEX index_habit_records_habitId_timestamp ON habit_records(habitId, timestamp)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sleep_log_entries (
                        dateEpoch INTEGER NOT NULL PRIMARY KEY,
                        durationMinutes INTEGER NOT NULL,
                        quality TEXT NOT NULL
                    )
                """.trimIndent())
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
