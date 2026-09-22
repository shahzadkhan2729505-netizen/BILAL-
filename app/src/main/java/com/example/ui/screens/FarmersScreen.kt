package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.MainViewModel
import com.example.ui.components.AppFormField
import com.example.ui.components.OwnerWhatsAppSetupDialog
import com.example.ui.theme.AppGradients
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkRed
import com.example.ui.theme.MilkSky
import java.util.Locale

@Composable
fun FarmersScreen(
    viewModel: MainViewModel,
    onNavigateToWeekly: ((Farmer) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val userProfile by authManager.userProfile.collectAsStateWithLifecycle()
    val farmers by viewModel.farmers.collectAsStateWithLifecycle()

    var showOwnerSetupDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile.ownerWhatsApp) {
        if (userProfile.ownerWhatsApp.isBlank()) {
            showOwnerSetupDialog = true
        }
    }

    var idInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }
    var villageInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("200.00") }
    var isEditing by remember { mutableStateOf(false) }

    var idError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }

    var farmerFilterQuery by remember { mutableStateOf("") }
    var farmerToDelete by remember { mutableStateOf<Farmer?>(null) }

    fun clearForm() {
        idInput = ""
        nameInput = ""
        mobileInput = ""
        villageInput = ""
        rateInput = "200.00"
        isEditing = false
        idError = null
        nameError = null
        mobileError = null
    }

    val filteredFarmers = remember(farmers, farmerFilterQuery) {
        val q = farmerFilterQuery.trim().lowercase()
        if (q.isEmpty()) farmers
        else farmers.filter { it.name.lowercase().contains(q) || it.id.lowercase().contains(q) || it.village.lowercase().contains(q) }
    }

    if (showOwnerSetupDialog) {
        OwnerWhatsAppSetupDialog(
            initialNumber = userProfile.ownerWhatsApp,
            onDismiss = if (userProfile.ownerWhatsApp.isNotBlank()) { { showOwnerSetupDialog = false } } else null,
            onSave = { savedNumber ->
                authManager.saveOwnerWhatsApp(savedNumber)
                showOwnerSetupDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (farmerToDelete != null) {
        AlertDialog(
            onDismissRequest = { farmerToDelete = null },
            title = { Text("Delete Farmer?") },
            text = { Text("Are you sure you want to delete farmer ${farmerToDelete?.name} (${farmerToDelete?.id})? If they have milk records, those must be deleted first.") },
            confirmButton = {
                Button(
                    onClick = {
                        farmerToDelete?.let { viewModel.deleteFarmer(it.id) }
                        farmerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MilkRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { farmerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("farmers_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Owner WhatsApp Status Banner Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF128C7E),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DAIRY OWNER WHATSAPP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (userProfile.ownerWhatsApp.isNotBlank()) userProfile.ownerWhatsApp else "Not Configured Yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (userProfile.ownerWhatsApp.isNotBlank()) Color(0xFF0F172A) else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showOwnerSetupDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userProfile.ownerWhatsApp.isNotBlank()) "Change" else "Setup",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Farmer Registration Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isEditing) "Edit Farmer" else "Register New Farmer",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppFormField(
                            label = "FARMER CODE *",
                            value = idInput,
                            onValueChange = {
                                idInput = it
                                if (idError != null) idError = null
                            },
                            placeholder = "e.g. F-001",
                            enabled = !isEditing,
                            errorMessage = idError,
                            modifier = Modifier.weight(1f),
                            testTag = "farmer_id_input"
                        )

                        AppFormField(
                            label = "DEFAULT RATE (RS/L)",
                            value = rateInput,
                            onValueChange = { rateInput = it },
                            placeholder = "200.00",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            testTag = "farmer_rate_input"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AppFormField(
                        label = "FARMER NAME *",
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            if (nameError != null) nameError = null
                        },
                        placeholder = "Enter full name (e.g. Muhammad Ali)",
                        errorMessage = nameError,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "farmer_name_input"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppFormField(
                            label = "FARMER WHATSAPP *",
                            value = mobileInput,
                            onValueChange = {
                                mobileInput = it
                                if (mobileError != null) mobileError = null
                            },
                            placeholder = "03001234567",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            errorMessage = mobileError,
                            modifier = Modifier.weight(1f),
                            testTag = "farmer_mobile_input"
                        )

                        AppFormField(
                            label = "VILLAGE / AREA",
                            value = villageInput,
                            onValueChange = { villageInput = it },
                            placeholder = "e.g. Chak 42",
                            modifier = Modifier.weight(1f),
                            testTag = "farmer_village_input"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                var hasError = false
                                if (idInput.trim().isEmpty()) {
                                    idError = "Farmer ID is required."
                                    hasError = true
                                }
                                if (nameInput.trim().isEmpty()) {
                                    nameError = "Farmer Name is required."
                                    hasError = true
                                }
                                if (mobileInput.trim().isEmpty()) {
                                    mobileError = "Farmer WhatsApp number is required."
                                    hasError = true
                                }
                                if (hasError) return@Button

                                val rate = rateInput.toDoubleOrNull() ?: 200.0
                                viewModel.saveFarmer(idInput, nameInput, mobileInput, villageInput, rate) {
                                    clearForm()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MilkGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("save_farmer_button")
                        ) {
                            Text(
                                text = if (isEditing) "Update Farmer" else "Save Farmer",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { clearForm() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(50.dp)
                                .testTag("clear_farmer_form_button")
                        ) {
                            Text("Clear", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Farmers Directory Header & Search
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registered Farmers (${farmers.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            AppFormField(
                label = "SEARCH FARMERS",
                value = farmerFilterQuery,
                onValueChange = { farmerFilterQuery = it },
                placeholder = "Search by name, ID or village...",
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "farmer_search_field"
            )
        }

        // Farmers List
        if (filteredFarmers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (farmers.isEmpty()) "No farmers registered yet." else "No farmers matching search.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredFarmers, key = { it.id }) { farmer ->
                FarmerCard(
                    farmer = farmer,
                    onEdit = {
                        idInput = farmer.id
                        nameInput = farmer.name
                        mobileInput = farmer.mobile
                        villageInput = farmer.village
                        rateInput = String.format(Locale.US, "%.2f", farmer.defaultRate)
                        isEditing = true
                    },
                    onDelete = {
                        farmerToDelete = farmer
                    },
                    onWeeklyClick = onNavigateToWeekly?.let { { it(farmer) } }
                )
            }
        }
    }
}

@Composable
fun FarmerCard(
    farmer: Farmer,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWeeklyClick: (() -> Unit)? = null
) {
    val avatarGradients = listOf(
        AppGradients.BlueCobalt,
        AppGradients.EmeraldVibrant,
        AppGradients.PurpleAi,
        AppGradients.AmberGold,
        AppGradients.SunsetCoral,
        AppGradients.OceanTeal
    )
    val chosenGradient = avatarGradients[Math.abs(farmer.id.hashCode()) % avatarGradients.size]

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("farmer_card_${farmer.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(chosenGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = farmer.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MilkBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = farmer.id,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (farmer.mobile.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MilkBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(farmer.mobile, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (farmer.village.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp), tint = MilkSky)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(farmer.village, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        text = "Default Rate: Rs ${String.format(Locale.US, "%.2f", farmer.defaultRate)} / L",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MilkGreen,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onWeeklyClick != null) {
                    IconButton(
                        onClick = onWeeklyClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MilkBlue.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Weekly Entry & History",
                            tint = MilkBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Farmer", tint = MilkBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Farmer", tint = MilkRed)
                }
            }
        }
    }
}
