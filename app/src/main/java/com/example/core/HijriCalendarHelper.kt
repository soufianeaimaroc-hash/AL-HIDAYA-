package com.example.core

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DualDateDisplay(
    val hijriText: String,
    val gregorianText: String
)

object HijriCalendarHelper {
    private val hijriMonthsAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    fun getTodayDates(language: String, date: Date = Date()): DualDateDisplay {
        val hijriCal = IslamicCalendar(ULocale("@calendar=islamic-umalqura")).apply {
            time = date
        }
        val hDay = hijriCal.get(IslamicCalendar.DAY_OF_MONTH)
        val hMonthIndex = hijriCal.get(IslamicCalendar.MONTH).coerceIn(0, 11)
        val hYear = hijriCal.get(IslamicCalendar.YEAR)

        val hijriText = if (language == "ar") {
            "$hDay ${hijriMonthsAr[hMonthIndex]} $hYear هـ"
        } else {
            "$hDay ${hijriMonthsEn[hMonthIndex]} $hYear AH"
        }

        val locale = if (language == "ar") Locale("ar", "MA") else Locale.ENGLISH
        val pattern = if (language == "ar") "EEEE، d MMMM yyyy" else "EEEE, dd MMM yyyy"
        val gregText = SimpleDateFormat(pattern, locale).format(date)

        return DualDateDisplay(hijriText = hijriText, gregorianText = gregText)
    }
}
