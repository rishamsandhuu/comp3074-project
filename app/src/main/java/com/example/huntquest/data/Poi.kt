package com.example.huntquest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "POIs")
data class Poi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val openUntil: String,       // e.g., "Open until 10:00 pm"
    val latitude: Double? = null,
    val longitude: Double? = null,
    // new fields
    val rating: Float = 0f,
    val address: String? = null,

    //tags for poi - nirja edits
    val tagsCsv: String = ""
)
