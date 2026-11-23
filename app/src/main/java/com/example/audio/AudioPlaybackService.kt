package com.example.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RepeatMode {
    CONTINUOUS,     // Default: play all surahs/tracks sequentially once without repeating
    REPEAT_SURAH,   // Repeat the current surah only
    REPEAT_AYAH     // Repeat the selected ayah N times (0 = unlimited)
}

data class QueueTrack(
    val trackId: String,
    val source: String,
    val title: String,
    val subtitle: String,
    val surahNumber: Int = 0,
    val ayahNumber: Int = 0
)

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isCompleted: Boolean = false,
    val trackId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val currentSource: String = "",
    val currentSurahNumber: Int = 0,
    val currentAyahNumber: Int = 0,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.CONTINUOUS,
    val ayahRepeatTarget: Int = 3, // Used when repeatMode == REPEAT_AYAH (0 = unlimited)
    val ayahRepeatDone: Int = 0,
    val queueIndex: Int = 0,
    val queueSize: Int = 0,
    val volume: Float = 1.0f,
    val sleepTimerRemainingMs: Long = 0L,
    val sleepTimerMinutes: Int = 0,
    val loopWholeQueueOnRepeatSurah: Boolean = false,
    val errorMessage: String? = null
)

object AudioPlayerController {
    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    internal fun updateState(transform: (AudioPlaybackState) -> AudioPlaybackState) {
        _state.value = transform(_state.value)
    }

    fun shouldReplayAyahOnCompletion(
        repeatMode: RepeatMode,
        ayahRepeatTarget: Int,
        ayahRepeatDoneSoFar: Int
    ): Boolean {
        if (repeatMode != RepeatMode.REPEAT_AYAH) return false
        if (ayahRepeatTarget <= 0) return true // Unlimited when 0
        return (ayahRepeatDoneSoFar + 1) < ayahRepeatTarget
    }

    fun computeSeekTarget(currentMs: Long, deltaMs: Long, durationMs: Long): Long {
        val raw = currentMs + deltaMs
        return if (durationMs > 0L) raw.coerceIn(0L, durationMs) else raw.coerceAtLeast(0L)
    }
}

class AudioPlaybackService : Service() {

    private val playerLock = Any()
    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var playbackQueue: List<QueueTrack> = emptyList()
    private var currentQueueIndex: Int = 0
    private var playbackGeneration: Long = 0L
    private var lastCompletedGeneration: Long = -1L
    private var currentVolume: Float = 1.0f

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var resumeOnFocusGain: Boolean = false

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnFocusGain = false
                pauseInternal()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                val wasPlaying = AudioPlayerController.state.value.isPlaying
                resumeOnFocusGain = wasPlaying
                if (wasPlaying) pauseInternal()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(playerLock) {
                    try {
                        val ducked = (currentVolume * 0.25f).coerceIn(0f, 1f)
                        mediaPlayer?.setVolume(ducked, ducked)
                    } catch (_: Exception) {}
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                synchronized(playerLock) {
                    try {
                        mediaPlayer?.setVolume(currentVolume, currentVolume)
                    } catch (_: Exception) {}
                }
                if (resumeOnFocusGain) {
                    resumeOnFocusGain = false
                    resumeInternal()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val source = intent.getStringExtra(EXTRA_SOURCE) ?: return START_NOT_STICKY
                val trackId = intent.getStringExtra(EXTRA_TRACK_ID) ?: source
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "AL HIDAYA – الهداية"
                val subtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""
                val surahNum = intent.getIntExtra(EXTRA_SURAH_NUMBER, 0)
                val ayahNum = intent.getIntExtra(EXTRA_AYAH_NUMBER, 0)
                val requestedModeName = intent.getStringExtra(EXTRA_REPEAT_MODE)
                val requestedRepeatCount = intent.getIntExtra(EXTRA_AYAH_REPEAT_COUNT, -1)

                playbackQueue = listOf(
                    QueueTrack(
                        trackId = trackId,
                        source = source,
                        title = title,
                        subtitle = subtitle,
                        surahNumber = surahNum,
                        ayahNumber = ayahNum
                    )
                )
                currentQueueIndex = 0

                val targetMode = requestedModeName?.let {
                    runCatching { RepeatMode.valueOf(it) }.getOrNull()
                } ?: RepeatMode.CONTINUOUS

                AudioPlayerController.updateState { prev ->
                    prev.copy(
                        repeatMode = targetMode,
                        ayahRepeatTarget = if (requestedRepeatCount >= 0) requestedRepeatCount else prev.ayahRepeatTarget,
                        ayahRepeatDone = 0,
                        loopWholeQueueOnRepeatSurah = false
                    )
                }
                startPlaybackAtIndex(0)
            }

            ACTION_PLAY_QUEUE -> {
                val trackIds = intent.getStringArrayListExtra(EXTRA_QUEUE_IDS).orEmpty()
                val sources = intent.getStringArrayListExtra(EXTRA_QUEUE_SOURCES).orEmpty()
                val titles = intent.getStringArrayListExtra(EXTRA_QUEUE_TITLES).orEmpty()
                val subtitles = intent.getStringArrayListExtra(EXTRA_QUEUE_SUBTITLES).orEmpty()
                val surahNums = intent.getIntegerArrayListExtra(EXTRA_QUEUE_SURAHS).orEmpty()
                val ayahNums = intent.getIntegerArrayListExtra(EXTRA_QUEUE_AYAHS).orEmpty()
                val loopWholeQueue = intent.getBooleanExtra(EXTRA_LOOP_WHOLE_QUEUE, false)
                if (sources.isEmpty()) return START_NOT_STICKY

                // Deduplicate strictly by unique trackId so no Surah or Ayah is ever queued twice
                val rawTracks = sources.mapIndexed { idx, src ->
                    QueueTrack(
                        trackId = trackIds.getOrNull(idx) ?: "track_${idx}_$src",
                        source = src,
                        title = titles.getOrNull(idx) ?: "AL HIDAYA – الهداية",
                        subtitle = subtitles.getOrNull(idx) ?: "",
                        surahNumber = surahNums.getOrNull(idx) ?: 0,
                        ayahNumber = ayahNums.getOrNull(idx) ?: 0
                    )
                }.distinctBy { it.trackId }

                playbackQueue = rawTracks
                val startIdx = intent.getIntExtra(EXTRA_QUEUE_START_INDEX, 0).coerceIn(0, playbackQueue.lastIndex)
                currentQueueIndex = startIdx

                // Default mode is strictly CONTINUOUS (single-pass sequential, zero automatic repeat)
                AudioPlayerController.updateState {
                    it.copy(
                        repeatMode = RepeatMode.CONTINUOUS,
                        ayahRepeatDone = 0,
                        loopWholeQueueOnRepeatSurah = loopWholeQueue
                    )
                }
                startPlaybackAtIndex(startIdx)
            }

            ACTION_REPLAY -> {
                AudioPlayerController.updateState {
                    it.copy(repeatMode = RepeatMode.CONTINUOUS, ayahRepeatDone = 0)
                }
                if (playbackQueue.isNotEmpty()) {
                    startPlaybackAtIndex(0)
                } else {
                    val s = AudioPlayerController.state.value
                    if (s.currentSource.isNotBlank()) {
                        playbackQueue = listOf(
                            QueueTrack(
                                trackId = s.trackId.ifBlank { s.currentSource },
                                source = s.currentSource,
                                title = s.title,
                                subtitle = s.subtitle,
                                surahNumber = s.currentSurahNumber,
                                ayahNumber = s.currentAyahNumber
                            )
                        )
                        startPlaybackAtIndex(0)
                    }
                }
            }

            ACTION_NEXT -> {
                if (currentQueueIndex + 1 < playbackQueue.size) {
                    AudioPlayerController.updateState { it.copy(ayahRepeatDone = 0) }
                    startPlaybackAtIndex(currentQueueIndex + 1)
                }
            }

            ACTION_PREV -> {
                if (currentQueueIndex > 0 && playbackQueue.isNotEmpty()) {
                    AudioPlayerController.updateState { it.copy(ayahRepeatDone = 0) }
                    startPlaybackAtIndex(currentQueueIndex - 1)
                }
            }

            ACTION_JUMP_TO_INDEX -> {
                val targetIndex = intent.getIntExtra(EXTRA_QUEUE_START_INDEX, 0)
                if (targetIndex in playbackQueue.indices) {
                    AudioPlayerController.updateState { it.copy(ayahRepeatDone = 0) }
                    startPlaybackAtIndex(targetIndex)
                }
            }

            ACTION_TOGGLE_PAUSE -> togglePause()
            ACTION_PAUSE_ONLY -> pauseInternal()
            ACTION_STOP -> stopPlayback()

            ACTION_SET_SPEED -> {
                val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
                setSpeed(speed)
            }

            ACTION_CYCLE_REPEAT -> cycleRepeat()

            ACTION_SET_REPEAT_CONFIG -> {
                val modeName = intent.getStringExtra(EXTRA_REPEAT_MODE)
                val count = intent.getIntExtra(EXTRA_AYAH_REPEAT_COUNT, -1)
                val newMode = modeName?.let {
                    runCatching { RepeatMode.valueOf(it) }.getOrNull()
                } ?: AudioPlayerController.state.value.repeatMode
                setRepeatConfiguration(newMode, count)
            }

            ACTION_SEEK -> {
                val pos = intent.getLongExtra(EXTRA_SEEK_MS, 0L)
                seekToInternal(pos)
            }

            ACTION_SEEK_BY -> {
                val delta = intent.getLongExtra(EXTRA_SEEK_DELTA_MS, 0L)
                val snapshot = AudioPlayerController.state.value
                val target = AudioPlayerController.computeSeekTarget(
                    currentMs = snapshot.positionMs,
                    deltaMs = delta,
                    durationMs = snapshot.durationMs
                )
                seekToInternal(target)
            }

            ACTION_SET_VOLUME -> {
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 1.0f).coerceIn(0f, 1f)
                setVolumeInternal(volume)
            }

            ACTION_SET_SLEEP_TIMER -> {
                val minutes = intent.getIntExtra(EXTRA_SLEEP_TIMER_MINUTES, 0).coerceAtLeast(0)
                startSleepTimer(minutes)
            }
        }
        return START_NOT_STICKY
    }

    private fun startPlaybackAtIndex(index: Int) {
        val track = playbackQueue.getOrNull(index) ?: return
        currentQueueIndex = index

        val notification = buildNotification(track.title, track.subtitle, isPlaying = true)
        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            AudioPlayerController.updateState {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = e.localizedMessage ?: "Foreground playback not allowed"
                )
            }
            stopSelf()
            return
        }

        if (!requestAudioFocus()) {
            AudioPlayerController.updateState {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = "Audio focus unavailable"
                )
            }
            return
        }

        val myGeneration: Long
        synchronized(playerLock) {
            releasePlayerLocked()
            myGeneration = ++playbackGeneration
        }

        AudioPlayerController.updateState {
            it.copy(
                isPlaying = false,
                isBuffering = true,
                isCompleted = false,
                trackId = track.trackId,
                title = track.title,
                subtitle = track.subtitle,
                currentSource = track.source,
                currentSurahNumber = track.surahNumber,
                currentAyahNumber = track.ayahNumber,
                positionMs = 0L,
                durationMs = 0L,
                ayahRepeatDone = 0,
                queueIndex = index,
                queueSize = playbackQueue.size,
                errorMessage = null
            )
        }

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(track.source)
                setOnPreparedListener { player ->
                    synchronized(playerLock) {
                        if (myGeneration != playbackGeneration || player !== mediaPlayer) return@setOnPreparedListener
                    }
                    val snapshot = AudioPlayerController.state.value
                    if (snapshot.speed != 1.0f) {
                        try {
                            player.playbackParams = PlaybackParams().setSpeed(snapshot.speed)
                        } catch (_: Exception) {}
                    }
                    // Always manage repeat transitions explicitly in OnCompletionListener (never native looping)
                    try {
                        player.isLooping = false
                        player.setVolume(currentVolume, currentVolume)
                        player.start()
                        val durMs = try {
                            player.duration.toLong().coerceAtLeast(0L)
                        } catch (_: Exception) {
                            0L
                        }
                        AudioPlayerController.updateState {
                            it.copy(
                                isPlaying = true,
                                isBuffering = false,
                                isCompleted = false,
                                durationMs = durMs
                            )
                        }
                        updateNotification(track.title, track.subtitle, true)
                        startTicker()
                    } catch (e: Exception) {
                        AudioPlayerController.updateState {
                            it.copy(
                                isPlaying = false,
                                isBuffering = false,
                                errorMessage = e.localizedMessage
                            )
                        }
                    }
                }
                setOnCompletionListener { player ->
                    synchronized(playerLock) {
                        if (myGeneration != playbackGeneration || player !== mediaPlayer) return@setOnCompletionListener
                        if (lastCompletedGeneration == myGeneration) return@setOnCompletionListener
                        lastCompletedGeneration = myGeneration
                    }

                    val currentState = AudioPlayerController.state.value
                    when (currentState.repeatMode) {
                        RepeatMode.REPEAT_SURAH -> {
                            if (currentState.loopWholeQueueOnRepeatSurah && playbackQueue.size > 1) {
                                val nextIdx = if (currentQueueIndex + 1 < playbackQueue.size) currentQueueIndex + 1 else 0
                                startPlaybackAtIndex(nextIdx)
                            } else {
                                // Explicitly requested by user: repeat current Surah/track only
                                synchronized(playerLock) {
                                    lastCompletedGeneration = -1L
                                }
                                try {
                                    player.seekTo(0)
                                    player.start()
                                    AudioPlayerController.updateState { it.copy(positionMs = 0L, isPlaying = true) }
                                } catch (_: Exception) {
                                    markPlaybackCompleted(track.title, track.subtitle, currentState.ayahRepeatDone)
                                }
                            }
                        }
                        RepeatMode.REPEAT_AYAH -> {
                            if (AudioPlayerController.shouldReplayAyahOnCompletion(
                                    repeatMode = currentState.repeatMode,
                                    ayahRepeatTarget = currentState.ayahRepeatTarget,
                                    ayahRepeatDoneSoFar = currentState.ayahRepeatDone
                                )
                            ) {
                                synchronized(playerLock) {
                                    lastCompletedGeneration = -1L
                                }
                                try {
                                    AudioPlayerController.updateState {
                                        it.copy(ayahRepeatDone = it.ayahRepeatDone + 1, positionMs = 0L, isPlaying = true)
                                    }
                                    player.seekTo(0)
                                    player.start()
                                } catch (_: Exception) {
                                    markPlaybackCompleted(track.title, track.subtitle, currentState.ayahRepeatDone + 1)
                                }
                            } else if (currentQueueIndex + 1 < playbackQueue.size) {
                                startPlaybackAtIndex(currentQueueIndex + 1)
                            } else {
                                markPlaybackCompleted(track.title, track.subtitle, currentState.ayahRepeatDone + 1)
                            }
                        }
                        RepeatMode.CONTINUOUS -> {
                            // Default single-pass mode: advance strictly to the next Surah/track once; stop at end
                            if (currentQueueIndex + 1 < playbackQueue.size) {
                                startPlaybackAtIndex(currentQueueIndex + 1)
                            } else {
                                markPlaybackCompleted(track.title, track.subtitle, currentState.ayahRepeatDone)
                            }
                        }
                    }
                }
                setOnErrorListener { player, _, _ ->
                    synchronized(playerLock) {
                        if (myGeneration != playbackGeneration || player !== mediaPlayer) return@setOnErrorListener true
                    }
                    AudioPlayerController.updateState {
                        it.copy(
                            isPlaying = false,
                            isBuffering = false,
                            errorMessage = "Audio stream unavailable"
                        )
                    }
                    updateNotification(track.title, track.subtitle, false)
                    stopTicker()
                    true
                }
                prepareAsync()
            }
            synchronized(playerLock) {
                if (myGeneration == playbackGeneration) {
                    mediaPlayer = mp
                } else {
                    mp.release()
                }
            }
        } catch (e: Exception) {
            AudioPlayerController.updateState {
                it.copy(isPlaying = false, isBuffering = false, errorMessage = e.localizedMessage)
            }
        }
    }

    private fun markPlaybackCompleted(title: String, subtitle: String, finalAyahCount: Int) {
        AudioPlayerController.updateState {
            it.copy(
                isPlaying = false,
                isCompleted = true,
                ayahRepeatDone = finalAyahCount,
                positionMs = 0L
            )
        }
        updateNotification(title, subtitle, false)
        stopTicker()
        abandonAudioFocus()
    }

    private fun togglePause() {
        val state = AudioPlayerController.state.value
        if (state.isCompleted && playbackQueue.isNotEmpty()) {
            startPlaybackAtIndex(0)
            return
        }
        if (state.isPlaying) {
            pauseInternal()
        } else {
            resumeInternal()
        }
    }

    private fun pauseInternal() {
        synchronized(playerLock) {
            val mp = mediaPlayer ?: return
            try {
                if (mp.isPlaying) mp.pause()
            } catch (_: Exception) {}
        }
        AudioPlayerController.updateState { it.copy(isPlaying = false) }
        val s = AudioPlayerController.state.value
        if (s.currentSource.isNotBlank()) {
            updateNotification(s.title, s.subtitle, false)
        }
    }

    private fun resumeInternal() {
        if (!requestAudioFocus()) return
        synchronized(playerLock) {
            val mp = mediaPlayer ?: return
            try {
                mp.start()
            } catch (_: Exception) {
                return
            }
        }
        AudioPlayerController.updateState { it.copy(isPlaying = true, isCompleted = false) }
        val s = AudioPlayerController.state.value
        updateNotification(s.title, s.subtitle, true)
        startTicker()
    }

    private fun setSpeed(speed: Float) {
        AudioPlayerController.updateState { it.copy(speed = speed) }
        synchronized(playerLock) {
            val mp = mediaPlayer ?: return
            if (!AudioPlayerController.state.value.isBuffering) {
                try {
                    val wasPlaying = mp.isPlaying
                    mp.playbackParams = PlaybackParams().setSpeed(speed)
                    if (!wasPlaying) mp.pause()
                } catch (_: Exception) {}
            }
        }
    }

    private fun cycleRepeat() {
        val prev = AudioPlayerController.state.value
        val next = when (prev.repeatMode) {
            RepeatMode.CONTINUOUS -> RepeatMode.REPEAT_SURAH
            RepeatMode.REPEAT_SURAH -> RepeatMode.REPEAT_AYAH
            RepeatMode.REPEAT_AYAH -> RepeatMode.CONTINUOUS
        }
        setRepeatConfiguration(next, -1)
    }

    private fun setRepeatConfiguration(mode: RepeatMode, ayahRepeatCount: Int) {
        val prev = AudioPlayerController.state.value
        AudioPlayerController.updateState {
            it.copy(
                repeatMode = mode,
                ayahRepeatTarget = if (ayahRepeatCount >= 0) ayahRepeatCount else it.ayahRepeatTarget,
                ayahRepeatDone = 0
            )
        }
        if (prev.isCompleted && mode != RepeatMode.CONTINUOUS && playbackQueue.isNotEmpty()) {
            startPlaybackAtIndex(currentQueueIndex.coerceIn(0, playbackQueue.lastIndex))
        }
    }

    private fun stopPlayback() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        stopTicker()
        synchronized(playerLock) {
            releasePlayerLocked()
        }
        abandonAudioFocus()
        playbackQueue = emptyList()
        currentQueueIndex = 0
        AudioPlayerController.updateState { AudioPlaybackState(volume = currentVolume) }
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        stopSelf()
    }

    private fun seekToInternal(posMs: Long) {
        val clamped = synchronized(playerLock) {
            val mp = mediaPlayer
            val maxDur = try {
                mp?.duration?.toLong()?.takeIf { it > 0L }
            } catch (_: Exception) {
                null
            }
            val safePos = if (maxDur != null) posMs.coerceIn(0L, maxDur) else posMs.coerceAtLeast(0L)
            try {
                mp?.seekTo(safePos.toInt())
            } catch (_: Exception) {}
            safePos
        }
        AudioPlayerController.updateState { it.copy(positionMs = clamped) }
    }

    private fun setVolumeInternal(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        synchronized(playerLock) {
            try {
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            } catch (_: Exception) {}
        }
        AudioPlayerController.updateState { it.copy(volume = currentVolume) }
    }

    private fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        if (minutes <= 0) {
            AudioPlayerController.updateState {
                it.copy(sleepTimerMinutes = 0, sleepTimerRemainingMs = 0L)
            }
            return
        }
        val totalMs = minutes * 60_000L
        AudioPlayerController.updateState {
            it.copy(sleepTimerMinutes = minutes, sleepTimerRemainingMs = totalMs)
        }
        sleepTimerJob = serviceScope.launch {
            var remaining = totalMs
            while (remaining > 0L) {
                delay(1000L)
                remaining = (remaining - 1000L).coerceAtLeast(0L)
                AudioPlayerController.updateState { it.copy(sleepTimerRemainingMs = remaining) }
            }
            AudioPlayerController.updateState { it.copy(sleepTimerMinutes = 0, sleepTimerRemainingMs = 0L) }
            pauseInternal()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (true) {
                val (pos, dur, playing) = synchronized(playerLock) {
                    val mp = mediaPlayer
                    try {
                        if (mp != null && mp.isPlaying) {
                            Triple(mp.currentPosition.toLong().coerceAtLeast(0L), mp.duration.toLong().coerceAtLeast(0L), true)
                        } else {
                            Triple(0L, 0L, false)
                        }
                    } catch (_: Exception) {
                        Triple(0L, 0L, false)
                    }
                }
                if (playing) {
                    AudioPlayerController.updateState {
                        it.copy(positionMs = pos, durationMs = dur)
                    }
                }
                delay(400L)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun releasePlayerLocked() {
        playbackGeneration++
        lastCompletedGeneration = -1L
        val mp = mediaPlayer
        mediaPlayer = null
        if (mp != null) {
            try {
                mp.setOnPreparedListener(null)
                mp.setOnCompletionListener(null)
                mp.setOnErrorListener(null)
                if (mp.isPlaying) mp.stop()
            } catch (_: Exception) {}
            try {
                mp.release()
            } catch (_: Exception) {}
        }
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build()
                audioFocusRequest = req
                am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(focusChangeListener)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        sleepTimerJob?.cancel()
        stopTicker()
        synchronized(playerLock) {
            releasePlayerLocked()
        }
        abandonAudioFocus()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AL HIDAYA – الهداية • الصوتيات",
                NotificationManager.IMPORTANCE_LOW
            )
            runCatching { nm.createNotificationChannel(channel) }
        }
    }

    private fun updateNotification(title: String, subtitle: String, isPlaying: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        try {
            nm.notify(NOTIFICATION_ID, buildNotification(title, subtitle, isPlaying))
        } catch (_: SecurityException) {
        } catch (_: Exception) {}
    }

    private fun buildNotification(title: String, subtitle: String, isPlaying: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val togglePi = PendingIntent.getService(
            this,
            1,
            Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_TOGGLE_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextPi = PendingIntent.getService(
            this,
            3,
            Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getService(
            this,
            2,
            Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosque_notif)
            .setSubText("AL HIDAYA – الهداية")
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(openIntent)
            .setOngoing(isPlaying)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                togglePi
            )
        if (playbackQueue.size > 1) {
            builder.addAction(android.R.drawable.ic_media_next, "Next", nextPi)
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPi)
        return builder.build()
    }

    companion object {
        private const val CHANNEL_ID = "soufian_audio_channel"
        private const val NOTIFICATION_ID = 7001

        const val ACTION_PLAY = "com.example.audio.PLAY"
        const val ACTION_PLAY_QUEUE = "com.example.audio.PLAY_QUEUE"
        const val ACTION_REPLAY = "com.example.audio.REPLAY"
        const val ACTION_NEXT = "com.example.audio.NEXT"
        const val ACTION_PREV = "com.example.audio.PREV"
        const val ACTION_JUMP_TO_INDEX = "com.example.audio.JUMP_TO_INDEX"
        const val ACTION_TOGGLE_PAUSE = "com.example.audio.TOGGLE_PAUSE"
        const val ACTION_PAUSE_ONLY = "com.example.audio.PAUSE_ONLY"
        const val ACTION_STOP = "com.example.audio.STOP"
        const val ACTION_SET_SPEED = "com.example.audio.SET_SPEED"
        const val ACTION_CYCLE_REPEAT = "com.example.audio.CYCLE_REPEAT"
        const val ACTION_SET_REPEAT_CONFIG = "com.example.audio.SET_REPEAT_CONFIG"
        const val ACTION_SEEK = "com.example.audio.SEEK"
        const val ACTION_SEEK_BY = "com.example.audio.SEEK_BY"
        const val ACTION_SET_VOLUME = "com.example.audio.SET_VOLUME"
        const val ACTION_SET_SLEEP_TIMER = "com.example.audio.SET_SLEEP_TIMER"

        const val EXTRA_TRACK_ID = "extra_track_id"
        const val EXTRA_SOURCE = "extra_source"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_SURAH_NUMBER = "extra_surah_number"
        const val EXTRA_AYAH_NUMBER = "extra_ayah_number"
        const val EXTRA_QUEUE_IDS = "extra_queue_ids"
        const val EXTRA_QUEUE_SOURCES = "extra_queue_sources"
        const val EXTRA_QUEUE_TITLES = "extra_queue_titles"
        const val EXTRA_QUEUE_SUBTITLES = "extra_queue_subtitles"
        const val EXTRA_QUEUE_SURAHS = "extra_queue_surahs"
        const val EXTRA_QUEUE_AYAHS = "extra_queue_ayahs"
        const val EXTRA_QUEUE_START_INDEX = "extra_queue_start_index"
        const val EXTRA_LOOP_WHOLE_QUEUE = "extra_loop_whole_queue"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_REPEAT_MODE = "extra_repeat_mode"
        const val EXTRA_AYAH_REPEAT_COUNT = "extra_ayah_repeat_count"
        const val EXTRA_SEEK_MS = "extra_seek_ms"
        const val EXTRA_SEEK_DELTA_MS = "extra_seek_delta_ms"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_SLEEP_TIMER_MINUTES = "extra_sleep_timer_minutes"

        private fun startServiceSafely(context: Context, intent: Intent, foreground: Boolean) {
            try {
                if (foreground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                AudioPlayerController.updateState {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = e.localizedMessage ?: "Audio service unavailable"
                    )
                }
            }
        }

        fun startPlay(
            context: Context,
            urlOrPath: String,
            title: String,
            subtitle: String,
            trackId: String = urlOrPath,
            surahNumber: Int = 0,
            ayahNumber: Int = 0,
            repeatMode: RepeatMode = RepeatMode.CONTINUOUS,
            ayahRepeatCount: Int? = null
        ) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_TRACK_ID, trackId)
                putExtra(EXTRA_SOURCE, urlOrPath)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SUBTITLE, subtitle)
                putExtra(EXTRA_SURAH_NUMBER, surahNumber)
                putExtra(EXTRA_AYAH_NUMBER, ayahNumber)
                putExtra(EXTRA_REPEAT_MODE, repeatMode.name)
                if (ayahRepeatCount != null) {
                    putExtra(EXTRA_AYAH_REPEAT_COUNT, ayahRepeatCount)
                }
            }
            startServiceSafely(context, intent, foreground = true)
        }

        fun startQueue(
            context: Context,
            tracks: List<QueueTrack>,
            startIndex: Int = 0,
            loopWholeQueueOnRepeatSurah: Boolean = false
        ) {
            if (tracks.isEmpty()) return
            val uniqueTracks = tracks.distinctBy { it.trackId }
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_PLAY_QUEUE
                putStringArrayListExtra(EXTRA_QUEUE_IDS, ArrayList(uniqueTracks.map { it.trackId }))
                putStringArrayListExtra(EXTRA_QUEUE_SOURCES, ArrayList(uniqueTracks.map { it.source }))
                putStringArrayListExtra(EXTRA_QUEUE_TITLES, ArrayList(uniqueTracks.map { it.title }))
                putStringArrayListExtra(EXTRA_QUEUE_SUBTITLES, ArrayList(uniqueTracks.map { it.subtitle }))
                putIntegerArrayListExtra(EXTRA_QUEUE_SURAHS, ArrayList(uniqueTracks.map { it.surahNumber }))
                putIntegerArrayListExtra(EXTRA_QUEUE_AYAHS, ArrayList(uniqueTracks.map { it.ayahNumber }))
                putExtra(EXTRA_QUEUE_START_INDEX, startIndex.coerceIn(0, uniqueTracks.lastIndex))
                putExtra(EXTRA_LOOP_WHOLE_QUEUE, loopWholeQueueOnRepeatSurah)
            }
            startServiceSafely(context, intent, foreground = true)
        }

        fun replay(context: Context) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_REPLAY },
                foreground = true
            )
        }

        fun next(context: Context) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_NEXT },
                foreground = false
            )
        }

        fun prev(context: Context) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_PREV },
                foreground = false
            )
        }

        fun jumpToIndex(context: Context, index: Int) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply {
                    action = ACTION_JUMP_TO_INDEX
                    putExtra(EXTRA_QUEUE_START_INDEX, index)
                },
                foreground = false
            )
        }

        fun togglePause(context: Context) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_TOGGLE_PAUSE },
                foreground = false
            )
        }

        fun pauseIfPlaying(context: Context) {
            if (AudioPlayerController.state.value.isPlaying) {
                startServiceSafely(
                    context,
                    Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_PAUSE_ONLY },
                    foreground = false
                )
            }
        }

        fun stop(context: Context) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_STOP },
                foreground = false
            )
        }

        fun setSpeed(context: Context, speed: Float) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply {
                    action = ACTION_SET_SPEED
                    putExtra(EXTRA_SPEED, speed)
                },
                foreground = false
            )
        }

        fun cycleRepeat(context: Context) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_CYCLE_REPEAT },
                foreground = false
            )
        }

        fun setRepeatConfig(context: Context, mode: RepeatMode, ayahRepeatCount: Int? = null) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply {
                    action = ACTION_SET_REPEAT_CONFIG
                    putExtra(EXTRA_REPEAT_MODE, mode.name)
                    if (ayahRepeatCount != null) {
                        putExtra(EXTRA_AYAH_REPEAT_COUNT, ayahRepeatCount)
                    }
                },
                foreground = false
            )
        }

        fun seekTo(context: Context, ms: Long) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply {
                    action = ACTION_SEEK
                    putExtra(EXTRA_SEEK_MS, ms)
                },
                foreground = false
            )
        }

        fun seekBy(context: Context, deltaMs: Long) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply {
                    action = ACTION_SEEK_BY
                    putExtra(EXTRA_SEEK_DELTA_MS, deltaMs)
                },
                foreground = false
            )
        }

        fun setVolume(context: Context, volume: Float) {
            val clamped = volume.coerceIn(0f, 1f)
            AudioPlayerController.updateState { it.copy(volume = clamped) }
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply {
                    action = ACTION_SET_VOLUME
                    putExtra(EXTRA_VOLUME, clamped)
                },
                foreground = false
            )
        }

        fun setSleepTimer(context: Context, minutes: Int) {
            startServiceSafely(
                context,
                Intent(context, AudioPlaybackService::class.java).apply {
                    action = ACTION_SET_SLEEP_TIMER
                    putExtra(EXTRA_SLEEP_TIMER_MINUTES, minutes.coerceAtLeast(0))
                },
                foreground = false
            )
        }
    }
}

