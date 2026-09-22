package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(
    tableName = "farmer_weekly_history",
    indices = [
        Index(value = ["farmerId", "year", "weekNumber"], unique = true)
    ]
)
data class FarmerWeeklyHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val farmerId: String,
    val farmerName: String,
    val year: Int,
    val weekNumber: Int,
    val weekStartDate: String, // YYYY-MM-DD (Monday)
    val weekEndDate: String,   // YYYY-MM-DD (Sunday)
    val totalVolume: Double,
    val averageFat: Double,
    val averageLr: Double,
    val averageTs: Double,
    val totalPayment: Double = 0.0,
    val entryCount: Int = 0,
    val savedTimestamp: Long = System.currentTimeMillis()
) {
    fun formattedVolume(): String = String.format(Locale.US, "%.2f L", totalVolume)
    fun formattedFat(): String = String.format(Locale.US, "%.2f%%", averageFat)
    fun formattedLr(): String = String.format(Locale.US, "%.1f", averageLr)
    fun formattedTs(): String = String.format(Locale.US, "%.2f%%", averageTs)
    fun formattedPayment(): String = String.format(Locale.US, "Rs %,.0f", totalPayment)
}
