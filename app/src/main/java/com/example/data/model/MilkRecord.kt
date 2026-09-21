package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

const val REFERENCE_TS = 13.027

@Entity(tableName = "milk_records")
data class MilkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val farmerId: String,
    val farmerName: String,
    val liters: Double,
    val fat: Double,
    val lr: Double,
    val snf: Double,
    val ts: Double,
    val spGravity: Double,
    val milkKg: Double,
    val fatKg: Double,
    val snfKg: Double,
    val tsKg: Double,
    val tsMilk: Double,
    val rate: Double,
    val payment: Double,
    val remarks: String = ""
)

data class MilkCalculations(
    val snf: Double,
    val ts: Double,
    val spGravity: Double,
    val milkKg: Double,
    val fatKg: Double,
    val snfKg: Double,
    val tsKg: Double,
    val tsMilk: Double,
    val payment: Double
) {
    fun formattedSnf(): String = String.format(Locale.US, "%.2f%%", snf)
    fun formattedTs(): String = String.format(Locale.US, "%.2f%%", ts)
    fun formattedSpGravity(): String = String.format(Locale.US, "%.3f", spGravity)
    fun formattedMilkKg(): String = String.format(Locale.US, "%.3f KG", milkKg)
    fun formattedFatKg(): String = String.format(Locale.US, "%.3f KG", fatKg)
    fun formattedSnfKg(): String = String.format(Locale.US, "%.3f KG", snfKg)
    fun formattedTsKg(): String = String.format(Locale.US, "%.3f KG", tsKg)
    fun formattedTsMilk(): String = String.format(Locale.US, "%.2f L", tsMilk)
    fun formattedPayment(): String = String.format(Locale.US, "Rs %.2f", payment)

    companion object {
        fun compute(liters: Double, fat: Double, lr: Double, rate: Double): MilkCalculations {
            val snf = (0.25 * lr) + (0.22 * fat) + 0.72
            val ts = fat + snf
            val spGravity = 1.0 + (lr / 1000.0)
            val milkKg = liters * spGravity
            val fatKg = milkKg * fat / 100.0
            val snfKg = milkKg * snf / 100.0
            val tsKg = fatKg + snfKg
            val tsMilk = if (REFERENCE_TS > 0) (liters * ts) / REFERENCE_TS else 0.0
            val payment = tsMilk * rate
            return MilkCalculations(
                snf = snf,
                ts = ts,
                spGravity = spGravity,
                milkKg = milkKg,
                fatKg = fatKg,
                snfKg = snfKg,
                tsKg = tsKg,
                tsMilk = tsMilk,
                payment = payment
            )
        }
    }
}
