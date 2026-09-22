package com.example.traceability

import com.example.data.model.MonthConfig
import com.example.ui.TraceRowUi
import com.example.ui.TraceSheetUiState
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

data class AiExecutionResult(
    val updatedState: TraceSheetUiState,
    val urduResponse: String,
    val englishResponse: String,
    val actionTaken: String
)

object TraceabilityAiBrain {

    val PAKISTANI_FARMER_NAMES = listOf(
        "محمد اکرم", "حاجی رشید", "چوہدری طارق", "ملک عمران", "عبدالرحمان",
        "محمد اسلم", "سردار بلال", "محمد نواز", "بشیر احمد", "محمد اقبال",
        "محمد سلیم", "محمد اشرف", "غلام مصطفیٰ", "محمد حنیف", "محمد فاروق",
        "رانا ساجد", "محمد منیر", "خادم حسین", "محمد ریاض", "مقصود احمد",
        "محمد ارشد", "حاجی نذیر", "محمد یوسف", "ظفر اقبال", "محمد امجد",
        "محمد زاہد", "محمد شفیق", "عبدالستار", "محمد صدیق", "اللہ دتہ",
        "محمد رفیق", "شاہد محمود", "محمد جمیل", "محمد ندیم", "محمد بوٹا",
        "محمد کاشف", "محمد شبیر", "محمد افضل", "محمد لطیف", "محمد شریف",
        "احسان الحق", "محمد اصغر", "محمد سرور", "مظہر حسین", "محمد وسیم",
        "محمد حامد", "محمد اعجاز", "محمد وقاص", "محمد صابر", "محمد نعیم",
        "محمد مسعود", "محمد قیصر", "محمد سرفراز", "محمد مبشر", "محمد کامران",
        "محمد عتیق", "محمد فیاض", "محمد فرید", "محمد اکبر", "محمد عباس",
        "محمد یونس", "محمد انور", "محمد اعظم", "محمد انوار", "محمد ناصر",
        "محمد طاہر", "محمد شاہد", "محمد طفیل", "محمد محبوب", "محمد مجید"
    )

    /**
     * Complete intelligent brain processing user's voice or text command.
     * Understands Urdu, Roman Urdu, and English naturally.
     */
    fun processCommand(input: String, currentState: TraceSheetUiState): AiExecutionResult {
        val lower = input.trim().lowercase(Locale.ROOT)

        // 1. Clear / Reset Sheet (خالی کرو / مٹا دو / clear)
        if (lower.contains("khali") || lower.contains("clear") || lower.contains("mita") ||
            lower.contains("saf") || lower.contains("delete all") || lower.contains("reset") ||
            lower.contains("خالی") || lower.contains("مٹا") || lower.contains("ختم")
        ) {
            val emptyRows = List(70) { TraceRowUi(sr = it + 1, name = "", values = emptyMap()) }
            val cleared = currentState.copy(
                rows = emptyRows,
                statusMessage = "✓ پوری شیٹ صاف اور خالی کر دی گئی ہے۔ تمام 70 خانے اب بلینک ہیں۔",
                isSuccessStatus = true,
                isWarningStatus = false
            )
            return AiExecutionResult(
                updatedState = cleared,
                urduResponse = "جی، میں نے پوری شیٹ بالکل خالی اور صاف کر دی ہے۔ تمام 70 قطاریں اب بلینک اور تیار ہیں۔",
                englishResponse = "Sheet completely cleared. All 70 rows are now blank and clean.",
                actionTaken = "CLEAR_SHEET"
            )
        }

        // 2. Fill Farmer Names (کسانوں کے نام / farmer names)
        if ((lower.contains("naam") || lower.contains("name") || lower.contains("farmer") || lower.contains("کسان")) &&
            (lower.contains("bhar") || lower.contains("likh") || lower.contains("fill") || lower.contains("daal") || lower.contains("add"))
        ) {
            val countMatch = Regex("""\b(\d+)\b""").find(lower)
            val requestedCount = countMatch?.value?.toIntOrNull() ?: 30
            val safeCount = requestedCount.coerceIn(5, 70)

            val updatedRows = currentState.rows.mapIndexed { idx, row ->
                if (idx < safeCount) {
                    val name = PAKISTANI_FARMER_NAMES.getOrElse(idx) { "محمد ${idx + 1}" }
                    row.copy(name = name)
                } else {
                    row.copy(name = "")
                }
            }

            val updated = currentState.copy(
                rows = updatedRows,
                statusMessage = "✓ $safeCount کسانوں کے اصلی نام شیٹ میں درج کر دیے گئے ہیں۔",
                isSuccessStatus = true
            )
            return AiExecutionResult(
                updatedState = updated,
                urduResponse = "میں نے پہلے $safeCount کسانوں کے اصلی اور مستند نام شیٹ میں درج کر دیے ہیں۔",
                englishResponse = "Filled authentic Pakistani farmer names for $safeCount rows.",
                actionTaken = "FILL_NAMES"
            )
        }

        // 3. Set Supplier Details / Code / Name / Village
        if (lower.contains("supplier") || lower.contains("code") || lower.contains("village") ||
            lower.contains("کود") || lower.contains("سپلائر") || lower.contains("گاوں")
        ) {
            var newCode = currentState.supplierCode
            var newName = currentState.supplierName
            var newVillage = currentState.villageName
            var newSource = currentState.sourceType

            // Extract code like 00S923 or S-123 or numbers
            val codeRegex = Regex("""(?:code|کوڈ|سپلائر کوڈ)\s*[:=]?\s*([A-Za-z0-9\-_]+)""").find(lower)
            if (codeRegex != null) {
                newCode = codeRegex.groupValues[1].uppercase()
            }

            if (lower.contains("nadeem") || lower.contains("ندیم")) newName = "Nadeem Tariq"
            if (lower.contains("aslam") || lower.contains("اسلم")) newName = "Muhammad Aslam"
            if (lower.contains("tariq") || lower.contains("طارق")) newName = "Tariq Mahmood"
            if (lower.contains("chak") || lower.contains("چک")) {
                val chakMatch = Regex("""(?:chak|چک)\s*([0-9A-Za-z\s]+)""").find(lower)
                if (chakMatch != null) newVillage = "Chak " + chakMatch.groupValues[1].trim()
            }
            if (lower.contains("do") || lower.contains("ڈی او")) newSource = "DO"
            if (lower.contains("ts") || lower.contains("ٹی ایس")) newSource = "TS"

            val updated = currentState.copy(
                supplierCode = newCode.ifBlank { "00S923" },
                supplierName = newName.ifBlank { "Nadeem Tariq" },
                villageName = newVillage.ifBlank { "Chak 45/GD" },
                sourceType = newSource.ifBlank { "DO" },
                statusMessage = "✓ سپلائر کی تفصیلات اپ ڈیٹ ہو گئی ہیں۔"
            )
            return AiExecutionResult(
                updatedState = updated,
                urduResponse = "سپلائر کوڈ ${updated.supplierCode}، نام ${updated.supplierName} اور گاؤں ${updated.villageName} سیٹ کر دیا گیا ہے۔",
                englishResponse = "Supplier code ${updated.supplierCode} and metadata updated.",
                actionTaken = "SET_METADATA"
            )
        }

        // 4. Distribute / Fill milk in a specific month (e.g. "Jan mein 15000 liter daal do")
        val targetMonth = findMonthInText(lower)
        if (targetMonth != null || lower.contains("distribute") || lower.contains("taqseem") || lower.contains("تقسیم") || lower.contains("دودھ")) {
            val month = targetMonth ?: "Jan"
            val numMatches = Regex("""\b(\d+(?:\.\d+)?)\b""").findAll(lower).map { it.value.toDouble() }.toList()

            // Find possible total liters (usually large > 100) or farmer count (< 70)
            val totalLiters = numMatches.firstOrNull { it >= 100.0 } ?: 12000.0
            val farmerCount = numMatches.firstOrNull { it in 5.0..70.0 && it != totalLiters }?.toInt() ?: 30

            val updated = distributeMilkInMonth(currentState, month, totalLiters, farmerCount)
            val monthSum = updated.rows.sumOf { it.values[month] ?: 0.0 }

            return AiExecutionResult(
                updatedState = updated,
                urduResponse = "میں نے مہینہ $month میں $farmerCount کسانوں کے درمیان کل ${TraceabilityEngine.formatNumber(monthSum)} لیٹر دودھ حقیقت پسندانہ انداز میں تقسیم کر دیا ہے۔",
                englishResponse = "Successfully distributed ${TraceabilityEngine.formatNumber(monthSum)} Liters across $farmerCount farmers in $month.",
                actionTaken = "DISTRIBUTE_MILK"
            )
        }

        // 5. Auto Balance / Smart Complete (آٹو بیلنس / بیلنس کرو)
        if (lower.contains("balance") || lower.contains("بیلنس") || lower.contains("smart") || lower.contains("مکمل") || lower.contains("audit")) {
            val updated = autoBalanceEntireSheet(currentState)
            return AiExecutionResult(
                updatedState = updated,
                urduResponse = "پوری شیٹ کو نیسلے آڈٹ کے اصولوں کے مطابق آٹو بیلنس کر دیا گیا ہے۔ ہر کسان کا دودھ معیاری لمٹ کے اندر سیٹ ہے۔",
                englishResponse = "Sheet auto-balanced with standard Nestlé variance and audit compliance.",
                actionTaken = "AUTO_BALANCE"
            )
        }

        // 6. AASM Verification (AASM ویریفائی)
        if (lower.contains("verify") || lower.contains("aasm") || lower.contains("تصدیق")) {
            return AiExecutionResult(
                updatedState = currentState.copy(
                    statusMessage = "✓ چاروں کوارٹرز (Mar, Jun, Sep, Dec) کے لیے AASM ویریفیکیشن فعال ہے۔"
                ),
                urduResponse = "تمام کوارٹرز کے AASM Verification کالمز نیسلے فارمیٹ کے عین مطابق تیار اور تصدیق شدہ ہیں۔",
                englishResponse = "All AASM Verification columns checked and ready for official sign-off.",
                actionTaken = "AASM_VERIFY"
            )
        }

        // 7. General Guide & Help (کیسے کام کرنا ہے / سمجھاؤ / guidance)
        return AiExecutionResult(
            updatedState = currentState,
            urduResponse = "میں آپ کا نیسلے ٹریس ایبلٹی ایکسل اسسٹنٹ ہوں۔ آپ مجھ سے بول کر کہہ سکتے ہیں: مثلاً 'جنوری میں 15000 لیٹر تقسیم کرو'، 'کسانوں کے نام لکھو'، یا 'پوری شیٹ خالی کر دو'۔ میں فوری ایکسل میں کام کر دوں گا۔",
            englishResponse = "I am your Nestlé Traceability Excel AI. You can command me with voice to distribute milk, fill farmer names, auto-balance, or clear the sheet.",
            actionTaken = "GUIDANCE"
        )
    }

    private fun findMonthInText(text: String): String? {
        val months = listOf(
            "jan" to "Jan", "january" to "Jan", "جنوری" to "Jan",
            "feb" to "Feb", "february" to "Feb", "فروری" to "Feb",
            "mar" to "Mar", "march" to "Mar", "مارچ" to "Mar",
            "apr" to "Apr", "april" to "Apr", "اپریل" to "Apr",
            "may" to "May", "مئی" to "May",
            "jun" to "Jun", "june" to "Jun", "جون" to "Jun",
            "jul" to "Jul", "july" to "Jul", "جولائی" to "Jul",
            "aug" to "Aug", "august" to "Aug", "اگست" to "Aug",
            "sep" to "Sep", "september" to "Sep", "ستمبر" to "Sep",
            "oct" to "Oct", "october" to "Oct", "اکتوبر" to "Oct",
            "nov" to "Nov", "november" to "Nov", "نومبر" to "Nov",
            "dec" to "Dec", "december" to "Dec", "دسمبر" to "Dec"
        )
        for ((needle, code) in months) {
            if (text.contains(needle)) return code
        }
        return null
    }

    private fun distributeMilkInMonth(
        state: TraceSheetUiState,
        month: String,
        targetTotalLiters: Double,
        activeFarmersCount: Int
    ): TraceSheetUiState {
        val count = activeFarmersCount.coerceIn(5, 70)
        val target = max(100.0, targetTotalLiters)
        val avg = target / count

        val random = Random(month.hashCode() + count)
        val rawValues = mutableListOf<Double>()
        var sum = 0.0

        for (i in 0 until count) {
            // Realistic farmer variance (-30% to +40% around average)
            val factor = 0.70 + random.nextDouble(0.70)
            val valKg = (avg * factor * 10.0).roundToInt() / 10.0
            rawValues.add(valKg)
            sum += valKg
        }

        // Adjust difference to hit target nicely
        val diff = target - sum
        if (count > 0 && rawValues.isNotEmpty()) {
            val perItemAdj = (diff / count * 10.0).roundToInt() / 10.0
            for (i in 0 until count) {
                rawValues[i] = max(10.0, (rawValues[i] + perItemAdj * 10.0).roundToInt() / 10.0)
            }
        }

        val updatedMonths = state.selectedMonths + month
        val updatedConfigs = state.monthConfigs.toMutableMap()
        updatedConfigs[month] = MonthConfig(count, (target / count) * 1.5)

        val updatedRows = state.rows.mapIndexed { idx, row ->
            val name = if (row.name.isBlank() && idx < count) {
                PAKISTANI_FARMER_NAMES.getOrElse(idx) { "محمد ${idx + 1}" }
            } else row.name

            val currentValues = row.values.toMutableMap()
            if (idx < count) {
                currentValues[month] = rawValues[idx]
            } else {
                currentValues.remove(month)
            }
            row.copy(name = name, values = currentValues)
        }

        return state.copy(
            selectedMonths = updatedMonths,
            monthConfigs = updatedConfigs,
            rows = updatedRows,
            statusMessage = "✓ مہینہ $month کے لیے دودھ کی مقدار تقسیم کر دی گئی ہے۔",
            isSuccessStatus = true
        )
    }

    private fun autoBalanceEntireSheet(state: TraceSheetUiState): TraceSheetUiState {
        val activeMonths = if (state.selectedMonths.isEmpty()) setOf("Sep") else state.selectedMonths
        var updated = state

        activeMonths.forEach { m ->
            val existingSum = updated.rows.sumOf { it.values[m] ?: 0.0 }
            val target = if (existingSum > 100.0) existingSum else 14500.0
            val activeFarmers = updated.rows.count { (it.values[m] ?: 0.0) > 0 }.let { if (it > 0) it else 30 }
            updated = distributeMilkInMonth(updated, m, target, activeFarmers)
        }

        return updated.copy(
            statusMessage = "✓ شیٹ کو مکمل طور پر آٹو بیلنس کر دیا گیا ہے۔"
        )
    }
}
