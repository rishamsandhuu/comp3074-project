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

// ➕ ADD: imports for Team feature (new package)
import com.example.huntquest.team.TeamMember
import com.example.huntquest.team.TeamMemberDao

@Database(
    entities = [Poi::class, TeamMember::class],
    version = 7,                      //bump version -  edits
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poiDao(): PoiDao
    abstract fun teamMemberDao(): TeamMemberDao

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

        //v4 -> v5: nirja edits; adding tagsCsv
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `POIs` ADD COLUMN `tagsCsv` TEXT NOT NULL DEFAULT ''")
            }
        }

        // ➕ v5 -> v6: create TeamMember table
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `TeamMember` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }


        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huntquest.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6) // include all
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
                        task = "Soar into the clouds and conquer Toronto’s skyline! Ride the glass elevator up the CN Tower, feel the floor tremble beneath your feet on the EdgeWalk, and uncover the hidden clue etched near the viewing glass. Remember—real adventurers look down without fear."
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
                        task = "Wander beneath the canopy of ancient oaks and chase the whispers of the breeze along Grenadier Pond. Your mission: find the statue that watches over the water, and listen closely—it may tell you where the next clue lies."
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
                        task = "Step back in time to where the Humber River powered the heart of the city. Search for the waterwheel that still hums with old stories. Snap a photo, follow the sound of rushing water, and uncover the secret that the mill has guarded for generations."
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
                        task = "Among cobblestones and copper stills, creativity brews again. Explore the art alleys and spot the massive heart sculpture. Decode the quote carved on the nearby wall to reveal your next destination."
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
                        task = "Follow the scent of fresh bread and spices. Your quest: find the oldest vendor still telling stories of the city’s beginnings. Ask what they sell that’s been there since the 1800s, and note their answer—it’s a key part of your legend."
                    )
                )
            }
        }
    }
}
