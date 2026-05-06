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
 * Room database for the Habitual app.
 *
 * This class owns the local schema, the migration chain between versions,
 * and the single shared database instance used by the app.
 */
@Database(
    // All persisted app data lives in these tables.
    entities = [Habit::class, HabitRecord::class, DiaryEntry::class, WellbeingStats::class, SleepLogEntry::class],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitualDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        // Add the diary lock flag introduced in schema version 2.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Add mood support for diary entries in version 3.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN mood TEXT")
            }
        }

        // Extend diary entries with media and location fields in version 4.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN photoUri TEXT")
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN audioFilePath TEXT")
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN locationText TEXT")
            }
        }

        // Mark diary entries that are journal-style entries in version 5.
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN isJournal INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Rebuild habit_records so the table enforces foreign keys and cleans up orphan rows.
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM habit_records WHERE habitId NOT IN (SELECT id FROM habits)")
                db.execSQL("""
                    CREATE TABLE habit_records_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        habitId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        FOREIGN KEY (habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO habit_records_new SELECT * FROM habit_records")
                db.execSQL("DROP TABLE habit_records")
                db.execSQL("ALTER TABLE habit_records_new RENAME TO habit_records")
                db.execSQL("CREATE INDEX index_habit_records_habitId ON habit_records(habitId)")
            }
        }

        // Replace the habit record index and add the new sleep log table in version 7.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS index_habit_records_habitId")
                db.execSQL("CREATE INDEX index_habit_records_habitId_timestamp ON habit_records(habitId, timestamp)")
                db.execSQL("""
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
         * Returns the shared database instance.
         *
         * Double-checked locking keeps initialization lazy while preventing
         * multiple Room databases from being created in parallel.
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
