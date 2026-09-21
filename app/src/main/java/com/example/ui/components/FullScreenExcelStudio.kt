package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.traceability.TraceabilityEngine
import com.example.ui.MainViewModel
import com.example.ui.TraceRowUi
import com.example.ui.TraceSheetUiState
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkRed
import com.example.util.PdfGenerator

private val ExcelGreen = Color(0xFF107C41)
private val ExcelDarkGreen = Color(0xFF0E5C31)
private val ExcelHeaderBg = Color(0xFFE6ECE8)
private val ExcelGridBorder = Color(0xFFD4D4D4)
private val ExcelSelectedBorder = Color(0xFF107C41)

data class SelectedCell(
    val sr: Int,
    val columnKey: String, // "NAME", "MONTH_Sep", etc.
    val colLabel: String
)

/**
 * Immersive Full-Screen Excel Spreadsheet Studio.
 * Allows working on the Traceability Log Sheet exactly like Microsoft Excel.
 */
@Composable
fun FullScreenExcelStudioDialog(
    viewModel: MainViewModel,
    traceState: TraceSheetUiState,
    onDismiss: () -> Unit,
    onOpenPdfPreview: () -> Unit,
    onPrint: () -> Unit,
    onShareXls: () -> Unit
) {
    val context = LocalContext.current
    var selectedCell by remember { mutableStateOf<SelectedCell?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBox by remember { mutableStateOf(false) }
    var lastDownloadedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    val activeMonths = TraceabilityEngine.TRACE_MONTHS

    // Find active cell value for formula bar
    val selectedRow = traceState.rows.find { it.sr == selectedCell?.sr }
    val formulaBarValue = when {
        selectedCell == null || selectedRow == null -> ""
        selectedCell!!.columnKey == "NAME" -> selectedRow.name
        selectedCell!!.columnKey.startsWith("MONTH_") -> {
            val m = selectedCell!!.columnKey.removePrefix("MONTH_")
            val v = selectedRow.values[m]
            if (v != null && v > 0) TraceabilityEngine.formatNumber(v) else ""
        }
        else -> ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Excel Green App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ExcelGreen)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Subcenter_Traceability_Log_Sheet.xlsx",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Single A4 Page Setup", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "Excel Mode • ${traceState.rows.size} Rows • Auto Recalculating",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Top Quick Action Icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val uri = PdfGenerator.downloadTraceSheetPdf(context, traceState)
                                lastDownloadedPdfUri = uri
                                notificationMessage = if (uri != null) "✓ Downloaded to Downloads (Single A4 Page)" else "Download failed"
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Download PDF", tint = Color.White)
                        }

                        IconButton(onClick = onOpenPdfPreview, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Visibility, contentDescription = "A4 Preview", tint = Color.White)
                        }

                        IconButton(onClick = onShareXls, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share XLS", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                viewModel.saveTraceSheetEdits()
                                notificationMessage = "✓ Edits saved to phone database"
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save Edits", tint = Color.White)
                        }
                    }
                }

                // Excel Ribbon Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RibbonActionChip(icon = Icons.Default.Add, label = "Add Row") {
                            viewModel.addTraceRow()
                            notificationMessage = "Row added at bottom"
                        }
                        if (selectedCell != null) {
                            RibbonActionChip(icon = Icons.Default.Delete, label = "Del Row", isDestructive = true) {
                                viewModel.deleteTraceRow(selectedCell!!.sr)
                                selectedCell = null
                                notificationMessage = "Row deleted"
                            }
                        }
                        RibbonActionChip(icon = Icons.Default.Refresh, label = "Recalculate") {
                            viewModel.recalculateTraceTotals()
                            notificationMessage = "Totals recalculated"
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showSearchBox = !showSearchBox },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Optional Search Bar
                if (showSearchBox) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Notification Banner
                if (notificationMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notificationMessage!!,
                            color = Color(0xFF0369A1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (lastDownloadedPdfUri != null) {
                            Text(
                                text = "OPEN PDF",
                                color = ExcelGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .clickable {
                                        PdfGenerator.openPdf(context, lastDownloadedPdfUri!!)
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }
                }

                // Excel Formula Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(0.5.dp, ExcelGridBorder)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cell Coordinate (e.g. "B12" or "Row 12: Sep")
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(28.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (selectedCell != null) "[Row ${selectedCell!!.sr}, ${selectedCell!!.colLabel}]" else "fx Ready",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Formula",
                        tint = ExcelGreen,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Live Formula Editor Input
                    BasicTextField(
                        value = formulaBarValue,
                        onValueChange = { newVal ->
                            if (selectedCell != null) {
                                if (selectedCell!!.columnKey == "NAME") {
                                    viewModel.updateRowFarmerName(selectedCell!!.sr, newVal)
                                } else if (selectedCell!!.columnKey.startsWith("MONTH_")) {
                                    val m = selectedCell!!.columnKey.removePrefix("MONTH_")
                                    viewModel.updateRowMonthValue(selectedCell!!.sr, m, newVal)
                                }
                            }
                        },
                        enabled = selectedCell != null,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Interactive Excel Data Grid
                val horizontalScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                ) {
                    val filteredRows = if (searchQuery.isBlank()) {
                        traceState.rows
                    } else {
                        traceState.rows.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.sr.toString() == searchQuery.trim()
                        }
                    }

                    Column(modifier = Modifier.width(1080.dp)) {
                        // Excel Column Letters Header (A, B, C, D...)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExcelHeaderBg)
                                .border(0.5.dp, ExcelGridBorder)
                        ) {
                            ExcelHeaderCell("#", 46.dp)
                            ExcelHeaderCell("A (Sr)", 46.dp)
                            ExcelHeaderCell("B (Farmer Name)", 180.dp)
                            ExcelHeaderCell("C (Village)", 80.dp)

                            activeMonths.forEachIndexed { idx, m ->
                                val letter = ('D' + idx).toString()
                                ExcelHeaderCell("$letter ($m)", 66.dp)
                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    ExcelHeaderCell("Verify", 56.dp)
                                }
                            }
                        }

                        // Table Column Name Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDCE3E9))
                                .border(1.dp, Color(0xFF94A3B8))
                        ) {
                            ExcelHeaderTitleCell("Row", 46.dp)
                            ExcelHeaderTitleCell("Sr #", 46.dp)
                            ExcelHeaderTitleCell("Farmer Name", 180.dp)
                            ExcelHeaderTitleCell("Village", 80.dp)

                            activeMonths.forEach { m ->
                                ExcelHeaderTitleCell(m, 66.dp)
                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    ExcelHeaderTitleCell("AASM Verify", 56.dp)
                                }
                            }
                        }

                        // Scrollable Rows
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            itemsIndexed(filteredRows, key = { _, item -> item.sr }) { index, row ->
                                val isEven = index % 2 == 0
                                val baseBg = if (isEven) Color.White else Color(0xFFF8FAFC)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(baseBg)
                                        .border(0.5.dp, ExcelGridBorder),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Row index
                                    Box(
                                        modifier = Modifier
                                            .width(46.dp)
                                            .height(30.dp)
                                            .background(Color(0xFFF1F5F9))
                                            .border(0.5.dp, ExcelGridBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(row.sr.toString(), fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    }

                                    // Sr #
                                    Box(
                                        modifier = Modifier
                                            .width(46.dp)
                                            .height(30.dp)
                                            .border(0.5.dp, ExcelGridBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(row.sr.toString(), fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }

                                    // Farmer Name Cell (Editable & Selectable)
                                    val isNameSelected = selectedCell?.sr == row.sr && selectedCell?.columnKey == "NAME"
                                    Box(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(30.dp)
                                            .background(if (isNameSelected) Color(0xFFE8F5E9) else Color.Transparent)
                                            .border(
                                                width = if (isNameSelected) 2.dp else 0.5.dp,
                                                color = if (isNameSelected) ExcelSelectedBorder else ExcelGridBorder
                                            )
                                            .clickable {
                                                selectedCell = SelectedCell(row.sr, "NAME", "Name")
                                            }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        BasicTextField(
                                            value = row.name,
                                            onValueChange = { viewModel.updateRowFarmerName(row.sr, it) },
                                            textStyle = TextStyle(fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Medium),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // Village Cell (Blank)
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(30.dp)
                                            .border(0.5.dp, ExcelGridBorder)
                                    )

                                    // Month Value Cells
                                    activeMonths.forEach { m ->
                                        val colKey = "MONTH_$m"
                                        val isCellSelected = selectedCell?.sr == row.sr && selectedCell?.columnKey == colKey
                                        val valNum = row.values[m]
                                        val valStr = if (valNum != null && valNum > 0) TraceabilityEngine.formatNumber(valNum) else ""

                                        Box(
                                            modifier = Modifier
                                                .width(66.dp)
                                                .height(30.dp)
                                                .background(if (isCellSelected) Color(0xFFE8F5E9) else Color.Transparent)
                                                .border(
                                                    width = if (isCellSelected) 2.dp else 0.5.dp,
                                                    color = if (isCellSelected) ExcelSelectedBorder else ExcelGridBorder
                                                )
                                                .clickable {
                                                    selectedCell = SelectedCell(row.sr, colKey, m)
                                                }
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            BasicTextField(
                                                value = valStr,
                                                onValueChange = { viewModel.updateRowMonthValue(row.sr, m, it) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                textStyle = TextStyle(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    color = Color.Black,
                                                    fontFamily = FontFamily.Monospace
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                            Box(
                                                modifier = Modifier
                                                    .width(56.dp)
                                                    .height(30.dp)
                                                    .border(0.5.dp, ExcelGridBorder)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Excel Bottom Grand Total Row (Frozen / Sticky)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDCE3E9))
                                .border(1.5.dp, Color(0xFF475569))
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .height(34.dp)
                                    .border(0.5.dp, ExcelGridBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("TOTAL", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF0F172A))
                            }

                            Box(
                                modifier = Modifier
                                    .width(260.dp)
                                    .height(34.dp)
                                    .border(0.5.dp, ExcelGridBorder)
                                    .padding(start = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text("Sum of all farmers", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                            }

                            activeMonths.forEach { m ->
                                val total = traceState.rows.sumOf { it.values[m] ?: 0.0 }
                                val totalStr = if (total > 0) TraceabilityEngine.formatNumber(total) else "—"

                                Box(
                                    modifier = Modifier
                                        .width(66.dp)
                                        .height(34.dp)
                                        .border(0.5.dp, ExcelGridBorder),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = totalStr,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ExcelDarkGreen
                                    )
                                }

                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    Box(
                                        modifier = Modifier
                                            .width(56.dp)
                                            .height(34.dp)
                                            .border(0.5.dp, ExcelGridBorder)
                                    )
                                }
                            }
                        }
                    }
                }

                // Excel Bottom Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .border(0.5.dp, Color(0xFFCBD5E1))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sheet1 • ${traceState.rows.size} Farmers • Ready",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                val uri = PdfGenerator.downloadTraceSheetPdf(context, traceState)
                                lastDownloadedPdfUri = uri
                                notificationMessage = "✓ Downloaded to Downloads (Single A4 Page)"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp).testTag("excel_download_pdf_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download A4 PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenPdfPreview,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("A4 Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RibbonActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isDestructive) MilkRed.copy(alpha = 0.1f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isDestructive) MilkRed.copy(alpha = 0.4f) else Color(0xFFCBD5E1)),
        modifier = Modifier
            .height(28.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MilkRed else Color(0xFF334155),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) MilkRed else Color(0xFF334155)
            )
        }
    }
}

@Composable
private fun ExcelHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(22.dp)
            .border(0.5.dp, ExcelGridBorder),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
    }
}

@Composable
private fun ExcelHeaderTitleCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(28.dp)
            .border(0.5.dp, ExcelGridBorder),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), textAlign = TextAlign.Center)
    }
}
