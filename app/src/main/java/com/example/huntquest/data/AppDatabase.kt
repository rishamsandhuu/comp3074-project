package com.example.huntquest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration        // ✅ import Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Poi::class, Address::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poiDao(): PoiDao
    abstract fun addressDao(): AddressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // ✅ Proper Migration object
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // New table for addresses
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `addresses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `line` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                // New columns in pois
                db.execSQL("ALTER TABLE `pois` ADD COLUMN `rating` REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `pois` ADD COLUMN `addressId` INTEGER")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huntquest.db"
                )
                    .addMigrations(MIGRATION_1_2)             // ✅ now the type matches
                    .addCallback(SeedCallback(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
            }
    }

    // Seed stays as before
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
                        longitude = -79.3871
                    )
                )
                dao.upsert(
                    Poi(
                        name = "High Park",
                        openUntil = "Open until 11:00 pm",
                        latitude = 43.6465,
                        longitude = -79.4637
                    )
                )
                dao.upsert(
                    Poi(
                        name = "Old Mill",
                        openUntil = "Open until 11:00 pm",
                        latitude = 43.6501,
                        longitude = -79.4934
                    )
                )
            }
        }
    }
}
