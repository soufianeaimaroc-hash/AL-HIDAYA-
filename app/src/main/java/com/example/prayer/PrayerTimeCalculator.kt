package com.example.prayer

import com.example.data.seed.CitiesCatalog
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class CalculationMethod(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null,
    val fajrOffsetMin: Int = 0,
    val dhuhrOffsetMin: Int = 0,
    val maghribOffsetMin: Int = 0,
    val ishaOffsetMin: Int = 0
) {
    MOROCCO(
        id = "MOROCCO",
        nameAr = "وزارة الأوقاف والشؤون الإسلامية (المغرب)",
        nameEn = "Morocco Ministry of Awqaf",
        fajrAngle = 19.0,
        ishaAngle = 17.0,
        maghribOffsetMin = 5,
        dhuhrOffsetMin = 5
    ),
    MWL(
        id = "MWL",
        nameAr = "رابطة العالم الإسلامي",
        nameEn = "Muslim World League",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    UMM_AL_QURA(
        id = "UMM_AL_QURA",
        nameAr = "أم القرى (مكة المكرمة)",
        nameEn = "Umm al-Qura (Makkah)",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaMinutesAfterMaghrib = 90
    ),
    ISNA(
        id = "ISNA",
        nameAr = "الجمعية الإسلامية لأمريكا الشمالية",
        nameEn = "ISNA (North America)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    EGYPT(
        id = "EGYPT",
        nameAr = "الهيئة المصرية العامة للمساحة",
        nameEn = "Egyptian General Authority",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    KARACHI(
        id = "KARACHI",
        nameAr = "جامعة العلوم الإسلامية (كراتشي)",
        nameEn = "University of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    DUBAI(
        id = "DUBAI",
        nameAr = "دبي (الإمارات العربية المتحدة)",
        nameEn = "Dubai (UAE)",
        fajrAngle = 18.2,
        ishaAngle = 18.2
    );

    companion object {
        fun fromId(id: String): CalculationMethod = entries.firstOrNull { it.id == id } ?: MOROCCO
    }
}

enum class AsrMadhab(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val shadowFactor: Double
) {
    SHAFI(
        id = "SHAFI",
        nameAr = "الشافعي والجمهور (ظل 1×)",
        nameEn = "Shafi'i / Standard (1x shadow)",
        shadowFactor = 1.0
    ),
    HANAFI(
        id = "HANAFI",
        nameAr = "المذهب الحنفي (ظل 2×)",
        nameEn = "Hanafi (2x shadow)",
        shadowFactor = 2.0
    );

    companion object {
        fun fromId(id: String): AsrMadhab =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SHAFI
    }
}

data class PrayerMinuteOffsets(
    val fajr: Int = 0,
    val sunrise: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0
) {
    fun offsetFor(type: PrayerType): Int = when (type) {
        PrayerType.FAJR -> fajr
        PrayerType.SUNRISE -> sunrise
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }

    fun hasAnyOffset(): Boolean =
        fajr != 0 || sunrise != 0 || dhuhr != 0 || asr != 0 || maghrib != 0 || isha != 0
}

enum class PrayerType(val key: String, val nameAr: String, val nameEn: String) {
    FAJR("FAJR", "الفجر", "Fajr"),
    SUNRISE("SUNRISE", "الشروق", "Sunrise"),
    DHUHR("DHUHR", "الظهر", "Dhuhr"),
    ASR("ASR", "العصر", "Asr"),
    MAGHRIB("MAGHRIB", "المغرب", "Maghrib"),
    ISHA("ISHA", "العشاء", "Isha")
}

data class PrayerTimeEntry(
    val type: PrayerType,
    val timeMillis: Long,
    val formattedTime: String
)

data class DailyPrayerSchedule(
    val prayers: List<PrayerTimeEntry>,
    val nextPrayer: PrayerTimeEntry,
    val remainingMillis: Long,
    val currentPrayer: PrayerTimeEntry? = null,
    val isDstActive: Boolean = false,
    val timeZoneId: String = "Africa/Casablanca",
    val isTimeZoneVerified: Boolean = true,
    val isFromCache: Boolean = false
)

object PrayerTimeCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val lat1 = Math.toRadians(latitude)
        val lon1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lon2 = Math.toRadians(KAABA_LON)
        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    fun calculateDistanceToKaabaKm(latitude: Double, longitude: Double): Double =
        CitiesCatalog.distanceKm(latitude, longitude, KAABA_LAT, KAABA_LON)

    fun dateKeyFor(nowMillis: Long, timeZone: TimeZone): String {
        val cal = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun calculateDailySchedule(
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        madhab: AsrMadhab = AsrMadhab.SHAFI,
        offsets: PrayerMinuteOffsets = PrayerMinuteOffsets()
    ): DailyPrayerSchedule {
        val cal = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        val todayPrayers = computePrayersForDate(cal, latitude, longitude, method, madhab, offsets)

        val currentPrayer = todayPrayers.lastOrNull { it.timeMillis <= nowMillis } ?: run {
            val yesterdayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            computePrayersForDate(yesterdayCal, latitude, longitude, method, madhab, offsets)
                .lastOrNull { it.type == PrayerType.ISHA }
        }

        val upcoming = todayPrayers.firstOrNull { it.timeMillis > nowMillis }
        val nextPrayer = if (upcoming != null) {
            upcoming
        } else {
            val tomorrowCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            computePrayersForDate(tomorrowCal, latitude, longitude, method, madhab, offsets).first()
        }
        val remaining = (nextPrayer.timeMillis - nowMillis).coerceAtLeast(0L)
        val isDst = timeZone.inDaylightTime(Date(nowMillis))
        val tzVerified = CitiesCatalog.isTimeZoneConsistentWithLongitude(timeZone, longitude, nowMillis)

        return DailyPrayerSchedule(
            prayers = todayPrayers,
            nextPrayer = nextPrayer,
            remainingMillis = remaining,
            currentPrayer = currentPrayer,
            isDstActive = isDst,
            timeZoneId = timeZone.id,
            isTimeZoneVerified = tzVerified,
            isFromCache = false
        )
    }

    fun computePrayersForDate(
        cal: Calendar,
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        madhab: AsrMadhab = AsrMadhab.SHAFI,
        offsets: PrayerMinuteOffsets = PrayerMinuteOffsets()
    ): List<PrayerTimeEntry> {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dayNoonCal = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tzOffsetHours = cal.timeZone.getOffset(dayNoonCal.timeInMillis) / 3_600_000.0

        val jd = julianDate(year, month, day) - longitude / 360.0
        val sun = sunPosition(jd)

        val dhuhrBaseHours = fixHour(12.0 - sun.equationOfTime) + tzOffsetHours - longitude / 15.0
        val dhuhrHours = dhuhrBaseHours + (method.dhuhrOffsetMin + offsets.dhuhr) / 60.0

        val sunriseDiff = sunAngleTimeDiff(0.833, latitude, sun.declination)
        val sunriseHours = dhuhrBaseHours - sunriseDiff + offsets.sunrise / 60.0
        val sunsetHours = dhuhrBaseHours + sunriseDiff

        val fajrDiff = sunAngleTimeDiff(method.fajrAngle, latitude, sun.declination)
        val fajrHours = dhuhrBaseHours - fajrDiff + (method.fajrOffsetMin + offsets.fajr) / 60.0

        val asrAlt = Math.toDegrees(
            atan2(1.0, madhab.shadowFactor + tan(Math.toRadians(kotlin.math.abs(latitude - sun.declination))))
        )
        val asrCos = (sin(Math.toRadians(asrAlt)) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(sun.declination))) /
            (cos(Math.toRadians(latitude)) * cos(Math.toRadians(sun.declination)))
        val asrDiff = Math.toDegrees(acos(asrCos.coerceIn(-0.9999, 0.9999))) / 15.0
        val asrHours = dhuhrBaseHours + asrDiff + offsets.asr / 60.0

        val maghribHours = sunsetHours + (method.maghribOffsetMin + offsets.maghrib) / 60.0

        val ishaHours = if (method.ishaMinutesAfterMaghrib != null) {
            maghribHours + (method.ishaMinutesAfterMaghrib + offsets.isha) / 60.0
        } else {
            val ishaDiff = sunAngleTimeDiff(method.ishaAngle, latitude, sun.declination)
            dhuhrBaseHours + ishaDiff + (method.ishaOffsetMin + offsets.isha) / 60.0
        }

        val dayStartCal = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStartMillis = dayStartCal.timeInMillis

        fun toEntry(type: PrayerType, hours: Double): PrayerTimeEntry {
            val clampedHours = hours.coerceIn(0.0, 23.99)
            val totalMinutes = kotlin.math.round(clampedHours * 60.0).toInt().coerceIn(0, 1439)
            val h = totalMinutes / 60
            val m = totalMinutes % 60
            val millis = dayStartMillis + totalMinutes * 60_000L
            return PrayerTimeEntry(type, millis, String.format(Locale.US, "%02d:%02d", h, m))
        }

        return listOf(
            toEntry(PrayerType.FAJR, fajrHours),
            toEntry(PrayerType.SUNRISE, sunriseHours),
            toEntry(PrayerType.DHUHR, dhuhrHours),
            toEntry(PrayerType.ASR, asrHours),
            toEntry(PrayerType.MAGHRIB, maghribHours),
            toEntry(PrayerType.ISHA, ishaHours)
        )
    }

    fun serializeSchedule(schedule: DailyPrayerSchedule): String =
        schedule.prayers.joinToString(";") { "${it.type.key}|${it.timeMillis}|${it.formattedTime}" }

    fun parseCachedSchedule(
        serialized: String,
        timeZoneId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): DailyPrayerSchedule? {
        if (serialized.isBlank()) return null
        val entries = serialized.split(";").mapNotNull { token ->
            val parts = token.split("|")
            if (parts.size != 3) return@mapNotNull null
            val type = PrayerType.entries.firstOrNull { it.key == parts[0] } ?: return@mapNotNull null
            val millis = parts[1].toLongOrNull() ?: return@mapNotNull null
            val formatted = parts[2]
            PrayerTimeEntry(type, millis, formatted)
        }
        if (entries.size != 6) return null
        val next = entries.firstOrNull { it.timeMillis > nowMillis } ?: entries.first().let {
            it.copy(timeMillis = it.timeMillis + 86_400_000L)
        }
        val current = entries.lastOrNull { it.timeMillis <= nowMillis }
        val tz = TimeZone.getTimeZone(timeZoneId.ifBlank { TimeZone.getDefault().id })
        return DailyPrayerSchedule(
            prayers = entries,
            nextPrayer = next,
            remainingMillis = (next.timeMillis - nowMillis).coerceAtLeast(0L),
            currentPrayer = current,
            isDstActive = tz.inDaylightTime(Date(nowMillis)),
            timeZoneId = tz.id,
            isTimeZoneVerified = true,
            isFromCache = true
        )
    }

    private data class SunPosition(val declination: Double, val equationOfTime: Double)

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): SunPosition {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        return SunPosition(decl, eqt)
    }

    private fun sunAngleTimeDiff(angle: Double, latitude: Double, declination: Double): Double {
        val latRad = Math.toRadians(latitude)
        val declRad = Math.toRadians(declination)
        val cosVal = (-sin(Math.toRadians(angle)) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val safeCos = cosVal.coerceIn(-0.9999, 0.9999)
        return Math.toDegrees(acos(safeCos)) / 15.0
    }

    private fun fixAngle(a: Double): Double = a - 360.0 * floor(a / 360.0)
    private fun fixHour(h: Double): Double = h - 24.0 * floor(h / 24.0)
}
