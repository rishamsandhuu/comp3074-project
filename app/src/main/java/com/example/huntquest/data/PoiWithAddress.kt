package com.example.huntquest.data

import androidx.room.Embedded
import androidx.room.Relation

data class PoiWithAddress(
    @Embedded val poi: Poi,
    @Relation(
        parentColumn = "addressId",
        entityColumn = "id"
    )
    val address: Address?
)
