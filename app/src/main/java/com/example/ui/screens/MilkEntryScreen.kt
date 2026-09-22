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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.AuthManager
import com.example.data.model.Farmer
import com.example.data.model.MilkRecord
import com.example.ui.MainViewModel
import com.example.ui.components.AppFormField
import com.example.ui.theme.AppGradients
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkSky
import com.example.util.WhatsAppNotifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MilkEntryScreen(
    viewModel: MainViewModel,
    onSuccessSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val userProfile by authManager.userProfile.collectAsStateWithLifecycle()
    val formState by viewModel.entryFormState.collectAsStateWithLifecycle()
    val farmers by viewModel.farmers.collectAsStateWithLifecycle()

    var farmerDropdownExpanded by remember { mutableStateOf(false) }
    var showFormulaInfo by remember { mutableStateOf(false) }

    var farmerError by remember { mutableStateOf<String?>(null) }
    var litersError by remember { mutableStateOf<String?>(null) }
    var fatError by remember { mutableStateOf<String?>(null) }
    var lrError by remember { mutableStateOf<String?>(null) }
    var rateError by remember { mutableStateOf<String?>(null) }

    // Date Picker Dialog setup
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCal.time)
                viewModel.onEntryDateChanged(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val selectedFarmer = farmers.find { it.id == formState.selectedFarmerId }
    var autoSendWhatsApp by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("milk_entry_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Form Card
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
                            text = if (formState.editingRecordId != null) "Edit Milk Collection" else "New Milk Collection",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showFormulaInfo = !showFormulaInfo }) {
                            Icon(Icons.Default.Info, contentDescription = "Formulas", tint = MilkSky)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Farmer Selection Field (Structured AppFormField with Dropdown)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AppFormField(
                            label = "SELECT REGISTERED FARMER *",
                            value = if (selectedFarmer != null) "${selectedFarmer.id} — ${selectedFarmer.name} (${selectedFarmer.village})" else "",
                            onValueChange = {},
                            placeholder = "Tap to choose farmer...",
                            readOnly = true,
                            onClick = {
                                farmerDropdownExpanded = true
                                farmerError = null
                            },
                            errorMessage = farmerError,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Open Farmer List",
                                    tint = MilkBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "entry_farmer_dropdown"
                        )

                        DropdownMenu(
                            expanded = farmerDropdownExpanded,
                            onDismissRequest = { farmerDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (farmers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No farmers registered yet. Please register farmer first.") },
                                    onClick = { farmerDropdownExpanded = false }
                                )
                            } else {
                                farmers.forEach { farmer ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(MilkBlue.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MilkBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text("${farmer.id} — ${farmer.name}", fontWeight = FontWeight.Bold)
                                                    Text(
                                                        "${farmer.village} • WhatsApp: ${farmer.mobile} • Default Rs ${farmer.defaultRate}",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.onEntryFarmerChanged(farmer.id)
                                            farmerDropdownExpanded = false
                                            farmerError = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Field
                    AppFormField(
                        label = "COLLECTION DATE",
                        value = formState.date,
                        onValueChange = { viewModel.onEntryDateChanged(it) },
                        placeholder = "YYYY-MM-DD",
                        readOnly = true,
                        onClick = { datePickerDialog.show() },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = MilkBlue)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "entry_date_field"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Milk Quality Presets
                    Text(
                        text = "QUICK QUALITY PRESETS:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.onEntryFatChanged("6.50")
                                viewModel.onEntryLrChanged("29.0")
                                fatError = null
                                lrError = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🐃 Buffalo (6.5/29)", fontSize = 10.5.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.onEntryFatChanged("3.80")
                                viewModel.onEntryLrChanged("28.0")
                                fatError = null
                                lrError = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🐄 Cow (3.8/28)", fontSize = 10.5.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.onEntryFatChanged("7.00")
                                viewModel.onEntryLrChanged("30.0")
                                fatError = null
                                lrError = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⭐ Rich (7.0/30)", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Milk Liters & Fat %
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppFormField(
                            label = "MILK QUANTITY (L) *",
                            value = formState.litersText,
                            onValueChange = {
                                viewModel.onEntryLitersChanged(it)
                                if (litersError != null) litersError = null
                            },
                            placeholder = "0.00",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            errorMessage = litersError,
                            modifier = Modifier.weight(1f),
                            testTag = "entry_liters_field"
                        )

                        AppFormField(
                            label = "FAT PERCENTAGE (%) *",
                            value = formState.fatText,
                            onValueChange = {
                                viewModel.onEntryFatChanged(it)
                                if (fatError != null) fatError = null
                            },
                            placeholder = "0.00",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            errorMessage = fatError,
                            modifier = Modifier.weight(1f),
                            testTag = "entry_fat_field"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // LR / CLR & Rate (Rs/L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppFormField(
                            label = "LACTOMETER (LR) *",
                            value = formState.lrText,
                            onValueChange = {
                                viewModel.onEntryLrChanged(it)
                                if (lrError != null) lrError = null
                            },
                            placeholder = "28.0",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            errorMessage = lrError,
                            modifier = Modifier.weight(1f),
                            testTag = "entry_lr_field"
                        )

                        AppFormField(
                            label = "RATE PER LITER (RS) *",
                            value = formState.rateText,
                            onValueChange = {
                                viewModel.onEntryRateChanged(it)
                                if (rateError != null) rateError = null
                            },
                            placeholder = "200.00",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            errorMessage = rateError,
                            modifier = Modifier.weight(1f),
                            testTag = "entry_rate_field"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remarks
                    AppFormField(
                        label = "REMARKS / NOTES",
                        value = formState.remarks,
                        onValueChange = { viewModel.onEntryRemarksChanged(it) },
                        placeholder = "e.g. Morning collection, Good quality",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "entry_remarks_field"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total Payment Banner Card with Vibrant Gradient
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(brush = AppGradients.EmeraldVibrant)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TOTAL PAYMENT",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White.copy(alpha = 0.92f),
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "(TS Milk Equiv L × Rate)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.80f)
                                    )
                                }
                                Text(
                                    text = formState.calculations.formattedPayment(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // WhatsApp Auto-Notification Card (Shows clear distinction between Owner Sender & Farmer Recipient)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4), // Emerald 50
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22C55E).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "WhatsApp",
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "WhatsApp Slip Notification",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF14532D)
                                        )
                                        Text(
                                            text = "Instant receipt with Liters, Fat, LR, TS & Payment",
                                            fontSize = 11.sp,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                                Checkbox(
                                    checked = autoSendWhatsApp,
                                    onCheckedChange = { autoSendWhatsApp = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF16A34A),
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("entry_whatsapp_auto_checkbox")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sender (Owner) details
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "FROM (Dairy Owner): ",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = if (userProfile.ownerWhatsApp.isNotBlank()) userProfile.ownerWhatsApp else "Not set",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            // Recipient (Farmer) details
                            val farmerMobile = selectedFarmer?.mobile.orEmpty().trim()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                            ) {
                                Text(
                                    text = "TO (Farmer WhatsApp): ",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = if (farmerMobile.isNotEmpty()) farmerMobile else if (selectedFarmer != null) "Missing number" else "Select farmer first",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (farmerMobile.isNotEmpty()) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Actions: Save & Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                var hasError = false
                                if (formState.selectedFarmerId.isEmpty() || selectedFarmer == null) {
                                    farmerError = "Farmer selection is required."
                                    hasError = true
                                }
                                val litersVal = formState.litersText.toDoubleOrNull()
                                if (litersVal == null || litersVal <= 0.0) {
                                    litersError = "Enter valid milk liters (> 0)."
                                    hasError = true
                                }
                                val fatVal = formState.fatText.toDoubleOrNull()
                                if (fatVal == null || fatVal <= 0.0) {
                                    fatError = "Enter valid fat %."
                                    hasError = true
                                }
                                val lrVal = formState.lrText.toDoubleOrNull()
                                if (lrVal == null || lrVal <= 0.0) {
                                    lrError = "Enter valid LR."
                                    hasError = true
                                }
                                val rateVal = formState.rateText.toDoubleOrNull()
                                if (rateVal == null || rateVal <= 0.0) {
                                    rateError = "Enter valid rate."
                                    hasError = true
                                }

                                if (hasError) return@Button

                                viewModel.saveMilkEntry(onSuccess = { savedRecord, farmer ->
                                    if (autoSendWhatsApp) {
                                        val message = WhatsAppNotifier.buildMilkEntryMessage(
                                            record = savedRecord,
                                            farmerMobile = farmer.mobile,
                                            ownerWhatsApp = userProfile.ownerWhatsApp
                                        )
                                        WhatsAppNotifier.sendWhatsAppMessage(
                                            context = context,
                                            rawPhone = farmer.mobile,
                                            message = message
                                        )
                                    }
                                    onSuccessSaved()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MilkGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("save_milk_entry_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (formState.editingRecordId != null) "Update Entry" else "Save Entry",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.cancelEntryForm()
                                onCancel()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(50.dp)
                                .testTag("cancel_milk_entry_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Live Results Card Grid
        item {
            Text(
                text = "Live Quality & Weight Results",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultBadge(title = "SNF", value = formState.calculations.formattedSnf(), modifier = Modifier.weight(1f))
                ResultBadge(title = "TS", value = formState.calculations.formattedTs(), modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultBadge(title = "SPECIFIC GRAVITY", value = formState.calculations.formattedSpGravity(), modifier = Modifier.weight(1f))
                ResultBadge(title = "MILK KG", value = formState.calculations.formattedMilkKg(), modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultBadge(title = "FAT KG", value = formState.calculations.formattedFatKg(), modifier = Modifier.weight(1f))
                ResultBadge(title = "SNF KG", value = formState.calculations.formattedSnfKg(), modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultBadge(title = "TS KG", value = formState.calculations.formattedTsKg(), highlight = true, modifier = Modifier.weight(1f))
                ResultBadge(title = "TS MILK EQUIV", value = formState.calculations.formattedTsMilk(), highlight = true, modifier = Modifier.weight(1f))
            }
        }

        // Calculation Formula Accordion
        item {
            AnimatedVisibility(visible = showFormulaInfo) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MilkSky.copy(alpha = 0.08f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MilkSky.copy(alpha = 0.3f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Standard Dairy Calculation Formulas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MilkBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• SNF = (0.25 × LR) + (0.22 × Fat) + 0.72\n" +
                                "• TS = Fat + SNF\n" +
                                "• Specific Gravity = 1 + (LR ÷ 1000)\n" +
                                "• Milk KG = Milk (L) × Specific Gravity\n" +
                                "• Fat KG = Milk KG × Fat ÷ 100\n" +
                                "• SNF KG = Milk KG × SNF ÷ 100\n" +
                                "• TS KG = Fat KG + SNF KG\n" +
                                "• TS Milk Equiv (L) = Actual Milk × TS ÷ 13.027\n" +
                                "• Payment = TS Milk Equiv (L) × Rate\n\n" +
                                "Reference: 1 L + LR 28 = Sp. Gravity 1.028 → 1.028 KG",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultBadge(
    title: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (highlight) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = AppGradients.BlueCobalt)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.88f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MilkGreen,
                    letterSpacing = 0.4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
