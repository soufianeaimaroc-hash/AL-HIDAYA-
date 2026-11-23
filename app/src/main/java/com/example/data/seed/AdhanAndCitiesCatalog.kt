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
    val timeZoneId: String,
    val countryCode: String = ""
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
    const val KAABA_LAT = 21.422487
    const val KAABA_LON = 39.826206

    val cities: List<CityOption> = listOf(
        // Morocco (Ministry of Awqaf & Islamic Affairs default support)
        CityOption("الدار البيضاء", "Casablanca", "المغرب", "Morocco", 33.5731, -7.5898, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("الرباط", "Rabat", "المغرب", "Morocco", 34.0209, -6.8416, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("مراكش", "Marrakech", "المغرب", "Morocco", 31.6295, -7.9811, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("فاس", "Fes", "المغرب", "Morocco", 34.0181, -5.0078, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("طنجة", "Tangier", "المغرب", "Morocco", 35.7595, -5.8340, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("تطوان", "Tetouan", "المغرب", "Morocco", 35.5889, -5.3626, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("أكادير", "Agadir", "المغرب", "Morocco", 30.4278, -9.5981, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("مكناس", "Meknes", "المغرب", "Morocco", 33.8935, -5.5473, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("وجدة", "Oujda", "المغرب", "Morocco", 34.6814, -1.9086, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("العيون", "Laayoune", "المغرب", "Morocco", 27.1253, -13.1625, "MOROCCO", "Africa/Casablanca", "MA"),
        CityOption("الداخلة", "Dakhla", "المغرب", "Morocco", 23.6848, -15.9570, "MOROCCO", "Africa/Casablanca", "MA"),
        // Holy Cities & Arab/Islamic World
        CityOption("مكة المكرمة", "Makkah", "السعودية", "Saudi Arabia", 21.4225, 39.8262, "UMM_AL_QURA", "Asia/Riyadh", "SA"),
        CityOption("المدينة المنورة", "Madinah", "السعودية", "Saudi Arabia", 24.4672, 39.6111, "UMM_AL_QURA", "Asia/Riyadh", "SA"),
        CityOption("الرياض", "Riyadh", "السعودية", "Saudi Arabia", 24.7136, 46.6753, "UMM_AL_QURA", "Asia/Riyadh", "SA"),
        CityOption("جدة", "Jeddah", "السعودية", "Saudi Arabia", 21.4858, 39.1925, "UMM_AL_QURA", "Asia/Riyadh", "SA"),
        CityOption("القدس", "Jerusalem", "فلسطين", "Palestine", 31.7767, 35.2345, "MWL", "Asia/Jerusalem", "PS"),
        CityOption("القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, "EGYPT", "Africa/Cairo", "EG"),
        CityOption("دبي", "Dubai", "الإمارات", "UAE", 25.2048, 55.2708, "DUBAI", "Asia/Dubai", "AE"),
        CityOption("الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, "UMM_AL_QURA", "Asia/Qatar", "QA"),
        CityOption("الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, "UMM_AL_QURA", "Asia/Kuwait", "KW"),
        CityOption("عمّان", "Amman", "الأردن", "Jordan", 31.9454, 35.9284, "MWL", "Asia/Amman", "JO"),
        CityOption("إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, "MWL", "Europe/Istanbul", "TR"),
        CityOption("تونس", "Tunis", "تونس", "Tunisia", 36.8065, 10.1815, "MWL", "Africa/Tunis", "TN"),
        CityOption("الجزائر", "Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, "MWL", "Africa/Algiers", "DZ"),
        // International
        CityOption("لندن", "London", "المملكة المتحدة", "United Kingdom", 51.5074, -0.1278, "MWL", "Europe/London", "GB"),
        CityOption("باريس", "Paris", "فرنسا", "France", 48.8566, 2.3522, "MWL", "Europe/Paris", "FR"),
        CityOption("مدريد", "Madrid", "إسبانيا", "Spain", 40.4168, -3.7038, "MWL", "Europe/Madrid", "ES"),
        CityOption("برلين", "Berlin", "ألمانيا", "Germany", 52.5200, 13.4050, "MWL", "Europe/Berlin", "DE"),
        CityOption("نيويورك", "New York", "الولايات المتحدة", "USA", 40.7128, -74.0060, "ISNA", "America/New_York", "US"),
        CityOption("تورونتو", "Toronto", "كندا", "Canada", 43.6532, -79.3832, "ISNA", "America/Toronto", "CA"),
        CityOption("كوالالمبور", "Kuala Lumpur", "ماليزيا", "Malaysia", 3.1390, 101.6869, "MWL", "Asia/Kuala_Lumpur", "MY"),
        CityOption("جاكرتا", "Jakarta", "إندونيسيا", "Indonesia", -6.2088, 106.8456, "MWL", "Asia/Jakarta", "ID")
    )

    private val countryPrimaryTimeZones = mapOf(
        "MA" to "Africa/Casablanca",
        "EH" to "Africa/El_Aaiun",
        "DZ" to "Africa/Algiers",
        "TN" to "Africa/Tunis",
        "LY" to "Africa/Tripoli",
        "EG" to "Africa/Cairo",
        "SA" to "Asia/Riyadh",
        "AE" to "Asia/Dubai",
        "QA" to "Asia/Qatar",
        "KW" to "Asia/Kuwait",
        "BH" to "Asia/Bahrain",
        "OM" to "Asia/Muscat",
        "YE" to "Asia/Aden",
        "JO" to "Asia/Amman",
        "PS" to "Asia/Jerusalem",
        "LB" to "Asia/Beirut",
        "SY" to "Asia/Damascus",
        "IQ" to "Asia/Baghdad",
        "TR" to "Europe/Istanbul",
        "FR" to "Europe/Paris",
        "GB" to "Europe/London",
        "DE" to "Europe/Berlin",
        "ES" to "Europe/Madrid",
        "IT" to "Europe/Rome",
        "NL" to "Europe/Amsterdam",
        "BE" to "Europe/Brussels",
        "CH" to "Europe/Zurich",
        "MY" to "Asia/Kuala_Lumpur",
        "PK" to "Asia/Karachi",
        "BD" to "Asia/Dhaka",
        "IN" to "Asia/Kolkata"
    )

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2.0) * kotlin.math.sin(dLat / 2.0) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2.0) * kotlin.math.sin(dLon / 2.0)
        val c = 2.0 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt((1.0 - a).coerceAtLeast(0.0)))
        return r * c
    }

    fun distanceToKaabaKm(latitude: Double, longitude: Double): Double =
        distanceKm(latitude, longitude, KAABA_LAT, KAABA_LON)

    fun findNearestCity(latitude: Double, longitude: Double, maxDistanceKm: Double = 80.0): CityOption? {
        return cities
            .map { it to distanceKm(latitude, longitude, it.latitude, it.longitude) }
            .filter { it.second <= maxDistanceKm }
            .minByOrNull { it.second }
            ?.first
    }

    fun suggestedMethodForCountry(countryCode: String?, latitude: Double, longitude: Double): String {
        return when (countryCode?.trim()?.uppercase(java.util.Locale.US)) {
            "MA", "EH" -> "MOROCCO"
            "SA", "YE", "QA", "KW", "BH", "OM" -> "UMM_AL_QURA"
            "EG" -> "EGYPT"
            "AE" -> "DUBAI"
            "PK", "IN", "BD", "AF" -> "KARACHI"
            "US", "CA" -> "ISNA"
            else -> {
                val nearest = findNearestCity(latitude, longitude, maxDistanceKm = 180.0)
                if (nearest != null) {
                    nearest.defaultMethod
                } else if (latitude in 20.5..36.2 && longitude in -17.5..-0.9) {
                    "MOROCCO"
                } else {
                    "MWL"
                }
            }
        }
    }

    fun isTimeZoneConsistentWithLongitude(
        timeZone: TimeZone,
        longitude: Double,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val nominalSolarHours = longitude / 15.0
        val actualHours = timeZone.getOffset(nowMillis) / 3_600_000.0
        var diff = abs(actualHours - nominalSolarHours)
        if (diff > 12.0) diff = 24.0 - diff
        // Political zones (e.g. Western Europe / Morocco DST / Western China) can vary up to ~3.2 hours
        return diff <= 3.2
    }

    fun resolveTimeZoneId(
        cityNameEn: String,
        countryCode: String?,
        latitude: Double,
        longitude: Double,
        preferredTimeZoneId: String = ""
    ): String {
        val now = System.currentTimeMillis()
        if (preferredTimeZoneId.isNotBlank()) {
            val preferredTz = TimeZone.getTimeZone(preferredTimeZoneId)
            val isRealZone = preferredTimeZoneId == "UTC" || preferredTimeZoneId == "GMT" || preferredTz.id == preferredTimeZoneId
            if (isRealZone && isTimeZoneConsistentWithLongitude(preferredTz, longitude, now)) {
                return preferredTz.id
            }
        }

        val exactCity = cities.firstOrNull {
            it.nameEn.equals(cityNameEn, ignoreCase = true) ||
                (abs(it.latitude - latitude) < 0.08 && abs(it.longitude - longitude) < 0.08)
        }
        if (exactCity != null) return exactCity.timeZoneId

        val nearest = findNearestCity(latitude, longitude, maxDistanceKm = 75.0)
        if (nearest != null) return nearest.timeZoneId

        val code = countryCode?.trim()?.uppercase(java.util.Locale.US)
        if (!code.isNullOrBlank()) {
            val mapped = countryPrimaryTimeZones[code]
            if (mapped != null) return mapped
        }

        val deviceDefault = TimeZone.getDefault()
        if (isTimeZoneConsistentWithLongitude(deviceDefault, longitude, now)) {
            return deviceDefault.id
        }

        val approxOffsetHours = kotlin.math.round(longitude / 15.0).toInt().coerceIn(-12, 14)
        return when {
            approxOffsetHours == 0 -> "GMT"
            approxOffsetHours > 0 -> "GMT+$approxOffsetHours"
            else -> "GMT$approxOffsetHours"
        }
    }

    fun findTimeZone(
        cityNameEn: String,
        latitude: Double,
        longitude: Double,
        preferredTimeZoneId: String = "",
        countryCode: String? = null
    ): TimeZone {
        val resolvedId = resolveTimeZoneId(
            cityNameEn = cityNameEn,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
            preferredTimeZoneId = preferredTimeZoneId
        )
        return TimeZone.getTimeZone(resolvedId)
    }
}
