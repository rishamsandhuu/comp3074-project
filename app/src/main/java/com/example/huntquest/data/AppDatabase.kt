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

import com.example.huntquest.team.TeamMember
import com.example.huntquest.team.TeamMemberDao

@Database(
    entities = [Poi::class, TeamMember::class],
    version = 8,                      // current schema version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun poiDao(): PoiDao
    abstract fun teamMemberDao(): TeamMemberDao

    companion object {

        @Volatile private var INSTANCE: AppDatabase? = null

        /** ---------------- Migrations -------------- **/

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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `TeamMember` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        //Adding 'completed' field to POI model
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE POIs ADD COLUMN completed INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huntquest.db"
                )
                    // add all existing migrations
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_7_8
                    )

                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
            }
    }

    /** Seeds the POI table on new DB creation */
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
                        task = "Soar into the clouds and conquer Toronto’s skyline! Ride the glass elevator..."
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
                        task = "Wander beneath the canopy of ancient oaks..."
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
                        task = "Step back in time to where the Humber River powered the heart of the city..."
                    )
                )

                dao.upsert(
                    Poi(
                        name = "Distillery District",
                        openUntil = "Open until 9:00 pm",
                        latitude = 43.6500,
                        longitude = -79.3590,
                        address = "55 Mill St, Toronto, ON",
                        tagsCsv = "#arts #heritage",
                        task = "Among cobblestones and copper stills, creativity brews again..."
                    )
                )

                dao.upsert(
                    Poi(
                        name = "St. Lawrence Market",
                        openUntil = "Open until 6:00 pm",
                        latitude = 43.6487,
                        longitude = -79.3716,
                        address = "93 Front St E, Toronto, ON",
                        tagsCsv = "#food #culture",
                        task = "Follow the scent of fresh bread and spices..."
                    )
                )
            }
        }
    }
}