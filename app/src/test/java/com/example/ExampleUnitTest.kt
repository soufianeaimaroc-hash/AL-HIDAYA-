package com.example

import com.example.data.seed.AdhanCatalog
import com.example.data.seed.HadithSeedData
import com.example.data.seed.QuranCatalog
import com.example.prayer.CalculationMethod
import com.example.prayer.PrayerTimeCalculator
import com.example.prayer.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun `morocco calculation produces strictly ordered daily prayers for casablanca`() {
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Casablanca")).apply {
            set(2026, Calendar.MAY, 15, 12, 0, 0)
        }
        val prayers = PrayerTimeCalculator.computePrayersForDate(
            cal = cal,
            latitude = 33.5731,
            longitude = -7.5898,
            method = CalculationMethod.MOROCCO
        )
        assertEquals(6, prayers.size)
        for (i in 0 until prayers.lastIndex) {
            assertTrue(
                "Expected ${prayers[i].type} before ${prayers[i + 1].type}",
                prayers[i].timeMillis < prayers[i + 1].timeMillis
            )
        }
    }

    @Test
    fun `umm al qura sets isha 90 minutes after maghrib`() {
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Riyadh")).apply {
            set(2026, Calendar.MARCH, 20, 12, 0, 0)
        }
        val prayers = PrayerTimeCalculator.computePrayersForDate(
            cal = cal,
            latitude = 21.4225,
            longitude = 39.8262,
            method = CalculationMethod.UMM_AL_QURA
        )
        val maghrib = prayers.first { it.type == PrayerType.MAGHRIB }
        val isha = prayers.first { it.type == PrayerType.ISHA }
        assertEquals(90L * 60_000L, isha.timeMillis - maghrib.timeMillis)
    }

    @Test
    fun `qibla bearing calculation matches geographical direction`() {
        val casablancaBearing = PrayerTimeCalculator.calculateQiblaBearing(33.5731, -7.5898)
        assertTrue("Casablanca Qibla ($casablancaBearing) should be East ~94 deg", casablancaBearing in 90.0..98.0)

        val newYorkBearing = PrayerTimeCalculator.calculateQiblaBearing(40.7128, -74.0060)
        assertTrue("New York Qibla ($newYorkBearing) should be ENE ~58.5 deg", newYorkBearing in 55.0..62.0)
    }

    @Test
    fun `quran catalog contains all 114 surahs 30 juz and 60 hizb`() {
        assertEquals(114, QuranCatalog.surahs.size)
        assertEquals((1..114).toList(), QuranCatalog.surahs.map { it.number })
        assertEquals(6236, QuranCatalog.surahs.sumOf { it.ayahCount })
        assertEquals(30, QuranCatalog.juzList.size)
        assertEquals(60, QuranCatalog.hizbList.size)
    }

    @Test
    fun `hadith and adhan catalogs cover all required canonical books and tracks`() {
        val bookIds = HadithSeedData.books.map { it.id }.toSet()
        assertEquals(setOf("bukhari", "muslim", "abudawud", "tirmidhi", "nasai", "ibnmajah"), bookIds)
        assertTrue(HadithSeedData.starterHadiths.isNotEmpty())
        assertTrue(AdhanCatalog.tracks.size >= 6)
        assertTrue(com.example.data.seed.RuqyahCatalog.audioTracks.isNotEmpty())
        assertTrue(com.example.data.seed.RuqyahCatalog.ruqyahItems.isNotEmpty())
        assertEquals(com.example.audio.RepeatMode.CONTINUOUS, com.example.audio.AudioPlaybackState().repeatMode)
    }

    @Test
    fun `full quran queue orders surah 1 to 114 sequentially without duplicates`() {
        val normalized = com.example.data.QuranAndHadithRepository.normalizeHttpsUrl("http://server11.mp3quran.net/a_mousa/")
        assertEquals("https://server11.mp3quran.net/a_mousa", normalized)

        val queue = com.example.data.QuranAndHadithRepository.buildSurahQueueList(
            startSurah = 1,
            serverUrl = "http://server11.mp3quran.net/a_mousa/",
            localFiles = mapOf("surah_001_abdullah_mousa" to "/tmp/001.mp3")
        )
        assertEquals(114, queue.size)
        assertEquals((1..114).toList(), queue.map { it.first })
        assertEquals(114, queue.map { it.first }.distinct().size)
        assertEquals("/tmp/001.mp3", queue.first().second)
        assertEquals("https://server11.mp3quran.net/a_mousa/114.mp3", queue.last().second)
    }

    @Test
    fun `ayah repeat count logic stops at exact target and never repeats in continuous mode`() {
        assertEquals(
            false,
            com.example.audio.AudioPlayerController.shouldReplayAyahOnCompletion(
                com.example.audio.RepeatMode.CONTINUOUS,
                ayahRepeatTarget = 3,
                ayahRepeatDoneSoFar = 0
            )
        )
        assertEquals(
            true,
            com.example.audio.AudioPlayerController.shouldReplayAyahOnCompletion(
                com.example.audio.RepeatMode.REPEAT_AYAH,
                ayahRepeatTarget = 3,
                ayahRepeatDoneSoFar = 0
            )
        )
        assertEquals(
            true,
            com.example.audio.AudioPlayerController.shouldReplayAyahOnCompletion(
                com.example.audio.RepeatMode.REPEAT_AYAH,
                ayahRepeatTarget = 3,
                ayahRepeatDoneSoFar = 1
            )
        )
        assertEquals(
            false,
            com.example.audio.AudioPlayerController.shouldReplayAyahOnCompletion(
                com.example.audio.RepeatMode.REPEAT_AYAH,
                ayahRepeatTarget = 3,
                ayahRepeatDoneSoFar = 2
            )
        )
        assertEquals(
            true,
            com.example.audio.AudioPlayerController.shouldReplayAyahOnCompletion(
                com.example.audio.RepeatMode.REPEAT_AYAH,
                ayahRepeatTarget = 0,
                ayahRepeatDoneSoFar = 99
            )
        )
    }

    @Test
    fun `mushaf page image url formats 1 to 604 and preserves existing tafsir on page merge`() {
        assertEquals(
            "https://android.quran.com/data/width_1024/page001.png",
            com.example.data.QuranAndHadithRepository.mushafPageImageUrl(1)
        )
        assertEquals(
            "https://android.quran.com/data/width_1024/page049.png",
            com.example.data.QuranAndHadithRepository.mushafPageImageUrl(49)
        )
        assertEquals(
            "https://android.quran.com/data/width_1024/page604.png",
            com.example.data.QuranAndHadithRepository.mushafPageImageUrl(604)
        )
        assertEquals(
            "https://android.quran.com/data/width_1024/page604.png",
            com.example.data.QuranAndHadithRepository.mushafPageImageUrl(999)
        )

        val starterPage1 = QuranCatalog.offlineStarterAyahs.filter { it.page == 1 }
        val starterPage604 = QuranCatalog.offlineStarterAyahs.filter { it.page == 604 }
        assertEquals(7, starterPage1.size)
        assertEquals(15, starterPage604.size)

        val incoming = listOf(
            com.example.data.local.CachedSurahAyahEntity("1_1", 1, 1, 1, 1, 1, 1, "بِسْمِ اللَّهِ", "", "")
        )
        val merged = com.example.data.QuranAndHadithRepository.mergePreservingTextAndTafsir(
            incoming = incoming,
            existingByKey = starterPage1.associateBy { it.globalKey }
        )
        assertTrue(merged.first().englishTranslation.isNotBlank())
        assertTrue(merged.first().arabicTafsir.isNotBlank())
    }

    @Test
    fun `moshi parses nullable and empty quran and mp3quran json payloads without JsonDataException`() {
        val moshi = com.example.data.remote.NetworkModule.moshi

        val surahListAdapter = moshi.adapter(com.example.data.remote.AlQuranSurahListResponse::class.java)
        val parsedList = surahListAdapter.fromJson(
            """{"code":200,"status":"OK","data":[{"number":1,"name":null,"englishName":"Al-Faatiha","numberOfAyahs":7,"revelationType":"Meccan"}]}"""
        )
        assertEquals(200, parsedList?.code)
        assertEquals(1, parsedList?.data?.size)
        val normalizedCatalog = com.example.data.QuranAndHadithRepository.parseSurahListFromApiDtos(
            dtos = parsedList?.data,
            fallbackCatalog = QuranCatalog.surahs
        )
        assertEquals(114, normalizedCatalog.size)
        assertEquals("الفاتحة", normalizedCatalog.first().arabicName)
        assertEquals(7, normalizedCatalog.first().ayahCount)

        val emptyErrorResp = surahListAdapter.fromJson("""{"code":500,"status":"FAIL","data":null}""")
        assertEquals(500, emptyErrorResp?.code)
        assertTrue(
            com.example.data.QuranAndHadithRepository.parseSurahListFromApiDtos(emptyErrorResp?.data).isEmpty()
        )

        val editionsAdapter = moshi.adapter(com.example.data.remote.AlQuranEditionsResponse::class.java)
        val editionsNullData = editionsAdapter.fromJson("""{"code":404,"status":"NOT FOUND","data":null}""")
        assertEquals(404, editionsNullData?.code)
        assertEquals(null, editionsNullData?.data)

        val recitersAdapter = moshi.adapter(com.example.data.remote.Mp3QuranRecitersResponse::class.java)
        val recitersResp = recitersAdapter.fromJson(
            """{"reciters":[{"id":99,"name":"عبدالله بن موسى","moshaf":[{"id":1,"name":null,"server":"http://server.example.com/mousa/","surah_total":null,"surah_list":"1,2,114"}]}]}"""
        )
        assertEquals(1, recitersResp?.reciters?.size)
        val parsedSet = com.example.data.QuranAndHadithRepository.parseAvailableSurahList(
            recitersResp?.reciters?.first()?.moshaf?.first()?.surahList
        )
        assertEquals(setOf(1, 2, 114), parsedSet)
        val queue = com.example.data.QuranAndHadithRepository.buildSurahQueueList(
            startSurah = 1,
            serverUrl = recitersResp?.reciters?.first()?.moshaf?.first()?.server,
            localFiles = emptyMap(),
            availableSurahs = parsedSet
        )
        assertEquals(listOf(1, 2, 114), queue.map { it.first })
    }
}

