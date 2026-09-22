package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeekInfo(
    val year: Int,
    val weekNumber: Int,
    val mondayDate: String, // YYYY-MM-DD
    val sundayDate: String, // YYYY-MM-DD
    val mondayDateFormatted: String, // dd/MM/yyyy
    val sundayDateFormatted: String  // dd/MM/yyyy
)

object WeekDateUtils {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    /**
     * Determines Monday-Sunday week metadata for a given date string or current time.
     * Monday is treated strictly as the first day of the week, Sunday as the last day.
     */
    fun getWeekInfoForDate(dateStr: String? = null): WeekInfo {
        val cal = Calendar.getInstance(Locale.US).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4 // ISO standard week definition
        }

        if (!dateStr.isNullOrBlank()) {
            try {
                val parsed = isoDateFormat.parse(dateStr)
                if (parsed != null) {
                    cal.time = parsed
                }
            } catch (_: Exception) {}
        }

        // Set to Monday of this week
        val year = cal.get(Calendar.YEAR)
        val weekNumber = cal.get(Calendar.WEEK_OF_YEAR)

        val monCal = (cal.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        val sunCal = (cal.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            // In case calendar week rolls into next year on Sunday or vice-versa
            if (sunCalIsBeforeMon(monCal, this)) {
                add(Calendar.DAY_OF_MONTH, 7)
            }
        }

        val mondayIso = isoDateFormat.format(monCal.time)
        val sundayIso = isoDateFormat.format(sunCal.time)

        return WeekInfo(
            year = year,
            weekNumber = weekNumber,
            mondayDate = mondayIso,
            sundayDate = sundayIso,
            mondayDateFormatted = displayDateFormat.format(monCal.time),
            sundayDateFormatted = displayDateFormat.format(sunCal.time)
        )
    }

    private fun sunCalIsBeforeMon(mon: Calendar, sun: Calendar): Boolean {
        return sun.timeInMillis < mon.timeInMillis
    }

    /**
     * Returns the English and Urdu name for day of week from YYYY-MM-DD string.
     */
    fun getDayOfWeekLabel(dateStr: String): Pair<String, String> {
        return try {
            val date = isoDateFormat.parse(dateStr) ?: return Pair("", "")
            val cal = Calendar.getInstance(Locale.US).apply { time = date }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> Pair("Monday", "پیر")
                Calendar.TUESDAY -> Pair("Tuesday", "منگل")
                Calendar.WEDNESDAY -> Pair("Wednesday", "بدھ")
                Calendar.THURSDAY -> Pair("Thursday", "جمعرات")
                Calendar.FRIDAY -> Pair("Friday", "جمعہ")
                Calendar.SATURDAY -> Pair("Saturday", "ہفتہ")
                Calendar.SUNDAY -> Pair("Sunday", "اتوار")
                else -> Pair("", "")
            }
        } catch (_: Exception) {
            Pair("", "")
        }
    }

    /**
     * Checks whether a date string (YYYY-MM-DD) falls between Monday and Sunday (inclusive).
     */
    fun isDateInWeek(dateStr: String, mondayDate: String, sundayDate: String): Boolean {
        return dateStr in mondayDate..sundayDate
    }
}
