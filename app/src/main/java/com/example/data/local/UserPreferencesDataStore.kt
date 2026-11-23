package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "soufian_islam_prefs")

data class UserPrefsState(
    val language: String = "ar",
    val themeMode: String = "system",
    val cityName: String = "الدار البيضاء",
    val cityNameEn: String = "Casablanca",
    val regionNameAr: String = "",
    val regionNameEn: String = "",
    val countryNameAr: String = "المغرب",
    val countryNameEn: String = "Morocco",
    val countryCode: String = "MA",
    val timeZoneId: String = "Africa/Casablanca",
    val locationSource: String = "DEFAULT",
    val gpsConsentGranted: Boolean = false,
    val lastGpsCheckMillis: Long = 0L,
    val latitude: Double = 33.5731,
    val longitude: Double = -7.5898,
    val calculationMethod: String = "MOROCCO",
    val asrMadhab: String = "SHAFI",
    val offsetFajr: Int = 0,
    val offsetSunrise: Int = 0,
    val offsetDhuhr: Int = 0,
    val offsetAsr: Int = 0,
    val offsetMaghrib: Int = 0,
    val offsetIsha: Int = 0,
    val quranFontSize: Int = 24,
    val lastSurahNumber: Int = 1,
    val lastAyahNumber: Int = 1,
    val lastSurahName: String = "الفاتحة",
    val tasbihTotalCount: Int = 0,
    val tasbihTarget: Int = 33,
    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val prePrayerReminderEnabled: Boolean = true,
    val playAdhanOnNotification: Boolean = true,
    val fajrAdhanId: String = "adhan_madinah",
    val dhuhrAdhanId: String = "adhan_makkah",
    val asrAdhanId: String = "adhan_makkah",
    val maghribAdhanId: String = "adhan_alaqsa",
    val ishaAdhanId: String = "adhan_cairo",
    val reciterServerUrl: String = "",
    val lastRuqyahTrackId: String = "",
    val lastRuqyahTitle: String = "",
    val lastRuqyahReciter: String = "",
    val lastRuqyahPositionMs: Long = 0L,
    val ruqyahReciterEdition: String = "ar.alafasy",
    val cachedScheduleDateKey: String = "",
    val cachedScheduleJson: String = ""
) {
    fun toPrayerMinuteOffsets(): com.example.prayer.PrayerMinuteOffsets =
        com.example.prayer.PrayerMinuteOffsets(
            fajr = offsetFajr,
            sunrise = offsetSunrise,
            dhuhr = offsetDhuhr,
            asr = offsetAsr,
            maghrib = offsetMaghrib,
            isha = offsetIsha
        )
}

class UserPreferencesDataStore(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CITY_NAME = stringPreferencesKey("city_name")
        val CITY_NAME_EN = stringPreferencesKey("city_name_en")
        val REGION_AR = stringPreferencesKey("region_name_ar")
        val REGION_EN = stringPreferencesKey("region_name_en")
        val COUNTRY_AR = stringPreferencesKey("country_name_ar")
        val COUNTRY_EN = stringPreferencesKey("country_name_en")
        val COUNTRY_CODE = stringPreferencesKey("country_code")
        val TIME_ZONE_ID = stringPreferencesKey("time_zone_id")
        val LOCATION_SOURCE = stringPreferencesKey("location_source")
        val GPS_CONSENT_GRANTED = booleanPreferencesKey("gps_consent_granted")
        val LAST_GPS_CHECK_MS = longPreferencesKey("last_gps_check_ms")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CALC_METHOD = stringPreferencesKey("calculation_method")
        val ASR_MADHAB = stringPreferencesKey("asr_madhab")
        val OFFSET_FAJR = intPreferencesKey("offset_fajr")
        val OFFSET_SUNRISE = intPreferencesKey("offset_sunrise")
        val OFFSET_DHUHR = intPreferencesKey("offset_dhuhr")
        val OFFSET_ASR = intPreferencesKey("offset_asr")
        val OFFSET_MAGHRIB = intPreferencesKey("offset_maghrib")
        val OFFSET_ISHA = intPreferencesKey("offset_isha")
        val QURAN_FONT_SIZE = intPreferencesKey("quran_font_size")
        val LAST_SURAH_NUM = intPreferencesKey("last_surah_num")
        val LAST_AYAH_NUM = intPreferencesKey("last_ayah_num")
        val LAST_SURAH_NAME = stringPreferencesKey("last_surah_name")
        val TASBIH_TOTAL = intPreferencesKey("tasbih_total")
        val TASBIH_TARGET = intPreferencesKey("tasbih_target")
        val FAJR_ENABLED = booleanPreferencesKey("fajr_enabled")
        val DHUHR_ENABLED = booleanPreferencesKey("dhuhr_enabled")
        val ASR_ENABLED = booleanPreferencesKey("asr_enabled")
        val MAGHRIB_ENABLED = booleanPreferencesKey("maghrib_enabled")
        val ISHA_ENABLED = booleanPreferencesKey("isha_enabled")
        val PRE_REMINDER = booleanPreferencesKey("pre_reminder")
        val PLAY_ADHAN_ON_NOTIF = booleanPreferencesKey("play_adhan_on_notif")
        val FAJR_ADHAN = stringPreferencesKey("fajr_adhan")
        val DHUHR_ADHAN = stringPreferencesKey("dhuhr_adhan")
        val ASR_ADHAN = stringPreferencesKey("asr_adhan")
        val MAGHRIB_ADHAN = stringPreferencesKey("maghrib_adhan")
        val ISHA_ADHAN = stringPreferencesKey("isha_adhan")
        val RECITER_SERVER_URL = stringPreferencesKey("reciter_server_url")
        val LAST_RUQYAH_TRACK_ID = stringPreferencesKey("last_ruqyah_track_id")
        val LAST_RUQYAH_TITLE = stringPreferencesKey("last_ruqyah_title")
        val LAST_RUQYAH_RECITER = stringPreferencesKey("last_ruqyah_reciter")
        val LAST_RUQYAH_POS_MS = longPreferencesKey("last_ruqyah_pos_ms")
        val RUQYAH_RECITER_EDITION = stringPreferencesKey("ruqyah_reciter_edition")
        val CACHED_SCHEDULE_DATE = stringPreferencesKey("cached_schedule_date")
        val CACHED_SCHEDULE_JSON = stringPreferencesKey("cached_schedule_json")
    }

    val prefsFlow: Flow<UserPrefsState> = context.dataStore.data.map { prefs ->
        UserPrefsState(
            language = prefs[Keys.LANGUAGE] ?: "ar",
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            cityName = prefs[Keys.CITY_NAME] ?: "الدار البيضاء",
            cityNameEn = prefs[Keys.CITY_NAME_EN] ?: "Casablanca",
            regionNameAr = prefs[Keys.REGION_AR] ?: "",
            regionNameEn = prefs[Keys.REGION_EN] ?: "",
            countryNameAr = prefs[Keys.COUNTRY_AR] ?: "المغرب",
            countryNameEn = prefs[Keys.COUNTRY_EN] ?: "Morocco",
            countryCode = prefs[Keys.COUNTRY_CODE] ?: "MA",
            timeZoneId = prefs[Keys.TIME_ZONE_ID] ?: "Africa/Casablanca",
            locationSource = prefs[Keys.LOCATION_SOURCE] ?: "DEFAULT",
            gpsConsentGranted = prefs[Keys.GPS_CONSENT_GRANTED] ?: false,
            lastGpsCheckMillis = prefs[Keys.LAST_GPS_CHECK_MS] ?: 0L,
            latitude = prefs[Keys.LATITUDE] ?: 33.5731,
            longitude = prefs[Keys.LONGITUDE] ?: -7.5898,
            calculationMethod = prefs[Keys.CALC_METHOD] ?: "MOROCCO",
            asrMadhab = prefs[Keys.ASR_MADHAB] ?: "SHAFI",
            offsetFajr = prefs[Keys.OFFSET_FAJR] ?: 0,
            offsetSunrise = prefs[Keys.OFFSET_SUNRISE] ?: 0,
            offsetDhuhr = prefs[Keys.OFFSET_DHUHR] ?: 0,
            offsetAsr = prefs[Keys.OFFSET_ASR] ?: 0,
            offsetMaghrib = prefs[Keys.OFFSET_MAGHRIB] ?: 0,
            offsetIsha = prefs[Keys.OFFSET_ISHA] ?: 0,
            quranFontSize = prefs[Keys.QURAN_FONT_SIZE] ?: 24,
            lastSurahNumber = prefs[Keys.LAST_SURAH_NUM] ?: 1,
            lastAyahNumber = prefs[Keys.LAST_AYAH_NUM] ?: 1,
            lastSurahName = prefs[Keys.LAST_SURAH_NAME] ?: "الفاتحة",
            tasbihTotalCount = prefs[Keys.TASBIH_TOTAL] ?: 0,
            tasbihTarget = prefs[Keys.TASBIH_TARGET] ?: 33,
            fajrEnabled = prefs[Keys.FAJR_ENABLED] ?: true,
            dhuhrEnabled = prefs[Keys.DHUHR_ENABLED] ?: true,
            asrEnabled = prefs[Keys.ASR_ENABLED] ?: true,
            maghribEnabled = prefs[Keys.MAGHRIB_ENABLED] ?: true,
            ishaEnabled = prefs[Keys.ISHA_ENABLED] ?: true,
            prePrayerReminderEnabled = prefs[Keys.PRE_REMINDER] ?: true,
            playAdhanOnNotification = prefs[Keys.PLAY_ADHAN_ON_NOTIF] ?: true,
            fajrAdhanId = prefs[Keys.FAJR_ADHAN] ?: "adhan_madinah",
            dhuhrAdhanId = prefs[Keys.DHUHR_ADHAN] ?: "adhan_makkah",
            asrAdhanId = prefs[Keys.ASR_ADHAN] ?: "adhan_makkah",
            maghribAdhanId = prefs[Keys.MAGHRIB_ADHAN] ?: "adhan_alaqsa",
            ishaAdhanId = prefs[Keys.ISHA_ADHAN] ?: "adhan_cairo",
            reciterServerUrl = prefs[Keys.RECITER_SERVER_URL] ?: "",
            lastRuqyahTrackId = prefs[Keys.LAST_RUQYAH_TRACK_ID] ?: "",
            lastRuqyahTitle = prefs[Keys.LAST_RUQYAH_TITLE] ?: "",
            lastRuqyahReciter = prefs[Keys.LAST_RUQYAH_RECITER] ?: "",
            lastRuqyahPositionMs = prefs[Keys.LAST_RUQYAH_POS_MS] ?: 0L,
            ruqyahReciterEdition = prefs[Keys.RUQYAH_RECITER_EDITION] ?: "ar.alafasy",
            cachedScheduleDateKey = prefs[Keys.CACHED_SCHEDULE_DATE] ?: "",
            cachedScheduleJson = prefs[Keys.CACHED_SCHEDULE_JSON] ?: ""
        )
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setLocation(cityAr: String, cityEn: String, lat: Double, lon: Double) {
        context.dataStore.edit {
            it[Keys.CITY_NAME] = cityAr
            it[Keys.CITY_NAME_EN] = cityEn
            it[Keys.LATITUDE] = lat
            it[Keys.LONGITUDE] = lon
        }
    }

    suspend fun setRichLocation(
        cityAr: String,
        cityEn: String,
        regionAr: String,
        regionEn: String,
        countryAr: String,
        countryEn: String,
        countryCode: String,
        lat: Double,
        lon: Double,
        timeZoneId: String,
        locationSource: String,
        calculationMethod: String? = null
    ) {
        context.dataStore.edit {
            it[Keys.CITY_NAME] = cityAr
            it[Keys.CITY_NAME_EN] = cityEn
            it[Keys.REGION_AR] = regionAr
            it[Keys.REGION_EN] = regionEn
            it[Keys.COUNTRY_AR] = countryAr
            it[Keys.COUNTRY_EN] = countryEn
            it[Keys.COUNTRY_CODE] = countryCode
            it[Keys.LATITUDE] = lat
            it[Keys.LONGITUDE] = lon
            it[Keys.TIME_ZONE_ID] = timeZoneId
            it[Keys.LOCATION_SOURCE] = locationSource
            if (calculationMethod != null) {
                it[Keys.CALC_METHOD] = calculationMethod
            }
        }
    }

    suspend fun setGpsConsentGranted(granted: Boolean) {
        context.dataStore.edit { it[Keys.GPS_CONSENT_GRANTED] = granted }
    }

    suspend fun setLastGpsCheckMillis(millis: Long) {
        context.dataStore.edit { it[Keys.LAST_GPS_CHECK_MS] = millis }
    }

    suspend fun setCalculationMethod(method: String) {
        context.dataStore.edit { it[Keys.CALC_METHOD] = method }
    }

    suspend fun setAsrMadhab(madhab: String) {
        context.dataStore.edit { it[Keys.ASR_MADHAB] = madhab }
    }

    suspend fun setPrayerOffset(prayerKey: String, minutes: Int) {
        val clamped = minutes.coerceIn(-30, 30)
        context.dataStore.edit {
            when (prayerKey) {
                "FAJR" -> it[Keys.OFFSET_FAJR] = clamped
                "SUNRISE" -> it[Keys.OFFSET_SUNRISE] = clamped
                "DHUHR" -> it[Keys.OFFSET_DHUHR] = clamped
                "ASR" -> it[Keys.OFFSET_ASR] = clamped
                "MAGHRIB" -> it[Keys.OFFSET_MAGHRIB] = clamped
                "ISHA" -> it[Keys.OFFSET_ISHA] = clamped
            }
        }
    }

    suspend fun resetPrayerOffsets() {
        context.dataStore.edit {
            it[Keys.OFFSET_FAJR] = 0
            it[Keys.OFFSET_SUNRISE] = 0
            it[Keys.OFFSET_DHUHR] = 0
            it[Keys.OFFSET_ASR] = 0
            it[Keys.OFFSET_MAGHRIB] = 0
            it[Keys.OFFSET_ISHA] = 0
        }
    }

    suspend fun setPlayAdhanOnNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PLAY_ADHAN_ON_NOTIF] = enabled }
    }

    suspend fun saveCachedSchedule(dateKey: String, serializedJson: String) {
        context.dataStore.edit {
            it[Keys.CACHED_SCHEDULE_DATE] = dateKey
            it[Keys.CACHED_SCHEDULE_JSON] = serializedJson
        }
    }

    suspend fun setQuranFontSize(size: Int) {
        context.dataStore.edit { it[Keys.QURAN_FONT_SIZE] = size.coerceIn(16, 40) }
    }

    suspend fun setLastRead(surahNumber: Int, ayahNumber: Int, surahName: String) {
        context.dataStore.edit {
            it[Keys.LAST_SURAH_NUM] = surahNumber
            it[Keys.LAST_AYAH_NUM] = ayahNumber
            it[Keys.LAST_SURAH_NAME] = surahName
        }
    }

    suspend fun incrementTasbihTotal(delta: Int = 1) {
        context.dataStore.edit {
            val current = it[Keys.TASBIH_TOTAL] ?: 0
            it[Keys.TASBIH_TOTAL] = (current + delta).coerceAtLeast(0)
        }
    }

    suspend fun setTasbihTotal(total: Int) {
        context.dataStore.edit { it[Keys.TASBIH_TOTAL] = total.coerceAtLeast(0) }
    }

    suspend fun setTasbihTarget(target: Int) {
        context.dataStore.edit { it[Keys.TASBIH_TARGET] = target }
    }

    suspend fun setPrayerEnabled(prayerKey: String, enabled: Boolean) {
        context.dataStore.edit {
            when (prayerKey) {
                "FAJR" -> it[Keys.FAJR_ENABLED] = enabled
                "DHUHR" -> it[Keys.DHUHR_ENABLED] = enabled
                "ASR" -> it[Keys.ASR_ENABLED] = enabled
                "MAGHRIB" -> it[Keys.MAGHRIB_ENABLED] = enabled
                "ISHA" -> it[Keys.ISHA_ENABLED] = enabled
                "REMINDER" -> it[Keys.PRE_REMINDER] = enabled
            }
        }
    }

    suspend fun setPrayerAdhan(prayerKey: String, adhanId: String) {
        context.dataStore.edit {
            when (prayerKey) {
                "FAJR" -> it[Keys.FAJR_ADHAN] = adhanId
                "DHUHR" -> it[Keys.DHUHR_ADHAN] = adhanId
                "ASR" -> it[Keys.ASR_ADHAN] = adhanId
                "MAGHRIB" -> it[Keys.MAGHRIB_ADHAN] = adhanId
                "ISHA" -> it[Keys.ISHA_ADHAN] = adhanId
                "ALL" -> {
                    it[Keys.FAJR_ADHAN] = adhanId
                    it[Keys.DHUHR_ADHAN] = adhanId
                    it[Keys.ASR_ADHAN] = adhanId
                    it[Keys.MAGHRIB_ADHAN] = adhanId
                    it[Keys.ISHA_ADHAN] = adhanId
                }
            }
        }
    }

    suspend fun setReciterServerUrl(url: String) {
        context.dataStore.edit { it[Keys.RECITER_SERVER_URL] = url }
    }

    suspend fun setLastRuqyah(
        trackId: String,
        title: String,
        reciter: String,
        positionMs: Long = 0L
    ) {
        context.dataStore.edit {
            it[Keys.LAST_RUQYAH_TRACK_ID] = trackId
            it[Keys.LAST_RUQYAH_TITLE] = title
            it[Keys.LAST_RUQYAH_RECITER] = reciter
            it[Keys.LAST_RUQYAH_POS_MS] = positionMs.coerceAtLeast(0L)
        }
    }

    suspend fun setRuqyahReciterEdition(edition: String) {
        context.dataStore.edit { it[Keys.RUQYAH_RECITER_EDITION] = edition }
    }

    suspend fun applyCloudProfile(
        language: String,
        themeMode: String,
        cityName: String,
        lat: Double,
        lon: Double,
        calcMethod: String,
        fontSize: Int,
        lastSurah: Int,
        lastAyah: Int,
        lastSurahName: String,
        tasbihTotal: Int
    ) {
        context.dataStore.edit {
            it[Keys.LANGUAGE] = language
            it[Keys.THEME_MODE] = themeMode
            it[Keys.CITY_NAME] = cityName
            it[Keys.LATITUDE] = lat
            it[Keys.LONGITUDE] = lon
            it[Keys.CALC_METHOD] = calcMethod
            it[Keys.QURAN_FONT_SIZE] = fontSize.coerceIn(16, 40)
            it[Keys.LAST_SURAH_NUM] = lastSurah.coerceIn(1, 114)
            it[Keys.LAST_AYAH_NUM] = lastAyah.coerceAtLeast(1)
            it[Keys.LAST_SURAH_NAME] = lastSurahName
            it[Keys.TASBIH_TOTAL] = tasbihTotal.coerceAtLeast(0)
        }
    }
}
