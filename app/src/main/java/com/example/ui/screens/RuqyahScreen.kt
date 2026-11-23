package com.example.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.AudioPlaybackService
import com.example.audio.AudioPlaybackState
import com.example.audio.RepeatMode
import com.example.core.LocalAppStrings
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserPrefsState
import com.example.data.seed.RuqyahAudioTrack
import com.example.data.seed.RuqyahCatalog
import com.example.data.seed.RuqyahCategory
import com.example.data.seed.RuqyahVerseRef
import com.example.ui.components.GoldOrnamentalDivider
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuqyahScreen(
    prefs: UserPrefsState,
    audioState: AudioPlaybackState,
    bookmarks: List<BookmarkEntity>,
    downloadedKeys: Set<String>,
    downloadingKeys: Set<String>,
    selectedCategory: RuqyahCategory?,
    activeRuqyahTrackId: String,
    onSelectCategory: (RuqyahCategory?) -> Unit,
    onSelectReciterEdition: (String) -> Unit,
    onPlayRuqyah: (RuqyahAudioTrack) -> Unit,
    onResumeLastRuqyah: () -> Unit,
    onDownloadRuqyah: (RuqyahAudioTrack) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBy: (Long) -> Unit,
    onSetVolume: (Float) -> Unit,
    onSetSleepTimerMinutes: (Int) -> Unit,
    onSetRepeatConfig: (RepeatMode, Int?) -> Unit,
    onToggleBookmark: (String, String, String, String, String) -> Unit,
    onShareText: (String) -> Unit,
    onBack: () -> Unit
) {
    val s = LocalAppStrings.current
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = s.ruqyahScreenTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ruqyah_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.backLabel,
                            tint = GoldChampagne
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldDeep,
                    titleContentColor = GoldChampagne
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RuqyahLibraryContent(
                prefs = prefs,
                audioState = audioState,
                bookmarks = bookmarks,
                downloadedKeys = downloadedKeys,
                downloadingKeys = downloadingKeys,
                selectedCategory = selectedCategory,
                activeRuqyahTrackId = activeRuqyahTrackId,
                onSelectCategory = onSelectCategory,
                onSelectReciterEdition = onSelectReciterEdition,
                onPlayRuqyah = onPlayRuqyah,
                onResumeLastRuqyah = onResumeLastRuqyah,
                onDownloadRuqyah = onDownloadRuqyah,
                onSeekTo = onSeekTo,
                onSeekBy = onSeekBy,
                onSetVolume = onSetVolume,
                onSetSleepTimerMinutes = onSetSleepTimerMinutes,
                onSetRepeatConfig = onSetRepeatConfig,
                onToggleBookmark = onToggleBookmark,
                onShareText = onShareText
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RuqyahLibraryContent(
    prefs: UserPrefsState,
    audioState: AudioPlaybackState,
    bookmarks: List<BookmarkEntity>,
    downloadedKeys: Set<String>,
    downloadingKeys: Set<String>,
    selectedCategory: RuqyahCategory?,
    activeRuqyahTrackId: String,
    onSelectCategory: (RuqyahCategory?) -> Unit,
    onSelectReciterEdition: (String) -> Unit,
    onPlayRuqyah: (RuqyahAudioTrack) -> Unit,
    onResumeLastRuqyah: () -> Unit,
    onDownloadRuqyah: (RuqyahAudioTrack) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBy: (Long) -> Unit,
    onSetVolume: (Float) -> Unit,
    onSetSleepTimerMinutes: (Int) -> Unit,
    onSetRepeatConfig: (RepeatMode, Int?) -> Unit,
    onToggleBookmark: (String, String, String, String, String) -> Unit,
    onShareText: (String) -> Unit
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val isAr = prefs.language == "ar"
    val isOnline = remember { isDeviceOnlineSafe(context) }

    var focusedTrackId by remember { mutableStateOf(activeRuqyahTrackId) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var speakingItemId by remember { mutableStateOf<String?>(null) }

    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var instance: TextToSpeech? = null
        try {
            instance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    instance?.language = Locale.forLanguageTag("ar")
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
            try {
                tts?.stop()
            } catch (_: Exception) {
            }
            speakingItemId = null
        }
    }

    LaunchedEffect(activeRuqyahTrackId) {
        if (activeRuqyahTrackId.isNotBlank()) {
            focusedTrackId = activeRuqyahTrackId
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (_: Exception) {
            }
        }
    }

    val filteredTracks = remember(selectedCategory) {
        if (selectedCategory == null) RuqyahCatalog.audioTracks
        else RuqyahCatalog.audioTracks.filter { it.category == selectedCategory }
    }

    val highlightedTrack = remember(activeRuqyahTrackId, focusedTrackId, filteredTracks) {
        RuqyahCatalog.findTrackById(activeRuqyahTrackId)
            ?: RuqyahCatalog.findTrackById(focusedTrackId)
            ?: filteredTracks.firstOrNull()
            ?: RuqyahCatalog.audioTracks.first()
    }

    val displayedTexts: List<RuqyahVerseRef> = remember(selectedCategory, focusedTrackId, activeRuqyahTrackId) {
        val chosen = RuqyahCatalog.findTrackById(activeRuqyahTrackId.ifBlank { focusedTrackId })
        when {
            chosen != null -> chosen.associatedTexts
            selectedCategory != null -> filteredTracks.flatMap { it.associatedTexts }.distinctBy { it.id }
            else -> RuqyahCatalog.allScripturalItems
        }
    }

    val isRuqyahAudioActive = audioState.currentSource.isNotBlank() &&
        (activeRuqyahTrackId.isNotBlank() || audioState.trackId.startsWith("ruqyah"))

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp)
                .testTag("ruqyah_lazy_list"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Luxury Emerald & Gold Hero Banner
            item(key = "hero") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ruqyah_hero_banner"),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.2.dp, GoldRoyal.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ruqyah_hero),
                            contentDescription = s.ruqyahScreenTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            EmeraldDeep.copy(alpha = 0.55f),
                                            EmeraldDeep.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = GoldRoyal.copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, GoldChampagne)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = null,
                                        tint = GoldChampagne,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = s.ruqyahScreenTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldChampagne
                                    )
                                    Text(
                                        text = s.ruqyahScreenSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.90f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = GoldRoyal.copy(alpha = 0.20f),
                                    border = BorderStroke(0.8.dp, GoldChampagne.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = GoldChampagne,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isAr) "مصادر موثوقة ومرخصة" else "Verified Authentic Sources",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldChampagne,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = EmeraldVibrant.copy(alpha = 0.45f)
                                ) {
                                    Text(
                                        text = if (isAr) "10 أقسام شاملة" else "10 Categories",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Resume Last Listened Ruqyah Card
            if (prefs.lastRuqyahTrackId.isNotBlank()) {
                item(key = "resume_last") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onResumeLastRuqyah)
                            .testTag("ruqyah_resume_last_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        ),
                        border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = GoldChampagne,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.ruqyahResumeLast,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = prefs.lastRuqyahTitle.ifBlank { s.ruqyahTitle },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (prefs.lastRuqyahReciter.isNotBlank()) {
                                    Text(
                                        text = prefs.lastRuqyahReciter,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Button(
                                onClick = onResumeLastRuqyah,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = GoldChampagne, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isAr) "متابعة" else "Resume", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 3. Dedicated Ruqyah Audio Player Card (Play/Pause, -15s/+15s, Seekbar, Volume, Repeat, Sleep Timer)
            item(key = "player_card") {
                RuqyahAudioPlayerCard(
                    highlightedTrack = highlightedTrack,
                    audioState = audioState,
                    isRuqyahAudioActive = isRuqyahAudioActive,
                    selectedReciterEdition = prefs.ruqyahReciterEdition,
                    isAr = isAr,
                    onPlayHighlighted = {
                        focusedTrackId = highlightedTrack.id
                        onPlayRuqyah(highlightedTrack)
                    },
                    onTogglePause = { AudioPlaybackService.togglePause(context) },
                    onSeekTo = onSeekTo,
                    onSeekBy = onSeekBy,
                    onSetVolume = onSetVolume,
                    onOpenSleepTimer = { showSleepTimerSheet = true },
                    onCycleRepeat = {
                        val nextMode = when (audioState.repeatMode) {
                            RepeatMode.CONTINUOUS -> RepeatMode.REPEAT_SURAH
                            RepeatMode.REPEAT_SURAH -> RepeatMode.REPEAT_AYAH
                            RepeatMode.REPEAT_AYAH -> RepeatMode.CONTINUOUS
                        }
                        onSetRepeatConfig(nextMode, null)
                    }
                )
            }

            // 4. Verified Reciters Selector
            item(key = "reciters") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = s.ruqyahRecitersTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(RuqyahCatalog.cdnReciters, key = { it.id }) { profile ->
                            val selected = prefs.ruqyahReciterEdition == profile.cdnEdition
                            Card(
                                modifier = Modifier
                                    .width(240.dp)
                                    .clickable {
                                        profile.cdnEdition?.let(onSelectReciterEdition)
                                    }
                                    .testTag("ruqyah_reciter_${profile.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 0.8.dp,
                                    color = if (selected) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReciterMonogramPortrait(
                                        monogram = profile.monogram,
                                        isSelected = selected
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAr) profile.nameAr else profile.nameEn,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isAr) profile.roleAr else profile.roleEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isAr) profile.attributionAr else profile.attributionEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. All 10 Category Filter Chips
            item(key = "categories") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ruqyah_categories_row")
                ) {
                    item(key = "all") {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) },
                            label = {
                                Text(
                                    text = s.ruqyahAllCategories,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = GoldChampagne
                            ),
                            modifier = Modifier.testTag("ruqyah_cat_all")
                        )
                    }
                    items(RuqyahCategory.entries, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onSelectCategory(cat) },
                            label = {
                                Text(
                                    text = if (isAr) cat.titleAr else cat.titleEn,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = GoldChampagne
                            ),
                            modifier = Modifier.testTag("ruqyah_cat_${cat.id}")
                        )
                    }
                }
            }

            // Selected Category Subtitle / Offline Hint
            if (!isOnline) {
                item(key = "offline_banner") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = s.ruqyahOfflineNotice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 6. Ruqyah Audio Cards for Filtered Category
            items(filteredTracks, key = { "track_${it.id}" }) { track ->
                val isPlayingThis = isRuqyahAudioActive &&
                    (activeRuqyahTrackId == track.id || audioState.trackId.startsWith(track.id)) &&
                    audioState.isPlaying
                val isFocused = focusedTrackId == track.id
                val isSavedOffline = downloadedKeys.contains(track.id)
                val isTrackDownloading = downloadingKeys.contains(track.id)
                val resolvedReciterName = if (track.supportsReciterSelection) {
                    val reciter = RuqyahCatalog.findReciterByEdition(prefs.ruqyahReciterEdition)
                    if (isAr) reciter.nameAr else reciter.nameEn
                } else {
                    if (isAr) track.reciterAr else track.reciterEn
                }
                val monogram = if (track.supportsReciterSelection) {
                    RuqyahCatalog.findReciterByEdition(prefs.ruqyahReciterEdition).monogram
                } else {
                    track.reciterAr.take(2)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ruqyah_audio_${track.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlayingThis || isFocused) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isPlayingThis || isFocused) 1.5.dp else 0.9.dp,
                        color = if (isPlayingThis || isFocused) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReciterMonogramPortrait(
                                monogram = monogram,
                                isSelected = isPlayingThis
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) track.titleAr else track.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = resolvedReciterName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    focusedTrackId = track.id
                                    if (isPlayingThis) AudioPlaybackService.togglePause(context)
                                    else onPlayRuqyah(track)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("play_ruqyah_${track.id}")
                            ) {
                                Icon(
                                    imageVector = if (isPlayingThis) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlayingThis) s.pauseAudio else s.playSurah,
                                    tint = GoldRoyal,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDownloadRuqyah(track) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("download_ruqyah_${track.id}")
                            ) {
                                when {
                                    isTrackDownloading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                    isSavedOffline -> Icon(Icons.Filled.CheckCircle, s.downloadedOffline, tint = GoldRoyal)
                                    else -> Icon(Icons.Filled.CloudDownload, s.downloadOffline)
                                }
                            }
                        }

                        Text(
                            text = if (isAr) track.descriptionAr else track.descriptionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (isAr) track.category.titleAr else track.category.titleEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GoldRoyal.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = if (isAr) track.durationLabelAr else track.durationLabelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (track.supportsLiveTextSync) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = EmeraldVibrant.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = s.ruqyahLiveSyncActive,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldVibrant,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAr) track.sourceAttributionAr else track.sourceAttributionEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            TextButton(
                                onClick = { focusedTrackId = track.id },
                                modifier = Modifier.testTag("texts_ruqyah_${track.id}")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(s.ruqyahShowTexts, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 7. Verified Scriptural Texts Section Header
            item(key = "texts_header") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GoldOrnamentalDivider()
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = s.ruqyahTextsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (focusedTrackId.isNotBlank() && selectedCategory == null) {
                            TextButton(onClick = { focusedTrackId = "" }) {
                                Text(if (isAr) "عرض الكل" else "Show All", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 8. Scriptural Verses & Authentic Prophetic Duas
            items(displayedTexts, key = { "text_${it.id}" }) { verse ->
                val isCurrentlyRecited = audioState.isPlaying &&
                    verse.isQuranAyah &&
                    audioState.currentSurahNumber == verse.surahNumber &&
                    audioState.currentAyahNumber == verse.ayahNumber

                val bmKey = "ruqyah_${verse.id}"
                val isSaved = bookmarks.any { it.referenceKey == bmKey }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ruqyah_verse_${verse.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentlyRecited) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isCurrentlyRecited) 2.dp else 0.9.dp,
                        color = if (isCurrentlyRecited) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCurrentlyRecited) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldPrimary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = GoldChampagne, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (isAr) "يُتلى الآن • مزامنة حية" else "Now Reciting • Live Sync",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldChampagne,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = verse.arabicText,
                            fontFamily = AmiriFontFamily,
                            fontSize = prefs.quranFontSize.coerceIn(18, 30).sp,
                            lineHeight = (prefs.quranFontSize.coerceIn(18, 30) * 1.65f).sp,
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!isAr && verse.englishText.isNotBlank()) {
                            Text(
                                text = verse.englishText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val benefit = if (isAr) verse.benefitAr else verse.benefitEn
                        if (benefit.isNotBlank()) {
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        GoldOrnamentalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) verse.sourceAr else verse.sourceEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (verse.repeatRecommended > 1) {
                                    Text(
                                        text = if (isAr) "التكرار المأثور: ${verse.repeatRecommended} مرات" else "Sunnah repetition: ${verse.repeatRecommended}×",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!verse.isQuranAyah && ttsReady) {
                                    IconButton(
                                        onClick = {
                                            if (speakingItemId == verse.id) {
                                                tts?.stop()
                                                speakingItemId = null
                                            } else {
                                                AudioPlaybackService.pauseIfPlaying(context)
                                                speakingItemId = verse.id
                                                tts?.speak(verse.arabicText, TextToSpeech.QUEUE_FLUSH, null, verse.id)
                                            }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (speakingItemId == verse.id) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = s.listenDhikr,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        onToggleBookmark(
                                            bmKey,
                                            "RUQYAH",
                                            if (isAr) verse.sourceAr else verse.sourceEn,
                                            verse.englishText,
                                            verse.arabicText
                                        )
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = s.saveBookmark,
                                        tint = if (isSaved) GoldRoyal else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onShareText("${verse.arabicText}\n\n${verse.englishText}\n(${verse.sourceAr}) - AL HIDAYA – الهداية")
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = s.shareText,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSleepTimerSheet) {
        val options = listOf(0, 10, 15, 30, 45, 60)
        ModalBottomSheet(onDismissRequest = { showSleepTimerSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = s.ruqyahSleepTimer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { mins ->
                        val label = if (mins == 0) s.ruqyahSleepTimerOff else "$mins ${s.ruqyahMinutesShort}"
                        FilterChip(
                            selected = audioState.sleepTimerMinutes == mins,
                            onClick = {
                                onSetSleepTimerMinutes(mins)
                                showSleepTimerSheet = false
                            },
                            label = { Text(label, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("sleep_timer_option_$mins")
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RuqyahAudioPlayerCard(
    highlightedTrack: RuqyahAudioTrack,
    audioState: AudioPlaybackState,
    isRuqyahAudioActive: Boolean,
    selectedReciterEdition: String,
    isAr: Boolean,
    onPlayHighlighted: () -> Unit,
    onTogglePause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBy: (Long) -> Unit,
    onSetVolume: (Float) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onCycleRepeat: () -> Unit
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current

    val reciterProfile = remember(selectedReciterEdition) {
        RuqyahCatalog.findReciterByEdition(selectedReciterEdition)
    }

    val displayTitle = if (isRuqyahAudioActive) {
        audioState.title
    } else {
        if (isAr) highlightedTrack.titleAr else highlightedTrack.titleEn
    }
    val displayReciter = if (isRuqyahAudioActive) {
        audioState.subtitle
    } else if (highlightedTrack.supportsReciterSelection) {
        if (isAr) reciterProfile.nameAr else reciterProfile.nameEn
    } else {
        if (isAr) highlightedTrack.reciterAr else highlightedTrack.reciterEn
    }

    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosFraction by remember { mutableFloatStateOf(0f) }

    val durationMs = if (isRuqyahAudioActive) audioState.durationMs else 0L
    val positionMs = if (isRuqyahAudioActive) audioState.positionMs else 0L
    val fraction = if (isSeeking) {
        sliderPosFraction
    } else if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ruqyah_player_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
        border = BorderStroke(1.2.dp, GoldRoyal.copy(alpha = 0.75f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReciterMonogramPortrait(
                    monogram = if (highlightedTrack.supportsReciterSelection) reciterProfile.monogram else highlightedTrack.reciterAr.take(2),
                    isSelected = audioState.isPlaying && isRuqyahAudioActive
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldChampagne,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayReciter,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isRuqyahAudioActive && audioState.queueSize > 1) {
                        Text(
                            text = "${s.ruqyahLiveSyncActive} • ${audioState.queueIndex + 1}/${audioState.queueSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldChampagne.copy(alpha = 0.9f)
                        )
                    }
                }

                // Sleep Timer Chip
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenSleepTimer)
                        .testTag("ruqyah_sleep_timer_chip"),
                    color = if (audioState.sleepTimerRemainingMs > 0L) GoldRoyal else Color.White.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = s.ruqyahSleepTimer,
                            tint = if (audioState.sleepTimerRemainingMs > 0L) EmeraldDeep else GoldChampagne,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (audioState.sleepTimerRemainingMs > 0L) {
                                formatDurationMs(audioState.sleepTimerRemainingMs)
                            } else {
                                s.ruqyahSleepTimer
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (audioState.sleepTimerRemainingMs > 0L) EmeraldDeep else Color.White
                        )
                    }
                }
            }

            // Seekable Duration Slider + Time Labels
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = fraction,
                    onValueChange = { next ->
                        isSeeking = true
                        sliderPosFraction = next
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        if (durationMs > 0L) {
                            onSeekTo((sliderPosFraction * durationMs).toLong())
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = GoldChampagne,
                        activeTrackColor = GoldRoyal,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ruqyah_seek_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDurationMs(positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldChampagne
                    )
                    Text(
                        text = formatDurationMs(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }

            // Transport Controls: Repeat | -15s | Play/Pause | +15s | Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ruqyah_repeat_button")
                ) {
                    Icon(
                        imageVector = if (audioState.repeatMode == RepeatMode.REPEAT_AYAH) {
                            Icons.Filled.RepeatOne
                        } else {
                            Icons.Filled.Repeat
                        },
                        contentDescription = s.repeatMode,
                        tint = if (audioState.repeatMode == RepeatMode.CONTINUOUS) {
                            Color.White.copy(alpha = 0.65f)
                        } else {
                            GoldChampagne
                        }
                    )
                }

                OutlinedButton(
                    onClick = { onSeekBy(-15_000L) },
                    border = BorderStroke(1.dp, GoldChampagne.copy(alpha = 0.45f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("ruqyah_rewind_15")
                ) {
                    Icon(Icons.Filled.FastRewind, contentDescription = s.ruqyahRewind15, tint = GoldChampagne, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(s.ruqyahRewind15, style = MaterialTheme.typography.labelSmall, color = Color.White)
                }

                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isRuqyahAudioActive) onTogglePause() else onPlayHighlighted()
                        }
                        .testTag("ruqyah_main_play_button"),
                    color = GoldRoyal
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRuqyahAudioActive && audioState.isBuffering) {
                            CircularProgressIndicator(
                                color = EmeraldDeep,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isRuqyahAudioActive && audioState.isPlaying) {
                                    Icons.Filled.Pause
                                } else {
                                    Icons.Filled.PlayArrow
                                },
                                contentDescription = if (isRuqyahAudioActive && audioState.isPlaying) s.pauseAudio else s.playSurah,
                                tint = EmeraldDeep,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onSeekBy(15_000L) },
                    border = BorderStroke(1.dp, GoldChampagne.copy(alpha = 0.45f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("ruqyah_forward_15")
                ) {
                    Text(s.ruqyahForward15, style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.Filled.FastForward, contentDescription = s.ruqyahForward15, tint = GoldChampagne, modifier = Modifier.size(18.dp))
                }

                val nextSpeed = when (audioState.speed) {
                    1.0f -> 1.25f
                    1.25f -> 1.5f
                    1.5f -> 0.75f
                    else -> 1.0f
                }
                TextButton(
                    onClick = { AudioPlaybackService.setSpeed(context, nextSpeed) },
                    modifier = Modifier.testTag("ruqyah_speed_button")
                ) {
                    Text(
                        text = "${audioState.speed}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldChampagne
                    )
                }
            }

            // Volume Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val nextVol = if (audioState.volume > 0f) 0f else 1f
                        onSetVolume(nextVol)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ruqyah_mute_button")
                ) {
                    Icon(
                        imageVector = if (audioState.volume <= 0.01f) {
                            Icons.AutoMirrored.Filled.VolumeOff
                        } else {
                            Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = s.ruqyahVolumeLabel,
                        tint = GoldChampagne
                    )
                }
                Slider(
                    value = audioState.volume,
                    onValueChange = onSetVolume,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldChampagne,
                        activeTrackColor = GoldChampagne,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ruqyah_volume_slider")
                )
                Text(
                    text = "${(audioState.volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ReciterMonogramPortrait(
    monogram: String,
    isSelected: Boolean
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = if (isSelected) GoldRoyal else EmeraldPrimary,
        border = BorderStroke(1.2.dp, if (isSelected) GoldChampagne else GoldRoyal.copy(alpha = 0.65f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = monogram,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) EmeraldDeep else GoldChampagne
            )
        }
    }
}

fun formatDurationMs(ms: Long): String {
    val totalSec = (ms.coerceAtLeast(0L) / 1000L)
    val minutes = totalSec / 60L
    val seconds = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

private fun isDeviceOnlineSafe(context: Context): Boolean {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (_: Exception) {
        true
    }
}
