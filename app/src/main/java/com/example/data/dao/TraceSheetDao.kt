package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TraceRowEntity
import com.example.data.model.TraceSheetConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface TraceSheetDao {
    @Query("SELECT * FROM trace_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<TraceSheetConfig?>

    @Query("SELECT * FROM trace_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigOnce(): TraceSheetConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: TraceSheetConfig)

    @Query("SELECT * FROM trace_rows ORDER BY sr ASC")
    fun getAllRows(): Flow<List<TraceRowEntity>>

    @Query("SELECT * FROM trace_rows ORDER BY sr ASC")
    suspend fun getAllRowsOnce(): List<TraceRowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<TraceRowEntity>)

    @Query("DELETE FROM trace_rows")
    suspend fun clearRows()

    @Query("DELETE FROM trace_config")
    suspend fun clearConfig()
}
