package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Farmer
import com.example.data.model.FarmerWeeklyHistory
import com.example.data.model.MilkRecord
import com.example.ui.MainViewModel
import com.example.ui.WeeklySummaryCalculations
import com.example.ui.theme.AppGradients
import com.example.ui.theme.MilkAmber
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkPurple
import com.example.ui.theme.MilkSky
import com.example.util.WeekDateUtils
import com.example.util.WeekInfo
import java.util.Locale

@Composable
fun FarmerWeeklyEntryScreen(
    viewModel: MainViewModel,
    onBackToList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val farmer by viewModel.selectedWeeklyFarmer.collectAsStateWithLifecycle()
    val weekInfo by viewModel.currentWeekInfo.collectAsStateWithLifecycle()
    val weeklyEntries by viewModel.currentWeeklyEntries.collectAsStateWithLifecycle()
    val weeklySummary by viewModel.currentWeeklySummary.collectAsStateWithLifecycle()
    val weeklyHistory by viewModel.selectedFarmerWeeklyHistory.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Current Week, 1 = Weekly History

    if (farmer == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No farmer selected.")
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onBackToList) {
                    Text("Return to Farmer List")
                }
            }
        }
        return
    }

    val currentFarmer = farmer!!

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("farmer_weekly_entry_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Navigation & Farmer Identity Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppGradients.BlueCobalt)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onBackToList,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Farmer List",
                                    tint = Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "FARMER CODE: ${currentFarmer.id}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = currentFarmer.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentFarmer.village.isNotBlank()) "Village: ${currentFarmer.village}" else "Standard Dairy Member",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Segmented Tabs: Current Week (Monday -> Sunday) vs Weekly History
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MilkBlue,
                    indicator = {}
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Current Week",
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )

                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Weekly History (${weeklyHistory.size})",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                }
            }
        }

        if (selectedTabIndex == 0) {
            // ================== TAB 0: CURRENT WEEK (MONDAY -> SUNDAY) ==================
            // Week Header Info
            item {
                CurrentWeekBanner(weekInfo = weekInfo)
            }

            // Entries List
            if (weeklyEntries.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No entries for ${currentFarmer.name} in current week yet.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Week: Monday (${weekInfo.mondayDateFormatted}) to Sunday (${weekInfo.sundayDateFormatted})\nEntries recorded in Milk Entry will appear here automatically date-wise.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Current Week Entries (${weeklyEntries.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(weeklyEntries, key = { it.id }) { record ->
                    WeeklyMilkEntryCard(record = record)
                }

                // ================== SECTION 3: WEEKLY CALCULATION SUMMARY ==================
                item {
                    WeeklyCalculationCard(
                        farmer = currentFarmer,
                        weekInfo = weekInfo,
                        summary = weeklySummary,
                        onSaveToHistory = {
                            viewModel.saveWeeklySummaryToHistory(
                                farmer = currentFarmer,
                                weekInfo = weekInfo,
                                summary = weeklySummary
                            )
                        }
                    )
                }
            }
        } else {
            // ================== TAB 1: WEEKLY HISTORY ==================
            item {
                WeeklyHistoryBanner(farmerName = currentFarmer.name)
            }

            if (weeklyHistory.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No past completed weeks recorded yet for this farmer.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "When Sunday ends and a new Monday begins, or when you tap 'Save into Weekly History', completed weekly totals and averages are permanently archived here.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            } else {
                items(weeklyHistory, key = { "${it.farmerId}_${it.year}_${it.weekNumber}" }) { historyItem ->
                    WeeklyHistoryRecordCard(history = historyItem)
                }
            }
        }
    }
}

@Composable
fun CurrentWeekBanner(weekInfo: WeekInfo) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MilkBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MilkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "CURRENT WEEK (Monday → Sunday)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Week ${weekInfo.weekNumber}, ${weekInfo.year}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MilkGreen.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "${weekInfo.mondayDateFormatted} - ${weekInfo.sundayDateFormatted}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MilkGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyMilkEntryCard(record: MilkRecord) {
    val dayLabels = remember(record.date) { WeekDateUtils.getDayOfWeekLabel(record.date) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Day and Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MilkNavy
                    ) {
                        Text(
                            text = if (dayLabels.first.isNotBlank()) "${dayLabels.first} (${dayLabels.second})" else record.date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = record.date,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = String.format(Locale.US, "Rs %,.0f", record.payment),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MilkGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Volume | FAT | LR | TS Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeeklyMetricColumn(
                    label = "VOLUME / LITRES",
                    value = String.format(Locale.US, "%.2f L", record.liters),
                    color = MilkBlue
                )
                WeeklyMetricColumn(
                    label = "FAT",
                    value = String.format(Locale.US, "%.2f%%", record.fat),
                    color = MilkAmber
                )
                WeeklyMetricColumn(
                    label = "LR",
                    value = String.format(Locale.US, "%.1f", record.lr),
                    color = MilkSky
                )
                WeeklyMetricColumn(
                    label = "TS",
                    value = String.format(Locale.US, "%.2f%%", record.ts),
                    color = MilkPurple
                )
            }
        }
    }
}

@Composable
fun WeeklyMetricColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun WeeklyCalculationCard(
    farmer: Farmer,
    weekInfo: WeekInfo,
    summary: WeeklySummaryCalculations,
    onSaveToHistory: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_calculation_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MilkGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "WEEKLY CALCULATION SUMMARY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "Farmer Code: ${farmer.id} • ${farmer.name}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Total Volume / Litres
            WeeklySummaryRow(
                label = "Weekly Total Volume",
                value = summary.formattedTotalVolume(),
                valueColor = MilkBlue,
                isHighlight = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Average FAT
            WeeklySummaryRow(
                label = "Average FAT",
                value = summary.formattedAverageFat(),
                valueColor = MilkAmber
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Average LR
            WeeklySummaryRow(
                label = "Average LR",
                value = summary.formattedAverageLr(),
                valueColor = MilkSky
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Average TS
            WeeklySummaryRow(
                label = "Average TS",
                value = summary.formattedAverageTs(),
                valueColor = MilkPurple
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Total Payment
            WeeklySummaryRow(
                label = "Total Weekly Payment",
                value = summary.formattedTotalPayment(),
                valueColor = MilkGreen,
                isHighlight = true
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(14.dp))

            // Safe Save button to explicitly archive or confirm history
            Button(
                onClick = onSaveToHistory,
                colors = ButtonDefaults.buttonColors(containerColor = MilkBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("save_weekly_summary_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Summary into Weekly History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun WeeklySummaryRow(
    label: String,
    value: String,
    valueColor: Color,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isHighlight) 14.sp else 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            fontSize = if (isHighlight) 16.sp else 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@Composable
fun WeeklyHistoryBanner(farmerName: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "PERMANENT WEEKLY HISTORY / ہفتہ وار ریکارڈ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MilkNavy,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Permanent weekly archives for $farmerName. Previous weeks are kept safe and never overwritten.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyHistoryRecordCard(history: FarmerWeeklyHistory) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Year and Week Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MilkBlue
                    ) {
                        Text(
                            text = "Year ${history.year}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "Week ${history.weekNumber}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "${history.entryCount} entries",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Monday -> Sunday Date Range
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Monday: ${history.weekStartDate}   •   Sunday: ${history.weekEndDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Total Volume | Average FAT | Average LR | Average TS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeeklyMetricColumn(
                    label = "TOTAL VOLUME",
                    value = history.formattedVolume(),
                    color = MilkBlue
                )
                WeeklyMetricColumn(
                    label = "AVG FAT",
                    value = history.formattedFat(),
                    color = MilkAmber
                )
                WeeklyMetricColumn(
                    label = "AVG LR",
                    value = history.formattedLr(),
                    color = MilkSky
                )
                WeeklyMetricColumn(
                    label = "AVG TS",
                    value = history.formattedTs(),
                    color = MilkPurple
                )
            }

            if (history.totalPayment > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Paid Amount:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = history.formattedPayment(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MilkGreen
                    )
                }
            }
        }
    }
}
