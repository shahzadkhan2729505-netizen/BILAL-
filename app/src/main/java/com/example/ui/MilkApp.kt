package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.auth.AuthManager
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.AiSheetScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FarmersScreen
import com.example.ui.screens.MilkEntryScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.AppGradients
import com.example.ui.theme.MilkAmber
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkSky
import com.example.util.PdfGenerator

enum class AppScreen(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    MilkEntry("Milk Entry", Icons.Default.AddCircle),
    Farmers("Farmers", Icons.Default.People),
    Records("Records", Icons.Default.ListAlt),
    Reports("Reports", Icons.Default.Assessment),
    Calculator("Calculator", Icons.Default.Calculate),
    AiAdvisor("AI Advisor", Icons.Default.AutoAwesome),
    AiSheet("Log Sheet", Icons.Default.Description)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val authManager = remember { AuthManager.getInstance(context) }
    val userProfile by authManager.userProfile.collectAsStateWithLifecycle()
    var showAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // In-app Back Navigation: If not on Dashboard, Back returns to Dashboard
    BackHandler(enabled = currentScreen != AppScreen.Dashboard) {
        currentScreen = AppScreen.Dashboard
    }

    fun exportCsvAction() {
        // Direct Download to Phone Internal Storage / Downloads
        val uri = PdfGenerator.downloadMilkRecordsCsv(context, records)
        if (uri != null) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, "Bilal_Ahmad_Milk_Report.csv")
                putExtra(Intent.EXTRA_SUBJECT, "Bilal Ahmad Milk Report")
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "text/csv"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            try {
                context.startActivity(Intent.createChooser(sendIntent, "Share or Open Milk CSV"))
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppGradients.TopBar)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_dairy_logo),
                                    contentDescription = "Bilal Ahmad Milk Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Bilal Ahmad Milk Collection",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White
                                )
                                Text(
                                    text = currentScreen.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.90f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (currentScreen != AppScreen.Dashboard) {
                            IconButton(onClick = { currentScreen = AppScreen.Dashboard }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Dashboard",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    actions = {
                        // Google User Account Profile Chip
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { showAccountDialog = true }
                                .testTag("top_bar_account_chip")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (userProfile.isLoggedIn) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userProfile.avatarInitial,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (userProfile.isLoggedIn) Color(0xFF003366) else Color.Black
                                    )
                                }
                                Text(
                                    text = if (userProfile.isLoggedIn) "Google" else "Sign In",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                ScrollableTabRow(
                    selectedTabIndex = currentScreen.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MilkBlue,
                    edgePadding = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_navigation_tabs")
                ) {
                    AppScreen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        Tab(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            text = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected) MilkBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            selectedContentColor = MilkBlue,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToEntry = { currentScreen = AppScreen.MilkEntry },
                    onNavigateToFarmers = { currentScreen = AppScreen.Farmers },
                    onNavigateToRecords = { currentScreen = AppScreen.Records },
                    onNavigateToAiSheet = { currentScreen = AppScreen.AiSheet },
                    onNavigateToCalculator = { currentScreen = AppScreen.Calculator },
                    onNavigateToAiAdvisor = { currentScreen = AppScreen.AiAdvisor },
                    onExportCsv = { exportCsvAction() }
                )

                AppScreen.MilkEntry -> MilkEntryScreen(
                    viewModel = viewModel,
                    onSuccessSaved = {
                        currentScreen = AppScreen.Records
                    },
                    onCancel = {
                        currentScreen = AppScreen.Dashboard
                    }
                )

                AppScreen.Farmers -> FarmersScreen(
                    viewModel = viewModel
                )

                AppScreen.Records -> RecordsScreen(
                    viewModel = viewModel,
                    onNavigateToEditRecord = { record ->
                        viewModel.startEditRecord(record) {
                            currentScreen = AppScreen.MilkEntry
                        }
                    },
                    onExportCsv = { exportCsvAction() }
                )

                AppScreen.Reports -> ReportsScreen(
                    viewModel = viewModel
                )

                AppScreen.Calculator -> CalculatorScreen(
                    viewModel = viewModel
                )

                AppScreen.AiAdvisor -> AiAdvisorScreen(
                    viewModel = viewModel
                )

                AppScreen.AiSheet -> AiSheetScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // Google Sign-In & User Account Dialog
    if (showAccountDialog) {
        var customEmailInput by remember { mutableStateOf(userProfile.email) }
        var customNameInput by remember { mutableStateOf(userProfile.name) }

        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MilkSky.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MilkNavy)
                    }
                    Text("Google Authentication", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (userProfile.isLoggedIn) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MilkNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userProfile.avatarInitial,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(userProfile.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(userProfile.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MilkGreen, modifier = Modifier.size(13.dp))
                                        Text("Google Account Verified", fontSize = 10.sp, color = MilkGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MilkBlue.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("ROLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MilkBlue)
                                    Text(userProfile.role, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MilkGreen.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("SYNC STATUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MilkGreen)
                                    Text("Cloud & Local Synced", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        HorizontalDivider()

                        Text("Switch Google / Gmail Account:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        OutlinedTextField(
                            value = customEmailInput,
                            onValueChange = { customEmailInput = it },
                            label = { Text("Gmail Address", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = customNameInput,
                            onValueChange = { customNameInput = it },
                            label = { Text("Full Name", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (customEmailInput.isNotBlank()) {
                                    authManager.signInWithGoogle(customEmailInput, customNameInput.ifBlank { "Dairy Admin" })
                                    Toast.makeText(context, "Signed in as $customEmailInput", Toast.LENGTH_SHORT).show()
                                    showAccountDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update / Switch Gmail Account")
                        }

                        OutlinedButton(
                            onClick = {
                                authManager.signOut()
                                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                                showAccountDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign Out")
                        }
                    } else {
                        Text(
                            "Sign in with your Google account to enable cloud synchronization, automated backups, and AI features.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = customEmailInput,
                            onValueChange = { customEmailInput = it },
                            label = { Text("Enter Gmail Address", fontSize = 12.sp) },
                            placeholder = { Text("e.g. bilal.dairy@gmail.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = customNameInput,
                            onValueChange = { customNameInput = it },
                            label = { Text("Operator Name", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Bilal Ahmad") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                val email = if (customEmailInput.isNotBlank()) customEmailInput else "bilal.dairy@gmail.com"
                                val name = if (customNameInput.isNotBlank()) customNameInput else "Bilal Ahmad"
                                authManager.signInWithGoogle(email, name)
                                Toast.makeText(context, "Signed in with Google ($email)", Toast.LENGTH_SHORT).show()
                                showAccountDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MilkNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue with Google (Gmail)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
