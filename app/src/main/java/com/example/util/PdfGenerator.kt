package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.MilkRecord
import com.example.traceability.TraceabilityEngine
import com.example.ui.DashboardKpis
import com.example.ui.TraceSheetUiState
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    /**
     * Generates and downloads the Milk Collection Report PDF to device storage.
     * STRICT REQUIREMENT: Single-Page Constraint (Fit to 1 Page Wide by 1 Page Tall).
     * All summary metrics, table headers, and rows fit strictly on exactly 1 single page.
     */
    fun downloadMilkReportPdf(
        context: Context,
        records: List<MilkRecord>,
        kpis: DashboardKpis,
        dateFilter: String = ""
    ): Uri? {
        val document = PdfDocument()
        // A4 Landscape: 842 x 595 points (Excel Fit to 1 Page Standard)
        val pageWidth = 842
        val pageHeight = 595

        val titlePaint = Paint().apply {
            color = Color.rgb(11, 31, 54) // MilkNavy
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(20, 93, 160) // MilkBlue
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val badgePaint = Paint().apply {
            color = Color.rgb(5, 150, 105) // Emerald Green
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(220, 227, 233)
            style = Paint.Style.FILL
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        val linePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        val greenPaint = Paint().apply {
            color = Color.rgb(22, 163, 74) // MilkGreen
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // STRICT SINGLE PAGE CONSTRAINT: exactly 1 page created
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = 28f
        val left = 32f
        val right = (pageWidth - 32).toFloat()

        // Top Brand Banner
        canvas.drawText("BILAL AHMAD MILK COLLECTION", left, y, titlePaint)
        canvas.drawText("PAGE SETUP: FIT TO 1 PAGE WIDE × 1 PAGE TALL [OFFICIAL AUDIT]", right - 310f, y, badgePaint)
        y += 14f

        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val filterInfo = if (dateFilter.isNotBlank()) " • Filter: $dateFilter" else ""
        canvas.drawText("MILK COLLECTION & QUALITY SUMMARY$filterInfo • Generated: $dateStr", left, y, subTitlePaint)
        y += 16f

        // KPI Metrics Row (Fit in single compact horizontal box)
        val kpiHeight = 36f
        val kpiRect = RectF(left, y, right, y + kpiHeight)
        canvas.drawRoundRect(kpiRect, 4f, 4f, cardBgPaint)
        canvas.drawRoundRect(kpiRect, 4f, 4f, linePaint)

        val totalMilkStr = String.format(Locale.US, "%.1f L", kpis.totalMilkLiters)
        val tsEquivStr = String.format(Locale.US, "%.1f L", kpis.totalTsMilkLiters)
        val avgFatStr = String.format(Locale.US, "%.2f%%", kpis.averageFat)
        val avgLrStr = String.format(Locale.US, "%.1f", kpis.averageLr)
        val avgSnfStr = if (records.isNotEmpty()) String.format(Locale.US, "%.2f%%", records.map { it.snf }.average()) else "0.00%"
        val avgTsStr = if (records.isNotEmpty()) String.format(Locale.US, "%.2f%%", records.map { it.ts }.average()) else "0.00%"
        val totalPayStr = String.format(Locale.US, "Rs %,.0f", kpis.totalPayment)

        canvas.drawText("Total Milk: $totalMilkStr", left + 10f, y + 15f, boldTextPaint)
        canvas.drawText("TS Equiv: $tsEquivStr", left + 130f, y + 15f, boldTextPaint)
        canvas.drawText("Avg Fat: $avgFatStr", left + 250f, y + 15f, boldTextPaint)
        canvas.drawText("Avg LR: $avgLrStr", left + 355f, y + 15f, boldTextPaint)
        canvas.drawText("Avg SNF: $avgSnfStr", left + 440f, y + 15f, boldTextPaint)
        canvas.drawText("Avg TS: $avgTsStr", left + 535f, y + 15f, boldTextPaint)
        canvas.drawText("Total Payout: $totalPayStr", left + 630f, y + 15f, greenPaint)

        canvas.drawText("Total Entries: ${records.size} records", left + 10f, y + 28f, textPaint)
        canvas.drawText("Center: Bilal Ahmad Dairy Center • Internal Storage Export", left + 250f, y + 28f, textPaint)
        canvas.drawText("Standard: TS Ref ${com.example.data.model.REFERENCE_TS}%", left + 630f, y + 28f, textPaint)

        y += kpiHeight + 10f

        // Table Header
        val thHeight = 18f
        canvas.drawRect(left, y, right, y + thHeight, headerBgPaint)
        canvas.drawRect(left, y, right, y + thHeight, linePaint)

        // Column Layout: 10 columns across (842 - 64 = 778 pt)
        // Col offsets from left:
        // 0: Date (0..75)
        // 1: Farmer Name (75..245)
        // 2: Milk (L) (245..310)
        // 3: Fat % (310..370)
        // 4: LR (370..425)
        // 5: SNF % (425..485)
        // 6: TS % (485..545)
        // 7: TS Milk L (545..620)
        // 8: Rate (620..680)
        // 9: Payment Rs (680..778)
        val colOffsets = floatArrayOf(0f, 75f, 245f, 310f, 370f, 425f, 485f, 545f, 620f, 680f)

        canvas.drawText("Date", left + colOffsets[0] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("Farmer Name", left + colOffsets[1] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("Milk (L)", left + colOffsets[2] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("Fat %", left + colOffsets[3] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("LR", left + colOffsets[4] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("SNF %", left + colOffsets[5] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("TS %", left + colOffsets[6] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("TS Milk (L)", left + colOffsets[7] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("Rate", left + colOffsets[8] + 4f, y + 12f, boldTextPaint)
        canvas.drawText("Payment (Rs)", left + colOffsets[9] + 4f, y + 12f, boldTextPaint)

        y += thHeight

        // Fit-to-1-Page Row Sizing Calculation
        val availableHeight = pageHeight - y - 30f // Reserve 30pt for footer
        val count = records.size

        if (count == 0) {
            canvas.drawText("No milk collection records found for the selected period.", left + 10f, y + 25f, textPaint)
        } else {
            val maxDisplayRows = 38 // Max readable rows without microscopic fonts
            val isTruncated = count > maxDisplayRows
            val displayRecords = if (isTruncated) records.take(maxDisplayRows - 1) else records

            val rowCount = if (isTruncated) displayRecords.size + 1 else displayRecords.size
            val rowHeight = (availableHeight / rowCount).coerceIn(9.5f, 15f)
            val dynamicFontSize = (rowHeight * 0.62f).coerceIn(6.5f, 8.5f)

            val dynamicTextPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = dynamicFontSize
                isAntiAlias = true
            }

            val dynamicBoldPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = dynamicFontSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            displayRecords.forEachIndexed { idx, record ->
                val rowBg = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                val rowPaint = Paint().apply { color = rowBg; style = Paint.Style.FILL }
                canvas.drawRect(left, y, right, y + rowHeight, rowPaint)
                canvas.drawLine(left, y + rowHeight, right, y + rowHeight, linePaint)

                val nameShort = if (record.farmerName.length > 24) record.farmerName.substring(0, 22) + ".." else record.farmerName
                val textBaseline = y + (rowHeight * 0.72f)

                canvas.drawText(record.date, left + colOffsets[0] + 4f, textBaseline, dynamicTextPaint)
                canvas.drawText(nameShort, left + colOffsets[1] + 4f, textBaseline, dynamicBoldPaint)
                canvas.drawText(String.format(Locale.US, "%.1f", record.liters), left + colOffsets[2] + 4f, textBaseline, dynamicTextPaint)
                canvas.drawText(String.format(Locale.US, "%.2f", record.fat), left + colOffsets[3] + 4f, textBaseline, dynamicTextPaint)
                canvas.drawText(String.format(Locale.US, "%.1f", record.lr), left + colOffsets[4] + 4f, textBaseline, dynamicTextPaint)
                canvas.drawText(String.format(Locale.US, "%.2f", record.snf), left + colOffsets[5] + 4f, textBaseline, dynamicTextPaint)
                canvas.drawText(String.format(Locale.US, "%.2f", record.ts), left + colOffsets[6] + 4f, textBaseline, dynamicTextPaint)
                canvas.drawText(String.format(Locale.US, "%.1f", record.tsMilk), left + colOffsets[7] + 4f, textBaseline, dynamicBoldPaint)
                canvas.drawText(String.format(Locale.US, "%.0f", record.rate), left + colOffsets[8] + 4f, textBaseline, dynamicTextPaint)
                canvas.drawText(String.format(Locale.US, "%,.1f", record.payment), left + colOffsets[9] + 4f, textBaseline, dynamicBoldPaint)

                y += rowHeight
            }

            if (isTruncated) {
                // Consolidated row representing the remaining records to maintain 1 single page!
                val remainingCount = count - (maxDisplayRows - 1)
                val remainingRecords = records.drop(maxDisplayRows - 1)
                val remainingMilk = remainingRecords.sumOf { it.liters }
                val remainingPay = remainingRecords.sumOf { it.payment }
                val remTsMilk = remainingRecords.sumOf { it.tsMilk }

                val rowPaint = Paint().apply { color = Color.rgb(254, 243, 199); style = Paint.Style.FILL }
                canvas.drawRect(left, y, right, y + rowHeight, rowPaint)
                canvas.drawLine(left, y + rowHeight, right, y + rowHeight, linePaint)

                val textBaseline = y + (rowHeight * 0.72f)
                canvas.drawText("... +$remainingCount more", left + colOffsets[0] + 4f, textBaseline, dynamicBoldPaint)
                canvas.drawText("Consolidated Records ($remainingCount)", left + colOffsets[1] + 4f, textBaseline, dynamicBoldPaint)
                canvas.drawText(String.format(Locale.US, "%.1f", remainingMilk), left + colOffsets[2] + 4f, textBaseline, dynamicBoldPaint)
                canvas.drawText(String.format(Locale.US, "%.1f", remTsMilk), left + colOffsets[7] + 4f, textBaseline, dynamicBoldPaint)
                canvas.drawText(String.format(Locale.US, "%,.1f", remainingPay), left + colOffsets[9] + 4f, textBaseline, dynamicBoldPaint)

                y += rowHeight
            }
        }

        // Single-Page Footer
        val pageFooter = "Page 1 of 1 • Single-Page Layout (Fit to 1 Page Wide by 1 Page Tall) • Saved to Phone Storage (Downloads) • Bilal Ahmad Milk Collection"
        canvas.drawText(pageFooter, left, pageHeight - 14f, textPaint)

        document.finishPage(page)

        val fileName = "Milk_Report_SinglePage_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
        val uri = savePdfToDownloads(context, document, fileName)
        document.close()
        return uri
    }

    private fun drawNestleLogo(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        val darkColor = Color.rgb(30, 41, 59)
        val fillPaint = Paint().apply {
            color = darkColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val strokePaint = Paint().apply {
            color = darkColor
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
            isAntiAlias = true
        }

        // Branch
        canvas.drawLine(x + 2f, y + height * 0.76f, x + width * 0.36f, y + height * 0.74f, strokePaint.apply { strokeWidth = 2.2f })

        // Nest bowl
        val nestRect = RectF(x + 6f, y + height * 0.44f, x + width * 0.34f, y + height * 0.78f)
        canvas.drawArc(nestRect, 0f, 180f, true, fillPaint)

        // Mother Bird Body & Head
        canvas.drawCircle(x + 11f, y + height * 0.34f, 3.4f, fillPaint)
        canvas.drawCircle(x + 16f, y + height * 0.42f, 4.2f, fillPaint)
        // Mother Bird Beak
        val beakPath = android.graphics.Path().apply {
            moveTo(x + 14f, y + height * 0.34f)
            lineTo(x + 19f, y + height * 0.36f)
            lineTo(x + 14f, y + height * 0.38f)
            close()
        }
        canvas.drawPath(beakPath, fillPaint)

        // Baby Birds in Nest (3 hungry fledglings)
        canvas.drawCircle(x + 21f, y + height * 0.45f, 2.2f, fillPaint)
        canvas.drawCircle(x + 24.5f, y + height * 0.43f, 2.2f, fillPaint)
        canvas.drawCircle(x + 28f, y + height * 0.45f, 2.2f, fillPaint)

        // "Nestlé" typography
        val textPaint = Paint().apply {
            color = darkColor
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textSize = height * 0.65f
            isAntiAlias = true
        }
        val wordX = x + width * 0.40f
        val wordY = y + height * 0.68f
        canvas.drawText("Nestlé", wordX, wordY, textPaint)

        // Famous Nestle Top Horizontal Bar (from N across to é)
        canvas.drawRect(wordX - 1f, y + height * 0.12f, wordX + width * 0.58f, y + height * 0.18f, fillPaint)
    }

    /**
     * Generates and downloads the Subcenter Traceability Log Sheet PDF (A4 Landscape, 70 rows).
     */
    fun downloadTraceSheetPdf(
        context: Context,
        state: TraceSheetUiState
    ): Uri? {
        val document = PdfDocument()
        // A4 Landscape: 842 x 595 points
        val pageWidth = 842
        val pageHeight = 595

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val brandPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subBrandPaint = Paint().apply {
            color = Color.rgb(60, 60, 60)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            isAntiAlias = true
        }

        val metaBoldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val thPaint = Paint().apply {
            color = Color.BLACK
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        val thBgPaint = Paint().apply {
            color = Color.rgb(220, 227, 233)
            style = Paint.Style.FILL
        }

        var y = 20f

        // Top Header
        canvas.drawText("Nestlé Pakistan Ltd.", 20f, y, brandPaint)
        y += 9f
        canvas.drawText("(Milk Collection & Dairy Development)", 20f, y, subBrandPaint)

        // Draw Official Nestlé Logo on Top Right
        drawNestleLogo(canvas, pageWidth - 150f, 10f, 130f, 26f)

        canvas.drawText("Document #: 1583-CAM-D4-13.00", 20f, y + 10f, metaPaint)
        canvas.drawText("Location Code & Name: _________________________________", pageWidth - 320f, y + 10f, metaPaint)
        y += 20f

        // Center Title Bar
        val titleBarRect = RectF(20f, y - 10f, pageWidth - 20f, y + 6f)
        canvas.drawRect(titleBarRect, thBgPaint)
        canvas.drawRect(titleBarRect, linePaint)
        canvas.drawText("Subcenter Traceability Log Sheet", pageWidth / 2f, y + 2f, titlePaint)
        y += 14f

        // Metadata row 1
        canvas.drawText("Supplier Code: ", 20f, y, metaPaint)
        canvas.drawText(state.supplierCode.ifBlank { "________________" }, 82f, y, metaBoldPaint)

        canvas.drawText("Supplier Name: ", 220f, y, metaPaint)
        canvas.drawText(state.supplierName.ifBlank { "____________________________" }, 285f, y, metaBoldPaint)

        canvas.drawText("Source Type: ", pageWidth - 160f, y, metaPaint)
        canvas.drawText(state.sourceType.ifBlank { "________" }, pageWidth - 100f, y, metaBoldPaint)
        y += 10f

        // Metadata row 2
        canvas.drawText("Village Name: ", 20f, y, metaPaint)
        canvas.drawText(state.villageName.ifBlank { "________________" }, 80f, y, metaBoldPaint)

        canvas.drawText("Telephone Number: ${state.telephoneNumber.ifBlank { "____________________________" }}", 220f, y, metaPaint)
        y += 13f

        // Table Geometry (Exact 802.0 pt total width across 842 pt page, margins 20pt left and right)
        val left = 20f
        val right = pageWidth - 20f // 822f
        val srW = 24f
        val nameW = 130f
        val villageW = 44f
        val monthW = 38f // 12 * 38 = 456f
        val verW = 37f // 4 * 37 = 148f
        // Total: 24 + 130 + 44 + 456 + 148 = 802f (Exactly right - left!)

        // Header Row
        val headerH = 15f
        canvas.drawRect(left, y, right, y + headerH, thBgPaint)
        canvas.drawRect(left, y, right, y + headerH, linePaint)

        var curX = left
        canvas.drawText("Sr #", curX + srW / 2f, y + 10.5f, thPaint)
        canvas.drawLine(curX + srW, y, curX + srW, y + headerH, linePaint)
        curX += srW

        canvas.drawText("Farmer Name", curX + nameW / 2f, y + 10.5f, thPaint)
        canvas.drawLine(curX + nameW, y, curX + nameW, y + headerH, linePaint)
        curX += nameW

        canvas.drawText("Village", curX + villageW / 2f, y + 10.5f, thPaint)
        canvas.drawLine(curX + villageW, y, curX + villageW, y + headerH, linePaint)
        curX += villageW

        TraceabilityEngine.TRACE_MONTHS.forEach { m ->
            canvas.drawText(m, curX + monthW / 2f, y + 10.5f, thPaint)
            canvas.drawLine(curX + monthW, y, curX + monthW, y + headerH, linePaint)
            curX += monthW

            if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                canvas.drawText("AASM", curX + verW / 2f, y + 7f, thPaint)
                canvas.drawText("Verify", curX + verW / 2f, y + 13.5f, thPaint.apply { textSize = 5.5f })
                thPaint.textSize = 7f
                canvas.drawLine(curX + verW, y, curX + verW, y + headerH, linePaint)
                curX += verW
            }
        }

        y += headerH

        // Strict Single A4 Page Guarantee: calculate row height to fit exactly within page bounds
        val totalH = 13f
        val footerH = 12f
        val bottomMargin = 6f
        val availableForRows = pageHeight - y - totalH - footerH - bottomMargin
        val rowCount = state.rows.size.coerceAtLeast(1)
        val rowH = (availableForRows / rowCount).coerceIn(4.2f, 10.5f)
        val dynamicFontSize = (rowH * 0.70f).coerceIn(5.0f, 7.8f)

        val dynamicCellPaint = Paint().apply {
            color = Color.BLACK
            textSize = dynamicFontSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val dynamicCellLeftPaint = Paint().apply {
            color = Color.BLACK
            textSize = dynamicFontSize
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val textBaselineOffset = rowH * 0.75f

        state.rows.forEachIndexed { i, row ->
            val rowTop = y
            val rowBottom = y + rowH

            if (i % 2 == 1) {
                val rowAltBg = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL }
                canvas.drawRect(left, rowTop, right, rowBottom, rowAltBg)
            }
            canvas.drawRect(left, rowTop, right, rowBottom, linePaint)

            var rx = left
            // Sr #
            canvas.drawText(row.sr.toString(), rx + srW / 2f, rowTop + textBaselineOffset, dynamicCellPaint)
            canvas.drawLine(rx + srW, rowTop, rx + srW, rowBottom, linePaint)
            rx += srW

            // Farmer Name
            val nameDisplay = if (row.name.length > 24) row.name.substring(0, 24) else row.name
            canvas.drawText(nameDisplay, rx + 3f, rowTop + textBaselineOffset, dynamicCellLeftPaint)
            canvas.drawLine(rx + nameW, rowTop, rx + nameW, rowBottom, linePaint)
            rx += nameW

            // Village (blank)
            canvas.drawLine(rx + villageW, rowTop, rx + villageW, rowBottom, linePaint)
            rx += villageW

            // Month values
            TraceabilityEngine.TRACE_MONTHS.forEach { m ->
                val v = row.values[m]
                val s = if (v != null && v > 0) TraceabilityEngine.formatNumber(v) else ""
                if (s.isNotEmpty()) {
                    canvas.drawText(s, rx + monthW / 2f, rowTop + textBaselineOffset, dynamicCellPaint)
                }
                canvas.drawLine(rx + monthW, rowTop, rx + monthW, rowBottom, linePaint)
                rx += monthW

                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                    canvas.drawLine(rx + verW, rowTop, rx + verW, rowBottom, linePaint)
                    rx += verW
                }
            }

            y += rowH
        }

        // Total Row
        canvas.drawRect(left, y, right, y + totalH, thBgPaint)
        canvas.drawRect(left, y, right, y + totalH, linePaint)

        var tx = left
        val totalLabelW = srW + nameW + villageW
        canvas.drawText("Total", tx + totalLabelW / 2f, y + 9.5f, thPaint)
        canvas.drawLine(tx + totalLabelW, y, tx + totalLabelW, y + totalH, linePaint)
        tx += totalLabelW

        TraceabilityEngine.TRACE_MONTHS.forEach { m ->
            val total = state.rows.sumOf { it.values[m] ?: 0.0 }
            val s = if (total > 0) TraceabilityEngine.formatNumber(total) else ""
            if (s.isNotEmpty()) {
                canvas.drawText(s, tx + monthW / 2f, y + 9.5f, thPaint)
            }
            canvas.drawLine(tx + monthW, y, tx + monthW, y + totalH, linePaint)
            tx += monthW

            if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                canvas.drawLine(tx + verW, y, tx + verW, y + totalH, linePaint)
                tx += verW
            }
        }

        // 1-Page A4 Guarantee Footer (Centered title matching official print layout)
        val footerPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 7f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Subcenter Traceability Log Sheet", pageWidth / 2f, pageHeight - 6f, footerPaint)

        document.finishPage(page)

        val fileName = "Subcenter_Traceability_Log_Sheet_${state.supplierCode.ifBlank { "0S1055" }}.pdf"
        val uri = savePdfToDownloads(context, document, fileName)
        document.close()
        return uri
    }

    /**
     * Generates and downloads a single farmer Milk Slip / Receipt PDF.
     */
    fun downloadSingleSlipPdf(
        context: Context,
        record: MilkRecord
    ): Uri? {
        val document = PdfDocument()
        // Thermal / Slip size: 300 x 420 points
        val pageWidth = 300
        val pageHeight = 420

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(11, 31, 54)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.rgb(20, 93, 160)
            textSize = 10f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9.5f
            isAntiAlias = true
        }

        val valRightPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val greenBigPaint = Paint().apply {
            color = Color.rgb(22, 163, 74)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var y = 30f
        canvas.drawText("BILAL AHMAD MILK COLLECTION", (pageWidth / 2).toFloat(), y, titlePaint)
        y += 14f
        canvas.drawText("OFFICIAL MILK RECEIPT SLIP", (pageWidth / 2).toFloat(), y, subPaint)
        y += 10f
        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, linePaint)
        y += 18f

        fun drawLineItem(label: String, value: String, isGreen: Boolean = false) {
            canvas.drawText(label, 24f, y, labelPaint)
            if (isGreen) {
                canvas.drawText(value, (pageWidth - 24).toFloat(), y, greenBigPaint)
            } else {
                canvas.drawText(value, (pageWidth - 24).toFloat(), y, valRightPaint)
            }
            y += 17f
        }

        drawLineItem("Receipt Date:", record.date)
        drawLineItem("Farmer Name:", record.farmerName)
        drawLineItem("Farmer ID:", record.farmerId)
        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, linePaint)
        y += 14f

        drawLineItem("Milk Quantity:", "${String.format(Locale.US, "%.2f", record.liters)} Liters")
        drawLineItem("Fat (%):", "${String.format(Locale.US, "%.2f", record.fat)} %")
        drawLineItem("LR / CLR:", String.format(Locale.US, "%.1f", record.lr))
        drawLineItem("SNF (%):", "${String.format(Locale.US, "%.2f", record.snf)} %")
        drawLineItem("Total Solids (TS):", "${String.format(Locale.US, "%.2f", record.ts)} %")
        drawLineItem("Specific Gravity:", String.format(Locale.US, "%.3f", record.spGravity))
        drawLineItem("Milk Weight (KG):", "${String.format(Locale.US, "%.3f", record.milkKg)} KG")
        drawLineItem("TS Milk Equiv:", "${String.format(Locale.US, "%.2f", record.tsMilk)} Liters")
        drawLineItem("Milk Rate:", "Rs ${String.format(Locale.US, "%.2f", record.rate)} / L")

        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, linePaint)
        y += 20f

        canvas.drawText("TOTAL PAYMENT:", 24f, y, boldPaint)
        canvas.drawText("Rs ${String.format(Locale.US, "%,.2f", record.payment)}", (pageWidth - 24).toFloat(), y, greenBigPaint)
        y += 24f

        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, linePaint)
        y += 14f
        val footPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Thank you for choosing Bilal Ahmad Milk Collection", (pageWidth / 2).toFloat(), y, footPaint)

        document.finishPage(page)

        val fileName = "Milk_Slip_${record.farmerId}_${record.date}.pdf"
        val uri = savePdfToDownloads(context, document, fileName)
        document.close()
        return uri
    }

    /**
     * Directly downloads and saves CSV file to phone's Internal Storage / Downloads folder.
     */
    fun downloadMilkRecordsCsv(
        context: Context,
        records: List<MilkRecord>,
        fileName: String = "Milk_Records_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
    ): Uri? {
        val sb = StringBuilder()
        sb.append("Sr,Date,Farmer ID,Farmer Name,Milk Liters,Fat %,LR,SNF %,TS %,Milk Kg,Fat Kg,SNF Kg,TS Kg,TS Milk L,Rate,Payment Rs,Remarks\n")
        records.forEachIndexed { idx, r ->
            sb.append("${idx + 1},\"${r.date}\",\"${r.farmerId}\",\"${r.farmerName}\",")
            sb.append(String.format(Locale.US, "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,\"%s\"\n",
                r.liters, r.fat, r.lr, r.snf, r.ts, r.milkKg, r.fatKg, r.snfKg, r.tsKg, r.tsMilk, r.rate, r.payment, r.remarks.replace("\"", "\"\"")))
        }
        return saveCsvToDownloads(context, sb.toString(), fileName)
    }

    /**
     * Saves CSV directly to device Downloads directory (Internal Storage / Downloads/MilkCollection).
     */
    fun saveCsvToDownloads(context: Context, csvContent: String, fileName: String): Uri? {
        try {
            var fileUri: Uri? = null
            var outStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MilkCollection")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outStream = resolver.openOutputStream(uri)
                    fileUri = uri
                }
            }

            if (outStream == null) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                outStream = FileOutputStream(targetFile)
                fileUri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        targetFile
                    )
                } catch (e: Exception) {
                    Uri.fromFile(targetFile)
                }
            }

            outStream?.use { stream ->
                stream.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val cacheFile = File(context.cacheDir, "csv/$fileName")
            cacheFile.parentFile?.mkdirs()
            FileOutputStream(cacheFile).use { stream ->
                stream.write(csvContent.toByteArray(Charsets.UTF_8))
            }
            val cacheUri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )
            } catch (e: Exception) {
                Uri.fromFile(cacheFile)
            }

            try {
                Toast.makeText(context, "✅ CSV Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {}

            return cacheUri ?: fileUri
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(context, "Error saving CSV: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}
            return null
        }
    }

    /**
     * Saves the PdfDocument into public Downloads folder using MediaStore (Android 10+)
     * or Environment (legacy) and returns the FileProvider Uri for opening/sharing.
     */
    private fun savePdfToDownloads(context: Context, document: PdfDocument, fileName: String): Uri? {
        try {
            var fileUri: Uri? = null
            var outStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MilkCollection")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outStream = resolver.openOutputStream(uri)
                    fileUri = uri
                }
            }

            if (outStream == null) {
                // Fallback for older Android or app-specific documents
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                outStream = FileOutputStream(targetFile)
                fileUri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        targetFile
                    )
                } catch (e: Exception) {
                    Uri.fromFile(targetFile)
                }
            }

            outStream?.use { stream ->
                document.writeTo(stream)
            }

            // Also keep a copy in app cache for immediate FileProvider intent sharing
            val cacheFile = File(context.cacheDir, "pdfs/$fileName")
            cacheFile.parentFile?.mkdirs()
            FileOutputStream(cacheFile).use { stream ->
                document.writeTo(stream)
            }
            val cacheUri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )
            } catch (e: Exception) {
                Uri.fromFile(cacheFile)
            }

            try {
                Toast.makeText(context, "✅ PDF Downloaded: $fileName", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {}

            return cacheUri ?: fileUri
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}
            return null
        }
    }

    /**
     * Opens a PDF file in any installed PDF viewer app.
     */
    fun openPdf(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found. Please install a PDF reader.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares a PDF file via WhatsApp, Gmail, Drive, etc.
     */
    fun sharePdf(context: Context, uri: Uri, title: String = "Share PDF") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_SUBJECT, title)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
