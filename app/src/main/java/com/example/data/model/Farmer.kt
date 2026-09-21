package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmers")
data class Farmer(
    @PrimaryKey
    val id: String, // e.g. "F-001"
    val name: String,
    val mobile: String = "",
    val village: String = "",
    val defaultRate: Double = 200.0
)
