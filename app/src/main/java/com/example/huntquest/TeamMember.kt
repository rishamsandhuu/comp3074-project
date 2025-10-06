package com.example.huntquest

import java.util.UUID

data class TeamMember(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    // Kept for compatibility with your earlier model; not shown in UI
    var role: String = ""
)
