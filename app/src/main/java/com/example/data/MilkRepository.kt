package com.example.data

import android.content.Context
import com.example.data.dao.FarmerDao
import com.example.data.dao.MilkRecordDao
import com.example.data.dao.TraceSheetDao
import com.example.data.model.Farmer
import com.example.data.model.MilkRecord
import com.example.data.model.MonthConfig
import com.example.data.model.TraceRowEntity
import com.example.data.model.TraceSheetConfig
import com.example.traceability.TraceabilityEngine
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MilkRepository(
    private val farmerDao: FarmerDao,
    private val milkRecordDao: MilkRecordDao,
    private val traceSheetDao: TraceSheetDao
) {
    val allFarmers: Flow<List<Farmer>> = farmerDao.getAllFarmers()
    val allRecords: Flow<List<MilkRecord>> = milkRecordDao.getAllRecords()
    val traceConfig: Flow<TraceSheetConfig?> = traceSheetDao.getConfig()
    val traceRows: Flow<List<TraceRowEntity>> = traceSheetDao.getAllRows()

    suspend fun insertFarmer(farmer: Farmer) = farmerDao.insertFarmer(farmer)
    suspend fun updateFarmer(farmer: Farmer) = farmerDao.updateFarmer(farmer)
    suspend fun deleteFarmer(id: String) = farmerDao.deleteFarmerById(id)
    suspend fun canDeleteFarmer(farmerId: String): Boolean {
        return milkRecordDao.countRecordsForFarmer(farmerId) == 0
    }

    suspend fun insertRecord(record: MilkRecord): Long = milkRecordDao.insertRecord(record)
    suspend fun updateRecord(record: MilkRecord) = milkRecordDao.updateRecord(record)
    suspend fun deleteRecord(id: Long) = milkRecordDao.deleteRecordById(id)
    suspend fun clearAll() {
        milkRecordDao.clearAllRecords()
        farmerDao.clearAllFarmers()
    }

    suspend fun saveTraceConfig(config: TraceSheetConfig) = traceSheetDao.saveConfig(config)
    suspend fun saveTraceRows(rows: List<TraceRowEntity>) = traceSheetDao.insertRows(rows)

    suspend fun clearTraceSheet() {
        traceSheetDao.clearConfig()
        traceSheetDao.clearRows()
    }

    fun exportRecordsToCsv(records: List<MilkRecord>): String {
        val sb = StringBuilder()
        sb.append("Date,Farmer ID,Farmer Name,Milk (L),Specific Gravity,Milk (KG),Fat (%),Fat (KG),LR,SNF (%),SNF (KG),TS (%),TS (KG),TS Milk Equivalent (L),Rate (Rs/L),Payment (Rs),Remarks\n")
        records.forEach { r ->
            sb.append("\"${r.date}\",")
            sb.append("\"${r.farmerId.replace("\"", "\"\"")}\",")
            sb.append("\"${r.farmerName.replace("\"", "\"\"")}\",")
            sb.append(String.format(Locale.US, "%.2f,", r.liters))
            sb.append(String.format(Locale.US, "%.3f,", r.spGravity))
            sb.append(String.format(Locale.US, "%.3f,", r.milkKg))
            sb.append(String.format(Locale.US, "%.2f,", r.fat))
            sb.append(String.format(Locale.US, "%.3f,", r.fatKg))
            sb.append(String.format(Locale.US, "%.1f,", r.lr))
            sb.append(String.format(Locale.US, "%.2f,", r.snf))
            sb.append(String.format(Locale.US, "%.3f,", r.snfKg))
            sb.append(String.format(Locale.US, "%.2f,", r.ts))
            sb.append(String.format(Locale.US, "%.3f,", r.tsKg))
            sb.append(String.format(Locale.US, "%.2f,", r.tsMilk))
            sb.append(String.format(Locale.US, "%.2f,", r.rate))
            sb.append(String.format(Locale.US, "%.2f,", r.payment))
            sb.append("\"${r.remarks.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun exportBackupJson(farmers: List<Farmer>, records: List<MilkRecord>): String {
        val root = JSONObject()
        root.put("version", 6)
        root.put("app", "Bilal Ahmad Milk Collection")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))

        val farmersArray = JSONArray()
        farmers.forEach { f ->
            val fo = JSONObject()
            fo.put("id", f.id)
            fo.put("name", f.name)
            fo.put("mobile", f.mobile)
            fo.put("village", f.village)
            fo.put("rate", f.defaultRate)
            farmersArray.put(fo)
        }
        root.put("farmers", farmersArray)

        val recordsArray = JSONArray()
        records.forEach { r ->
            val ro = JSONObject()
            ro.put("id", r.id)
            ro.put("date", r.date)
            ro.put("farmerId", r.farmerId)
            ro.put("farmer", r.farmerName)
            ro.put("liters", r.liters)
            ro.put("fat", r.fat)
            ro.put("lr", r.lr)
            ro.put("snf", r.snf)
            ro.put("ts", r.ts)
            ro.put("spGravity", r.spGravity)
            ro.put("milkKg", r.milkKg)
            ro.put("fatKg", r.fatKg)
            ro.put("snfKg", r.snfKg)
            ro.put("tsKg", r.tsKg)
            ro.put("tsMilk", r.tsMilk)
            ro.put("rate", r.rate)
            ro.put("payment", r.payment)
            ro.put("remarks", r.remarks)
            recordsArray.put(ro)
        }
        root.put("records", recordsArray)

        return root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Pair<Int, Int>> {
        return try {
            val root = JSONObject(jsonString)
            val farmersArray = root.optJSONArray("farmers") ?: JSONArray()
            val recordsArray = root.optJSONArray("records") ?: JSONArray()

            val newFarmers = mutableListOf<Farmer>()
            for (i in 0 until farmersArray.length()) {
                val obj = farmersArray.getJSONObject(i)
                newFarmers.add(
                    Farmer(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        mobile = obj.optString("mobile", ""),
                        village = obj.optString("village", ""),
                        defaultRate = obj.optDouble("rate", 200.0)
                    )
                )
            }

            val newRecords = mutableListOf<MilkRecord>()
            for (i in 0 until recordsArray.length()) {
                val obj = recordsArray.getJSONObject(i)
                val liters = obj.getDouble("liters")
                val fat = obj.getDouble("fat")
                val lr = obj.getDouble("lr")
                val rate = obj.getDouble("rate")
                val calc = com.example.data.model.MilkCalculations.compute(liters, fat, lr, rate)
                newRecords.add(
                    MilkRecord(
                        id = obj.optLong("id", 0L),
                        date = obj.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())),
                        farmerId = obj.getString("farmerId"),
                        farmerName = obj.optString("farmer", obj.optString("farmerName", "")),
                        liters = liters,
                        fat = fat,
                        lr = lr,
                        snf = obj.optDouble("snf", calc.snf),
                        ts = obj.optDouble("ts", calc.ts),
                        spGravity = obj.optDouble("spGravity", calc.spGravity),
                        milkKg = obj.optDouble("milkKg", calc.milkKg),
                        fatKg = obj.optDouble("fatKg", calc.fatKg),
                        snfKg = obj.optDouble("snfKg", calc.snfKg),
                        tsKg = obj.optDouble("tsKg", calc.tsKg),
                        tsMilk = obj.optDouble("tsMilk", calc.tsMilk),
                        rate = rate,
                        payment = obj.optDouble("payment", calc.payment),
                        remarks = obj.optString("remarks", "")
                    )
                )
            }

            // Save to DB
            newFarmers.forEach { farmerDao.insertFarmer(it) }
            newRecords.forEach { milkRecordDao.insertRecord(it) }

            Result.success(Pair(newFarmers.size, newRecords.size))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
