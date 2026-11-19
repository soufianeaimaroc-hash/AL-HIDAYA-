package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppStrings
import com.example.core.LocalAppStrings
import com.example.data.QuranAndHadithRepository
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesDataStore
import com.example.data.local.UserPrefsState
import com.example.data.seed.QuranCatalog
import com.example.ui.SurahListUiState
import com.example.ui.screens.QuranScreen
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
        assertEquals("SOUFIAN ISLAM", appName)
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

        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                QuranScreen(
                    prefs = UserPrefsState(language = "ar"),
                    surahListState = SurahListUiState.Loading,
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

        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                QuranScreen(
                    prefs = UserPrefsState(language = "ar"),
                    surahListState = SurahListUiState.Error("خطأ في التحميل"),
                    bookmarks = emptyList(),
                    cachedSurahs = emptySet(),
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
        composeRule.onNodeWithTag("quran_surah_error").assertIsDisplayed()
        composeRule.onNodeWithTag("quran_surah_retry_button").performClick()
        assertTrue(retried)

        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppStrings.Arabic) {
                QuranScreen(
                    prefs = UserPrefsState(language = "ar"),
                    surahListState = SurahListUiState.Success(QuranCatalog.surahs),
                    bookmarks = emptyList(),
                    cachedSurahs = setOf(1, 112, 113, 114),
                    downloadedKeys = emptySet(),
                    downloadingKeys = emptySet(),
                    onRetryLoadSurahs = {},
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
        composeRule.onNodeWithTag("surah_card_1").assertIsDisplayed()
        composeRule.onNodeWithText("سورة الفاتحة").assertIsDisplayed()
        composeRule.onNodeWithTag("quran_search_input").performTextInput("الناس")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("surah_card_114").assertIsDisplayed()
        composeRule.onNodeWithTag("surah_card_114").performClick()
        assertEquals(114, openedSurah)
    }
}

