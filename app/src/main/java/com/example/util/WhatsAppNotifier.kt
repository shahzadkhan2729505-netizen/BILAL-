package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.MilkRecord
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppNotifier {

    /**
     * Cleans phone number to international format (e.g. 03001234567 -> 923001234567).
     */
    fun cleanPhoneNumber(rawPhone: String): String {
        var phone = rawPhone.trim().replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
        if (phone.startsWith("+")) {
            phone = phone.substring(1)
        } else if (phone.startsWith("03")) {
            phone = "92" + phone.substring(1)
        } else if (phone.startsWith("0092")) {
            phone = phone.substring(2)
        }
        return phone
    }

    /**
     * Builds standard, professional milk collection WhatsApp message.
     * Includes center name, owner contact, and complete farmer quality & payment details.
     */
    fun buildMilkEntryMessage(
        record: MilkRecord,
        farmerMobile: String = "",
        centerName: String = "Nestlé / Bilal Ahmad Milk Collection",
        ownerWhatsApp: String = ""
    ): String {
        val dateStr = record.date.ifBlank {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }

        val ownerLine = if (ownerWhatsApp.isNotBlank()) "📞 *رابطہ سنٹر (Owner):* $ownerWhatsApp\n" else ""

        return """
🥛 *MILK COLLECTION RECEIPT / دودھ وصولی پرچی* 🥛
━━━━━━━━━━━━━━━━━━━━
🏢 *${centerName.trim()}*
$ownerLine📅 *تاریخ (Date):* $dateStr
👤 *کسان کا نام (Farmer):* ${record.farmerName}
🆔 *کسان کوڈ (Farmer ID):* ${record.farmerId}
━━━━━━━━━━━━━━━━━━━━
🥛 *مقدار (Liters):* ${String.format(Locale.US, "%.2f", record.liters)} L
🧈 *فیٹ (Fat %):* ${String.format(Locale.US, "%.2f", record.fat)} %
🧪 *ایل آر (LR / CLR):* ${String.format(Locale.US, "%.1f", record.lr)}
🔬 *ایس این ایف (SNF %):* ${String.format(Locale.US, "%.2f", record.snf)} %
⚖️ *ٹوٹل سولڈز (TS %):* ${String.format(Locale.US, "%.2f", record.ts)} %
📊 *ٹی ایس مساوی دودھ (TS Equiv):* ${String.format(Locale.US, "%.2f", record.tsMilk)} L
💰 *ریٹ (Rate/L):* Rs. ${String.format(Locale.US, "%.2f", record.rate)}
━━━━━━━━━━━━━━━━━━━━
💵 *کل رقم (TOTAL PAYMENT):* *Rs. ${String.format(Locale.US, "%,.2f", record.payment)}*
━━━━━━━━━━━━━━━━━━━━
${if (record.remarks.isNotBlank()) "📝 *ریمارکس:* ${record.remarks}\n" else ""}شکریہ! دودھ کا ریکارڈ کامیابی سے محفوظ ہو گیا ہے۔
        """.trimIndent()
    }

    /**
     * Sends WhatsApp message directly to the member's registered phone number via WhatsApp Intent.
     * Falls back to general share if WhatsApp is not directly addressable.
     */
    fun sendWhatsAppMessage(
        context: Context,
        rawPhone: String,
        message: String
    ): Boolean {
        val cleanPhone = cleanPhoneNumber(rawPhone)
        val encodedMsg = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }

        return try {
            if (cleanPhone.isNotEmpty() && cleanPhone.length >= 10) {
                // Direct WhatsApp Chat link: https://api.whatsapp.com/send?phone=...
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            } else {
                // If number is missing or invalid, open general WhatsApp share
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(shareIntent)
                    true
                } catch (e: Exception) {
                    // Fallback to system share chooser
                    val chooser = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        },
                        "Share Milk Entry via WhatsApp"
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(chooser)
                    true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp open karne mein masla hua: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
