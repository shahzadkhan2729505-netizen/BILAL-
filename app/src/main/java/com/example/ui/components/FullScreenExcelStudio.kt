package com.example.ui.components

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.traceability.ExcelAiEngine
import com.example.traceability.TraceabilityAiBrain
import com.example.traceability.TraceabilityEngine
import com.example.ui.MainViewModel
import com.example.ui.TraceRowUi
import com.example.ui.TraceSheetUiState
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkRed
import com.example.util.PdfGenerator
import com.example.util.SheetFileParser
import com.example.util.VoiceSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
 * Guaranteed 100% original Nestlé layout matching user reference photo:
 * - Top edge-to-edge status bar protection (no clipping or cut off header!)
 * - Authentic Nestlé header banner, document ID, logo, metadata lines
 * - Real-time Voice AI Brain with Speech Recognition & Urdu/English voice feedback
 * - 19 columns: Sr, Farmer Name, Village, 12 Months + 4 AASM Verification quarters
 * - All 70 rows cleanly blank by default
 * - Bottom Total calculation row
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
    val coroutineScope = rememberCoroutineScope()
    var selectedCell by remember { mutableStateOf<SelectedCell?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBox by remember { mutableStateOf(false) }
    var showAiVoiceDialog by remember { mutableStateOf(false) }
    var showHeaderEditDialog by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var lastDownloadedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    // Voice Manager for Speech Recognition & Text-to-Speech
    val voiceManager = remember { VoiceSpeechManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceManager.startListening { recognizedText ->
                viewModel.executeAiCommand(recognizedText) { urdu, _ ->
                    voiceManager.speak(urdu)
                    notificationMessage = urdu
                }
            }
        } else {
            notificationMessage = "Microphone permission is required for voice AI."
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val parsed = SheetFileParser.parseUploadedFile(context, uri)
                    viewModel.replaceTraceSheetData(
                        newSupplierCode = parsed.supplierCode,
                        newSupplierName = parsed.supplierName,
                        newVillageName = parsed.villageName,
                        newRows = parsed.rows,
                        sourceFileName = parsed.fileName
                    )
                    notificationMessage = "✓ Replaced with ${parsed.fileName}"
                } catch (e: Exception) {
                    notificationMessage = "Failed to parse uploaded file"
                }
            }
        }
    }

    val activeMonths = TraceabilityEngine.TRACE_MONTHS

    // Formula bar active value
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding() // Avoid bottom gestures overlapping
            ) {
                // --- TOP EXCEL GREEN APP BAR (With statusBarsPadding so notch NEVER clips it!) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ExcelGreen)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = traceState.loadedDocumentName.ifBlank { "Subcenter_Traceability_Log_Sheet.xlsx" },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (traceState.isCustomUploaded) "Custom Upload Workspace" else "Nestlé 100% Original Layout",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Doc #: 1583-CAM-D4-13.00 • ${traceState.rows.size} Rows • Voice AI Active",
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
                        // Prominent Voice AI button in Top Bar
                        IconButton(
                            onClick = { showAiVoiceDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.20f), CircleShape)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice AI Assistant", tint = Color.White)
                        }

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
                                notificationMessage = "✓ Edits saved to database"
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save Edits", tint = Color.White)
                        }
                    }
                }

                // --- EXCEL RIBBON TOOLBAR ---
                val ribbonHScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .horizontalScroll(ribbonHScroll)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice AI Brain button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEDE9FE),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
                        modifier = Modifier
                            .height(30.dp)
                            .clickable { showAiVoiceDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice AI بولیں", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                        }
                    }

                    // Clear All / Reset to blank
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .height(30.dp)
                            .clickable { showClearConfirmation = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MilkRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("خالی کرو (Clear)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MilkRed)
                        }
                    }

                    // Edit Header Metadata
                    RibbonActionChip(icon = Icons.Default.Edit, label = "Header Details") {
                        showHeaderEditDialog = true
                    }

                    // Auto-Balance
                    RibbonActionChip(icon = Icons.Default.AutoAwesome, label = "Auto-Balance") {
                        viewModel.executeAiCommand("auto balance karo") { urdu, _ ->
                            notificationMessage = urdu
                        }
                    }

                    // Add Row
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

                    RibbonActionChip(icon = Icons.Default.UploadFile, label = "Upload Sheet") {
                        filePickerLauncher.launch("*/*")
                    }

                    IconButton(
                        onClick = { showSearchBox = !showSearchBox },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
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
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notificationMessage!!,
                            color = Color(0xFF0369A1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { notificationMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF0369A1), modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // --- EXCEL FORMULA BAR ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .border(0.5.dp, Color(0xFFCBD5E1))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cell Name Box
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(26.dp)
                            .background(Color.White)
                            .border(0.5.dp, Color(0xFF94A3B8), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val posText = if (selectedCell != null) {
                            "R${selectedCell!!.sr}:${selectedCell!!.colLabel}"
                        } else {
                            "A1"
                        }
                        Text(
                            text = posText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Formula symbol fx
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Function",
                        tint = ExcelGreen,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Formula input bar
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
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .background(Color.White)
                            .border(0.5.dp, Color(0xFF94A3B8), RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        enabled = selectedCell != null
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Voice mic on formula bar
                    IconButton(
                        onClick = {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = ExcelGreen, modifier = Modifier.size(18.dp))
                    }
                }

                // --- SPREADSHEET CANVAS ---
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

                    // Total width of all 19 columns:
                    // Row # (36) + Sr # (44) + Name (170) + Village (65) + 12 Months * 52 (624) + 4 AASM * 60 (240) = 1179 dp
                    val sheetCanvasWidth = 1180.dp

                    Column(modifier = Modifier.width(sheetCanvasWidth)) {

                        // =========================================================================
                        // --- 100% AUTHENTIC NESTLÉ DOCUMENT HEADER (EXACTLY MATCHING PHOTO) ---
                        // =========================================================================
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // Top Row: Nestlé title & Doc info on left, Nestlé Logo & Location on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Nestlé Pakistan Ltd.",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "(Milk Collection & Dairy Development)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Document #: 1583-CAM-D4-13.00",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_nestle_logo),
                                        contentDescription = "Nestlé Official Logo",
                                        modifier = Modifier
                                            .height(38.dp)
                                            .padding(end = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Location Code & Name: ${traceState.locationCode.ifBlank { "____________________________" }}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.clickable { showHeaderEditDialog = true }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Centered Shaded Banner: Subcenter Traceability Log Sheet
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE2E8F0))
                                    .border(1.dp, Color.Black)
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Subcenter Traceability Log Sheet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Metadata Line 1: Supplier Code, Supplier Name, Source Type
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showHeaderEditDialog = true }
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Supplier Code: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = traceState.supplierCode.ifBlank { "____________________" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (traceState.supplierCode.isNotBlank()) MilkNavy else Color(0xFF64748B)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Supplier Name: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = traceState.supplierName.ifBlank { "________________________________" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (traceState.supplierName.isNotBlank()) MilkNavy else Color(0xFF64748B)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Source Type: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = traceState.sourceType.ifBlank { "____________" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (traceState.sourceType.isNotBlank()) MilkNavy else Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Metadata Line 2: Village Name, Telephone Number
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showHeaderEditDialog = true }
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Village Name: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = traceState.villageName.ifBlank { "____________________" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (traceState.villageName.isNotBlank()) MilkNavy else Color(0xFF64748B)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Telephone Number: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = traceState.telephoneNumber.ifBlank { "________________________________" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (traceState.telephoneNumber.isNotBlank()) MilkNavy else Color(0xFF64748B)
                                    )
                                }

                                Text(
                                    text = "✎ Tap to Edit Header",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MilkBlue
                                )
                            }
                        }

                        // --- EXCEL COLUMN LETTERS ROW (A, B, C, D...) ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExcelHeaderBg)
                                .border(0.5.dp, ExcelGridBorder)
                        ) {
                            ExcelHeaderCell("#", 36.dp)
                            ExcelHeaderCell("A (Sr)", 44.dp)
                            ExcelHeaderCell("B (Farmer Name)", 170.dp)
                            ExcelHeaderCell("C (Village)", 65.dp)

                            var colLetter = 'D'
                            activeMonths.forEach { m ->
                                ExcelHeaderCell("$colLetter ($m)", 52.dp)
                                colLetter++
                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    ExcelHeaderCell("$colLetter (AASM)", 60.dp)
                                    colLetter++
                                }
                            }
                        }

                        // --- OFFICIAL TABLE HEADER ROW (19 COLUMNS EXACT) ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDCE3E9))
                                .border(1.dp, Color.Black)
                        ) {
                            ExcelHeaderTitleCell("Row", 36.dp)
                            ExcelHeaderTitleCell("Sr #", 44.dp)
                            ExcelHeaderTitleCell("Farmer Name", 170.dp)
                            ExcelHeaderTitleCell("Village", 65.dp)

                            activeMonths.forEach { m ->
                                ExcelHeaderTitleCell(m, 52.dp)
                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(28.dp)
                                            .border(0.5.dp, ExcelGridBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("AASM", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("Verification", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // --- SCROLLABLE DATA ROWS (1 to 70) ---
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
                                    // Row index #
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(28.dp)
                                            .background(Color(0xFFF1F5F9))
                                            .border(0.5.dp, ExcelGridBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(row.sr.toString(), fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    }

                                    // Sr #
                                    Box(
                                        modifier = Modifier
                                            .width(44.dp)
                                            .height(28.dp)
                                            .border(0.5.dp, ExcelGridBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(row.sr.toString(), fontSize = 10.sp, textAlign = TextAlign.Center)
                                    }

                                    // Farmer Name Cell (Editable & Selectable - BLANK IF EMPTY)
                                    val isNameSelected = selectedCell?.sr == row.sr && selectedCell?.columnKey == "NAME"
                                    Box(
                                        modifier = Modifier
                                            .width(170.dp)
                                            .height(28.dp)
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
                                        if (isNameSelected) {
                                            BasicTextField(
                                                value = row.name,
                                                onValueChange = { viewModel.updateRowFarmerName(row.sr, it) },
                                                textStyle = TextStyle(fontSize = 10.5.sp, color = Color.Black, fontWeight = FontWeight.Medium),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            Text(
                                                text = row.name,
                                                fontSize = 10.5.sp,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Village Cell (Stays Blank per Nestlé original standard)
                                    Box(
                                        modifier = Modifier
                                            .width(65.dp)
                                            .height(28.dp)
                                            .border(0.5.dp, ExcelGridBorder)
                                    )

                                    // Month Value Cells (BLANK IF ZERO)
                                    activeMonths.forEach { m ->
                                        val colKey = "MONTH_$m"
                                        val isCellSelected = selectedCell?.sr == row.sr && selectedCell?.columnKey == colKey
                                        val valNum = row.values[m]
                                        val valStr = if (valNum != null && valNum > 0) TraceabilityEngine.formatNumber(valNum) else ""

                                        Box(
                                            modifier = Modifier
                                                .width(52.dp)
                                                .height(28.dp)
                                                .background(if (isCellSelected) Color(0xFFE8F5E9) else Color.Transparent)
                                                .border(
                                                    width = if (isCellSelected) 2.dp else 0.5.dp,
                                                    color = if (isCellSelected) ExcelSelectedBorder else ExcelGridBorder
                                                )
                                                .clickable {
                                                    selectedCell = SelectedCell(row.sr, colKey, m)
                                                }
                                                .padding(horizontal = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCellSelected) {
                                                BasicTextField(
                                                    value = valStr,
                                                    onValueChange = { viewModel.updateRowMonthValue(row.sr, m, it) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    textStyle = TextStyle(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        textAlign = TextAlign.Center,
                                                        color = Color.Black,
                                                        fontFamily = FontFamily.Monospace
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            } else {
                                                Text(
                                                    text = valStr,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF0F172A),
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // AASM Verification column (Blank underline for official sign-off)
                                        if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                            Box(
                                                modifier = Modifier
                                                    .width(60.dp)
                                                    .height(28.dp)
                                                    .border(0.5.dp, ExcelGridBorder)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- TOTAL CALCULATION ROW ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0))
                                .border(1.dp, Color.Black),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp + 44.dp + 170.dp + 65.dp)
                                    .height(28.dp)
                                    .padding(start = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Total (${traceState.rows.count { it.name.isNotBlank() }} Active Farmers)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }

                            activeMonths.forEach { m ->
                                val sum = traceState.rows.sumOf { it.values[m] ?: 0.0 }
                                val sumStr = if (sum > 0) TraceabilityEngine.formatNumber(sum) else ""
                                Box(
                                    modifier = Modifier
                                        .width(52.dp)
                                        .height(28.dp)
                                        .border(0.5.dp, Color(0xFF94A3B8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sumStr,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = MilkNavy,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(28.dp)
                                            .border(0.5.dp, Color(0xFF94A3B8))
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .border(0.5.dp, Color(0xFFCBD5E1))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sheet1: Traceability • ${traceState.rows.size} Rows • A4 Single Page Fit",
                        fontSize = 10.sp,
                        color = Color(0xFF475569)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎤 Voice AI Ready",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED),
                            modifier = Modifier.clickable { showAiVoiceDialog = true }
                        )
                        Text(
                            text = "100%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // =========================================================================
            // --- VOICE AI ASSISTANT DIALOG (WITH MICROPHONE & TTS) ---
            // =========================================================================
            if (showAiVoiceDialog) {
                var manualInputText by remember { mutableStateOf("") }
                var lastAiUrduResponse by remember { mutableStateOf("میں آپ کا نیسلے ٹریس ایبلٹی ایکسل اسسٹنٹ ہوں۔ بولیں یا لکھ کر حکم دیں۔") }
                var lastAiEngResponse by remember { mutableStateOf("I am ready to edit the Excel sheet. Speak or type your instruction.") }

                val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (voiceManager.isListening) 1.25f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mic_scale"
                )

                AlertDialog(
                    onDismissRequest = {
                        voiceManager.stopListening()
                        voiceManager.stopSpeaking()
                        showAiVoiceDialog = false
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nestlé Traceability Voice AI Brain", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "اردو یا انگلش میں بولیں — میں خود بخود ایکسل شیٹ میں کام کروں گا:",
                                fontSize = 11.5.sp,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Large Microphone Button with Waveform Animation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (voiceManager.isListening) MilkRed else Color(0xFF7C3AED),
                                    modifier = Modifier
                                        .size(68.dp)
                                        .scale(pulseScale)
                                        .clickable {
                                            if (voiceManager.isListening) {
                                                voiceManager.stopListening()
                                            } else {
                                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Microphone",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (voiceManager.isListening) "🎤 سن رہا ہوں... بولیں (Listening...)" else "مائیک دبائیں اور بولیں (Tap Mic to Speak)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (voiceManager.isListening) MilkRed else Color(0xFF6D28D9),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // AI Voice Response Card
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF5F3FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("AI جواب (Response):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                                        IconButton(
                                            onClick = { voiceManager.speak(lastAiUrduResponse) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = Color(0xFF6D28D9), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text(
                                        text = lastAiUrduResponse,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E1B4B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lastAiEngResponse,
                                        fontSize = 10.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Action Voice Command Chips
                            Text("فوری احکامات (Quick Commands):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            Spacer(modifier = Modifier.height(4.dp))

                            val quickCommands = listOf(
                                "پوری شیٹ خالی کرو" to "Puri sheet khali kar do",
                                "کسانوں کے نام لکھو" to "30 farmers ke naam likho",
                                "جنوری میں 15000L تقسیم کرو" to "Jan mein 15000 liter distribute karo",
                                "آٹو بیلنس کرو" to "Auto balance sheet",
                                "سپلائر کوڈ 00S923 سیٹ کرو" to "Supplier code 00S923 set karo",
                                "AASM تصدیق چیک کرو" to "AASM verification check karo"
                            )

                            val chipScroll = rememberScrollState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(chipScroll),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickCommands.forEach { (label, cmd) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable {
                                            viewModel.executeAiCommand(cmd) { urdu, eng ->
                                                lastAiUrduResponse = urdu
                                                lastAiEngResponse = eng
                                                voiceManager.speak(urdu)
                                                notificationMessage = urdu
                                            }
                                        }
                                    ) {
                                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Manual typing option
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualInputText,
                                    onValueChange = { manualInputText = it },
                                    placeholder = { Text("یا یہاں لکھ کر ہدایت دیں...", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (manualInputText.isNotBlank()) {
                                            val txt = manualInputText
                                            manualInputText = ""
                                            viewModel.executeAiCommand(txt) { urdu, eng ->
                                                lastAiUrduResponse = urdu
                                                lastAiEngResponse = eng
                                                voiceManager.speak(urdu)
                                                notificationMessage = urdu
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                voiceManager.stopListening()
                                voiceManager.stopSpeaking()
                                showAiVoiceDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
                        ) {
                            Text("Done (مکمل)")
                        }
                    }
                )
            }

            // =========================================================================
            // --- HEADER EDIT DIALOG ---
            // =========================================================================
            if (showHeaderEditDialog) {
                var code by remember { mutableStateOf(traceState.supplierCode) }
                var name by remember { mutableStateOf(traceState.supplierName) }
                var source by remember { mutableStateOf(traceState.sourceType) }
                var village by remember { mutableStateOf(traceState.villageName) }
                var phone by remember { mutableStateOf(traceState.telephoneNumber) }
                var location by remember { mutableStateOf(traceState.locationCode) }

                AlertDialog(
                    onDismissRequest = { showHeaderEditDialog = false },
                    title = { Text("Edit Sheet Header Details", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it },
                                label = { Text("Supplier Code (e.g. 00S923)") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Supplier Name (e.g. Nadeem Tariq)") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            OutlinedTextField(
                                value = source,
                                onValueChange = { source = it },
                                label = { Text("Source Type (DO, TS, VD)") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            OutlinedTextField(
                                value = village,
                                onValueChange = { village = it },
                                label = { Text("Village Name (e.g. Chak 45)") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Telephone Number") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Location Code & Name") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.updateTraceHeader(
                                    supplierCode = code,
                                    supplierName = name,
                                    sourceType = source,
                                    villageName = village,
                                    farmerNameHint = traceState.farmerNameHint,
                                    isAutoMode = traceState.isAutoMode
                                )
                                showHeaderEditDialog = false
                                notificationMessage = "✓ Header details saved"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showHeaderEditDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // =========================================================================
            // --- CLEAR SHEET CONFIRMATION DIALOG ---
            // =========================================================================
            if (showClearConfirmation) {
                AlertDialog(
                    onDismissRequest = { showClearConfirmation = false },
                    title = { Text("Clear Entire Sheet? (پوری شیٹ خالی کریں؟)") },
                    text = {
                        Text(
                            text = "کیا آپ تمام 70 کسانوں کے نام اور 12 مہینوں کا تمام ڈیٹا مٹا کر شیٹ کو بالکل خالی اور بلینک کرنا چاہتے ہیں؟",
                            fontSize = 12.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.clearAllSheetData()
                                showClearConfirmation = false
                                notificationMessage = "✓ پوری شیٹ خالی اور بلینک کر دی گئی ہے۔"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkRed)
                        ) {
                            Text("خالی کرو (Clear All)")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showClearConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
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
            .height(30.dp)
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
