package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FarmerDao
import com.example.data.dao.FarmerWeeklyHistoryDao
import com.example.data.dao.MilkRecordDao
import com.example.data.dao.TraceSheetDao
import com.example.data.model.Farmer
import com.example.data.model.FarmerWeeklyHistory
import com.example.data.model.MilkRecord
import com.example.data.model.TraceRowEntity
import com.example.data.model.TraceSheetConfig

@Database(
    entities = [
        Farmer::class,
        MilkRecord::class,
        TraceSheetConfig::class,
        TraceRowEntity::class,
        FarmerWeeklyHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmerDao(): FarmerDao
    abstract fun milkRecordDao(): MilkRecordDao
    abstract fun traceSheetDao(): TraceSheetDao
    abstract fun farmerWeeklyHistoryDao(): FarmerWeeklyHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milk_collection_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
