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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.AppGradients
import com.example.ui.theme.MilkAmber
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkSky
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
    val formState by viewModel.entryFormState.collectAsStateWithLifecycle()
    val farmers by viewModel.farmers.collectAsStateWithLifecycle()

    var farmerDropdownExpanded by remember { mutableStateOf(false) }
    var showFormulaInfo by remember { mutableStateOf(false) }

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

                    // Farmer Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (selectedFarmer != null) "${selectedFarmer.id} — ${selectedFarmer.name}" else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("FARMER *") },
                            placeholder = { Text("Select registered farmer") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { farmerDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { farmerDropdownExpanded = true }
                                .testTag("entry_farmer_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        DropdownMenu(
                            expanded = farmerDropdownExpanded,
                            onDismissRequest = { farmerDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (farmers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No farmers registered yet") },
                                    onClick = { farmerDropdownExpanded = false }
                                )
                            } else {
                                farmers.forEach { farmer ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${farmer.id} — ${farmer.name}", fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${farmer.village} • Default Rs ${farmer.defaultRate}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.onEntryFarmerChanged(farmer.id)
                                            farmerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Field
                    OutlinedTextField(
                        value = formState.date,
                        onValueChange = { viewModel.onEntryDateChanged(it) },
                        label = { Text("DATE") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_date_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Milk Quality Presets
                    Text(
                        text = "Quick Quality Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.onEntryFatChanged("6.50")
                                viewModel.onEntryLrChanged("29.0")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🐃 Buffalo (6.5/29)", fontSize = 10.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.onEntryFatChanged("3.80")
                                viewModel.onEntryLrChanged("28.0")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🐄 Cow (3.8/28)", fontSize = 10.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.onEntryFatChanged("7.00")
                                viewModel.onEntryLrChanged("30.0")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⭐ Rich (7.0/30)", fontSize = 10.sp, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Milk Liters & Fat %
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.litersText,
                            onValueChange = { viewModel.onEntryLitersChanged(it) },
                            label = { Text("MILK (LITERS) *") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("entry_liters_field"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = formState.fatText,
                            onValueChange = { viewModel.onEntryFatChanged(it) },
                            label = { Text("FAT (%) *") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("entry_fat_field"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // LR / CLR & Rate (Rs/L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.lrText,
                            onValueChange = { viewModel.onEntryLrChanged(it) },
                            label = { Text("LR / CLR *") },
                            placeholder = { Text("28.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("entry_lr_field"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = formState.rateText,
                            onValueChange = { viewModel.onEntryRateChanged(it) },
                            label = { Text("RATE (RS/L) *") },
                            placeholder = { Text("200.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("entry_rate_field"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remarks
                    OutlinedTextField(
                        value = formState.remarks,
                        onValueChange = { viewModel.onEntryRemarksChanged(it) },
                        label = { Text("REMARKS (OPTIONAL)") },
                        placeholder = { Text("e.g. Morning, Good quality") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_remarks_field"),
                        shape = RoundedCornerShape(10.dp)
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Actions: Save & Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveMilkEntry(onSuccess = onSuccessSaved)
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
