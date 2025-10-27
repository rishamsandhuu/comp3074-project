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
    version = 6,                      // bumped to 6
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poiDao(): PoiDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `addresses`")
            }
        }

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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `POIs` ADD COLUMN `tagsCsv` TEXT NOT NULL DEFAULT ''")
            }
        }

        // ✅ v5 → v6 migration for new `task` column
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `POIs` ADD COLUMN `task` TEXT")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huntquest.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .addCallback(SeedCallback(context.applicationContext))
                    .build().also { INSTANCE = it }
            }
    }

    private class SeedCallback(private val appContext: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = get(appContext).poiDao()
                dao.upsert(
                    Poi(
                        name = "CN Tower",
                        openUntil = "Open until 10:00 pm",
                        latitude = 43.6426,
                        longitude = -79.3871,
                        address = "290 Bremner Blvd, Toronto, ON",
                        tagsCsv = "#landmark #view",
                        task = "Find the highest observation level."
                    )
                )
                dao.upsert(
                    Poi(
                        name = "High Park",
                        openUntil = "Open until 11:00 pm",
                        latitude = 43.6465,
                        longitude = -79.4637,
                        address = "1873 Bloor St W, Toronto, ON",
                        tagsCsv = "#nature #trails",
                        task = "Locate the statue near Grenadier Pond."
                    )
                )
                dao.upsert(
                    Poi(
                        name = "Old Mill",
                        openUntil = "Open until 11:00 pm",
                        latitude = 43.6501,
                        longitude = -79.4934,
                        address = "21 Old Mill Rd, Toronto, ON",
                        tagsCsv = "#history #architecture",
                        task = "Find the waterwheel and take a photo."
                    )
                )
            }
        }
    }
}
