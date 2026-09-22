package com.example.traceability

import com.example.ui.TraceRowUi
import com.example.ui.TraceSheetUiState
import java.util.Locale

/**
 * 100% Lifetime Free, On-Device AI Engine for Excel Sheet Operations.
 * Runs completely offline without API keys, token limits, or network dependencies.
 * Provides smart auto-balance, anomaly detection, audit compliance, and insights.
 */
object ExcelAiEngine {

    data class AiAuditReport(
        val totalFarmersWithSupply: Int,
        val totalAnnualVolumeKg: Double,
        val peakMonth: String,
        val peakVolumeKg: Double,
        val lowestMonth: String,
        val lowestVolumeKg: Double,
        val topFarmer: String,
        val topFarmerVolumeKg: Double,
        val auditReadinessScore: Int, // 0 - 100%
        val issuesFound: List<String>,
        val recommendations: List<String>,
        val summaryTextUrdu: String,
        val summaryTextEnglish: String
    )

    /**
     * Comprehensive AI Audit of the Traceability Excel Sheet.
     */
    fun auditSheet(state: TraceSheetUiState): AiAuditReport {
        val rows = state.rows
        val months = TraceabilityEngine.TRACE_MONTHS

        val monthlyTotals = mutableMapOf<String, Double>()
        months.forEach { m ->
            monthlyTotals[m] = rows.sumOf { it.values[m] ?: 0.0 }
        }

        var peakM = "Sep"
        var peakVol = 0.0
        var lowM = "Jan"
        var lowVol = Double.MAX_VALUE

        monthlyTotals.forEach { (m, vol) ->
            if (vol > peakVol) {
                peakVol = vol
                peakM = m
            }
            if (vol > 0 && vol < lowVol) {
                lowVol = vol
                lowM = m
            }
        }
        if (lowVol == Double.MAX_VALUE) lowVol = 0.0

        val farmerTotals = rows.map { r ->
            val sum = months.sumOf { r.values[it] ?: 0.0 }
            Pair(r.name.ifBlank { "Farmer ${r.sr}" }, sum)
        }.sortedByDescending { it.second }

        val activeFarmersCount = farmerTotals.count { it.second > 0 }
        val topFarmerName = farmerTotals.firstOrNull()?.first ?: "N/A"
        val topFarmerVol = farmerTotals.firstOrNull()?.second ?: 0.0
        val totalAnnual = monthlyTotals.values.sum()

        val issues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // Check for missing supplier codes
        if (state.supplierCode.isBlank()) {
            issues.add("Supplier Code is empty (Required for Nestlé/Subcenter audit).")
        }
        if (state.supplierName.isBlank()) {
            issues.add("Supplier Name is empty.")
        }
        if (state.villageName.isBlank()) {
            issues.add("Village Name is empty.")
        }

        // Check for negative or extreme outlier values
        var outlierCount = 0
        rows.forEach { r ->
            r.values.forEach { (m, v) ->
                val maxAllowed = state.monthConfigs[m]?.maxKg ?: 45.0
                if (v > maxAllowed * 1.5) outlierCount++
                if (v < 0) issues.add("Row ${r.sr}: Negative milk value detected ($v).")
            }
        }
        if (outlierCount > 0) {
            issues.add("$outlierCount farmer entries exceed the configured monthly limit threshold.")
            recommendations.add("Run AI Auto-Balance to normalize values within the target limits.")
        }

        if (activeFarmersCount == 0) {
            issues.add("No farmer data has been recorded in the sheet.")
            recommendations.add("Use Auto-Fill or upload a new sheet to populate data.")
        }

        val score = when {
            issues.isEmpty() -> 100
            issues.size == 1 -> 90
            issues.size == 2 -> 80
            issues.size <= 4 -> 65
            else -> 45
        }

        val urduSummary = """
            سب سینٹر ٹریس ایبلٹی لاگ شیٹ کا اے آئی تجزیہ:
            • کل فعال فارمرز: $activeFarmersCount
            • سالانہ کل دودھ مقدار: ${String.format(Locale.US, "%,.1f", totalAnnual)} کلوگرام / لیٹر
            • سب سے زیادہ سپلائی والا مہینہ: $peakM (${String.format(Locale.US, "%,.1f", peakVol)} KG)
            • ٹاپ سپلائر فارمر: $topFarmerName (${String.format(Locale.US, "%,.1f", topFarmerVol)} KG)
            • آڈٹ اسکور: $score% (${if (score >= 85) "کامیاب اور منظور شدہ" else "اصلاح درکار ہے"})
        """.trimIndent()

        val englishSummary = """
            AI Traceability Sheet Audit Summary:
            • Active Registered Farmers: $activeFarmersCount / 70
            • Total Volume: ${String.format(Locale.US, "%,.2f", totalAnnual)} KG
            • Peak Supply Period: $peakM (${String.format(Locale.US, "%,.1f", peakVol)} KG)
            • Audit Compliance Score: $score% (${if (score >= 85) "Audit Ready" else "Needs Review"})
        """.trimIndent()

        return AiAuditReport(
            totalFarmersWithSupply = activeFarmersCount,
            totalAnnualVolumeKg = totalAnnual,
            peakMonth = peakM,
            peakVolumeKg = peakVol,
            lowestMonth = lowM,
            lowestVolumeKg = lowVol,
            topFarmer = topFarmerName,
            topFarmerVolumeKg = topFarmerVol,
            auditReadinessScore = score,
            issuesFound = issues,
            recommendations = recommendations,
            summaryTextUrdu = urduSummary,
            summaryTextEnglish = englishSummary
        )
    }

    /**
     * AI Auto-Balance: Normalizes all entries in selected months so no value exceeds max limit
     * and balances total distribution.
     */
    fun autoBalanceSheet(state: TraceSheetUiState): List<TraceRowUi> {
        val updatedRows = state.rows.map { row ->
            val newValues = row.values.toMutableMap()
            state.selectedMonths.forEach { m ->
                val cfg = state.monthConfigs[m]
                val maxLimit = cfg?.maxKg ?: 45.0
                val current = newValues[m]
                if (current != null && current > 0) {
                    if (current > maxLimit) {
                        // Smoothly cap within 85% - 98% of max limit
                        val balanced = maxLimit * (0.85 + ((row.sr * 7) % 15) / 100.0)
                        newValues[m] = Math.round(balanced * 10.0) / 10.0
                    }
                }
            }
            row.copy(values = newValues)
        }
        return updatedRows
    }

    /**
     * AI Smart Completion: Fills missing month entries based on average farmer supply.
     */
    fun smartCompleteMissing(state: TraceSheetUiState): List<TraceRowUi> {
        return state.rows.map { row ->
            val newValues = row.values.toMutableMap()
            // Calculate farmer's existing average
            val existing = row.values.values.filter { it > 0 }
            val avg = if (existing.isNotEmpty()) existing.average() else 25.0

            state.selectedMonths.forEach { m ->
                val current = newValues[m]
                if (current == null || current <= 0) {
                    val cfg = state.monthConfigs[m]
                    val maxLimit = cfg?.maxKg ?: 45.0
                    val countLimit = cfg?.count ?: 20
                    if (row.sr <= countLimit) {
                        val genVal = (avg * (0.90 + ((row.sr * 13) % 20) / 100.0)).coerceIn(5.0, maxLimit)
                        newValues[m] = Math.round(genVal * 10.0) / 10.0
                    }
                }
            }
            row.copy(values = newValues)
        }
    }
}
