package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trace_config")
data class TraceSheetConfig(
    @PrimaryKey
    val id: Int = 1,
    val supplierCode: String = "",
    val supplierName: String = "",
    val sourceType: String = "DO",
    val villageName: String = "",
    val farmerNameHint: String = "",
    val selectedMonthsCsv: String = "Sep",
    val monthConfigsJson: String = "" // JSON string storing map of month -> MonthConfig(count, maxKg)
)

data class MonthConfig(
    val count: Int = 20,
    val maxKg: Double = 45.0
)

@Entity(tableName = "trace_rows")
data class TraceRowEntity(
    @PrimaryKey
    val sr: Int, // 1 to 70
    val farmerName: String = "",
    val valuesJson: String = "{}" // JSON map of Month -> Double
)
