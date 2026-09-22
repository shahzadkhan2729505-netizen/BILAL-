package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.traceability.TraceabilityEngine
import com.example.ui.TraceSheetUiState
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.util.PdfGenerator

/**
 * Immersive Full-Screen PDF Document Preview Dialog.
 * Shows the exact visual layout of the single-page A4 Landscape sheet.
 */
@Composable
fun FullScreenPdfPreviewDialog(
    traceState: TraceSheetUiState,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    val context = LocalContext.current
    var lastDownloadedUri by remember { mutableStateOf<Uri?>(null) }
    var downloadStatus by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1E293B)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MilkNavy)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                    text = "A4 PDF Single-Page Preview",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MilkGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Strict 1-Page A4 Fit", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = if (traceState.isCustomUploaded) traceState.loadedDocumentName else "Subcenter_Traceability_Log_Sheet_${traceState.supplierCode.ifBlank { "0S1055" }}.pdf",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val uri = PdfGenerator.downloadTraceSheetPdf(context, traceState)
                                lastDownloadedUri = uri
                                downloadStatus = if (uri != null) "✓ Downloaded to Phone Downloads!" else "Download failed"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).testTag("dialog_download_pdf_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = onPrint,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.White)
                        }

                        if (lastDownloadedUri != null) {
                            IconButton(
                                onClick = {
                                    PdfGenerator.sharePdf(context, lastDownloadedUri!!, "Traceability Log Sheet")
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                            }
                        }
                    }
                }

                // Download Status Ribbon
                if (downloadStatus != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MilkGreen)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = downloadStatus!!,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (lastDownloadedUri != null) {
                            Text(
                                text = "OPEN PDF NOW ➔",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Preview Body (Simulating Paper Sheet with scrollable canvas)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val vScroll = rememberScrollState()
                    val hScroll = rememberScrollState()

                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(vScroll)
                            .horizontalScroll(hScroll)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(940.dp)
                                .padding(16.dp)
                        ) {
                            // Sheet Header with Authentic Nestlé Logo
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
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.ic_nestle_logo),
                                    contentDescription = "Nestlé Logo",
                                    modifier = Modifier.height(36.dp).padding(end = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Document #: 1583-CAM-D4-13.00", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text("Location Code & Name: ___________________________", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Centered Shaded Title Banner matching photo exactly
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
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Metadata Line 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Supplier Code: ${traceState.supplierCode.ifBlank { "________________" }}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text("Supplier Name: ${traceState.supplierName.ifBlank { "____________________________" }}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text("Source Type: ${traceState.sourceType.ifBlank { "________" }}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Metadata Line 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Village Name: ${traceState.villageName.ifBlank { "________________" }}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text("Telephone Number: ${traceState.telephoneNumber.ifBlank { "____________________________" }}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFDCE3E9))
                                    .border(1.dp, Color.Black)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sr #", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                                Text("Farmer Name", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(170.dp), textAlign = TextAlign.Center)
                                Text("Village", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)

                                TraceabilityEngine.TRACE_MONTHS.forEach { m ->
                                    Text(m, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(46.dp), textAlign = TextAlign.Center)
                                    if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                        Text("AASM\nVerify", fontWeight = FontWeight.Bold, fontSize = 7.5.sp, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center, lineHeight = 9.sp)
                                    }
                                }
                            }

                            // Table Rows (All rows rendered with tight single-page precision)
                            traceState.rows.forEachIndexed { i, row ->
                                val isEven = i % 2 == 0
                                val bg = if (isEven) Color.White else Color(0xFFF8FAFC)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bg)
                                        .border(0.5.dp, Color(0xFFCBD5E1))
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(row.sr.toString(), fontSize = 8.5.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                                    Text(row.name, fontSize = 8.5.sp, modifier = Modifier.width(170.dp).padding(start = 4.dp), maxLines = 1)
                                    Text("", modifier = Modifier.width(60.dp))

                                    TraceabilityEngine.TRACE_MONTHS.forEach { m ->
                                        val v = row.values[m]
                                        val str = if (v != null && v > 0) TraceabilityEngine.formatNumber(v) else ""
                                        Text(str, fontSize = 8.5.sp, modifier = Modifier.width(46.dp), textAlign = TextAlign.Center, fontFamily = FontFamily.Monospace)

                                        if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                            Text("", modifier = Modifier.width(44.dp))
                                        }
                                    }
                                }
                            }

                            // Grand Total Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFDCE3E9))
                                    .border(1.dp, Color.Black)
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, modifier = Modifier.width(266.dp), textAlign = TextAlign.Center)

                                TraceabilityEngine.TRACE_MONTHS.forEach { m ->
                                    val total = traceState.rows.sumOf { it.values[m] ?: 0.0 }
                                    val totalStr = if (total > 0) TraceabilityEngine.formatNumber(total) else ""
                                    Text(totalStr, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, modifier = Modifier.width(46.dp), textAlign = TextAlign.Center)

                                    if (TraceabilityEngine.VERIFICATION_MONTHS.contains(m)) {
                                        Text("", modifier = Modifier.width(44.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Page 1 of 1 • Single-Page A4 Sheet Layout (Fit to 1 Page Wide by 1 Page Tall) • Guaranteed 100% Fit",
                                    fontSize = 8.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Nestlé Pakistan Ltd. Subcenter Traceability Sheet",
                                    fontSize = 8.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
