package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MilkRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkRecordDao {
    @Query("SELECT * FROM milk_records ORDER BY date DESC, id DESC")
    fun getAllRecords(): Flow<List<MilkRecord>>

    @Query("SELECT * FROM milk_records WHERE farmerId = :farmerId ORDER BY date DESC")
    fun getRecordsByFarmer(farmerId: String): Flow<List<MilkRecord>>

    @Query("SELECT * FROM milk_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): MilkRecord?

    @Query("SELECT COUNT(*) FROM milk_records")
    fun getRecordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM milk_records WHERE farmerId = :farmerId")
    suspend fun countRecordsForFarmer(farmerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MilkRecord): Long

    @Update
    suspend fun updateRecord(record: MilkRecord)

    @Delete
    suspend fun deleteRecord(record: MilkRecord)

    @Query("DELETE FROM milk_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM milk_records")
    suspend fun clearAllRecords()
}
