package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.AudioPlaybackService
import com.example.audio.AudioPlaybackState
import com.example.audio.RepeatMode
import com.example.core.LocalAppStrings
import com.example.data.seed.QuranCatalog
import com.example.ui.theme.GoldRoyal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MiniAudioPlayerBar(
    audioState: AudioPlaybackState,
    onSelectSurah: (Int) -> Unit = {},
    onSetRepeatConfig: (RepeatMode, Int?) -> Unit = { mode, count ->
        // Default fallback handled via AudioPlaybackService in component
    },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isAr = s.langCode == "ar"
    val visible = audioState.currentSource.isNotBlank()

    var showRepeatSheet by remember { mutableStateOf(false) }
    var showSurahPickerSheet by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = visible, modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.55f)),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { AudioPlaybackService.togglePause(context) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("mini_player_toggle")
                    ) {
                        if (audioState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (audioState.isPlaying) s.pauseAudio else s.playSurah,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSurahPickerSheet = true }
                            .padding(horizontal = 4.dp)
                    ) {
                        val queueTag = if (audioState.queueSize > 1) {
                            " (${audioState.queueIndex + 1}/${audioState.queueSize})"
                        } else ""
                        Text(
                            text = "${audioState.title}$queueTag",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (audioState.errorMessage != null) {
                            Text(
                                text = audioState.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            val modeHint = when {
                                audioState.isCompleted -> if (isAr) "اكتملت التلاوة • اضغط إعادة" else "Finished • Tap Replay"
                                audioState.repeatMode == RepeatMode.CONTINUOUS -> s.repeatContinuous
                                audioState.repeatMode == RepeatMode.REPEAT_SURAH -> s.repeatSurah
                                audioState.ayahRepeatTarget <= 0 -> "${s.repeatAyah} (∞)"
                                else -> "${s.repeatAyah} (${audioState.ayahRepeatDone + 1}/${audioState.ayahRepeatTarget})"
                            }
                            Text(
                                text = "${audioState.subtitle} • $modeHint",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                            )
                        }
                    }

                    val nextSpeed = when (audioState.speed) {
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        1.5f -> 0.75f
                        else -> 1.0f
                    }
                    TextButton(
                        onClick = { AudioPlaybackService.setSpeed(context, nextSpeed) },
                        modifier = Modifier.testTag("mini_player_speed")
                    ) {
                        Text(
                            text = "${audioState.speed}x",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Surah Picker Shortcut
                    IconButton(
                        onClick = { showSurahPickerSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("mini_player_surah_picker")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = s.chooseSurahLabel,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Explicit Replay or Repeat Settings Modal
                    IconButton(
                        onClick = {
                            if (audioState.isCompleted) {
                                AudioPlaybackService.replay(context)
                            } else {
                                showRepeatSheet = true
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("mini_player_repeat")
                    ) {
                        Icon(
                            imageVector = when {
                                audioState.isCompleted -> Icons.Filled.Replay
                                audioState.repeatMode == RepeatMode.REPEAT_AYAH -> Icons.Filled.RepeatOne
                                else -> Icons.Filled.Repeat
                            },
                            contentDescription = s.repeatMode,
                            tint = if (audioState.repeatMode == RepeatMode.CONTINUOUS && !audioState.isCompleted) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
                            } else {
                                GoldRoyal
                            }
                        )
                    }

                    IconButton(
                        onClick = { AudioPlaybackService.stop(context) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("mini_player_stop")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Stop Audio",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (audioState.queueSize > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { AudioPlaybackService.prev(context) },
                            enabled = audioState.queueIndex > 0,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mini_player_prev")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = if (audioState.queueIndex > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            )
                        }
                        Text(
                            text = if (isAr) {
                                "قراءة مستمرة بالترتيب • ${audioState.queueIndex + 1} / ${audioState.queueSize}"
                            } else {
                                "Continuous Order • ${audioState.queueIndex + 1} / ${audioState.queueSize}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = { AudioPlaybackService.next(context) },
                            enabled = audioState.queueIndex + 1 < audioState.queueSize,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mini_player_next")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                tint = if (audioState.queueIndex + 1 < audioState.queueSize) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                val progress = if (audioState.durationMs > 0) {
                    (audioState.positionMs.toFloat() / audioState.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = GoldRoyal,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            }
        }
    }

    // Repeat Settings Sheet (3 Modes + Ayah Repeat Count)
    if (showRepeatSheet) {
        ModalBottomSheet(onDismissRequest = { showRepeatSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = s.repeatSettingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                val options = listOf(
                    Triple(
                        RepeatMode.CONTINUOUS,
                        s.repeatContinuous,
                        if (isAr) "الوضع الافتراضي: تشغيل جميع السور بالترتيب مرة واحدة دون تكرار." else "Default: play all Surahs sequentially once without repeating."
                    ),
                    Triple(
                        RepeatMode.REPEAT_SURAH,
                        s.repeatSurah,
                        if (isAr) "إعادة السورة الحالية فقط حتى توقفها أو تغير الوضع." else "Repeat the current Surah only."
                    ),
                    Triple(
                        RepeatMode.REPEAT_AYAH,
                        s.repeatAyah,
                        if (isAr) "إعادة الآية التي يختارها المستخدم حسب عدد التكرارات." else "Repeat the selected Ayah for the chosen count."
                    )
                )

                options.forEach { (mode, title, desc) ->
                    val selected = audioState.repeatMode == mode
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 0.8.dp,
                            color = if (selected) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetRepeatConfig(mode, null)
                                AudioPlaybackService.setRepeatConfig(context, mode, null)
                                if (mode != RepeatMode.REPEAT_AYAH) showRepeatSheet = false
                            }
                            .testTag("repeat_mode_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (selected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = GoldRoyal)
                            }
                        }
                    }
                }

                if (audioState.repeatMode == RepeatMode.REPEAT_AYAH) {
                    Text(
                        text = s.ayahRepeatCountLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 5, 7, 10, 0).forEach { count ->
                            val label = if (count == 0) s.unlimitedLabel else "${count}×"
                            FilterChip(
                                selected = audioState.ayahRepeatTarget == count,
                                onClick = {
                                    onSetRepeatConfig(RepeatMode.REPEAT_AYAH, count)
                                    AudioPlaybackService.setRepeatConfig(context, RepeatMode.REPEAT_AYAH, count)
                                    showRepeatSheet = false
                                },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("ayah_repeat_count_$count")
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Surah Selector Sheet from Audio Player ("خيار السورة")
    if (showSurahPickerSheet) {
        var filterQuery by remember { mutableStateOf("") }
        val filtered = remember(filterQuery) {
            val q = filterQuery.trim()
            if (q.isEmpty()) QuranCatalog.surahs
            else QuranCatalog.surahs.filter {
                it.arabicName.contains(q) ||
                    it.englishName.contains(q, ignoreCase = true) ||
                    it.number.toString() == q
            }
        }
        ModalBottomSheet(onDismissRequest = { showSurahPickerSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = s.chooseSurahLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text(s.searchQuranHint) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    items(filtered, key = { it.number }) { surah ->
                        val isCurrent = audioState.currentSurahNumber == surah.number
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSurah(surah.number)
                                    showSurahPickerSheet = false
                                }
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RubElHizbNumberBadge(number = surah.number, size = 34.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) "سورة ${surah.arabicName}" else surah.englishName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) GoldRoyal else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${surah.ayahCount} ${s.ayahsLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isCurrent) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = GoldRoyal)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
