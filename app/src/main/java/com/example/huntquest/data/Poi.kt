package com.example.huntquest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "POIs")
data class Poi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var name: String,
    var openUntil: String,
    var latitude: Double? = null,
    var longitude: Double? = null,

    // mutable fields
    var rating: Float = 0f,
    var address: String? = null,

    // tags for poi
    var tagsCsv: String = "",

    // description / task
    var task: String? = null,

    var completed: Boolean = false     // new field
)
