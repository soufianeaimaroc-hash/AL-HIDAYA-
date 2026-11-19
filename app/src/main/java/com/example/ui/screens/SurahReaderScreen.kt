package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.audio.AudioPlaybackService
import com.example.audio.AudioPlaybackState
import com.example.audio.RepeatMode
import com.example.core.LocalAppStrings
import com.example.data.QuranAndHadithRepository
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedSurahAyahEntity
import com.example.data.local.UserPrefsState
import com.example.data.seed.QuranCatalog
import com.example.ui.components.GoldOrnamentalDivider
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldRoyal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderScreen(
    surahNumber: Int,
    initialPageMode: Boolean,
    prefs: UserPrefsState,
    bookmarks: List<BookmarkEntity>,
    audioState: AudioPlaybackState,
    downloadedKeys: Set<String>,
    downloadingKeys: Set<String>,
    loadSurahAyahs: suspend (Int) -> Result<List<CachedSurahAyahEntity>>,
    loadMushafPage: suspend (Int) -> Result<List<CachedSurahAyahEntity>>,
    onUpdateLastRead: (Int, Int, String) -> Unit,
    onPlaySurah: (Int) -> Unit,
    onPlayQuranOnceFromSurah: (Int) -> Unit,
    onPlayPageOnce: (Int, List<CachedSurahAyahEntity>) -> Unit,
    onPlayAyah: (Int, Int, Int) -> Unit,
    onSetRepeatConfig: (RepeatMode, Int?) -> Unit,
    onDownloadSurah: (Int) -> Unit,
    onToggleBookmark: (String, String, String, String, String) -> Unit,
    onShareText: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val s = LocalAppStrings.current
    val context = LocalContext.current
    val isAr = prefs.language == "ar"
    val surah = QuranCatalog.surahs.getOrNull(surahNumber - 1) ?: QuranCatalog.surahs.first()

    var isMushafMode by remember(initialPageMode) { mutableStateOf(initialPageMode) }
    var showMushafImage by remember { mutableStateOf(true) }
    var currentPage by remember(surahNumber) { mutableIntStateOf(surah.startPage.coerceIn(1, 604)) }
    var showJumpDialog by remember { mutableStateOf(false) }

    var ayahs by remember { mutableStateOf<List<CachedSurahAyahEntity>>(emptyList()) }
    var surahLoading by remember { mutableStateOf(true) }
    var surahError by remember { mutableStateOf<String?>(null) }

    var pageAyahs by remember { mutableStateOf<List<CachedSurahAyahEntity>>(emptyList()) }
    var pageLoading by remember { mutableStateOf(false) }
    var pageError by remember { mutableStateOf<String?>(null) }

    var expandedTafsirKey by remember { mutableStateOf<String?>(null) }
    var reloadNonce by remember { mutableIntStateOf(0) }

    val activeSurah = remember(isMushafMode, pageAyahs, currentPage, surah) {
        if (!isMushafMode) surah
        else {
            val sNum = pageAyahs.firstOrNull()?.surahNumber
            val byApi = sNum?.let { QuranCatalog.surahs.getOrNull(it - 1) }
            byApi ?: QuranCatalog.surahs.lastOrNull { it.startPage <= currentPage } ?: surah
        }
    }

    val audioKey = "surah_${activeSurah.number.toString().padStart(3, '0')}_abdullah_mousa"
    val isDownloaded = downloadedKeys.contains(audioKey)
    val isDownloading = downloadingKeys.contains(audioKey)

    LaunchedEffect(surahNumber, reloadNonce) {
        surahLoading = true
        surahError = null
        if (!initialPageMode) {
            onUpdateLastRead(surah.number, 1, surah.arabicName)
        }
        val res = loadSurahAyahs(surahNumber)
        res.onSuccess { list ->
            ayahs = list
            surahLoading = false
        }.onFailure { err ->
            surahError = err.localizedMessage ?: s.offlineNotice
            surahLoading = false
        }
    }

    LaunchedEffect(isMushafMode, currentPage, reloadNonce) {
        if (isMushafMode) {
            pageLoading = true
            pageError = null
            loadMushafPage(currentPage).onSuccess { list ->
                pageAyahs = list
                pageLoading = false
                val first = list.firstOrNull()
                if (first != null) {
                    val sInfo = QuranCatalog.surahs.getOrNull(first.surahNumber - 1)
                    if (sInfo != null) {
                        onUpdateLastRead(sInfo.number, first.ayahNumber, sInfo.arabicName)
                    }
                }
            }.onFailure { err ->
                pageError = err.localizedMessage ?: s.offlineNotice
                pageLoading = false
            }
        }
    }

    if (showJumpDialog) {
        var pageInput by remember(currentPage) { mutableStateOf(currentPage.toString()) }
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text(s.jumpToPageLabel) },
            text = {
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { input ->
                        pageInput = input.filter { it.isDigit() }.take(3)
                    },
                    label = { Text(s.pageNumberHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mushaf_jump_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = pageInput.toIntOrNull()
                        if (parsed != null) {
                            currentPage = parsed.coerceIn(1, 604)
                        }
                        showJumpDialog = false
                    },
                    modifier = Modifier.testTag("mushaf_jump_confirm")
                ) {
                    Text(s.goLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text(s.backLabel)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (isAr) "سورة ${activeSurah.arabicName}" else activeSurah.englishName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${s.reciterName} • ${activeSurah.ayahCount} ${s.ayahsLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.backLabel)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (audioState.isPlaying) AudioPlaybackService.togglePause(context)
                        else onPlayQuranOnceFromSurah(activeSurah.number)
                    },
                    modifier = Modifier.testTag("reader_play_surah")
                ) {
                    Icon(
                        imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = s.playSurah,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { onDownloadSurah(activeSurah.number) },
                    modifier = Modifier.testTag("reader_download_surah")
                ) {
                    when {
                        isDownloading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        isDownloaded -> Icon(Icons.Filled.CheckCircle, s.downloadedOffline, tint = GoldRoyal)
                        else -> Icon(Icons.Filled.CloudDownload, s.downloadOffline)
                    }
                }
            }
        )

        // Mode switcher (Verse View vs Real Mushaf Page View + Sequential Play Once)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !isMushafMode,
                onClick = { isMushafMode = false },
                label = { Text(s.verseMode) },
                leadingIcon = { Icon(Icons.Filled.RecordVoiceOver, null, Modifier.size(16.dp)) },
                modifier = Modifier.testTag("mode_verse_chip")
            )
            FilterChip(
                selected = isMushafMode,
                onClick = { isMushafMode = true },
                label = { Text("${s.mushafPageMode} ($currentPage/604)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null, Modifier.size(16.dp)) },
                modifier = Modifier.testTag("mode_mushaf_chip")
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = { onPlayQuranOnceFromSurah(activeSurah.number) },
                modifier = Modifier.testTag("reader_play_from_here_once")
            ) {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = s.playFromHereToEndOnce,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Compact Repeat Mode Bar (Default = Continuous Single-Pass)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = audioState.repeatMode == RepeatMode.CONTINUOUS,
                    onClick = { onSetRepeatConfig(RepeatMode.CONTINUOUS, null) },
                    label = { Text(s.repeatContinuous, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("reader_repeat_continuous")
                )
            }
            item {
                FilterChip(
                    selected = audioState.repeatMode == RepeatMode.REPEAT_SURAH,
                    onClick = { onSetRepeatConfig(RepeatMode.REPEAT_SURAH, null) },
                    label = { Text(s.repeatSurah, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.Repeat, null, Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("reader_repeat_surah")
                )
            }
            item {
                FilterChip(
                    selected = audioState.repeatMode == RepeatMode.REPEAT_AYAH,
                    onClick = { onSetRepeatConfig(RepeatMode.REPEAT_AYAH, null) },
                    label = {
                        val cntLabel = if (audioState.ayahRepeatTarget <= 0) "∞" else "${audioState.ayahRepeatTarget}×"
                        Text("${s.repeatAyah} ($cntLabel)", style = MaterialTheme.typography.labelSmall)
                    },
                    leadingIcon = { Icon(Icons.Filled.RepeatOne, null, Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("reader_repeat_ayah")
                )
            }
            if (audioState.repeatMode == RepeatMode.REPEAT_AYAH) {
                items(listOf(2, 3, 5, 7, 10, 0)) { count ->
                    FilterChip(
                        selected = audioState.ayahRepeatTarget == count,
                        onClick = { onSetRepeatConfig(RepeatMode.REPEAT_AYAH, count) },
                        label = { Text(if (count == 0) "∞" else "${count}×", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("reader_ayah_count_$count")
                    )
                }
            }
        }

        if (!isMushafMode && surahLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldRoyal)
            }
        } else if (!isMushafMode && surahError != null && ayahs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(surahError ?: s.offlineNotice, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    surahLoading = true
                    surahError = null
                    reloadNonce++
                }) {
                    Text(s.retry)
                }
            }
        } else if (isMushafMode) {
            // Authentic Mushaf Page Mode (1..604): Real Madinah Image + Formatted Multi-Surah Uthmani Fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Display style chips (Mushaf Image vs Uthmani Text) + Play Page
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showMushafImage,
                        onClick = { showMushafImage = true },
                        label = { Text(s.mushafImageTab, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Filled.Image, null, Modifier.size(15.dp)) },
                        modifier = Modifier.testTag("mushaf_display_image")
                    )
                    FilterChip(
                        selected = !showMushafImage,
                        onClick = { showMushafImage = false },
                        label = { Text(s.mushafTextTab, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Filled.Article, null, Modifier.size(15.dp)) },
                        modifier = Modifier.testTag("mushaf_display_text")
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = { onPlayPageOnce(currentPage, pageAyahs) },
                        enabled = pageAyahs.isNotEmpty(),
                        modifier = Modifier.testTag("mushaf_play_page_once")
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = s.playPageOnce, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(s.playPageOnce, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (showMushafImage) {
                            Color(0xFFFFFBF0)
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        }
                    ),
                    border = BorderStroke(2.dp, GoldRoyal.copy(alpha = 0.78f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${s.pageLabel} $currentPage • ${s.juzLabel} ${pageAyahs.firstOrNull()?.juz ?: activeSurah.startJuz}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (showMushafImage) EmeraldDeep else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAr) "سورة ${activeSurah.arabicName}" else activeSurah.englishName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (showMushafImage) EmeraldDeep.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        GoldOrnamentalDivider()
                        Spacer(Modifier.height(10.dp))

                        if (showMushafImage) {
                            val imageUrl = remember(currentPage) {
                                QuranAndHadithRepository.mushafPageImageUrl(currentPage)
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFFBF0),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mushaf_page_image_container")
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "${s.pageLabel} $currentPage",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth(),
                                    loading = {
                                        if (pageAyahs.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                CircularProgressIndicator(
                                                    color = GoldRoyal,
                                                    modifier = Modifier
                                                        .padding(vertical = 8.dp)
                                                        .size(24.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                MushafFormattedUthmaniPage(
                                                    pageAyahs = pageAyahs,
                                                    fontSize = prefs.quranFontSize,
                                                    textColor = EmeraldDeep,
                                                    bannerContainerColor = EmeraldDeep.copy(alpha = 0.08f),
                                                    bannerTextColor = EmeraldDeep
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 360.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = GoldRoyal)
                                            }
                                        }
                                    },
                                    error = {
                                        MushafTextOrRetrySection(
                                            pageAyahs = pageAyahs,
                                            pageLoading = pageLoading,
                                            pageError = pageError,
                                            fallbackBanner = s.mushafImageFallbackNotice,
                                            offlineNotice = s.offlineNotice,
                                            retryLabel = s.retry,
                                            fontSize = prefs.quranFontSize,
                                            textColor = EmeraldDeep,
                                            bannerContainerColor = EmeraldDeep.copy(alpha = 0.08f),
                                            bannerTextColor = EmeraldDeep,
                                            onRetry = { reloadNonce++ }
                                        )
                                    },
                                    success = {
                                        SubcomposeAsyncImageContent(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                )
                            }
                        } else {
                            MushafTextOrRetrySection(
                                pageAyahs = pageAyahs,
                                pageLoading = pageLoading,
                                pageError = pageError,
                                fallbackBanner = null,
                                offlineNotice = s.offlineNotice,
                                retryLabel = s.retry,
                                fontSize = prefs.quranFontSize,
                                textColor = MaterialTheme.colorScheme.onSurface,
                                bannerContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                bannerTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                onRetry = { reloadNonce++ }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentPage > 1) currentPage-- },
                        enabled = currentPage > 1,
                        modifier = Modifier.testTag("mushaf_prev_page")
                    ) {
                        Text(if (isAr) "الصفحة السابقة" else "Prev Page")
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showJumpDialog = true }
                            .testTag("mushaf_page_jump_button")
                    ) {
                        Text(
                            text = "$currentPage / 604",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Button(
                        onClick = { if (currentPage < 604) currentPage++ },
                        enabled = currentPage < 604,
                        modifier = Modifier.testTag("mushaf_next_page")
                    ) {
                        Text(if (isAr) "الصفحة التالية" else "Next Page")
                    }
                }
            }
        } else {
            // Verse-by-Verse Reader with Tafsir Al-Muyassar, Translation, & Audio
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (surahNumber != 9 && surahNumber != 1) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontFamily = AmiriFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                items(ayahs, key = { it.globalKey }) { ayah ->
                    val bmKey = "quran_${ayah.surahNumber}_${ayah.ayahNumber}"
                    val isBookmarked = bookmarks.any { it.referenceKey == bmKey }
                    val isTafsirOpen = expandedTafsirKey == ayah.globalKey
                    val isPlayingThisAyah = audioState.isPlaying &&
                        audioState.currentSurahNumber == ayah.surahNumber &&
                        audioState.currentAyahNumber == ayah.ayahNumber

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayingThisAyah) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (isPlayingThisAyah) 1.5.dp else 0.8.dp,
                            color = if (isPlayingThisAyah) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            onUpdateLastRead(surah.number, ayah.ayahNumber, surah.arabicName)
                                            onPlayAyah(ayah.surahNumber, ayah.globalAyahNumber, ayah.ayahNumber)
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Filled.VolumeUp, contentDescription = "Listen Ayah", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            expandedTafsirKey = if (isTafsirOpen) null else ayah.globalKey
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Info,
                                            contentDescription = s.tafsirTitle,
                                            tint = if (isTafsirOpen) GoldRoyal else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            onUpdateLastRead(surah.number, ayah.ayahNumber, surah.arabicName)
                                            onToggleBookmark(
                                                bmKey,
                                                "QURAN",
                                                "سورة ${surah.arabicName} - الآية ${ayah.ayahNumber}",
                                                ayah.englishTranslation,
                                                ayah.arabicTextUthmani
                                            )
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                            contentDescription = s.saveBookmark,
                                            tint = if (isBookmarked) GoldRoyal else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            onShareText("${ayah.arabicTextUthmani}\n\n${ayah.englishTranslation}\n(سورة ${surah.arabicName}: ${ayah.ayahNumber}) - SOUFIAN ISLAM")
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = s.shareText)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${ayah.arabicTextUthmani} ﴿${ayah.ayahNumber}﴾",
                                fontFamily = AmiriFontFamily,
                                fontSize = prefs.quranFontSize.sp,
                                lineHeight = (prefs.quranFontSize * 1.75).sp,
                                textAlign = TextAlign.Right,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (ayah.englishTranslation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ayah.englishTranslation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = isTafsirOpen && ayah.arabicTafsir.isNotBlank()) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    GoldOrnamentalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = s.tafsirTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ayah.arabicTafsir,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
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

@Composable
private fun MushafTextOrRetrySection(
    pageAyahs: List<CachedSurahAyahEntity>,
    pageLoading: Boolean,
    pageError: String?,
    fallbackBanner: String?,
    offlineNotice: String,
    retryLabel: String,
    fontSize: Int,
    textColor: Color,
    bannerContainerColor: Color,
    bannerTextColor: Color,
    onRetry: () -> Unit
) {
    when {
        pageLoading && pageAyahs.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldRoyal)
            }
        }
        pageAyahs.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pageError ?: offlineNotice,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) {
                    Text(retryLabel)
                }
            }
        }
        else -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!fallbackBanner.isNullOrBlank()) {
                    Text(
                        text = fallbackBanner,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                MushafFormattedUthmaniPage(
                    pageAyahs = pageAyahs,
                    fontSize = fontSize,
                    textColor = textColor,
                    bannerContainerColor = bannerContainerColor,
                    bannerTextColor = bannerTextColor
                )
            }
        }
    }
}

@Composable
private fun MushafFormattedUthmaniPage(
    pageAyahs: List<CachedSurahAyahEntity>,
    fontSize: Int,
    textColor: Color,
    bannerContainerColor: Color,
    bannerTextColor: Color
) {
    val groupedSurahs = remember(pageAyahs) {
        val groups = mutableListOf<Pair<Int, List<CachedSurahAyahEntity>>>()
        for (ayah in pageAyahs) {
            if (groups.isEmpty() || groups.last().first != ayah.surahNumber) {
                groups.add(ayah.surahNumber to mutableListOf(ayah))
            } else {
                (groups.last().second as MutableList).add(ayah)
            }
        }
        groups
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedSurahs.forEach { (surahNum, surahAyahs) ->
            val surahInfo = QuranCatalog.surahs.getOrNull(surahNum - 1)
            val startsHere = surahAyahs.firstOrNull()?.ayahNumber == 1
            if (startsHere && surahInfo != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = bannerContainerColor,
                    border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "سورة ${surahInfo.arabicName}",
                        fontFamily = AmiriFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = bannerTextColor,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                }
            }
            val combinedText = remember(surahAyahs) {
                surahAyahs.joinToString("  ") { ayah ->
                    "${ayah.arabicTextUthmani} ﴿${ayah.ayahNumber}﴾"
                }
            }
            Text(
                text = combinedText,
                fontFamily = AmiriFontFamily,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.85).sp,
                textAlign = TextAlign.Justify,
                color = textColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

