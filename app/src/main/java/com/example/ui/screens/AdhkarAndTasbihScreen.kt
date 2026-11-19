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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.core.LocalAppStrings
import com.example.data.local.AdhkarProgressEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserPrefsState
import com.example.data.seed.AdhkarData
import com.example.data.seed.RuqyahAudioTrack
import com.example.data.seed.RuqyahCatalog
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
    onUpdateCategoryProgress: (String, Int, Int) -> Unit,
    onIncrementTasbih: (Int) -> Unit,
    onSetTasbihTarget: (Int) -> Unit,
    onToggleBookmark: (String, String, String, String, String) -> Unit,
    onPlayRuqyah: (RuqyahAudioTrack) -> Unit,
    onDownloadRuqyah: (RuqyahAudioTrack) -> Unit
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
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale("ar")
                ttsReady = true
            }
        }
        instance
    }

    LaunchedEffect(audioState.isPlaying) {
        if (audioState.isPlaying && speakingItemId != null) {
            tts.stop()
            speakingItemId = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
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

            if (selectedTab == 0) {
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
                                                        tts.stop()
                                                        speakingItemId = null
                                                    } else {
                                                        AudioPlaybackService.pauseIfPlaying(context)
                                                        speakingItemId = item.id
                                                        tts.speak(item.arabicText, TextToSpeech.QUEUE_FLUSH, null, item.id)
                                                    }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    if (speakingItemId == item.id) Icons.Filled.Stop else Icons.Filled.VolumeUp,
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
            } else {
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
            if (selectedTab == 2) {
                // Authentic Ruqyah Shariah Section (Audio + Verified Texts)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.55f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(listOf(EmeraldDeep, EmeraldPrimary)))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Shield, null, tint = GoldChampagne, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = s.ruqyahTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldChampagne
                                        )
                                        Text(
                                            text = if (isAr) "الرقية الشرعية بالصوت والنص من القرآن الكريم والسنة الصحيحة" else "Authentic Audio & Text Ruqyah from Quran & Sahih Sunnah",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(RuqyahCatalog.audioTracks, key = { it.id }) { track ->
                        val isTrackActive = audioState.title == (if (isAr) track.titleAr else track.titleEn) && audioState.isPlaying
                        val isSavedOffline = downloadedKeys.contains(track.id)
                        val isTrackDownloading = downloadingKeys.contains(track.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ruqyah_audio_${track.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, if (isTrackActive) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAr) track.titleAr else track.titleEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isAr) track.reciterAr else track.reciterEn,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                if (isTrackActive) AudioPlaybackService.togglePause(context)
                                                else onPlayRuqyah(track)
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("play_ruqyah_${track.id}")
                                        ) {
                                            Icon(
                                                if (isTrackActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = s.playSurah,
                                                tint = GoldRoyal
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDownloadRuqyah(track) },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            when {
                                                isTrackDownloading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                                isSavedOffline -> Icon(Icons.Filled.CheckCircle, s.downloadedOffline, tint = GoldRoyal)
                                                else -> Icon(Icons.Filled.CloudDownload, s.downloadOffline)
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = if (isAr) track.descriptionAr else track.descriptionEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isAr) track.sourceAttributionAr else track.sourceAttributionEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    items(RuqyahCatalog.ruqyahItems, key = { it.id }) { item ->
                        val bmKey = "ruqyah_${item.id}"
                        val isSaved = bookmarks.any { it.referenceKey == bmKey }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = item.arabicText,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 20.sp,
                                    lineHeight = 34.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (!isAr) {
                                    Text(
                                        text = item.englishTranslation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                GoldOrnamentalDivider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isAr) item.sourceAr else item.sourceEn,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        if (ttsReady) {
                                            IconButton(
                                                onClick = {
                                                    if (speakingItemId == item.id) {
                                                        tts.stop()
                                                        speakingItemId = null
                                                    } else {
                                                        AudioPlaybackService.pauseIfPlaying(context)
                                                        speakingItemId = item.id
                                                        tts.speak(item.arabicText, TextToSpeech.QUEUE_FLUSH, null, item.id)
                                                    }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    if (speakingItemId == item.id) Icons.Filled.Stop else Icons.Filled.VolumeUp,
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
