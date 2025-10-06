package com.example.huntquest

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TeamPrefsRepository(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("huntquest_prefs", Context.MODE_PRIVATE)
    private val key = "team_members"

    fun load(): MutableList<TeamMember> {
        val json = prefs.getString(key, null) ?: return seed().also { save(it) }
        return try {
            val arr = JSONArray(json)
            MutableList(arr.length()) { i ->
                val o: JSONObject = arr.getJSONObject(i)
                TeamMember(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    email = o.optString("email"),
                    phone = o.optString("phone"),
                    role = o.optString("role")
                )
            }
        } catch (_: Exception) { seed().also { save(it) } }
    }

    fun save(list: List<TeamMember>) {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id); put("name", m.name)
                put("email", m.email); put("phone", m.phone)
                put("role", m.role)
            })
        }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    private fun seed(): MutableList<TeamMember> = mutableListOf(
        TeamMember(name = "Gia Nagpal",  email = "gia@gbc.ca",     phone = "6470000000", role = "Product / UI"),
        TeamMember(name = "Nirja Dabhi", email = "nirja@gbc.ca",   phone = "6470000001", role = "POI / Map"),
        TeamMember(name = "Rishamnoor Kaur", email = "risham@gbc.ca", phone = "6470000002", role = "Details / Share"),
        TeamMember(name = "Danuja Shankar",  email = "danuja@gbc.ca", phone = "6470000003", role = "Team Mgmt"),
    )
}
