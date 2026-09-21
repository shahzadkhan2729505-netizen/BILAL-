package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import com.example.util.PdfGenerator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MilkRecord
import com.example.ui.MainViewModel
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkRed
import com.example.ui.theme.MilkSky
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun RecordsScreen(
    viewModel: MainViewModel,
    onNavigateToEditRecord: (MilkRecord) -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.records.collectAsStateWithLifecycle()
    val kpis by viewModel.dashboardKpis.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val fromDate by viewModel.fromDate.collectAsStateWithLifecycle()
    val toDate by viewModel.toDate.collectAsStateWithLifecycle()

    var showClearAllDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<MilkRecord?>(null) }

    val calendar = remember { Calendar.getInstance() }

    val fromDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val c = Calendar.getInstance().apply { set(year, month, day) }
                val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
                viewModel.setDateFilter(d, toDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val toDatePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val c = Calendar.getInstance().apply { set(year, month, day) }
                val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
                viewModel.setDateFilter(fromDate, d)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Clear All Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Records?") },
            text = { Text("Are you sure you want to delete ALL milk collection records? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MilkRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Single Record Dialog
    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Milk Record?") },
            text = { Text("Delete record for ${recordToDelete?.farmerName} on ${recordToDelete?.date} (${recordToDelete?.liters} L)?") },
            confirmButton = {
                Button(
                    onClick = {
                        recordToDelete?.let { viewModel.deleteRecord(it.id) }
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MilkRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("records_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Filter Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Milk Records Filter",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search farmer name, ID, remarks...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("record_search_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = fromDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("FROM DATE") },
                            placeholder = { Text("YYYY-MM-DD") },
                            trailingIcon = {
                                IconButton(onClick = { fromDatePicker.show() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick From Date", modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { fromDatePicker.show() }
                                .testTag("from_date_filter"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = toDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("TO DATE") },
                            placeholder = { Text("YYYY-MM-DD") },
                            trailingIcon = {
                                IconButton(onClick = { toDatePicker.show() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick To Date", modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { toDatePicker.show() }
                                .testTag("to_date_filter"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                PdfGenerator.downloadMilkReportPdf(context, records, kpis)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onExportCsv,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearRecordFilters() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text("Reset", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Summary Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${records.size} of ${allRecords.size} Records",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (records.isNotEmpty()) {
                    val totalFilteredPay = records.sumOf { it.payment }
                    Text(
                        text = "Total: Rs ${String.format(Locale.US, "%,.0f", totalFilteredPay)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MilkGreen
                    )
                }
            }
        }

        // Records List
        if (records.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (allRecords.isEmpty()) "No milk records recorded yet." else "No records match current filters.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(records, key = { it.id }) { record ->
                MilkRecordCard(
                    record = record,
                    onEdit = { onNavigateToEditRecord(record) },
                    onDelete = { recordToDelete = record }
                )
            }
        }
    }
}

@Composable
fun MilkRecordCard(
    record: MilkRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("record_card_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Date, Farmer, Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MilkSky.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MilkSky, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = record.farmerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${record.farmerId} • ${record.date}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "Rs %,.2f", record.payment),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MilkGreen
                    )
                    Text(
                        text = "@ Rs ${record.rate}/L",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Stats Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecordStatChip(label = "Milk", value = "${String.format(Locale.US, "%.2f", record.liters)} L", modifier = Modifier.weight(1f))
                RecordStatChip(label = "Fat", value = "${String.format(Locale.US, "%.2f", record.fat)}%", modifier = Modifier.weight(1f))
                RecordStatChip(label = "LR", value = String.format(Locale.US, "%.1f", record.lr), modifier = Modifier.weight(1f))
                RecordStatChip(label = "TS Equiv", value = "${String.format(Locale.US, "%.2f", record.tsMilk)} L", highlight = true, modifier = Modifier.weight(1.2f))
            }

            // Expandable details (Specific Gravity, KG calculations, Remarks)
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Specific Gravity:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(Locale.US, "%.3f", record.spGravity), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Milk KG:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(Locale.US, "%.3f KG", record.milkKg), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fat KG:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(Locale.US, "%.3f KG", record.fatKg), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SNF (% & KG):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format(Locale.US, "%.2f%%", record.snf)} • ${String.format(Locale.US, "%.3f KG", record.snfKg)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TS (% & KG):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format(Locale.US, "%.2f%%", record.ts)} • ${String.format(Locale.US, "%.3f KG", record.tsKg)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (record.remarks.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Remarks:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(record.remarks, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer: Toggle details & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Less Details" else "All Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MilkSky
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MilkSky,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val cardContext = androidx.compose.ui.platform.LocalContext.current
                    IconButton(
                        onClick = { PdfGenerator.downloadSingleSlipPdf(cardContext, record) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Download Slip PDF", tint = MilkGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Record", tint = MilkSky, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Record", tint = MilkRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecordStatChip(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlight) MilkBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 6.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MilkBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (highlight) MilkBlue else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
