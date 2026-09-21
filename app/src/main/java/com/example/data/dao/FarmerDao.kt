package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Farmer
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers ORDER BY id ASC")
    fun getAllFarmers(): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE id = :id LIMIT 1")
    suspend fun getFarmerById(id: String): Farmer?

    @Query("SELECT COUNT(*) FROM farmers")
    fun getFarmerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: Farmer)

    @Update
    suspend fun updateFarmer(farmer: Farmer)

    @Delete
    suspend fun deleteFarmer(farmer: Farmer)

    @Query("DELETE FROM farmers WHERE id = :id")
    suspend fun deleteFarmerById(id: String)

    @Query("DELETE FROM farmers")
    suspend fun clearAllFarmers()
}
