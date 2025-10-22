package com.example.huntquest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Poi::class],
    version = 4,                      // ⬅️ bump version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poiDao(): PoiDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // v1 -> v2 (kept for users coming from very old version)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `addresses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `line` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE `pois` ADD COLUMN `rating` REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `pois` ADD COLUMN `addressId` INTEGER")
            }
        }

        // v2 -> v3 (dropped addresses table)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `addresses`")
            }
        }

        // ✅ v3 -> v4: create new table without addressId, add address TEXT
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pois_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `openUntil` TEXT NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `rating` REAL NOT NULL,
                        `address` TEXT
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `pois_new` (id, name, openUntil, latitude, longitude, rating, address)
                    SELECT id, name, openUntil, latitude, longitude, rating, NULL
                    FROM `pois`
                """.trimIndent())

                db.execSQL("DROP TABLE `pois`")
                db.execSQL("ALTER TABLE `pois_new` RENAME TO `pois`")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huntquest.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // ⬅️ include all
                    .addCallback(SeedCallback(context.applicationContext))
                    .build().also { INSTANCE = it }
            }
    }

    private class SeedCallback(private val appContext: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = get(appContext).poiDao()
                dao.upsert(Poi(name = "CN Tower",  openUntil = "Open until 10:00 pm", latitude = 43.6426, longitude = -79.3871))
                dao.upsert(Poi(name = "High Park", openUntil = "Open until 11:00 pm", latitude = 43.6465, longitude = -79.4637))
                dao.upsert(Poi(name = "Old Mill",  openUntil = "Open until 11:00 pm", latitude = 43.6501, longitude = -79.4934))
            }
        }
    }
}
