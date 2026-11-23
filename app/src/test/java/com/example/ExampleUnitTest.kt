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

    @Test
    fun `ruqyah catalog covers all 10 required categories with verified sources and valid global ayahs`() {
        val categories = com.example.data.seed.RuqyahCategory.entries
        assertEquals(10, categories.size)
        categories.forEach { category ->
            val tracks = com.example.data.seed.RuqyahCatalog.audioTracks.filter { it.category == category }
            assertTrue("Category ${category.id} must have audio tracks", tracks.isNotEmpty())
        }

        com.example.data.seed.RuqyahCatalog.audioTracks.forEach { track ->
            assertTrue(track.titleAr.isNotBlank())
            assertTrue(track.titleEn.isNotBlank())
            assertTrue(track.reciterAr.isNotBlank())
            assertTrue(track.sourceAttributionAr.isNotBlank())
            assertTrue(track.sourceAttributionEn.isNotBlank())
            assertTrue(track.associatedTexts.isNotEmpty())
        }

        assertEquals(1, QuranCatalog.globalAyahNumber(1, 1))
        assertEquals(262, QuranCatalog.globalAyahNumber(2, 255))
        assertEquals(6236, QuranCatalog.globalAyahNumber(114, 6))
        assertEquals(
            "https://cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3",
            com.example.data.QuranAndHadithRepository.buildIslamicNetworkAyahAudioUrl("ar.alafasy", 262)
        )
    }

    @Test
    fun `archive metadata parser skips appledouble files returns https url and seek clamps bounds`() {
        val moshi = com.example.data.remote.NetworkModule.moshi
        val adapter = moshi.adapter(com.example.data.remote.ArchiveMetadataResponse::class.java)
        val parsed = adapter.fromJson(
            """{"d1":"ia800100.us.archive.org","dir":"/12/items/MuaiqlyRuqyah","files":[{"name":"__MACOSX/._Ruqyah.mp3","format":"VBR MP3"},{"name":"._Ruqyah.mp3","format":"VBR MP3"},{"name":"Ruqyah Muaiqly.mp3","format":"VBR MP3"}]}"""
        )!!
        val selectedUrl = com.example.data.QuranAndHadithRepository.selectPreferredArchiveMp3Url(parsed)
        assertEquals("https://ia800100.us.archive.org/12/items/MuaiqlyRuqyah/Ruqyah%20Muaiqly.mp3", selectedUrl)

        assertEquals(0L, com.example.audio.AudioPlayerController.computeSeekTarget(5_000L, -15_000L, 120_000L))
        assertEquals(120_000L, com.example.audio.AudioPlayerController.computeSeekTarget(112_000L, 15_000L, 120_000L))
        assertEquals(45_000L, com.example.audio.AudioPlayerController.computeSeekTarget(30_000L, 15_000L, 120_000L))
    }

    @Test
    fun `asr hanafi madhab is later than shafi and manual offsets shift prayer times accurately`() {
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Casablanca")).apply {
            set(2026, Calendar.JUNE, 21, 12, 0, 0)
        }
        val shafi = PrayerTimeCalculator.computePrayersForDate(
            cal = cal,
            latitude = 33.5731,
            longitude = -7.5898,
            method = CalculationMethod.MOROCCO,
            madhab = com.example.prayer.AsrMadhab.SHAFI
        )
        val hanafi = PrayerTimeCalculator.computePrayersForDate(
            cal = cal,
            latitude = 33.5731,
            longitude = -7.5898,
            method = CalculationMethod.MOROCCO,
            madhab = com.example.prayer.AsrMadhab.HANAFI
        )
        val shafiAsr = shafi.first { it.type == PrayerType.ASR }.timeMillis
        val hanafiAsr = hanafi.first { it.type == PrayerType.ASR }.timeMillis
        val diffMinutes = (hanafiAsr - shafiAsr) / 60_000L
        assertTrue("Hanafi Asr should be 45-85 min later than Shafi (was $diffMinutes min)", diffMinutes in 45L..85L)

        val offsetted = PrayerTimeCalculator.computePrayersForDate(
            cal = cal,
            latitude = 33.5731,
            longitude = -7.5898,
            method = CalculationMethod.MOROCCO,
            madhab = com.example.prayer.AsrMadhab.SHAFI,
            offsets = com.example.prayer.PrayerMinuteOffsets(fajr = 5, maghrib = -3)
        )
        assertEquals(
            shafi.first { it.type == PrayerType.FAJR }.timeMillis + 5 * 60_000L,
            offsetted.first { it.type == PrayerType.FAJR }.timeMillis
        )
        assertEquals(
            shafi.first { it.type == PrayerType.MAGHRIB }.timeMillis - 3 * 60_000L,
            offsetted.first { it.type == PrayerType.MAGHRIB }.timeMillis
        )
    }

    @Test
    fun `timezone verification offline city fallback movement threshold and schedule cache work accurately`() {
        assertTrue(
            com.example.data.seed.CitiesCatalog.isTimeZoneConsistentWithLongitude(
                java.util.TimeZone.getTimeZone("Africa/Casablanca"),
                -7.5898
            )
        )
        assertEquals(
            false,
            com.example.data.seed.CitiesCatalog.isTimeZoneConsistentWithLongitude(
                java.util.TimeZone.getTimeZone("America/New_York"),
                39.8262
            )
        )
        assertEquals(
            "MOROCCO",
            com.example.data.seed.CitiesCatalog.suggestedMethodForCountry("MA", 33.5731, -7.5898)
        )
        assertEquals(
            "UMM_AL_QURA",
            com.example.data.seed.CitiesCatalog.suggestedMethodForCountry("SA", 21.4225, 39.8262)
        )

        val nearest = com.example.data.seed.CitiesCatalog.findNearestCity(33.59, -7.61, 30.0)
        assertEquals("Casablanca", nearest?.nameEn)

        val rabatLoc = com.example.prayer.DetectedLocation(
            cityAr = "الرباط",
            cityEn = "Rabat",
            latitude = 34.0209,
            longitude = -6.8416
        )
        assertTrue(
            com.example.prayer.LocationHelper.hasMovedSignificantly(
                prevLat = 33.5731,
                prevLon = -7.5898,
                prevCityEn = "Casablanca",
                next = rabatLoc
            )
        )
        assertEquals(false, com.example.prayer.LocationHelper.isValidCoordinate(0.0, 0.0))
        assertEquals(false, com.example.prayer.LocationHelper.isValidCoordinate(95.0, 10.0))
        assertTrue(com.example.prayer.LocationHelper.isValidCoordinate(33.5731, -7.5898))

        val schedule = PrayerTimeCalculator.calculateDailySchedule(
            latitude = 33.5731,
            longitude = -7.5898,
            method = CalculationMethod.MOROCCO,
            timeZone = java.util.TimeZone.getTimeZone("Africa/Casablanca")
        )
        val serialized = PrayerTimeCalculator.serializeSchedule(schedule)
        val restored = PrayerTimeCalculator.parseCachedSchedule(serialized, "Africa/Casablanca")
        assertEquals(6, restored?.prayers?.size)
        assertEquals(true, restored?.isFromCache)
    }

    @Test
    fun `uthmani text cleaner strips BOM and duplicate bismillah while preserving fatihah and tawbah`() {
        val fatihah1 = com.example.data.seed.BundledQuranLoader.cleanUthmaniAyahText(
            1, 1, "\uFEFFبِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
        )
        assertEquals("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", fatihah1)

        val baqarah1 = com.example.data.seed.BundledQuranLoader.cleanUthmaniAyahText(
            2, 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ الٓمٓ"
        )
        assertEquals("الٓمٓ", baqarah1)

        val teen1 = com.example.data.seed.BundledQuranLoader.cleanUthmaniAyahText(
            95, 1, "بِّسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ وَٱلتِّينِ وَٱلزَّيْتُونِ"
        )
        assertEquals("وَٱلتِّينِ وَٱلزَّيْتُونِ", teen1)

        val tawbah1 = com.example.data.seed.BundledQuranLoader.cleanUthmaniAyahText(
            9, 1, "بَرَآءَةٌۭ مِّنَ ٱللَّهِ"
        )
        assertEquals("بَرَآءَةٌۭ مِّنَ ٱللَّهِ", tawbah1)
    }

    @Test
    fun `moshi parses real alquran editions and mushaf page json payloads accurately`() {
        val moshi = com.example.data.remote.NetworkModule.moshi
        val editionsAdapter = moshi.adapter(com.example.data.remote.AlQuranEditionsResponse::class.java)
        val editionsJson = """
            {
              "code": 200,
              "status": "OK",
              "data": [
                {"number": 1, "name": "سُورَةُ ٱلْفَاتِحَةِ", "englishName": "Al-Faatiha", "ayahs": [{"number": 1, "text": "\ufeffبِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "numberInSurah": 1, "juz": 1, "page": 1, "hizbQuarter": 1}]},
                {"number": 1, "name": "سُورَةُ ٱلْفَاتِحَةِ", "englishName": "Al-Faatiha", "ayahs": [{"number": 1, "text": "In the name of Allah", "numberInSurah": 1, "juz": 1, "page": 1, "hizbQuarter": 1}]},
                {"number": 1, "name": "سُورَةُ ٱلْفَاتِحَةِ", "englishName": "Al-Faatiha", "ayahs": [{"number": 1, "text": "تفسير البسملة", "numberInSurah": 1, "juz": 1, "page": 1, "hizbQuarter": 1}]}
              ]
            }
        """.trimIndent()
        val parsedEditions = editionsAdapter.fromJson(editionsJson)!!
        assertEquals(200, parsedEditions.code)
        assertEquals(3, parsedEditions.data?.size)
        assertEquals(1, parsedEditions.data?.get(0)?.ayahs?.first()?.numberInSurah)
        assertEquals("تفسير البسملة", parsedEditions.data?.get(2)?.ayahs?.first()?.text)

        val pageAdapter = moshi.adapter(com.example.data.remote.AlQuranPageResponse::class.java)
        val pageJson = """
            {
              "code": 200,
              "status": "OK",
              "data": {
                "number": 1,
                "ayahs": [
                  {
                    "number": 1,
                    "text": "\ufeffبِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                    "surah": {"number": 1, "name": "سُورَةُ ٱلْفَاتِحَةِ", "englishName": "Al-Faatiha"},
                    "numberInSurah": 1,
                    "juz": 1,
                    "hizbQuarter": 1,
                    "page": 1
                  }
                ]
              }
            }
        """.trimIndent()
        val parsedPage = pageAdapter.fromJson(pageJson)!!
        assertEquals(200, parsedPage.code)
        assertEquals(1, parsedPage.data?.number)
        assertEquals(1, parsedPage.data?.ayahs?.first()?.surah?.number)
        assertEquals(1, parsedPage.data?.ayahs?.first()?.numberInSurah)
    }
}



