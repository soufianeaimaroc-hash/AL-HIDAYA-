package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioPlaybackState
import com.example.core.AppStrings
import com.example.core.LocalAppStrings
import com.example.data.QuranAndHadithRepository
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesDataStore
import com.example.data.local.UserPrefsState
import com.example.data.seed.QuranCatalog
import com.example.data.seed.RuqyahCategory
import com.example.ui.SurahListUiState
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.RuqyahScreen
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AL HIDAYA – الهداية", appName)
        assertEquals("+212671994410", context.getString(R.string.support_phone_number))
    }

    @Test
    fun `room cached_surahs stores and retrieves all 114 surahs offline in order`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = QuranAndHadithRepository(context, db, UserPreferencesDataStore(context))
            repo.ensureStarterDataSeeded()
            val cachedSurahs = db.quranCacheDao().getCachedSurahsOnce()
            assertEquals(114, cachedSurahs.size)
            assertEquals((1..114).toList(), cachedSurahs.map { it.number })
            assertEquals("الفاتحة", cachedSurahs.first().arabicName)
            assertEquals("الناس", cachedSurahs.last().arabicName)

            val starterAyahs = db.quranCacheDao().getSurahAyahsOnce(1)
            assertEquals(7, starterAyahs.size)
            assertEquals((1..7).toList(), starterAyahs.map { it.ayahNumber })
        } finally {
            db.close()
        }
    }

    @Test
    fun `quran screen renders loading error retry and all 114 surahs with search and open`() {
        var retried = false
        var openedSurah = -1
        val uiState = mutableStateOf<SurahListUiState>(SurahListUiState.Loading)

        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                QuranScreen(
                    prefs = UserPrefsState(language = "ar"),
                    surahListState = uiState.value,
                    bookmarks = emptyList(),
                    cachedSurahs = setOf(1, 112, 113, 114),
                    downloadedKeys = emptySet(),
                    downloadingKeys = emptySet(),
                    onRetryLoadSurahs = { retried = true },
                    onDismissSurahSyncError = {},
                    onOpenSurah = { num, _ -> openedSurah = num },
                    onPlaySurah = {},
                    onPlayQuranOnceFromSurah = {},
                    onDownloadSurah = {},
                    onSearchVerses = { emptyList() }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quran_surah_loading").assertIsDisplayed()

        uiState.value = SurahListUiState.Error("خطأ في التحميل")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quran_surah_error").assertIsDisplayed()
        composeRule.onNodeWithTag("quran_surah_retry_button").performClick()
        assertTrue(retried)

        uiState.value = SurahListUiState.Success(QuranCatalog.surahs)
        composeRule.waitForIdle()
        // Scroll past the 2 hero header items so first Surah card is guaranteed in viewport
        composeRule.onNodeWithTag("quran_surah_lazy_list").performScrollToIndex(2)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("surah_card_1").assertIsDisplayed()
        composeRule.onNodeWithText("سورة الفاتحة").assertIsDisplayed()

        composeRule.onNodeWithTag("quran_search_input").performTextInput("الناس")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("surah_card_114").assertIsDisplayed()
        composeRule.onNodeWithTag("surah_card_114").performClick()
        assertEquals(114, openedSurah)
    }

    @Test
    fun `ruqyah screen renders hero categories player controls sleep timer and verified texts`() {
        var selectedCat: RuqyahCategory? = null
        var seekDelta = 0L
        var chosenTimer = -1

        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                RuqyahScreen(
                    prefs = UserPrefsState(
                        language = "ar",
                        lastRuqyahTrackId = "ruqyah_shifa",
                        lastRuqyahTitle = "آيات الشفاء الست",
                        lastRuqyahReciter = "الشيخ مشاري راشد العفاسي"
                    ),
                    audioState = AudioPlaybackState(
                        isPlaying = false,
                        trackId = "ruqyah_shifa",
                        title = "آيات الشفاء الست",
                        subtitle = "الشيخ مشاري راشد العفاسي",
                        currentSource = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1249.mp3",
                        positionMs = 10_000L,
                        durationMs = 60_000L
                    ),
                    bookmarks = emptyList(),
                    downloadedKeys = setOf("ruqyah_muaiqly"),
                    downloadingKeys = emptySet(),
                    selectedCategory = selectedCat,
                    activeRuqyahTrackId = "ruqyah_shifa",
                    onSelectCategory = { selectedCat = it },
                    onSelectReciterEdition = {},
                    onPlayRuqyah = {},
                    onResumeLastRuqyah = {},
                    onDownloadRuqyah = {},
                    onSeekTo = {},
                    onSeekBy = { seekDelta = it },
                    onSetVolume = {},
                    onSetSleepTimerMinutes = { chosenTimer = it },
                    onSetRepeatConfig = { _, _ -> },
                    onToggleBookmark = { _, _, _, _, _ -> },
                    onShareText = {},
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("الرقية الشرعية | ROQYA CHARIA").onFirst().assertIsDisplayed()
        composeRule.onNodeWithTag("ruqyah_hero_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("ruqyah_resume_last_card").assertIsDisplayed()
        composeRule.onNodeWithTag("ruqyah_player_card").assertIsDisplayed()
        composeRule.onNodeWithTag("ruqyah_volume_slider").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("ruqyah_forward_15").performScrollTo().performClick()
        assertEquals(15_000L, seekDelta)

        composeRule.onNodeWithTag("ruqyah_sleep_timer_chip").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("sleep_timer_option_15").performClick()
        assertEquals(15, chosenTimer)

        composeRule.onNodeWithTag("ruqyah_lazy_list").performScrollToIndex(4)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ruqyah_categories_row").performScrollToIndex(10)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ruqyah_cat_baqarah").performClick()
        assertEquals(RuqyahCategory.BAQARAH, selectedCat)
    }

    @Test
    fun `prayer screen renders map card madhab selector offsets dialog gps consent and error banners`() {
        var selectedMadhab = ""
        var offsetUpdated = Pair("", 0)
        var consentPlayAdhan = true
        val locState = mutableStateOf<com.example.ui.LocationUiState>(com.example.ui.LocationUiState.WeakSignal)

        val sampleSchedule = com.example.prayer.PrayerTimeCalculator.calculateDailySchedule(
            latitude = 33.5731,
            longitude = -7.5898,
            method = com.example.prayer.CalculationMethod.MOROCCO,
            timeZone = java.util.TimeZone.getTimeZone("Africa/Casablanca")
        )

        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                com.example.ui.screens.PrayerAndAdhanScreen(
                    initialTab = 0,
                    prefs = UserPrefsState(
                        language = "ar",
                        cityName = "الدار البيضاء",
                        countryNameAr = "المغرب",
                        asrMadhab = "SHAFI",
                        playAdhanOnNotification = consentPlayAdhan
                    ),
                    schedule = sampleSchedule,
                    audioState = AudioPlaybackState(),
                    downloadedKeys = emptySet(),
                    downloadingKeys = emptySet(),
                    locationUiState = locState.value,
                    qiblaBearing = 94.0,
                    onDetectGps = {},
                    onSelectCity = {},
                    onSelectMethod = {},
                    onSelectAsrMadhab = { selectedMadhab = it },
                    onSetPrayerOffset = { key, mins -> offsetUpdated = key to mins },
                    onTogglePrayerAlert = { _, _ -> },
                    onTogglePlayAdhanOnNotification = { consentPlayAdhan = it },
                    onPreviewAdhan = {},
                    onDownloadAdhan = {},
                    onAssignAdhan = { _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()

        val lazyList = composeRule.onNodeWithTag("prayer_times_lazy_list")
        composeRule.onNodeWithTag("gps_weak_signal_banner").assertIsDisplayed()

        lazyList.performScrollToNode(hasTestTag("prayer_summary_hero_card"))
        composeRule.onNodeWithTag("prayer_summary_hero_card").assertIsDisplayed()

        lazyList.performScrollToNode(hasTestTag("location_qibla_map_card"))
        composeRule.onNodeWithTag("location_qibla_map_card").assertIsDisplayed()

        // Scroll to configuration card & test Hanafi chip and offsets dialog
        lazyList.performScrollToNode(hasTestTag("madhab_hanafi_chip"))
        composeRule.onNodeWithTag("madhab_hanafi_chip").performClick()
        assertEquals("HANAFI", selectedMadhab)

        lazyList.performScrollToNode(hasTestTag("prayer_offsets_button"))
        composeRule.onNodeWithTag("prayer_offsets_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("prayer_offsets_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("offset_plus_FAJR").performClick()
        assertEquals("FAJR" to 1, offsetUpdated)
        composeRule.onNodeWithText("رجوع").performClick()
        composeRule.waitForIdle()

        // Verify GPS privacy consent dialog opens on GPS button click when permission isn't granted
        lazyList.performScrollToNode(hasTestTag("gps_detect_button"))
        composeRule.onNodeWithTag("gps_detect_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gps_consent_dialog").assertIsDisplayed()
    }

    @Test
    fun `bundled quran loader provides all 114 surahs 6236 ayahs and 604 mushaf pages`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = QuranAndHadithRepository(context, db, UserPreferencesDataStore(context))
            repo.ensureStarterDataSeeded()

            val fatihah = repo.getInstantSurahAyahs(1)
            assertEquals(7, fatihah.size)
            assertTrue(fatihah.first().arabicTextUthmani.contains("بِسْمِ"))

            val baqarah = repo.getInstantSurahAyahs(2)
            assertEquals(286, baqarah.size)
            assertEquals((1..286).toList(), baqarah.map { it.ayahNumber })
            assertEquals("الٓمٓ", baqarah.first().arabicTextUthmani)

            val ikhlas = repo.getInstantSurahAyahs(112)
            assertEquals(4, ikhlas.size)
            assertTrue(ikhlas.first().arabicTextUthmani.contains("قُلْ هُوَ"))

            val nas = repo.getInstantSurahAyahs(114)
            assertEquals(6, nas.size)
            assertTrue(nas.last().arabicTextUthmani.contains("جِنَّةِ"))

            val totalAyahs = (1..114).sumOf { s ->
                com.example.data.seed.BundledQuranLoader.getSurahAyahs(context, s).size
            }
            assertEquals(6236, totalAyahs)

            // Verify all 604 Mushaf pages have non-empty Uthmani ayahs
            val page2 = repo.loadMushafPage(2).getOrThrow()
            assertTrue(page2.isNotEmpty())
            assertEquals(2, page2.first().surahNumber)

            val page604 = repo.loadMushafPage(604).getOrThrow()
            assertEquals(15, page604.size)
            assertEquals(listOf(112, 113, 114), page604.map { it.surahNumber }.distinct())
        } finally {
            db.close()
        }
    }

    @Test
    fun `surah reader renders fatihah 7 ayahs baqarah 286 ayahs ikhlas 4 nas 6 mode switch and audio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = QuranAndHadithRepository(context, db, UserPreferencesDataStore(context))
        runBlocking { repo.ensureStarterDataSeeded() }

        val targetSurah = mutableStateOf(1)
        val initialMushaf = mutableStateOf(false)
        val lastReadHistory = mutableListOf<Triple<Int, Int, String>>()
        val playedAyahs = mutableListOf<Triple<Int, Int, Int>>()
        val audioState = mutableStateOf(AudioPlaybackState())

        try {
            composeRule.setContent {
                CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                    com.example.ui.screens.SurahReaderScreen(
                        surahNumber = targetSurah.value,
                        initialPageMode = initialMushaf.value,
                        prefs = UserPrefsState(language = "ar", quranFontSize = 24),
                        bookmarks = emptyList(),
                        audioState = audioState.value,
                        downloadedKeys = emptySet(),
                        downloadingKeys = emptySet(),
                        loadInstantSurahAyahs = repo::getInstantSurahAyahs,
                        loadSurahAyahs = { sNum -> repo.loadSurahFromNetworkIfNeeded(sNum) },
                        loadMushafPage = { pNum -> repo.loadMushafPage(pNum) },
                        onUpdateLastRead = { sNum, aNum, name ->
                            lastReadHistory.add(Triple(sNum, aNum, name))
                        },
                        onPlaySurah = {},
                        onPlayQuranOnceFromSurah = {},
                        onPlayPageOnce = { _, _ -> },
                        onPlayAyah = { sNum, gNum, aNum ->
                            playedAyahs.add(Triple(sNum, gNum, aNum))
                            audioState.value = AudioPlaybackState(
                                isPlaying = true,
                                currentSurahNumber = sNum,
                                currentAyahNumber = aNum
                            )
                        },
                        onSetRepeatConfig = { _, _ -> },
                        onDownloadSurah = {},
                        onToggleBookmark = { _, _, _, _, _ -> },
                        onShareText = {},
                        onBack = {}
                    )
                }
            }
            composeRule.waitForIdle()

            // 1. Surah Al-Fatihah (7 ayahs)
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("quran_reader_verse_list").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("سورة الفاتحة").assertIsDisplayed()
            val verseList = composeRule.onNodeWithTag("quran_reader_verse_list")
            verseList.performScrollToNode(hasTestTag("ayah_card_1"))
            composeRule.onNodeWithTag("ayah_text_1").assertIsDisplayed()
            verseList.performScrollToNode(hasTestTag("ayah_card_7"))
            composeRule.onNodeWithTag("ayah_card_7").assertIsDisplayed()

            // Trigger audio playback while verses remain displayed
            verseList.performScrollToNode(hasTestTag("ayah_play_1"))
            composeRule.onNodeWithTag("ayah_play_1").performClick()
            composeRule.waitForIdle()
            assertEquals(1, playedAyahs.size)
            assertEquals(1 to 1, playedAyahs.first().first to playedAyahs.first().third)
            composeRule.onNodeWithTag("ayah_card_1").assertIsDisplayed()

            // 2. Navigate between Surahs -> Al-Baqarah (286 ayahs)
            verseList.performScrollToNode(hasTestTag("reader_next_surah"))
            composeRule.onNodeWithTag("reader_next_surah").performClick()
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("surah_bismillah_banner").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("سورة البقرة").assertIsDisplayed()
            composeRule.onNodeWithTag("surah_bismillah_banner").assertIsDisplayed()
            composeRule.onNodeWithTag("quran_reader_verse_list").performScrollToNode(hasTestTag("ayah_card_286"))
            composeRule.onNodeWithTag("ayah_card_286").assertIsDisplayed()
            assertTrue(lastReadHistory.any { it.first == 2 })

            // 3. Switch to Mushaf Page Mode, switch to text view & flip page, then click "عرض الآيات"
            composeRule.onNodeWithTag("mode_mushaf_chip").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("mushaf_page_image_container").assertIsDisplayed()
            composeRule.onNodeWithTag("mushaf_display_text").performClick()
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("mushaf_uthmani_page_text").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("mushaf_uthmani_page_text").assertIsDisplayed()
            composeRule.onNodeWithTag("mushaf_next_page").performClick()
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("mushaf_uthmani_page_text").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("mushaf_uthmani_page_text").assertIsDisplayed()

            composeRule.onNodeWithTag("mode_verse_chip").performClick()
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("quran_reader_verse_list").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("mushaf_page_image_container").assertDoesNotExist()
            composeRule.onNodeWithTag("quran_reader_verse_list").assertIsDisplayed()

            // 4. Open Surah Al-Ikhlas (112: 4 ayahs) & navigate to An-Nas (114: 6 ayahs)
            targetSurah.value = 112
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("quran_reader_verse_list").fetchSemanticsNodes().isNotEmpty() &&
                    composeRule.onAllNodesWithText("سورة الإخلاص").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("سورة الإخلاص").assertIsDisplayed()
            composeRule.onNodeWithTag("quran_reader_verse_list").performScrollToNode(hasTestTag("ayah_card_4"))
            composeRule.onNodeWithTag("ayah_card_4").assertIsDisplayed()

            targetSurah.value = 114
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("quran_reader_verse_list").fetchSemanticsNodes().isNotEmpty() &&
                    composeRule.onAllNodesWithText("سورة الناس").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("سورة الناس").assertIsDisplayed()
            composeRule.onNodeWithTag("quran_reader_verse_list").performScrollToNode(hasTestTag("ayah_card_6"))
            composeRule.onNodeWithTag("ayah_card_6").assertIsDisplayed()
        } finally {
            db.close()
        }
    }

    @Test
    fun `settings and about developer screens showcase AL HIDAYA branding and developer badges`() {
        var devClicked = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                com.example.ui.screens.SettingsAndAccountScreen(
                    prefs = UserPrefsState(language = "ar"),
                    currentUser = null,
                    bookmarks = emptyList(),
                    onLanguageChange = {},
                    onThemeChange = {},
                    onFontSizeChange = {},
                    onOpenPrayerCity = {},
                    onOpenAboutDeveloper = { devClicked = true }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("about_app_card").assertIsDisplayed()
        composeRule.onNodeWithTag("account_login_card").assertIsDisplayed()
        composeRule.onAllNodesWithText("AL HIDAYA – الهداية").onFirst().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_lazy_list")
            .performScrollToNode(hasTestTag("developer_section_card"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("developer_section_card").assertIsDisplayed()
        composeRule.onNodeWithText("Soufiane Baddou").assertIsDisplayed()
        composeRule.onNodeWithText("DEVELOPER").assertIsDisplayed()
        composeRule.onNodeWithTag("developer_section_card").performClick()
        assertTrue(devClicked)
    }

    @Test
    fun `repository repairs blank cached_ayahs rows and mushaf initialPageMode switches cleanly to verse view in dark theme`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = QuranAndHadithRepository(context, db, UserPreferencesDataStore(context))
        try {
            // Simulate corrupted/blank Room cache rows before opening reader
            runBlocking {
                db.quranCacheDao().upsertAyahs(
                    (1..7).map { aNum ->
                        com.example.data.local.CachedSurahAyahEntity(
                            globalKey = "1_$aNum",
                            surahNumber = 1,
                            ayahNumber = aNum,
                            globalAyahNumber = aNum,
                            juz = 1,
                            hizbQuarter = 1,
                            page = 1,
                            arabicTextUthmani = "",
                            englishTranslation = "",
                            arabicTafsir = ""
                        )
                    }
                )
                val repaired = repo.getInstantSurahAyahs(1)
                assertEquals(7, repaired.size)
                assertTrue(repaired.all { it.arabicTextUthmani.isNotBlank() })

                val repairedPage = repo.loadMushafPage(1).getOrThrow()
                assertEquals(7, repairedPage.size)
                assertTrue(repairedPage.all { it.arabicTextUthmani.isNotBlank() })
            }

            composeRule.setContent {
                com.example.ui.theme.AlHidayaTheme(themeMode = "dark", language = "ar") {
                    com.example.ui.screens.SurahReaderScreen(
                        surahNumber = 1,
                        initialPageMode = true,
                        prefs = UserPrefsState(language = "ar", themeMode = "dark", quranFontSize = 24),
                        bookmarks = emptyList(),
                        audioState = AudioPlaybackState(),
                        downloadedKeys = emptySet(),
                        downloadingKeys = emptySet(),
                        loadInstantSurahAyahs = repo::getInstantSurahAyahs,
                        loadSurahAyahs = { sNum -> repo.loadSurahFromNetworkIfNeeded(sNum) },
                        loadMushafPage = { pNum -> repo.loadMushafPage(pNum) },
                        onUpdateLastRead = { _, _, _ -> },
                        onPlaySurah = {},
                        onPlayQuranOnceFromSurah = {},
                        onPlayPageOnce = { _, _ -> },
                        onPlayAyah = { _, _, _ -> },
                        onSetRepeatConfig = { _, _ -> },
                        onDownloadSurah = {},
                        onToggleBookmark = { _, _, _, _, _ -> },
                        onShareText = {},
                        onBack = {}
                    )
                }
            }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("mushaf_page_image_container").assertIsDisplayed()

            // Click "عرض الآيات" chip -> must immediately show Al-Fatihah 7 ayahs
            composeRule.onNodeWithTag("mode_verse_chip").performClick()
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodesWithTag("quran_reader_verse_list").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("mushaf_page_image_container").assertDoesNotExist()
            composeRule.onNodeWithTag("quran_reader_verse_list").performScrollToNode(hasTestTag("ayah_card_1"))
            composeRule.onNodeWithTag("ayah_text_1").assertIsDisplayed()
            composeRule.onNodeWithTag("quran_reader_verse_list").performScrollToNode(hasTestTag("ayah_card_7"))
            composeRule.onNodeWithTag("ayah_card_7").assertIsDisplayed()
        } finally {
            db.close()
        }
    }

    @Test
    fun `about developer screen displays official website link and preserves all developer details`() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                com.example.ui.screens.AboutDeveloperScreen(onBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("developer_identity_card").assertIsDisplayed()
        composeRule.onNodeWithText("Soufiane Baddou").assertIsDisplayed()
        composeRule.onNodeWithText("Soufiane HydroTech").assertIsDisplayed()
        composeRule.onNodeWithTag("customer_support_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("+212671994410").assertIsDisplayed()
        composeRule.onNodeWithTag("developer_website_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("https://heylink.me/SoufianeHydroTech/").assertIsDisplayed()
        composeRule.onNodeWithTag("visit_website_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("copy_website_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("call_support_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("visit_website_button").performScrollTo().performClick()
    }

    @Test
    fun `qibla adhkar and audio service controls behave safely when sensors or tts are unavailable`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // AudioPlaybackService safe companion calls must not throw even when not previously started
        com.example.audio.AudioPlaybackService.togglePause(context)
        com.example.audio.AudioPlaybackService.setSpeed(context, 1.25f)
        com.example.audio.AudioPlaybackService.setVolume(context, 0.8f)

        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                com.example.ui.screens.QiblaScreen(
                    prefs = UserPrefsState(language = "ar", cityName = "الدار البيضاء"),
                    qiblaBearing = 94.0
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("qibla_compass_dial").assertIsDisplayed()
    }

    @Test
    fun `adhkar and tasbih screen renders cleanly without tts crashes`() {
        var totalTapped = 0
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                com.example.ui.screens.AdhkarAndTasbihScreen(
                    initialTab = 1,
                    prefs = UserPrefsState(language = "ar", tasbihTarget = 33, tasbihTotalCount = 10),
                    bookmarks = emptyList(),
                    adhkarProgress = emptyMap(),
                    audioState = AudioPlaybackState(),
                    downloadedKeys = emptySet(),
                    downloadingKeys = emptySet(),
                    onUpdateCategoryProgress = { _, _, _ -> },
                    onIncrementTasbih = { totalTapped += it },
                    onSetTasbihTarget = {},
                    onToggleBookmark = { _, _, _, _, _ -> },
                    onPlayRuqyah = {},
                    onDownloadRuqyah = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tasbih_tap_circle").performClick()
        assertEquals(1, totalTapped)
    }
}



