package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MilkBlue
import com.example.ui.theme.MilkGreen
import com.example.ui.theme.MilkNavy

@Composable
fun OwnerWhatsAppSetupDialog(
    initialNumber: String = "",
    onDismiss: (() -> Unit)? = null,
    onSave: (String) -> Unit
) {
    var numberInput by remember { mutableStateOf(initialNumber) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            onDismiss?.invoke()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF128C7E),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Owner WhatsApp Setup",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Sender & Dairy Owner Identity",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Please enter the Dairy Owner's official WhatsApp number. This is configured only once and saved securely on your device.",
                            fontSize = 12.sp,
                            color = Color(0xFF334155),
                            lineHeight = 16.sp
                        )
                    }
                }

                AppFormField(
                    label = "OWNER WHATSAPP NUMBER *",
                    value = numberInput,
                    onValueChange = {
                        numberInput = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = "e.g. 03001234567 or +923001234567",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    errorMessage = errorMessage,
                    testTag = "owner_whatsapp_input_field"
                )

                Text(
                    text = "🔒 Stored securely. Never mixed with member/farmer numbers.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = numberInput.trim()
                    if (clean.isEmpty()) {
                        errorMessage = "Owner WhatsApp number is required."
                    } else if (clean.length < 10) {
                        errorMessage = "Please enter a valid 10-12 digit phone number."
                    } else {
                        onSave(clean)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MilkGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_owner_whatsapp_btn")
            ) {
                Text("Save Owner Number", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = if (onDismiss != null) {
            {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        } else null
    )
}
