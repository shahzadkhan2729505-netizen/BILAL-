package com.example.traceability

import org.json.JSONObject
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object TraceabilityEngine {
    val TRACE_MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val VERIFICATION_MONTHS = setOf("Mar", "Jun", "Sep", "Dec")

    val URDU_FARMERS = listOf(
        "محمد یوسف", "محمد اقبال", "محمد اشرف", "محمد سلیم", "محمد امجد", "محمد حنیف", "محمد حامد",
        "محمد نواز", "محمد فرید", "محمد اکبر", "محمد عباس", "محمد شریف", "محمد رحمان", "محمد رحیم",
        "محمد کریم", "محمد غفار", "محمد غنی", "محمد نبی", "محمد رسول", "محمد شبیر", "محمد رشید",
        "محمد طارق", "محمد عمران", "محمد بلال", "محمد عظیم", "محمد لطیف", "محمد بشیر", "محمد کاظم",
        "محمد نعیم", "محمد صابر", "محمد شاکر", "محمد وقار", "محمد وقاص", "محمد حسین", "محمد احمد",
        "محمد علی", "محمد خان", "محمد ملک", "محمد مغل", "محمد چوہدری", "محمد اعوان", "محمد گجر",
        "محمد جٹ", "محمد راجپوت", "محمد شیخ", "محمد قریشی", "محمد سید", "محمد فاروق", "محمد نصیر",
        "محمد منیر", "محمد منظور", "محمد مقبول", "محمد مسعود", "محمد مختار", "محمد محمود", "محمد ساجد",
        "محمد کاشف", "محمد فہیم", "محمد ندیم", "محمد جمیل", "محمد جبار", "محمد جواد", "محمد حسیب",
        "محمد زاہد", "محمد طیب", "محمد حبیب", "محمد سرفراز", "احمد یوسف", "احمد اقبال", "احمد اشرف",
        "احمد سلیم", "احمد امجد", "احمد حنیف", "احمد حامد", "احمد نواز", "احمد فرید", "احمد اکبر",
        "احمد عباس", "احمد شریف", "احمد رحمان", "احمد رحیم", "احمد کریم", "احمد غفار", "احمد غنی",
        "احمد نبی", "احمد رسول", "احمد شبیر", "احمد رشید", "احمد طارق", "احمد عمران", "احمد بلال",
        "احمد عظیم", "احمد لطیف", "احمد بشیر", "احمد کاظم", "احمد نعیم", "احمد صابر", "احمد شاکر",
        "احمد وقار", "احمد وقاص", "احمد حسین", "احمد علی", "احمد خان", "احمد ملک", "احمد مغل",
        "علی یوسف", "علی اقبال", "علی اشرف", "علی سلیم", "علی امجد", "علی حنیف", "علی حامد",
        "علی نواز", "علی فرید", "علی اکبر", "علی عباس", "علی شریف", "علی رحمان", "علی رحیم",
        "حسن یوسف", "حسن اقبال", "حسن اشرف", "حسن سلیم", "حسن امجد", "حسن حنیف", "حسن حامد",
        "حسین یوسف", "حسین اقبال", "حسین اشرف", "حسین سلیم", "حسین امجد", "حسین حنیف", "حسین حامد",
        "عثمان یوسف", "عثمان اقبال", "عثمان اشرف", "عثمان سلیم", "عثمان امجد", "عثمان حنیف", "عثمان حامد",
        "عمر یوسف", "عمر اقبال", "عمر اشرف", "عمر سلیم", "عمر امجد", "عمر حنیف", "عمر حامد",
        "بلال یوسف", "بلال اقبال", "بلال اشرف", "بلال سلیم", "بلال امجد", "بلال حنیف", "بلال حامد",
        "سلمان یوسف", "سلمان اقبال", "سلمان اشرف", "سلمان سلیم", "سلمان امجد", "سلمان حنیف",
        "ساجد یوسف", "ساجد اقبال", "ساجد اشرف", "ساجد سلیم", "ساجد امجد", "ساجد حنیف",
        "شاہد یوسف", "شاہد اقبال", "شاہد اشرف", "شاہد سلیم", "شاہد امجد", "شاہد حنیف",
        "عامر یوسف", "عامر اقبال", "عامر اشرف", "عامر سلیم", "عامر امجد", "عامر حنیف",
        "وقاص یوسف", "وقاص اقبال", "وقاص اشرف", "وقاص سلیم", "وقاص امجد", "وقاص حنیف",
        "نعمان یوسف", "نعمان اقبال", "نعمان اشرف", "نعمان سلیم", "نعمان امجد", "نعمان حنیف",
        "عرفان یوسف", "عرفان اقبال", "عرفان اشرف", "عرفان سلیم", "عرفان امجد", "عرفان حنیف"
    )

    fun generateUniqueValues(count: Int, maxLimit: Double): List<Double> {
        val safeCount = max(1, min(70, count))
        val safeMax = max(0.01, maxLimit)
        var scale = 1.0
        while (floor(safeMax * scale) < safeCount && scale < 1000.0) {
            scale *= 10.0
        }
        val maxScaled = floor(safeMax * scale).toInt()
        val vals = mutableListOf<Double>()
        val used = mutableSetOf<Int>()

        for (i in 0 until safeCount) {
            var n = 1 + floor(((i + 1).toDouble() * (maxScaled - 1).toDouble()) / (safeCount + 1).toDouble()).toInt()
            while (used.contains(n) && n <= maxScaled) {
                n++
            }
            if (n > maxScaled) {
                n = maxScaled
                while (used.contains(n) && n > 1) {
                    n--
                }
            }
            used.add(n)
            val finalVal = if (scale == 1.0) n.toDouble() else String.format(Locale.US, "%.1f", n / scale).toDouble()
            vals.add(finalVal)
        }
        return vals
    }

    fun makeUrduNames(count: Int, seedName: String): List<String> {
        val safeCount = max(1, min(70, count))
        val pool = URDU_FARMERS
        val out = mutableListOf<String>()
        val used = mutableSetOf<String>()

        if (seedName.isNotBlank()) {
            out.add(seedName.trim())
            used.add(seedName.trim())
        }

        var idx = 0
        while (out.size < safeCount && idx < pool.size) {
            val candidate = pool[idx]
            if (!used.contains(candidate)) {
                out.add(candidate)
                used.add(candidate)
            }
            idx++
        }

        while (out.size < safeCount) {
            val fallback = "محمد ${out.size + 1}"
            out.add(fallback)
        }
        return out
    }

    fun parseValuesJson(json: String): Map<String, Double> {
        if (json.isBlank() || json == "{}") return emptyMap()
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Double>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.optDouble(k, 0.0)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun valuesToJson(map: Map<String, Double>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    fun formatNumber(num: Double): String {
        return if (num % 1.0 == 0.0) {
            num.toLong().toString()
        } else {
            String.format(Locale.US, "%.1f", num)
        }
    }
}
