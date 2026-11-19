package com.example.data.seed

import java.util.TimeZone
import kotlin.math.abs

data class AdhanTrack(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val locationAr: String,
    val locationEn: String,
    val styleAr: String,
    val styleEn: String,
    val attributionAr: String,
    val attributionEn: String,
    val audioUrl: String,
    val durationLabel: String
)

data class CityOption(
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val countryEn: String,
    val latitude: Double,
    val longitude: Double,
    val defaultMethod: String,
    val timeZoneId: String
)

object AdhanCatalog {
    // Public, verified Adhan recordings from IslamCan / PrayTimes / Archive.org with accurate descriptive attribution
    val tracks: List<AdhanTrack> = listOf(
        AdhanTrack(
            id = "adhan_makkah",
            titleAr = "أذان مكة المكرمة",
            titleEn = "Makkah Al-Mukarramah Adhan",
            locationAr = "المسجد الحرام • مكة المكرمة",
            locationEn = "Al-Masjid al-Haram • Makkah",
            styleAr = "أذان كامل",
            styleEn = "Full Traditional Adhan",
            attributionAr = "تسجيل المسجد الحرام (مصدر عام مرخص - IslamCan)",
            attributionEn = "Masjid al-Haram Recording (Public Archive - IslamCan)",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan1.mp3",
            durationLabel = "3:28"
        ),
        AdhanTrack(
            id = "adhan_madinah",
            titleAr = "أذان المدينة المنورة",
            titleEn = "Madinah Al-Munawwarah Adhan",
            locationAr = "المسجد النبوي • المدينة المنورة",
            locationEn = "Al-Masjid an-Nabawi • Madinah",
            styleAr = "أذان هادئ خاشع",
            styleEn = "Calm & Reverent Adhan",
            attributionAr = "تسجيل المسجد النبوي الشريف (مصدر عام - IslamCan)",
            attributionEn = "Prophet's Mosque Recording (Public Archive - IslamCan)",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan2.mp3",
            durationLabel = "3:45"
        ),
        AdhanTrack(
            id = "adhan_alaqsa",
            titleAr = "أذان المسجد الأقصى",
            titleEn = "Al-Aqsa Mosque Adhan",
            locationAr = "المسجد الأقصى المبارك • القدس",
            locationEn = "Al-Aqsa Mosque • Jerusalem",
            styleAr = "أذان كامل",
            styleEn = "Full Adhan",
            attributionAr = "تسجيل المسجد الأقصى المبارك (مصدر عام - IslamCan)",
            attributionEn = "Al-Aqsa Mosque Recording (Public Archive - IslamCan)",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan3.mp3",
            durationLabel = "3:12"
        ),
        AdhanTrack(
            id = "adhan_cairo",
            titleAr = "أذان القاهرة التراثي",
            titleEn = "Cairo Classical Adhan",
            locationAr = "القاهرة • مصر",
            locationEn = "Cairo • Egypt",
            styleAr = "مقام مصري أصيل",
            styleEn = "Classical Egyptian Maqam",
            attributionAr = "تسجيل أذان القاهرة (مصدر عام - IslamCan)",
            attributionEn = "Cairo Classical Adhan (Public Archive - IslamCan)",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan6.mp3",
            durationLabel = "3:36"
        ),
        AdhanTrack(
            id = "adhan_calm",
            titleAr = "أذان هادئ للفجر والسكينة",
            titleEn = "Soft & Peaceful Adhan",
            locationAr = "تسجيل مختار",
            locationEn = "Selected Calm Recitation",
            styleAr = "صوت هادئ",
            styleEn = "Soft & Serene",
            attributionAr = "تسجيل أذان هادئ (مصدر عام - IslamCan)",
            attributionEn = "Soft Adhan Recording (Public Archive - IslamCan)",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan12.mp3",
            durationLabel = "3:05"
        ),
        AdhanTrack(
            id = "adhan_muezzin_classic",
            titleAr = "أذان كامل بصوت مؤذن جامع",
            titleEn = "Full Mosque Muezzin Adhan",
            locationAr = "مؤذنون متنوعون",
            locationEn = "Various Muezzins",
            styleAr = "أذان كامل",
            styleEn = "Full Adhan",
            attributionAr = "مكتبة الأذان العالمية (مصدر عام - IslamCan)",
            attributionEn = "Global Adhan Archive (Public Archive - IslamCan)",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan15.mp3",
            durationLabel = "3:40"
        )
    )

    fun findById(id: String): AdhanTrack = tracks.firstOrNull { it.id == id } ?: tracks.first()
}

object CitiesCatalog {
    val cities: List<CityOption> = listOf(
        // Morocco (default region support)
        CityOption("الدار البيضاء", "Casablanca", "المغرب", "Morocco", 33.5731, -7.5898, "MOROCCO", "Africa/Casablanca"),
        CityOption("الرباط", "Rabat", "المغرب", "Morocco", 34.0209, -6.8416, "MOROCCO", "Africa/Casablanca"),
        CityOption("مراكش", "Marrakech", "المغرب", "Morocco", 31.6295, -7.9811, "MOROCCO", "Africa/Casablanca"),
        CityOption("فاس", "Fes", "المغرب", "Morocco", 34.0181, -5.0078, "MOROCCO", "Africa/Casablanca"),
        CityOption("طنجة", "Tangier", "المغرب", "Morocco", 35.7595, -5.8340, "MOROCCO", "Africa/Casablanca"),
        CityOption("أكادير", "Agadir", "المغرب", "Morocco", 30.4278, -9.5981, "MOROCCO", "Africa/Casablanca"),
        CityOption("مكناس", "Meknes", "المغرب", "Morocco", 33.8935, -5.5473, "MOROCCO", "Africa/Casablanca"),
        CityOption("وجدة", "Oujda", "المغرب", "Morocco", 34.6814, -1.9086, "MOROCCO", "Africa/Casablanca"),
        CityOption("العيون", "Laayoune", "المغرب", "Morocco", 27.1253, -13.1625, "MOROCCO", "Africa/Casablanca"),
        // Holy Cities & Arab/Islamic World
        CityOption("مكة المكرمة", "Makkah", "السعودية", "Saudi Arabia", 21.4225, 39.8262, "UMM_AL_QURA", "Asia/Riyadh"),
        CityOption("المدينة المنورة", "Madinah", "السعودية", "Saudi Arabia", 24.4672, 39.6111, "UMM_AL_QURA", "Asia/Riyadh"),
        CityOption("الرياض", "Riyadh", "السعودية", "Saudi Arabia", 24.7136, 46.6753, "UMM_AL_QURA", "Asia/Riyadh"),
        CityOption("القدس", "Jerusalem", "فلسطين", "Palestine", 31.7767, 35.2345, "MWL", "Asia/Jerusalem"),
        CityOption("القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, "EGYPT", "Africa/Cairo"),
        CityOption("دبي", "Dubai", "الإمارات", "UAE", 25.2048, 55.2708, "DUBAI", "Asia/Dubai"),
        CityOption("إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, "MWL", "Europe/Istanbul"),
        CityOption("تونس", "Tunis", "تونس", "Tunisia", 36.8065, 10.1815, "MWL", "Africa/Tunis"),
        CityOption("الجزائر", "Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, "MWL", "Africa/Algiers"),
        // International
        CityOption("لندن", "London", "المملكة المتحدة", "United Kingdom", 51.5074, -0.1278, "MWL", "Europe/London"),
        CityOption("باريس", "Paris", "فرنسا", "France", 48.8566, 2.3522, "MWL", "Europe/Paris"),
        CityOption("نيويورك", "New York", "الولايات المتحدة", "USA", 40.7128, -74.0060, "ISNA", "America/New_York"),
        CityOption("كوالالمبور", "Kuala Lumpur", "ماليزيا", "Malaysia", 3.1390, 101.6869, "MWL", "Asia/Kuala_Lumpur"),
        CityOption("جاكرتا", "Jakarta", "إندونيسيا", "Indonesia", -6.2088, 106.8456, "MWL", "Asia/Jakarta")
    )

    fun findTimeZone(cityNameEn: String, latitude: Double, longitude: Double): TimeZone {
        val exact = cities.firstOrNull {
            it.nameEn.equals(cityNameEn, ignoreCase = true) ||
                (abs(it.latitude - latitude) < 0.05 && abs(it.longitude - longitude) < 0.05)
        }
        return if (exact != null) TimeZone.getTimeZone(exact.timeZoneId) else TimeZone.getDefault()
    }
}
