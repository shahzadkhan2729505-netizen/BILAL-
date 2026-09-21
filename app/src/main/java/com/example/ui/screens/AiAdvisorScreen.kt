package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.AuthManager
import com.example.data.model.MilkRecord
import com.example.ui.MainViewModel
import com.example.ui.theme.MilkAmber
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy
import com.example.ui.theme.MilkRed
import com.example.ui.theme.MilkSky
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: String = "Just now"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAdvisorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val authManager = remember { AuthManager.getInstance(context) }
    val userProfile by authManager.userProfile.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val kpis by viewModel.dashboardKpis.collectAsStateWithLifecycle()

    var showProDialog by remember { mutableStateOf(false) }
    var queriesRemaining by remember { mutableIntStateOf(5) }
    var queryText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val initialGreeting = remember {
        ChatMessage(
            isUser = false,
            text = "👋 Welcome to **Bilal Ahmad AI Dairy Intelligence** (Trial Mode)!\n\nI can analyze your live milk collection data, audit Fat & SNF ratios, detect water dilution anomalies, and advise on dairy cattle feeding rations for maximum yield.\n\nTry asking a question below or tap a quick prompt!"
        )
    }

    var messages by remember {
        mutableStateOf(listOf(initialGreeting))
    }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun handleSend(prompt: String) {
        if (prompt.isBlank() || isThinking) return
        val userMsg = ChatMessage(isUser = true, text = prompt)
        messages = messages + userMsg
        queryText = ""
        isThinking = true

        coroutineScope.launch {
            delay(900) // Realistic AI synthesis latency
            val aiResponse = generateDairyAiResponse(prompt, records)
            messages = messages + ChatMessage(isUser = false, text = aiResponse)
            isThinking = false
            if (queriesRemaining > 0) {
                queriesRemaining--
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Header & Trial Status Banner
        Card(
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(MilkNavy, MilkBlue)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MilkSky.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "AI Dairy Intelligence",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (userProfile.isProTrialActive) "14-Day Free Trial Active" else "Standard Free Tier",
                                    color = MilkSky,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Pro Upgrade / Monetization Button
                        Button(
                            onClick = { showProDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MilkAmber,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("ai_upgrade_pro_button")
                        ) {
                            Icon(
                                Icons.Default.Diamond,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PRO TRIAL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Quality Audit Summary Chip Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("AVG HERD FAT", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.US, "%.2f%%", kpis.averageFat),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("AVG LR DENSITY", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.US, "%.1f", kpis.averageLr),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("PURITY STATUS", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                val isNormal = kpis.averageLr >= 27.0 || kpis.entriesCount == 0
                                Text(
                                    text = if (isNormal) "Normal (Pure)" else "Water Dilution Risk!",
                                    color = if (isNormal) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestion Pills
        val quickPrompts = listOf(
            "🥛 Increase Buffalo Fat %",
            "💧 Water Dilution Test (LR)",
            "💰 TS Rate Calculation",
            "⚖️ Cow vs Buffalo SNF",
            "🧪 Audit Live Records"
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPrompts.forEach { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { handleSend(prompt) },
                    label = { Text(prompt, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Chat Message History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(msg.text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MilkSky
                        )
                        Text(
                            "AI is analyzing dairy parameters...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Bottom Input Row
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Ask Dairy AI (Fat, LR, SNF, yield, pricing)...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_query_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MilkSky,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = { handleSend(queryText) },
                    enabled = queryText.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (queryText.isNotBlank()) MilkSky else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (queryText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Monetization Readiness: Pro Subscription & Trial Showcase Dialog
    if (showProDialog) {
        AlertDialog(
            onDismissRequest = { showProDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = MilkAmber)
                    Text("BAMC Dairy Pro AI Suite", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Activate your 14-Day Free Trial or choose a plan to unlock enterprise dairy management tools:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ProFeatureRow("✨ Unlimited Gemini 3.5 Dairy Consultations")
                    ProFeatureRow("🛡️ Automated Water Adulteration & Low-SNF Alerts")
                    ProFeatureRow("📊 Predictive Yields & Payout Forecasting")
                    ProFeatureRow("📱 Automated WhatsApp / SMS Farmer Slips")
                    ProFeatureRow("☁️ Multi-Center Cloud Sync & Unlimited History")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly Pro", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Rs. 1,499 / month", color = MilkGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("Cancel anytime • 14 days free trial included", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MilkNavy.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, MilkSky, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Annual Plan (Save 33%)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Rs. 11,999 / year", color = MilkSky, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("Includes 2 months free + priority WhatsApp support", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authManager.activateProTrial()
                        Toast.makeText(context, "🎉 14-Day Free Pro Trial Activated!", Toast.LENGTH_LONG).show()
                        showProDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MilkGreen)
                ) {
                    Text("Activate 14-Day Free Trial", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }
}

@Composable
fun ProFeatureRow(feature: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MilkGreen, modifier = Modifier.size(16.dp))
        Text(feature, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MilkNavy),
                contentAlignment = Alignment.Center
            ) {
                Text("🥛", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 16.dp
            ),
            color = if (message.isUser) MilkNavy else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = if (message.isUser) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )

                if (!message.isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateDairyAiResponse(prompt: String, records: List<MilkRecord>): String {
    val p = prompt.lowercase()

    return when {
        p.contains("fat") && (p.contains("increase") || p.contains("boost") || p.contains("buffalo")) -> {
            """
            🥛 **Strategies to Increase Buffalo Milk Fat %:**
            
            1. **Dietary Fiber Ratio**: Ensure buffaloes receive minimum 30-35% good quality dry roughage (Wheat straw / Rhodes grass) to stimulate ruminal acetate production, the precursor of milk fat.
            2. **Bypass Fats (Rumen-Protected)**: Supplement 100-150g of calcium salts of fatty acids daily during peak lactation.
            3. **Cottonseed Cake (Khal)**: High protein and oil content directly supports fat synthesis; feed 1.5 - 2.0 kg daily.
            4. **Avoid High Starch Shock**: Do not exceed 40% grain concentrate in one feeding, as it drops rumen pH (<5.8) causing Subacute Ruminal Acidosis (SARA) and acute milk fat depression.
            5. **Clean Water**: Buffaloes require 80-100L of cool fresh water daily to maintain metabolic milk synthesis.
            """.trimIndent()
        }

        p.contains("water") || p.contains("dilution") || p.contains("adulteration") -> {
            """
            💧 **Milk Water Dilution & Purity Detection Guide:**
            
            • **Standard Purity Indices**:
              - Pure Buffalo Milk: LR 29.0 - 32.0, Fat 6.0 - 7.5%, SNF > 8.5%
              - Pure Cow Milk: LR 27.5 - 30.0, Fat 3.5 - 4.5%, SNF > 8.3%
            
            • **Water Dilution Formula**:
              Approx % Added Water = ((Standard SNF - Observed SNF) / Standard SNF) × 100
              *Rule of Thumb: Every 1.0 drop in Lactometer Reading (LR) below 28 at 20°C corresponds to ~3% added water.*
            
            • **Temperature Correction**:
              True LR = Reading + 0.2 × (Temperature °C - 20°C). Always record LR at or corrected to standard 20°C (68°F).
            """.trimIndent()
        }

        p.contains("ts") || p.contains("rate") || p.contains("pricing") || p.contains("price") -> {
            """
            💰 **TS (Total Solids) Pricing & Milk Valuation Engine:**
            
            • **Current Standard TS Reference**: 13.0%
            • **TS Calculation**: TS% = Fat% + SNF%
            • **TS Milk Equivalent**: TS Milk (L) = Liters × (TS% / 13.0)
            • **Payment Formula**: Payment = TS Milk (L) × Rate per TS Liter
            
            *Example calculation:*
            A farmer bringing 40 Liters with 6.50% Fat and 29.0 LR:
            - SNF% = (29.0 / 4) + (0.25 × 6.50) + 0.35 = 7.25 + 1.625 + 0.35 = 9.225%
            - TS% = 6.50 + 9.225 = 15.725%
            - TS Milk = 40 × (15.725 / 13.0) = 48.38 Liters
            - Payment at Rs. 180 = Rs. 8,708.40
            """.trimIndent()
        }

        p.contains("cow") && p.contains("buffalo") || p.contains("difference") -> {
            """
            ⚖️ **Cow vs. Buffalo Milk Benchmark Comparison:**
            
            | Parameter | Buffalo Milk | Cow Milk |
            | :--- | :--- | :--- |
            | **Fat Range** | 6.0% - 8.5% | 3.5% - 4.8% |
            | **SNF Range** | 8.8% - 9.8% | 8.2% - 8.8% |
            | **Total Solids** | 15.0% - 18.0% | 12.0% - 13.5% |
            | **Lactometer (LR)**| 29.0 - 32.0 | 27.5 - 30.0 |
            | **Milk Density** | 1.030 - 1.032 | 1.028 - 1.030 |
            | **TS Yield Premium**| Higher Payout per Liter | Higher volume, lower TS |
            """.trimIndent()
        }

        p.contains("audit") || p.contains("records") || p.contains("live") || p.contains("status") -> {
            if (records.isEmpty()) {
                "📊 **Live Collection Audit**: No records logged yet. Add your morning or evening milk entries to generate real-time AI quality and yield insights."
            } else {
                val totalMilk = records.sumOf { it.liters }
                val avgFat = records.map { it.fat }.average()
                val avgLr = records.map { it.lr }.average()
                val avgSnf = records.map { it.snf }.average()
                val totalPay = records.sumOf { it.payment }
                val suspectWater = records.filter { it.lr < 26.0 || it.snf < 7.8 }

                """
                📊 **Live Collection Health Audit (${records.size} Records):**
                
                • **Total Volume**: ${String.format(Locale.US, "%.1f Liters", totalMilk)}
                • **Average Fat**: ${String.format(Locale.US, "%.2f%%", avgFat)} (Standard benchmark: 6.20%)
                • **Average LR**: ${String.format(Locale.US, "%.1f", avgLr)}
                • **Average SNF**: ${String.format(Locale.US, "%.2f%%", avgSnf)}
                • **Total Center Payout**: Rs ${String.format(Locale.US, "%,.2f", totalPay)}
                
                ${if (suspectWater.isNotEmpty()) "⚠️ **Adulteration Alert**: ${suspectWater.size} entries detected with LR < 26.0 or SNF < 7.8%. Sample farmers: ${suspectWater.take(2).joinToString { it.farmerName }}." else "✅ **Quality Grade**: All batches conform to high purity standards!"}
                """.trimIndent()
            }
        }

        else -> {
            """
            💡 **Dairy Intelligence Insight on "${prompt}":**
            
            In commercial milk collection centers, optimal profitability requires monitoring the correlation between **Fat %** and **Lactometer Reading (LR)**.
            
            • **Key Metric Checklist**:
              1. Standard Fat for Buffalo: 6.0% - 7.5%
              2. Standard LR at 20°C: 28.0 - 32.0
              3. Target SNF%: minimum 8.5%
              4. Payment Reference TS: 13.0%
            
            *Tip: Tap any prompt pill above or ask about specific farmer trends, feed recipes, or calculation formulas.*
            """.trimIndent()
        }
    }
}
