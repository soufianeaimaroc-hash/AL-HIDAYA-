package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppStrings
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedSurahAyahEntity
import com.example.data.local.UserPrefsState
import com.example.data.seed.QuranCatalog
import com.example.data.seed.SurahInfo
import com.example.ui.SurahListUiState
import com.example.ui.components.RubElHizbNumberBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import kotlinx.coroutines.delay

@Composable
fun QuranScreen(
    prefs: UserPrefsState,
    surahListState: SurahListUiState,
    bookmarks: List<BookmarkEntity>,
    cachedSurahs: Set<Int>,
    downloadedKeys: Set<String>,
    downloadingKeys: Set<String>,
    onRetryLoadSurahs: () -> Unit,
    onDismissSurahSyncError: () -> Unit,
    onOpenSurah: (surahNumber: Int, initialPageMode: Boolean) -> Unit,
    onPlaySurah: (Int) -> Unit,
    onPlayQuranOnceFromSurah: (Int) -> Unit,
    onDownloadSurah: (Int) -> Unit,
    onSearchVerses: suspend (String) -> List<CachedSurahAyahEntity>
) {
    val s = LocalAppStrings.current
    val isAr = prefs.language == "ar"
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var verseResults by remember { mutableStateOf<List<CachedSurahAyahEntity>>(emptyList()) }

    val availableSurahs: List<SurahInfo> = remember(surahListState) {
        when (surahListState) {
            is SurahListUiState.Success -> surahListState.surahs
            else -> emptyList()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            delay(350)
            verseResults = onSearchVerses(searchQuery)
        } else {
            verseResults = emptyList()
        }
    }

    val filteredSurahs = remember(searchQuery, availableSurahs) {
        val q = searchQuery.trim()
        if (q.isEmpty()) availableSurahs
        else availableSurahs.filter {
            it.arabicName.contains(q) ||
                "سورة ${it.arabicName}".contains(q) ||
                it.englishName.contains(q, ignoreCase = true) ||
                it.englishTranslation.contains(q, ignoreCase = true) ||
                it.number.toString() == q
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Compact Search & Refresh Bar pinned at top so the Surah list gets full vertical viewport height
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(s.searchQuranHint) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("quran_search_clear")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = s.backLabel)
                        }
                    } else {
                        IconButton(
                            onClick = onRetryLoadSurahs,
                            modifier = Modifier.testTag("quran_refresh_surahs")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = s.refreshSurahsLabel)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            val tabs = listOf(s.surahsTab, s.juzTab, s.hizbTab, s.bookmarksTab)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("quran_tab_$idx")
                    )
                }
            }

            if (surahListState is SurahListUiState.Success && surahListState.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("quran_sync_progress"),
                    color = GoldRoyal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                selectedTab == 0 && surahListState is SurahListUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp)
                            .testTag("quran_surah_loading"),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = GoldRoyal)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = s.loadingSurahs,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                selectedTab == 0 && surahListState is SurahListUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp)
                            .testTag("quran_surah_error"),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = surahListState.message.ifBlank { s.surahLoadError },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRetryLoadSurahs,
                            modifier = Modifier.testTag("quran_surah_retry_button")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.retry)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("quran_surah_lazy_list"),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (selectedTab == 0 && searchQuery.isBlank()) {
                            if (surahListState is SurahListUiState.Success && !surahListState.syncError.isNullOrBlank()) {
                                item(key = "header_sync_error") {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("quran_sync_error_banner"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = surahListState.syncError,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = onRetryLoadSurahs) {
                                                Text(s.retry)
                                            }
                                            IconButton(onClick = onDismissSurahSyncError) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = s.backLabel,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item(key = "header_continue_banner") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenSurah(prefs.lastSurahNumber, false) }
                                        .testTag("quran_continue_card"),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.55f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Brush.linearGradient(listOf(EmeraldDeep, EmeraldPrimary)))
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.RecordVoiceOver,
                                                        contentDescription = s.reciterName,
                                                        tint = GoldChampagne,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${s.reciterLabel}: ${s.reciterName}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldChampagne
                                                    )
                                                }
                                                Text(
                                                    text = s.reciterRiwayah,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.80f)
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "${s.continueReading}: ${prefs.lastSurahName} • ${s.ayahsLabel} ${prefs.lastAyahNumber}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = GoldRoyal,
                                                modifier = Modifier.clickable { onPlayQuranOnceFromSurah(prefs.lastSurahNumber) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = s.playSurah,
                                                    tint = EmeraldDeep,
                                                    modifier = Modifier
                                                        .padding(10.dp)
                                                        .size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item(key = "header_actions") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onPlayQuranOnceFromSurah(1) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldPrimary,
                                            contentColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.65f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("quran_play_all_once")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = GoldChampagne,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = s.playFromStartToEndOnce,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { onOpenSurah(prefs.lastSurahNumber, true) },
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.7f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("quran_open_mushaf_pages")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = GoldRoyal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = s.mushafPagesAction,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        if (verseResults.isNotEmpty() && selectedTab == 0) {
                            items(verseResults, key = { "search_${it.globalKey}" }) { verse ->
                                val surah = availableSurahs.getOrNull(verse.surahNumber - 1)
                                    ?: QuranCatalog.surahs.getOrNull(verse.surahNumber - 1)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenSurah(verse.surahNumber, false) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${surah?.arabicName ?: ""} • ${verse.ayahNumber}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = verse.arabicTextUthmani,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 19.sp
                                        )
                                    }
                                }
                            }
                        }

                        when (selectedTab) {
                            0 -> {
                                if (filteredSurahs.isEmpty()) {
                                    item(key = "surah_no_results") {
                                        Text(
                                            text = s.noResults,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp)
                                        )
                                    }
                                } else {
                                    items(filteredSurahs, key = { it.number }) { surah ->
                                        val audioKey = "surah_${surah.number.toString().padStart(3, '0')}_abdullah_mousa"
                                        val isAudioDownloaded = downloadedKeys.contains(audioKey)
                                        val isAudioDownloading = downloadingKeys.contains(audioKey)
                                        val isTextCached = cachedSurahs.contains(surah.number)

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenSurah(surah.number, false) }
                                                .testTag("surah_card_${surah.number}"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RubElHizbNumberBadge(number = surah.number)
                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = if (isAr) "سورة ${surah.arabicName}" else surah.englishName,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (isTextCached) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = s.downloadedOffline,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                    }
                                                    val rev = if (surah.isMeccan) s.makkiyah else s.madaniyah
                                                    Text(
                                                        text = "$rev • ${surah.ayahCount} ${s.ayahsLabel} • ${s.pageLabel} ${surah.startPage}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Text(
                                                    text = surah.arabicName,
                                                    fontFamily = AmiriFontFamily,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                IconButton(
                                                    onClick = { onPlayQuranOnceFromSurah(surah.number) },
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .testTag("surah_play_${surah.number}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PlayArrow,
                                                        contentDescription = s.playSurah,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { onDownloadSurah(surah.number) },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    when {
                                                        isAudioDownloading -> CircularProgressIndicator(
                                                            modifier = Modifier.size(20.dp),
                                                            strokeWidth = 2.dp
                                                        )
                                                        isAudioDownloaded -> Icon(
                                                            imageVector = Icons.Filled.CheckCircle,
                                                            contentDescription = s.downloadedOffline,
                                                            tint = GoldRoyal
                                                        )
                                                        else -> Icon(
                                                            imageVector = Icons.Filled.CloudDownload,
                                                            contentDescription = s.downloadOffline,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                    1 -> {
                        items(QuranCatalog.juzList, key = { it.number }) { juz ->
                            val startSurah = QuranCatalog.surahs.getOrNull(juz.startSurah - 1) ?: QuranCatalog.surahs.first()
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenSurah(juz.startSurah, true) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RubElHizbNumberBadge(number = juz.number)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAr) "${s.juzLabel} ${juz.number} • ${juz.arabicName}" else "${s.juzLabel} ${juz.number} • ${juz.englishName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isAr) {
                                                "سورة ${startSurah.arabicName} • الآية ${juz.startAyah} • ${s.pageLabel} ${juz.startPage}"
                                            } else {
                                                "${startSurah.englishName} • Ayah ${juz.startAyah} • ${s.pageLabel} ${juz.startPage}"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = GoldRoyal)
                                }
                            }
                        }
                    }

                    2 -> {
                        items(QuranCatalog.hizbList, key = { it.number }) { hizb ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenSurah(hizb.startSurah, false) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RubElHizbNumberBadge(number = hizb.number)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAr) hizb.titleAr else hizb.titleEn,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        val quranBookmarks = bookmarks.filter { it.itemType == "QURAN" }
                        if (quranBookmarks.isEmpty()) {
                            item {
                                Text(
                                    text = s.noResults,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        } else {
                            items(quranBookmarks, key = { it.referenceKey }) { bm ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = GoldRoyal)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = bm.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = bm.arabicText,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
}

