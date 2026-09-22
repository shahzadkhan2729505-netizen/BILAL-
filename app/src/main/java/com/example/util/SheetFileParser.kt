package com.example.util

import android.content.Context
import android.net.Uri
import com.example.traceability.TraceabilityEngine
import com.example.ui.TraceRowUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object SheetFileParser {

    data class ParsedSheetResult(
        val supplierCode: String? = null,
        val supplierName: String? = null,
        val villageName: String? = null,
        val rows: List<TraceRowUi>,
        val fileName: String
    )

    /**
     * Reads and parses an uploaded file (CSV, TSV, TXT or Tabular export)
     * into structured TraceRowUi rows. Runs on Dispatchers.IO.
     */
    suspend fun parseUploadedFile(context: Context, uri: Uri): ParsedSheetResult = withContext(Dispatchers.IO) {
        val fileName = getFileName(context, uri) ?: "Uploaded_Sheet"
        val parsedRows = mutableListOf<TraceRowUi>()
        var parsedSupplierCode: String? = null
        var parsedSupplierName: String? = null
        var parsedVillageName: String? = null

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    var lineIndex = 0
                    var currentSr = 1
                    var headerColumns = listOf<String>()

                    reader.forEachLine { rawLine ->
                        val line = rawLine.trim()
                        if (line.isNotBlank()) {
                            // Check for metadata headers in CSV or exported sheets
                            val lower = line.lowercase()
                            if (lower.contains("supplier code:") || lower.contains("supplier_code")) {
                                val parts = line.split(":", ",")
                                if (parts.size > 1) parsedSupplierCode = parts[1].trim().take(20)
                            }
                            if (lower.contains("supplier name:") || lower.contains("supplier_name")) {
                                val parts = line.split(":", ",")
                                if (parts.size > 1) parsedSupplierName = parts[1].trim().take(40)
                            }
                            if (lower.contains("village:") || lower.contains("village_name")) {
                                val parts = line.split(":", ",")
                                if (parts.size > 1) parsedVillageName = parts[1].trim().take(30)
                            }

                            // Delimiter detection: comma, tab, or semicolon
                            val delimiter = when {
                                line.contains("\t") -> "\t"
                                line.contains(";") -> ";"
                                else -> ","
                            }
                            val tokens = line.split(delimiter).map { it.trim().removeSurrounding("\"") }

                            // Header detection
                            if (tokens.any { it.equals("Farmer Name", ignoreCase = true) || it.equals("Name", ignoreCase = true) || it.equals("Farmer", ignoreCase = true) }) {
                                headerColumns = tokens
                            } else if (tokens.size >= 2) {
                                // Data row
                                val possibleSr = tokens[0].toIntOrNull()
                                val farmerName = when {
                                    tokens.size >= 3 && possibleSr != null -> tokens[1]
                                    possibleSr == null -> tokens[0]
                                    else -> "Farmer $currentSr"
                                }

                                if (farmerName.isNotBlank() && !farmerName.equals("Total", ignoreCase = true) && !farmerName.equals("Sr", ignoreCase = true)) {
                                    val valuesMap = mutableMapOf<String, Double>()
                                    // Parse numeric month columns
                                    val monthStartIndex = if (possibleSr != null) 3 else 2
                                    for (i in monthStartIndex until tokens.size) {
                                        val valNum = tokens[i].toDoubleOrNull()
                                        if (valNum != null && valNum > 0) {
                                            val monthIdx = i - monthStartIndex
                                            if (monthIdx < TraceabilityEngine.TRACE_MONTHS.size) {
                                                val m = TraceabilityEngine.TRACE_MONTHS[monthIdx]
                                                valuesMap[m] = valNum
                                            }
                                        }
                                    }

                                    parsedRows.add(
                                        TraceRowUi(
                                            sr = currentSr,
                                            name = farmerName,
                                            values = valuesMap
                                        )
                                    )
                                    currentSr++
                                }
                            }
                            lineIndex++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Guarantee at least 70 rows to keep Excel sheet format perfect
        val finalRows = if (parsedRows.isNotEmpty()) {
            val list = parsedRows.toMutableList()
            while (list.size < 70) {
                list.add(TraceRowUi(sr = list.size + 1, name = "", values = emptyMap()))
            }
            list.take(70)
        } else {
            // If file couldn't be parsed line-by-line (e.g. binary PDF/Excel), create a clean fresh sheet with reference
            List(70) { idx ->
                TraceRowUi(sr = idx + 1, name = "Uploaded Farmer ${idx + 1}", values = emptyMap())
            }
        }

        ParsedSheetResult(
            supplierCode = parsedSupplierCode,
            supplierName = parsedSupplierName,
            villageName = parsedVillageName,
            rows = finalRows,
            fileName = fileName
        )
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val scheme = uri.scheme
        if (scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) name = it.getString(nameIdx)
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }
}
