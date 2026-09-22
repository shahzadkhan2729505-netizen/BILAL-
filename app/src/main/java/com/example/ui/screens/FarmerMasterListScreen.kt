package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Farmer
import com.example.ui.MainViewModel
import com.example.ui.components.AppFormField
import com.example.ui.theme.AppGradients
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkSky

@Composable
fun FarmerMasterListScreen(
    viewModel: MainViewModel,
    onSelectFarmer: (Farmer) -> Unit,
    modifier: Modifier = Modifier
) {
    val farmers by viewModel.farmers.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredFarmers = remember(farmers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) farmers
        else farmers.filter {
            it.name.lowercase().contains(q) ||
            it.id.lowercase().contains(q) ||
            it.village.lowercase().contains(q)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("farmer_master_list_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner Card
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
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "FARMER MASTER DIRECTORY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Weekly Entry & History",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "کسان پر کلک کر کے موجودہ ہفتہ (پیر تا اتوار) کی انٹریز، ہفتہ وار ٹوٹل، اوسط فیٹ، LR اور TS دیکھیں۔ سابقہ ہفتے خودکار تاریخچہ (Weekly History) میں محفوظ رہیں گے۔",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            AppFormField(
                label = "SEARCH REGISTERED FARMERS (${farmers.size})",
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search by Farmer Code, Name, or Village...",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "weekly_farmer_search_field"
            )
        }

        // Farmers List
        if (filteredFarmers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (farmers.isEmpty()) "No farmers registered yet." else "No farmer matches \"$searchQuery\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add farmers from the 'Farmers' tab to track their weekly milk.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredFarmers, key = { it.id }) { farmer ->
                FarmerWeeklySelectorCard(
                    farmer = farmer,
                    onClick = { onSelectFarmer(farmer) }
                )
            }
        }
    }
}

@Composable
fun FarmerWeeklySelectorCard(
    farmer: Farmer,
    onClick: () -> Unit
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
            .clickable(onClick = onClick)
            .testTag("weekly_farmer_card_${farmer.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(chosenGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = farmer.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MilkBlue
                        ) {
                            Text(
                                text = farmer.id,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (farmer.village.isNotBlank()) {
                        Text(
                            text = "Village: ${farmer.village}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Text(
                        text = "Tap to view Monday-Sunday Weekly Entries",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MilkGreen,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MilkBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Weekly Details",
                    tint = MilkBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
