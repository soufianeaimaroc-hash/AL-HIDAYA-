package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackService
import com.example.audio.AudioPlaybackState
import com.example.audio.RepeatMode
import com.example.core.LocalAppStrings
import com.example.data.local.AdhkarProgressEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserPrefsState
import com.example.data.seed.AdhkarData
import com.example.data.seed.RuqyahAudioTrack
import com.example.data.seed.RuqyahCategory
import com.example.ui.components.GoldOrnamentalDivider
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import java.util.Locale

@Composable
fun AdhkarAndTasbihScreen(
    initialTab: Int = 0,
    prefs: UserPrefsState,
    bookmarks: List<BookmarkEntity>,
    adhkarProgress: Map<String, AdhkarProgressEntity>,
    audioState: AudioPlaybackState,
    downloadedKeys: Set<String>,
    downloadingKeys: Set<String>,
    selectedRuqyahCategory: RuqyahCategory? = null,
    activeRuqyahTrackId: String = "",
    onUpdateCategoryProgress: (String, Int, Int) -> Unit,
    onIncrementTasbih: (Int) -> Unit,
    onSetTasbihTarget: (Int) -> Unit,
    onToggleBookmark: (String, String, String, String, String) -> Unit,
    onPlayRuqyah: (RuqyahAudioTrack) -> Unit,
    onDownloadRuqyah: (RuqyahAudioTrack) -> Unit,
    onSelectRuqyahCategory: (RuqyahCategory?) -> Unit = {},
    onSelectRuqyahReciterEdition: (String) -> Unit = {},
    onResumeLastRuqyah: () -> Unit = {},
    onSeekAudioTo: (Long) -> Unit = {},
    onSeekAudioBy: (Long) -> Unit = {},
    onSetAudioVolume: (Float) -> Unit = {},
    onSetSleepTimerMinutes: (Int) -> Unit = {},
    onSetRepeatConfig: (RepeatMode, Int?) -> Unit = { _, _ -> },
    onShareText: (String) -> Unit = {}
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isAr = prefs.language == "ar"

    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    LaunchedEffect(initialTab) { selectedTab = initialTab.coerceIn(0, 2) }
    var selectedCategoryId by remember { mutableStateOf(AdhkarData.categories.first().id) }
    val itemCounts = remember { mutableStateMapOf<String, Int>() }
    var speakingItemId by remember { mutableStateOf<String?>(null) }

    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var instance: TextToSpeech? = null
        try {
            instance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    runCatching { instance?.language = Locale.forLanguageTag("ar") }
                    ttsReady = true
                }
            }
        } catch (_: Exception) {
            ttsReady = false
        }
        instance
    }

    LaunchedEffect(audioState.isPlaying) {
        if (audioState.isPlaying && speakingItemId != null) {
            runCatching { tts?.stop() }
            speakingItemId = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                tts?.stop()
                tts?.shutdown()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(s.adhkarTitle, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_adhkar_list")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(s.tasbihTitle, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_tasbih_counter")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(s.ruqyahTitle, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_ruqyah_shariah")
                )
            }

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                // Category chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AdhkarData.categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(if (isAr) cat.titleAr else cat.titleEn) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                val categoryItems = remember(selectedCategoryId) {
                    AdhkarData.items.filter { it.categoryId == selectedCategoryId }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryItems, key = { it.id }) { item ->
                        val count = itemCounts[item.id] ?: 0
                        val isDone = count >= item.targetCount
                        val bmKey = "dhikr_${item.id}"
                        val isSaved = bookmarks.any { it.referenceKey == bmKey }
                        var showVirtue by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (count < item.targetCount) {
                                        val next = count + 1
                                        itemCounts[item.id] = next
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val totalCategoryDone = categoryItems.sumOf {
                                            if (it.id == item.id) next else (itemCounts[it.id] ?: 0)
                                        }
                                        val totalCategoryTarget = categoryItems.sumOf { it.targetCount }
                                        onUpdateCategoryProgress(selectedCategoryId, totalCategoryDone, totalCategoryTarget)
                                    }
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isDone) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = item.arabicText,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 21.sp,
                                    lineHeight = 35.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (!isAr) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = item.englishTranslation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { (count.toFloat() / item.targetCount.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = GoldRoyal
                                )

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "$count / ${item.targetCount}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }

                                    Row {
                                        if (ttsReady) {
                                            IconButton(
                                                onClick = {
                                                    if (speakingItemId == item.id) {
                                                        runCatching { tts?.stop() }
                                                        speakingItemId = null
                                                    } else {
                                                        AudioPlaybackService.pauseIfPlaying(context)
                                                        speakingItemId = item.id
                                                        val spoken = runCatching {
                                                            tts?.speak(item.arabicText, TextToSpeech.QUEUE_FLUSH, null, item.id)
                                                        }.getOrDefault(TextToSpeech.ERROR)
                                                        if (spoken == TextToSpeech.ERROR) {
                                                            speakingItemId = null
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    if (speakingItemId == item.id) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = s.listenDhikr,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                onToggleBookmark(
                                                    bmKey,
                                                    "DHIKR",
                                                    if (isAr) item.sourceAr else item.sourceEn,
                                                    item.englishTranslation,
                                                    item.arabicText
                                                )
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                contentDescription = s.saveBookmark,
                                                tint = if (isSaved) GoldRoyal else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Button(onClick = { showVirtue = !showVirtue }) {
                                            Text(s.virtueAndSource, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = showVirtue) {
                                    Column {
                                        Spacer(Modifier.height(8.dp))
                                        GoldOrnamentalDivider()
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = if (isAr) item.virtueAr else item.virtueEn,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = if (isAr) item.sourceAr else item.sourceEn,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }
                1 -> {
                    // Electronic Tasbih Counter
                    val phrases = listOf(
                        "سُبْحَانَ اللَّهِ",
                        "الْحَمْدُ لِلَّهِ",
                        "اللَّهُ أَكْبَرُ",
                        "لَا إِلَهَ إِلَّا اللَّهُ",
                        "أَسْتَغْفِرُ اللَّهَ",
                        "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ"
                    )
                    var selectedPhrase by remember { mutableStateOf(phrases.first()) }
                    var sessionCount by remember { mutableIntStateOf(0) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(phrases) { phrase ->
                                FilterChip(
                                    selected = selectedPhrase == phrase,
                                    onClick = {
                                        selectedPhrase = phrase
                                        sessionCount = 0
                                    },
                                    label = { Text(phrase, fontFamily = AmiriFontFamily, fontSize = 16.sp) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(33, 99, 100, 1000).forEach { target ->
                                FilterChip(
                                    selected = prefs.tasbihTarget == target,
                                    onClick = { onSetTasbihTarget(target) },
                                    label = { Text("${s.tasbihTarget}: $target") }
                                )
                            }
                        }

                        // Tactile Luxury Circular Counter Button
                        Box(
                            modifier = Modifier
                                .size(230.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(EmeraldPrimary, EmeraldDeep)))
                                .clickable {
                                    sessionCount += 1
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onIncrementTasbih(1)
                                }
                                .testTag("tasbih_tap_circle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = selectedPhrase,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 24.sp,
                                    color = GoldChampagne,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = sessionCount.toString().padStart(3, '0'),
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "/ ${prefs.tasbihTarget}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldChampagne.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(s.tasbihTotal, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = prefs.tasbihTotalCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = { sessionCount = 0 },
                                modifier = Modifier.testTag("tasbih_reset_button")
                            ) {
                                Icon(Icons.Filled.Refresh, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.resetProgress)
                            }
                        }
                    }
                }
                else -> {
                    RuqyahLibraryContent(
                        prefs = prefs,
                        audioState = audioState,
                        bookmarks = bookmarks,
                        downloadedKeys = downloadedKeys,
                        downloadingKeys = downloadingKeys,
                        selectedCategory = selectedRuqyahCategory,
                        activeRuqyahTrackId = activeRuqyahTrackId,
                        onSelectCategory = onSelectRuqyahCategory,
                        onSelectReciterEdition = onSelectRuqyahReciterEdition,
                        onPlayRuqyah = onPlayRuqyah,
                        onResumeLastRuqyah = onResumeLastRuqyah,
                        onDownloadRuqyah = onDownloadRuqyah,
                        onSeekTo = onSeekAudioTo,
                        onSeekBy = onSeekAudioBy,
                        onSetVolume = onSetAudioVolume,
                        onSetSleepTimerMinutes = onSetSleepTimerMinutes,
                        onSetRepeatConfig = onSetRepeatConfig,
                        onToggleBookmark = onToggleBookmark,
                        onShareText = onShareText
                    )
                }
            }
        }
    }
}
