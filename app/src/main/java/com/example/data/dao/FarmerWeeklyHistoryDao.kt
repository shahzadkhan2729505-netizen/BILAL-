package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FarmerWeeklyHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerWeeklyHistoryDao {
    @Query("SELECT * FROM farmer_weekly_history WHERE farmerId = :farmerId ORDER BY year DESC, weekNumber DESC")
    fun getHistoryForFarmer(farmerId: String): Flow<List<FarmerWeeklyHistory>>

    @Query("SELECT * FROM farmer_weekly_history ORDER BY year DESC, weekNumber DESC")
    fun getAllHistory(): Flow<List<FarmerWeeklyHistory>>

    @Query("SELECT * FROM farmer_weekly_history WHERE farmerId = :farmerId AND year = :year AND weekNumber = :weekNumber LIMIT 1")
    suspend fun getSpecificWeeklyHistory(farmerId: String, year: Int, weekNumber: Int): FarmerWeeklyHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyHistory(history: FarmerWeeklyHistory): Long

    @Query("DELETE FROM farmer_weekly_history WHERE farmerId = :farmerId")
    suspend fun deleteHistoryByFarmer(farmerId: String)
}
