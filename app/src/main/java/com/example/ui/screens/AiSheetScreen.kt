package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.components.AppFormField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.traceability.ExcelAiEngine
import com.example.traceability.TraceabilityEngine
import com.example.ui.MainViewModel
import com.example.ui.TraceRowUi
import com.example.ui.components.FullScreenExcelStudioDialog
import com.example.ui.components.FullScreenPdfPreviewDialog
import com.example.ui.theme.MilkAmber
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkRed
import com.example.ui.theme.MilkSky
import com.example.util.PdfGenerator
import com.example.util.SheetFileParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiSheetScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val traceState by viewModel.traceState.collectAsStateWithLifecycle()

    var showClearConfirm by remember { mutableStateOf(false) }
    var lastDownloadedPdfUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showFullScreenExcelStudio by remember { mutableStateOf(false) }
    var showFullScreenPdfPreview by remember { mutableStateOf(false) }
    var showDownloadSuccessDialog by remember { mutableStateOf(false) }
    var showAiSheetDialog by remember { mutableStateOf(false) }
    var uploadStatusNotification by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
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
                    uploadStatusNotification = "✓ Sheet replaced with: ${parsed.fileName}"
                } catch (e: Exception) {
                    uploadStatusNotification = "Failed to load uploaded file"
                }
            }
        }
    }

    fun shareExport(name: String, content: String, mimeType: String = "text/html") {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, name)
            putExtra(Intent.EXTRA_SUBJECT, name)
            putExtra(Intent.EXTRA_TEXT, content)
            type = mimeType
        }
        context.startActivity(Intent.createChooser(intent, "Export Traceability Sheet"))
    }

    fun printSheetHtml(html: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Subcenter_Traceability_Log_Sheet")
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager?.print("Traceability_Log_Sheet", printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Traceability Sheet?") },
            text = { Text("Are you sure you want to clear all current traceability data and reset the sheet?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearTraceSheet()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MilkRed)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val maxFarmersCount = remember(traceState.selectedMonths, traceState.monthConfigs) {
        if (traceState.selectedMonths.isEmpty()) 20
        else traceState.selectedMonths.maxOf { traceState.monthConfigs[it]?.count ?: 20 }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_sheet_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0B1F36), Color(0xFF145DA0))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "AI Sheet Entry",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Professional Subcenter Traceability Log Sheet filling assistant. Enter header information, select months, set farmer counts and Max KGs/L, then auto-fill or enter manually.",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SheetChip("● Original layout preserved")
                            SheetChip("● Village column stays blank")
                            SheetChip("● Unused month cells stay blank")
                            SheetChip("● Standard 70 Rows")
                        }
                    }
                }
            }
        }

        // Header Data Entry Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sheet Header Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(MilkGreen.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("● Ready", color = MilkGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppFormField(
                            label = "SUPPLIER CODE *",
                            value = traceState.supplierCode,
                            onValueChange = {
                                viewModel.updateTraceHeader(it, traceState.supplierName, traceState.sourceType, traceState.villageName, traceState.farmerNameHint, traceState.isAutoMode)
                            },
                            placeholder = "e.g. 0S1055",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trace_supplier_code_input")
                        )

                        AppFormField(
                            label = "SOURCE TYPE",
                            value = traceState.sourceType,
                            onValueChange = {
                                viewModel.updateTraceHeader(traceState.supplierCode, traceState.supplierName, it, traceState.villageName, traceState.farmerNameHint, traceState.isAutoMode)
                            },
                            placeholder = "DO",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trace_source_type_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AppFormField(
                        label = "SUPPLIER NAME *",
                        value = traceState.supplierName,
                        onValueChange = {
                            viewModel.updateTraceHeader(traceState.supplierCode, it, traceState.sourceType, traceState.villageName, traceState.farmerNameHint, traceState.isAutoMode)
                        },
                        placeholder = "e.g. Bilal Ahmad Milk Collection",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trace_supplier_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppFormField(
                            label = "VILLAGE NAME (TOP) *",
                            value = traceState.villageName,
                            onValueChange = {
                                viewModel.updateTraceHeader(traceState.supplierCode, traceState.supplierName, traceState.sourceType, it, traceState.farmerNameHint, traceState.isAutoMode)
                            },
                            placeholder = "Village name",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trace_village_name_input")
                        )

                        AppFormField(
                            label = "1ST FARMER (OPTIONAL)",
                            value = traceState.farmerNameHint,
                            onValueChange = {
                                viewModel.updateTraceHeader(traceState.supplierCode, traceState.supplierName, traceState.sourceType, traceState.villageName, it, traceState.isAutoMode)
                            },
                            placeholder = "Leave blank for auto Urdu",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trace_farmer_name_hint_input")
                        )
                    }
                }
            }
        }

        // Months & Farmer Counts Controls
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Months & Farmer Counts",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select months. Each has its own # of farmers and Max KGs/L.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleAllTraceMonths() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("toggle_all_months_btn")
                        ) {
                            Text("All / Clear", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Month Checkboxes
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TraceabilityEngine.TRACE_MONTHS.forEach { month ->
                            val isSelected = traceState.selectedMonths.contains(month)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleTraceMonth(month) },
                                label = { Text(month, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("month_chip_$month")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Per-Month Configurations (Farmers & Max KG)
                    Text(
                        text = "Configured Limits for Selected Months:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    traceState.selectedMonths.forEach { m ->
                        val cfg = traceState.monthConfigs[m] ?: com.example.data.model.MonthConfig(20, 45.0)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(m, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MilkBlue)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("# Farmers:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    BasicTextField(
                                        value = cfg.count.toString(),
                                        onValueChange = {
                                            val c = it.toIntOrNull() ?: cfg.count
                                            viewModel.updateMonthConfig(m, c, cfg.maxKg)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .width(46.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Max KG:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    BasicTextField(
                                        value = TraceabilityEngine.formatNumber(cfg.maxKg),
                                        onValueChange = {
                                            val k = it.toDoubleOrNull() ?: cfg.maxKg
                                            viewModel.updateMonthConfig(m, cfg.count, k)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier
                                            .width(52.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.autoFillTraceSheet() },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkSky),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("auto_fill_sheet_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto Fill Sheet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.prepareManualTraceSheet() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("manual_entry_sheet_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manual Entry", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveTraceSheetEdits() },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("save_sheet_edits_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Edits", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val html = generateTraceHtml(traceState)
                                shareExport("Subcenter_Traceability_Log_Sheet.xls", html, "application/vnd.ms-excel")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("export_sheet_xls_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share / Excel", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Professional Excel + PDF Upload Workspace & Free AI Assistant
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Professional Document Workspace",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Free Built-in AI", color = Color(0xFF15803D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload your custom Excel (.xlsx/.xls/.csv) or PDF document. The app preserves original formatting, layout, fonts, and A4 borders with zero freezing or crashes.",
                                fontSize = 11.sp,
                                color = Color(0xFF166534),
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("upload_replace_sheet_main_button")
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload Excel / PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showAiSheetDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("free_ai_sheet_assistant_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Document Agent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (uploadStatusNotification != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uploadStatusNotification!!,
                            color = MilkGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Full Screen Excel Studio & Single-Page PDF Preview Trigger Ribbon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Full-Screen Workspaces",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Strict 1-Page A4 Setup",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MilkGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showFullScreenExcelStudio = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("open_fullscreen_excel_button")
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Full Excel Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showFullScreenPdfPreview = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MilkNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("open_fullscreen_pdf_preview_button")
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View A4 PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val uri = PdfGenerator.downloadTraceSheetPdf(context, traceState)
                                lastDownloadedPdfUri = uri
                                if (uri != null) {
                                    showDownloadSuccessDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("download_trace_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PDF Sheet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val html = generateTraceHtml(traceState)
                                printSheetHtml(html)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(44.dp)
                                .testTag("print_sheet_pdf_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print", fontSize = 12.sp)
                        }
                    }

                    if (lastDownloadedPdfUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { lastDownloadedPdfUri?.let { PdfGenerator.openPdf(context, it) } },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Text("📄 Open Downloaded PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { lastDownloadedPdfUri?.let { PdfGenerator.sharePdf(context, it, "Subcenter Traceability Log Sheet") } },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Text("↗ Share PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MilkRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("clear_sheet_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Traceability Sheet", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Message Banner
                    val statusBg = when {
                        traceState.isSuccessStatus -> MilkGreen.copy(alpha = 0.12f)
                        traceState.isWarningStatus -> MilkAmber.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                    val statusColor = when {
                        traceState.isSuccessStatus -> MilkGreen
                        traceState.isWarningStatus -> MilkAmber
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = traceState.statusMessage,
                            fontSize = 12.sp,
                            color = statusColor,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Sheet Preview Header & Legend
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nestlé Subcenter Traceability Log Sheet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF107C41))
                                    .clickable { showFullScreenExcelStudio = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Full-Screen", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(MilkSky.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("${traceState.rows.size} Rows", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MilkSky)
                            }
                        }
                    }

                    Text(
                        text = "Supplier: ${traceState.supplierCode.ifBlank { "—" }} • ${traceState.supplierName.ifBlank { "—" }} | Village: ${traceState.villageName.ifBlank { "—" }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    // Month totals summary chip row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        traceState.selectedMonths.forEach { m ->
                            val total = traceState.rows.sumOf { it.values[m] ?: 0.0 }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("$m: ${TraceabilityEngine.formatNumber(total)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MilkBlue)
                            }
                        }
                    }
                }
            }
        }

        // Horizontal scrollable table container for the 70 rows
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val horizontalScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScroll)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFDCE3E9))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sr #", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                            Text("Farmer Name", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(170.dp), textAlign = TextAlign.Center)
                            Text("Village", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)

                            TraceabilityEngine.TRACE_MONTHS.forEach { m ->
                                Text(m, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(52.dp), textAlign = TextAlign.Center)
                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    Text("AASM\nVerify", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(52.dp), textAlign = TextAlign.Center, lineHeight = 11.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = Color.Black, thickness = 1.dp)

                        // 70 Rows rendered with lightweight Text (Zero UI thread lag, instant scrolling)
                        traceState.rows.forEachIndexed { i, row ->
                            val isEven = i % 2 == 0
                            val rowBg = if (isEven) Color(0xFFF8FAFC) else Color.White

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .clickable { showFullScreenExcelStudio = true }
                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.sr.toString(),
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(42.dp),
                                    textAlign = TextAlign.Center
                                )

                                // Farmer Name
                                Text(
                                    text = row.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    modifier = Modifier
                                        .width(170.dp)
                                        .padding(horizontal = 4.dp)
                                )

                                // Village (stays blank per specification)
                                Text("", modifier = Modifier.width(70.dp))

                                // Month Values
                                TraceabilityEngine.TRACE_MONTHS.forEach { m ->
                                    val valNum = row.values[m]
                                    val valStr = if (valNum != null && valNum > 0) TraceabilityEngine.formatNumber(valNum) else ""

                                    Box(
                                        modifier = Modifier.width(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = valStr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (valStr.isNotEmpty()) Color(0xFF1E293B) else Color(0xFF94A3B8),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                        Text("", modifier = Modifier.width(52.dp))
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        }

                        // Total Row
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFDCE3E9))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.width(282.dp),
                                textAlign = TextAlign.Center
                            )

                            TraceabilityEngine.TRACE_MONTHS.forEach { m ->
                                val total = traceState.rows.sumOf { it.values[m] ?: 0.0 }
                                Text(
                                    text = if (total > 0) TraceabilityEngine.formatNumber(total) else "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(52.dp),
                                    textAlign = TextAlign.Center
                                )
                                if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                    Text("", modifier = Modifier.width(52.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFullScreenExcelStudio) {
        FullScreenExcelStudioDialog(
            viewModel = viewModel,
            traceState = traceState,
            onDismiss = { showFullScreenExcelStudio = false },
            onOpenPdfPreview = {
                showFullScreenExcelStudio = false
                showFullScreenPdfPreview = true
            },
            onPrint = {
                val html = generateTraceHtml(traceState)
                printSheetHtml(html)
            },
            onShareXls = {
                val html = generateTraceHtml(traceState)
                shareExport("Subcenter_Traceability_Log_Sheet.xls", html, "application/vnd.ms-excel")
            }
        )
    }

    if (showFullScreenPdfPreview) {
        FullScreenPdfPreviewDialog(
            traceState = traceState,
            onDismiss = { showFullScreenPdfPreview = false },
            onPrint = {
                val html = generateTraceHtml(traceState)
                printSheetHtml(html)
            }
        )
    }

    if (showDownloadSuccessDialog && lastDownloadedPdfUri != null) {
        AlertDialog(
            onDismissRequest = { showDownloadSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MilkGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PDF Sheet Ready!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Your Subcenter Traceability Log Sheet has been saved with strict 1-Page A4 formatting to your Downloads folder.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "📄 Subcenter_Traceability_Log_Sheet_${traceState.supplierCode.ifBlank { "0S1055" }}.pdf",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📁 Saved to: Internal Storage > Downloads > MilkCollection", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("✓ Strict 1-Page A4 Sheet (Fits 1 page with no overflow or extra pages)", fontSize = 10.sp, color = MilkGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        lastDownloadedPdfUri?.let { PdfGenerator.openPdf(context, it) }
                        showDownloadSuccessDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MilkNavy)
                ) {
                    Text("Open PDF")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        lastDownloadedPdfUri?.let { PdfGenerator.sharePdf(context, it, "Traceability Log Sheet") }
                        showDownloadSuccessDialog = false
                    }
                ) {
                    Text("Share")
                }
            }
        )
    }

    if (showAiSheetDialog) {
        val auditReport = remember(traceState) {
            ExcelAiEngine.auditSheet(traceState)
        }

        AlertDialog(
            onDismissRequest = { showAiSheetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MilkGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Free Excel AI Assistant", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (auditReport.auditReadinessScore >= 80) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Audit Compliance Score:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${auditReport.auditReadinessScore}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (auditReport.auditReadinessScore >= 80) MilkGreen else Color(0xFFE65100)
                            )
                        }
                    }

                    Text(
                        text = auditReport.summaryTextUrdu,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.applyAiAutoBalance()
                                uploadStatusNotification = "✓ AI Auto-Balance Completed"
                                showAiSheetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Auto-Balance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.applyAiSmartComplete()
                                uploadStatusNotification = "✓ AI Smart Complete Applied"
                                showAiSheetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Smart Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showAiSheetDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SheetChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

fun generateTraceHtml(state: com.example.ui.TraceSheetUiState): String {
    val sb = StringBuilder()
    val isLarge = state.rows.size > 40
    val fontSize = if (isLarge) "6.6px" else "7.8px"
    val lineHeight = if (isLarge) "8.0px" else "9.5px"
    val cellPadding = if (isLarge) "0.5px 1px" else "1.5px 2px"

    sb.append("<!doctype html><html><head><meta charset=\"utf-8\"><style>")
    sb.append("@page{size:A4 landscape;margin:2.5mm 3mm 2.5mm 3mm}@media print{html,body{width:100%;height:100%;margin:0;padding:0;overflow:hidden;-webkit-print-color-adjust:exact;print-color-adjust:exact}.sheet{page-break-after:avoid;page-break-inside:avoid}}")
    sb.append("*{box-sizing:border-box}body{font-family:Arial,Helvetica,sans-serif;margin:0;padding:2mm;color:#0f172a;-webkit-text-size-adjust:100%}")
    sb.append(".sheet{border-collapse:collapse;width:100%;table-layout:fixed;page-break-inside:avoid;page-break-after:avoid}")
    sb.append(".sheet td,.sheet th{border:0.65px solid #1e293b;text-align:center;padding:$cellPadding;font-size:$fontSize;line-height:$lineHeight}")
    sb.append(".sheet th{background:#dce3e9;font-weight:700}.brand{text-align:left!important;border:0!important;font-size:12px!important;font-weight:800;line-height:14px!important}")
    sb.append(".sub{font-size:7.5px;font-weight:700;color:#334155}.meta{border:0!important;text-align:left!important;font-size:7.5px!important;line-height:9px!important}.right{text-align:right!important}")
    sb.append(".title{font-size:12px!important;background:#dce3e9;font-weight:800;padding:2px 0!important}.left{text-align:left!important;padding-left:3px!important}.total{background:#dce3e9;font-weight:800;font-size:$fontSize!important}</style></head><body>")

    val nestleSvg = """<svg width="125" height="32" viewBox="0 0 160 44" fill="#1e293b" xmlns="http://www.w3.org/2000/svg"><path d="M6,34 Q20,31 34,33 Q42,34 46,31 Q40,36 28,36 Q16,36 6,34 Z"/><path d="M14,24 Q24,37 36,24 Q32,32 18,31 Z"/><path d="M11,15 Q14,11 18,12 Q20,13 22,17 Q25,20 22,23 Q18,25 15,22 Q12,20 11,15 Z"/><path d="M18,12 Q20,10 23,10 Q25,10 26,12 L29,13 L26,14 Q24,15 22,14 Z"/><path d="M15,14 Q22,8 30,12 Q27,15 21,17 Z"/><path d="M23,20 Q24,16 26,16 L28,18 Q27,21 24,22 Z"/><path d="M27,19 Q29,15 31,16 L33,18 Q31,21 28,21 Z"/><path d="M31,20 Q33,16 35,17 L36,19 Q34,22 32,22 Z"/><path d="M52,12 L57,12 L57,32 L52,32 Z"/><path d="M56,12 L67,29 L67,12 L71,12 L71,32 L66,32 L55,15 Z"/><path d="M67,12 L72,12 L72,32 L67,32 Z"/><path d="M52,8 L138,8 L138,11.5 L52,11.5 Z"/><path d="M76,24 Q76,17 83,17 Q90,17 90,24 L79.5,24 Q79.5,28.5 83.5,28.5 Q86,28.5 88,27.5 L89,30 Q86.5,31.5 83,31.5 Q76,31.5 76,24 Z M86.5,21.5 Q86.5,19.5 83,19.5 Q79.8,19.5 79.5,21.5 Z"/><path d="M94,28.5 Q95.5,29.3 97.5,29.3 Q99.5,29.3 99.5,28 Q99.5,26.8 97,26.2 Q93,25.2 93,21.5 Q93,17.2 98,17.2 Q100.5,17.2 102.5,18.2 L101.5,20.5 Q99.8,19.5 98,19.5 Q96,19.5 96,20.5 Q96,21.5 98.5,22 Q103,23.2 103,27 Q103,31.5 97.5,31.5 Q95,31.5 92.5,30.3 Z"/><path d="M106,14 L110,14 L110,18 L114,18 L114,20.5 L110,20.5 L110,27.5 Q110,29 111.5,29 Q112.5,29 113.5,28.5 L114,31 Q112.5,31.5 110.5,31.5 Q106.5,31.5 106.5,27 L106.5,20.5 L104,20.5 L104,18 L106.5,18 Z"/><path d="M118,10 L122.5,10 L122.5,32 L118,32 Z"/><path d="M126,24 Q126,17 133,17 Q140,17 140,24 L129.5,24 Q129.5,28.5 133.5,28.5 Q136,28.5 138,27.5 L139,30 Q136.5,31.5 133,31.5 Q126,31.5 126,24 Z M136.5,21.5 Q136.5,19.5 133,19.5 Q129.8,19.5 129.5,21.5 Z M135.5,12.5 L138.5,12.5 L135,16 L132.5,16 Z"/></svg>"""

    sb.append("<table class=\"sheet\">")
    sb.append("<tr><td colspan=\"6\" class=\"brand\">Nestlé Pakistan Ltd.<br><span class=\"sub\">(Milk Collection &amp; Dairy Development)</span></td><td colspan=\"13\" class=\"right\" style=\"border:0!important;vertical-align:top\">$nestleSvg</td></tr>")
    sb.append("<tr><td colspan=\"6\" class=\"meta\">Document #: 1583-CAM-D4-13.00</td><td colspan=\"13\" class=\"meta right\">Location Code &amp; Name: __________________________</td></tr>")
    sb.append("<tr><td colspan=\"19\" class=\"title\">Subcenter Traceability Log Sheet</td></tr>")
    sb.append("<tr><td colspan=\"6\" class=\"meta\">Supplier Code: <b>${state.supplierCode.ifBlank { "00S923" }}</b></td><td colspan=\"7\" class=\"meta\">Supplier Name: <b>${state.supplierName.ifBlank { "Nadeem Tariq" }}</b></td><td colspan=\"6\" class=\"meta right\">Source Type: <b>${state.sourceType.ifBlank { "DO" }}</b></td></tr>")
    sb.append("<tr><td colspan=\"6\" class=\"meta\">Village Name: <b>${state.villageName}</b></td><td colspan=\"13\" class=\"meta\">Telephone Number: __________________________</td></tr>")

    sb.append("<tr><th style=\"width:3.2%\">Sr #</th><th style=\"width:16%\">Farmer Name</th><th style=\"width:5.5%\">Village</th>")
    TraceabilityEngine.TRACE_MONTHS.forEach { m ->
        sb.append("<th style=\"width:4.8%\">$m</th>")
        if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
            sb.append("<th style=\"width:4.6%\">AASM<br>Verify</th>")
        }
    }
    sb.append("</tr>")

    state.rows.forEach { r ->
        sb.append("<tr><td>${r.sr}</td><td class=\"left\">${r.name}</td><td></td>")
        TraceabilityEngine.TRACE_MONTHS.forEach { m ->
            val v = r.values[m]
            val s = if (v != null && v > 0) TraceabilityEngine.formatNumber(v) else ""
            sb.append("<td>$s</td>")
            if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                sb.append("<td></td>")
            }
        }
        sb.append("</tr>")
    }

    sb.append("<tr><td colspan=\"3\" class=\"total\">Total</td>")
    TraceabilityEngine.TRACE_MONTHS.forEach { m ->
        val total = state.rows.sumOf { it.values[m] ?: 0.0 }
        val s = if (total > 0) TraceabilityEngine.formatNumber(total) else ""
        sb.append("<td class=\"total\"><b>$s</b></td>")
        if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
            sb.append("<td class=\"total\"></td>")
        }
    }
    sb.append("</tr></table>")
    sb.append("<div style=\"text-align:center;font-size:7px;color:#475569;margin-top:2px;font-family:Arial,sans-serif\">Subcenter Traceability Log Sheet</div>")
    sb.append("</body></html>")
    return sb.toString()
}
