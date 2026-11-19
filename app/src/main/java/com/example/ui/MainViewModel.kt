package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackService
import com.example.audio.AudioPlaybackState
import com.example.audio.AudioPlayerController
import com.example.audio.QueueTrack
import com.example.audio.RepeatMode
import com.example.data.QuranAndHadithRepository
import com.example.data.local.AdhkarProgressEntity
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedSurahAyahEntity
import com.example.data.local.UserPreferencesDataStore
import com.example.data.local.UserPrefsState
import com.example.data.seed.AdhanTrack
import com.example.data.seed.CitiesCatalog
import com.example.data.seed.CityOption
import com.example.data.seed.HadithBookInfo
import com.example.data.seed.HadithItem
import com.example.data.seed.HadithSeedData
import com.example.data.seed.QuranCatalog
import com.example.data.seed.RuqyahAudioTrack
import com.example.firebase.AuthManager
import com.example.firebase.FirebaseSyncRepository
import com.example.prayer.CalculationMethod
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.LocationHelper
import com.example.prayer.PrayerNotificationScheduler
import com.example.prayer.PrayerTimeCalculator
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val prefsStore = UserPreferencesDataStore(appContext)
    val repository = QuranAndHadithRepository(appContext, db, prefsStore)
    private val firebaseRepo = FirebaseSyncRepository(appContext)

    val prefsState: StateFlow<UserPrefsState> = prefsStore.prefsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPrefsState())

    val currentUser: StateFlow<FirebaseUser?> = AuthManager.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val bookmarks: StateFlow<List<BookmarkEntity>> = db.bookmarkDao().observeAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adhkarProgress: StateFlow<Map<String, AdhkarProgressEntity>> = db.adhkarProgressDao().observeAllProgress()
        .map { list -> list.associateBy { it.categoryId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val downloadedAudioKeys: StateFlow<Set<String>> = repository.observeAllDownloads()
        .map { list -> list.map { it.audioKey }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val cachedSurahNumbers: StateFlow<Set<Int>> = repository.observeCachedSurahNumbers()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val audioState: StateFlow<AudioPlaybackState> = AudioPlayerController.state

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())

    val prayerSchedule: StateFlow<DailyPrayerSchedule> = combine(prefsState, _nowMillis) { prefs, now ->
        PrayerTimeCalculator.calculateDailySchedule(
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            method = CalculationMethod.fromId(prefs.calculationMethod),
            nowMillis = now,
            timeZone = CitiesCatalog.findTimeZone(prefs.cityNameEn, prefs.latitude, prefs.longitude)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PrayerTimeCalculator.calculateDailySchedule(
            latitude = 33.5731,
            longitude = -7.5898,
            method = CalculationMethod.MOROCCO,
            timeZone = java.util.TimeZone.getTimeZone("Africa/Casablanca")
        )
    )

    val qiblaBearing: StateFlow<Double> = prefsState.map { prefs ->
        PrayerTimeCalculator.calculateQiblaBearing(prefs.latitude, prefs.longitude)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 94.0)

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _downloadingKeys = MutableStateFlow<Set<String>>(emptySet())
    val downloadingKeys: StateFlow<Set<String>> = _downloadingKeys.asStateFlow()

    private var cloudSyncJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureStarterDataSeeded()
            PrayerNotificationScheduler.scheduleDailyPrayers(appContext, prefsStore.prefsFlow.first())
        }
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _nowMillis.value = System.currentTimeMillis()
            }
        }
        viewModelScope.launch {
            currentUser.collectLatest { user ->
                cloudSyncJob?.cancel()
                if (user != null) {
                    startCloudListeners(user)
                }
            }
        }
        viewModelScope.launch {
            audioState
                .map { it.currentSurahNumber to it.currentAyahNumber }
                .distinctUntilChanged()
                .collectLatest { (sNum, aNum) ->
                    if (sNum in 1..114) {
                        val sInfo = QuranCatalog.surahs.getOrNull(sNum - 1) ?: return@collectLatest
                        val safeAyah = aNum.coerceAtLeast(1)
                        prefsStore.setLastRead(sNum, safeAyah, sInfo.arabicName)
                    }
                }
        }
    }

    private fun startCloudListeners(user: FirebaseUser) {
        cloudSyncJob = viewModelScope.launch {
            // Sync initial local profile to cloud if user is newly signed in
            val localPrefs = prefsStore.prefsFlow.first()
            firebaseRepo.syncUserProfile(localPrefs, user.displayName ?: "Muslim User")

            launch {
                firebaseRepo.observeBookmarks(user.uid)
                    .catch { /* logged in repo */ }
                    .collectLatest { cloudBookmarks ->
                        if (cloudBookmarks.isNotEmpty()) {
                            val localMapped = cloudBookmarks.map { doc ->
                                BookmarkEntity(
                                    referenceKey = doc.referenceKey,
                                    itemType = doc.itemType,
                                    title = doc.title,
                                    subtitle = doc.subtitle,
                                    arabicText = doc.arabicText,
                                    updatedAtMillis = doc.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
                                )
                            }
                            db.bookmarkDao().upsertBookmarks(localMapped)
                        }
                    }
            }
        }
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            prefsStore.setLanguage(lang)
            syncProfileIfSignedIn()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefsStore.setThemeMode(mode)
            syncProfileIfSignedIn()
        }
    }

    fun setQuranFontSize(size: Int) {
        viewModelScope.launch {
            prefsStore.setQuranFontSize(size)
            syncProfileIfSignedIn()
        }
    }

    fun selectCity(city: CityOption) {
        viewModelScope.launch {
            prefsStore.setLocation(city.nameAr, city.nameEn, city.latitude, city.longitude)
            prefsStore.setCalculationMethod(city.defaultMethod)
            val updated = prefsStore.prefsFlow.first()
            PrayerNotificationScheduler.scheduleDailyPrayers(appContext, updated)
            syncProfileIfSignedIn()
        }
    }

    fun detectGpsLocation() {
        viewModelScope.launch {
            val res = LocationHelper.detectCurrentLocation(appContext)
            res.onSuccess { loc ->
                prefsStore.setLocation(loc.cityAr, loc.cityEn, loc.latitude, loc.longitude)
                val updated = prefsStore.prefsFlow.first()
                PrayerNotificationScheduler.scheduleDailyPrayers(appContext, updated)
                syncProfileIfSignedIn()
            }.onFailure {
                _statusBanner.value = if (prefsState.value.language == "ar") {
                    "تعذر تحديد الموقع تلقائياً، يمكنك اختيار المدينة يدوياً."
                } else {
                    "Could not detect GPS location; please pick your city manually."
                }
            }
        }
    }

    fun setCalculationMethod(methodId: String) {
        viewModelScope.launch {
            prefsStore.setCalculationMethod(methodId)
            PrayerNotificationScheduler.scheduleDailyPrayers(appContext, prefsStore.prefsFlow.first())
            syncProfileIfSignedIn()
        }
    }

    fun setPrayerEnabled(prayerKey: String, enabled: Boolean) {
        viewModelScope.launch {
            prefsStore.setPrayerEnabled(prayerKey, enabled)
            PrayerNotificationScheduler.scheduleDailyPrayers(appContext, prefsStore.prefsFlow.first())
        }
    }

    fun assignPrayerAdhan(prayerKey: String, adhanId: String) {
        viewModelScope.launch {
            prefsStore.setPrayerAdhan(prayerKey, adhanId)
            PrayerNotificationScheduler.scheduleDailyPrayers(appContext, prefsStore.prefsFlow.first())
            _statusBanner.value = if (prefsState.value.language == "ar") {
                "تم تحديث صوت الأذان بنجاح"
            } else {
                "Adhan sound updated"
            }
        }
    }

    fun updateLastRead(surahNumber: Int, ayahNumber: Int, surahName: String) {
        viewModelScope.launch {
            prefsStore.setLastRead(surahNumber, ayahNumber, surahName)
            syncProfileIfSignedIn()
        }
    }

    fun toggleBookmark(
        referenceKey: String,
        itemType: String,
        title: String,
        subtitle: String,
        arabicText: String
    ) {
        viewModelScope.launch {
            val existing = bookmarks.value.any { it.referenceKey == referenceKey }
            if (existing) {
                db.bookmarkDao().deleteBookmarkByKey(referenceKey)
                if (currentUser.value != null) {
                    firebaseRepo.deleteBookmark(referenceKey)
                }
            } else {
                val entity = BookmarkEntity(
                    referenceKey = referenceKey,
                    itemType = itemType,
                    title = title,
                    subtitle = subtitle,
                    arabicText = arabicText,
                    updatedAtMillis = System.currentTimeMillis()
                )
                db.bookmarkDao().upsertBookmark(entity)
                if (currentUser.value != null) {
                    firebaseRepo.upsertBookmark(entity)
                }
            }
        }
    }

    fun updateAdhkarProgress(categoryId: String, completed: Int, target: Int) {
        viewModelScope.launch {
            val entity = AdhkarProgressEntity(
                categoryId = categoryId,
                completedCount = completed.coerceAtLeast(0),
                targetCount = target.coerceAtLeast(1),
                updatedAtMillis = System.currentTimeMillis()
            )
            db.adhkarProgressDao().upsertProgress(entity)
            if (currentUser.value != null) {
                firebaseRepo.syncAdhkarProgress(entity)
            }
        }
    }

    fun incrementTasbih(delta: Int = 1) {
        viewModelScope.launch {
            prefsStore.incrementTasbihTotal(delta)
            syncProfileIfSignedIn()
        }
    }

    fun setTasbihTarget(target: Int) {
        viewModelScope.launch { prefsStore.setTasbihTarget(target) }
    }

    fun playSurahAbdullahMousa(surahNumber: Int) {
        viewModelScope.launch {
            val surah = QuranCatalog.surahs.getOrNull(surahNumber - 1) ?: return@launch
            val srcRes = repository.getSurahPlaybackSource(surahNumber)
            srcRes.onSuccess { urlOrPath ->
                val isAr = prefsState.value.language == "ar"
                val title = if (isAr) "سورة ${surah.arabicName}" else "Surah ${surah.englishName}"
                val sub = if (isAr) "القارئ: عبدالله بن موسى" else "Reciter: Abdullah bin Mousa"
                AudioPlaybackService.startPlay(
                    context = appContext,
                    urlOrPath = urlOrPath,
                    title = title,
                    subtitle = sub,
                    trackId = "surah_${surahNumber.toString().padStart(3, '0')}",
                    surahNumber = surahNumber,
                    repeatMode = RepeatMode.CONTINUOUS
                )
            }.onFailure {
                _statusBanner.value = if (prefsState.value.language == "ar") {
                    "يرجى الاتصال بالإنترنت لتشغيل التلاوة أو تحميلها أولاً"
                } else {
                    "Connect to the internet to stream or download this recitation."
                }
            }
        }
    }

    fun playSingleAyahAudio(surahNumber: Int, globalAyahNumber: Int, ayahNumber: Int) {
        val surah = QuranCatalog.surahs.getOrNull(surahNumber - 1) ?: return
        val url = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$globalAyahNumber.mp3"
        val isAr = prefsState.value.language == "ar"
        val current = audioState.value
        val activeRepeatMode = if (current.repeatMode == RepeatMode.REPEAT_AYAH) RepeatMode.REPEAT_AYAH else RepeatMode.CONTINUOUS
        val title = if (isAr) "سورة ${surah.arabicName} • الآية $ayahNumber" else "${surah.englishName} • Ayah $ayahNumber"
        val sub = if (isAr) "تلاوة الآية" else "Ayah Recitation"
        AudioPlaybackService.startPlay(
            context = appContext,
            urlOrPath = url,
            title = title,
            subtitle = sub,
            trackId = "ayah_${surahNumber}_$ayahNumber",
            surahNumber = surahNumber,
            ayahNumber = ayahNumber,
            repeatMode = activeRepeatMode,
            ayahRepeatCount = current.ayahRepeatTarget
        )
    }

    fun playQuranOnceFromSurah(startSurah: Int) {
        viewModelScope.launch {
            val safeStart = startSurah.coerceIn(1, 114)
            val isAr = prefsState.value.language == "ar"
            val queueRes = repository.getSurahQueueSources(1)
            queueRes.onSuccess { pairs ->
                val tracks = pairs.mapNotNull { (sNum, src) ->
                    val sInfo = QuranCatalog.surahs.getOrNull(sNum - 1) ?: return@mapNotNull null
                    QueueTrack(
                        trackId = "surah_${sNum.toString().padStart(3, '0')}",
                        source = src,
                        title = if (isAr) "سورة ${sInfo.arabicName}" else "Surah ${sInfo.englishName}",
                        subtitle = if (isAr) "عبدالله بن موسى" else "Abdullah bin Mousa",
                        surahNumber = sNum
                    )
                }
                val startIdx = tracks.indexOfFirst { it.surahNumber >= safeStart }.coerceAtLeast(0)
                AudioPlaybackService.startQueue(appContext, tracks, startIdx)
            }.onFailure {
                _statusBanner.value = if (isAr) {
                    "يرجى الاتصال بالإنترنت لتشغيل التلاوة أو تحميلها أولاً"
                } else {
                    "Connect to the internet to stream or download this recitation."
                }
            }
        }
    }

    fun jumpToSurahInPlayer(surahNumber: Int) {
        val current = audioState.value
        if (current.queueSize == 114) {
            AudioPlaybackService.jumpToIndex(appContext, (surahNumber - 1).coerceIn(0, 113))
        } else {
            playQuranOnceFromSurah(surahNumber)
        }
    }

    fun setRepeatConfig(mode: RepeatMode, ayahRepeatCount: Int? = null) {
        AudioPlaybackService.setRepeatConfig(appContext, mode, ayahRepeatCount)
    }

    fun playPageAyahsOnce(pageNumber: Int, ayahs: List<CachedSurahAyahEntity>) {
        if (ayahs.isEmpty()) return
        val isAr = prefsState.value.language == "ar"
        val tracks = ayahs
            .distinctBy { it.globalKey }
            .sortedBy { it.globalAyahNumber }
            .map { ayah ->
                val surahName = QuranCatalog.surahs.getOrNull(ayah.surahNumber - 1)?.let {
                    if (isAr) it.arabicName else it.englishName
                } ?: "${ayah.surahNumber}"
                QueueTrack(
                    trackId = "ayah_${ayah.surahNumber}_${ayah.ayahNumber}",
                    source = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${ayah.globalAyahNumber}.mp3",
                    title = if (isAr) "صفحة $pageNumber • $surahName (${ayah.ayahNumber})" else "Page $pageNumber • $surahName (${ayah.ayahNumber})",
                    subtitle = if (isAr) "تلاوة الصفحة متصلة بدون تكرار" else "Sequential page reading",
                    surahNumber = ayah.surahNumber,
                    ayahNumber = ayah.ayahNumber
                )
            }
        AudioPlaybackService.startQueue(appContext, tracks)
    }

    fun playRuqyahTrack(track: RuqyahAudioTrack) {
        viewModelScope.launch {
            val isAr = prefsState.value.language == "ar"
            repository.getRuqyahPlaybackSource(track).onSuccess { src ->
                AudioPlaybackService.startPlay(
                    context = appContext,
                    urlOrPath = src,
                    title = if (isAr) track.titleAr else track.titleEn,
                    subtitle = if (isAr) track.reciterAr else track.reciterEn,
                    trackId = track.id,
                    repeatMode = RepeatMode.CONTINUOUS
                )
            }.onFailure {
                _statusBanner.value = if (isAr) {
                    "تعذر تشغيل مقطع الرقية الشرعية، تحقق من الاتصال أو استخدم النص المقروء"
                } else {
                    "Unable to stream Ruqyah audio offline. Check connection or read text below."
                }
            }
        }
    }

    fun downloadRuqyahTrack(track: RuqyahAudioTrack) {
        if (_downloadingKeys.value.contains(track.id)) return
        viewModelScope.launch {
            _downloadingKeys.value = _downloadingKeys.value + track.id
            val res = repository.downloadRuqyahAudio(track)
            _downloadingKeys.value = _downloadingKeys.value - track.id
            res.onFailure {
                _statusBanner.value = if (prefsState.value.language == "ar") {
                    "تعذر تحميل مقطع الرقية، تحقق من الاتصال"
                } else {
                    "Failed to download Ruqyah audio track."
                }
            }
        }
    }

    fun downloadSurahForOffline(surahNumber: Int) {
        val key = repository.getSurahAudioKey(surahNumber)
        if (_downloadingKeys.value.contains(key)) return
        viewModelScope.launch {
            _downloadingKeys.value = _downloadingKeys.value + key
            repository.loadSurahFromNetworkIfNeeded(surahNumber)
            val res = repository.downloadSurahAudio(surahNumber)
            _downloadingKeys.value = _downloadingKeys.value - key
            res.onFailure {
                _statusBanner.value = if (prefsState.value.language == "ar") {
                    "تعذر تحميل الملف الصوتي، تحقق من اتصال الإنترنت"
                } else {
                    "Audio download failed; check your connection."
                }
            }
        }
    }

    fun previewAdhan(track: AdhanTrack) {
        viewModelScope.launch {
            val source = repository.getAdhanPlaybackSource(track)
            val isAr = prefsState.value.language == "ar"
            AudioPlaybackService.startPlay(
                context = appContext,
                urlOrPath = source,
                title = if (isAr) track.titleAr else track.titleEn,
                subtitle = if (isAr) track.locationAr else track.locationEn,
                trackId = track.id,
                repeatMode = RepeatMode.CONTINUOUS
            )
        }
    }

    fun downloadAdhanForOffline(track: AdhanTrack) {
        if (_downloadingKeys.value.contains(track.id)) return
        viewModelScope.launch {
            _downloadingKeys.value = _downloadingKeys.value + track.id
            val res = repository.downloadAdhanAudio(track)
            _downloadingKeys.value = _downloadingKeys.value - track.id
            res.onFailure {
                _statusBanner.value = if (prefsState.value.language == "ar") {
                    "تعذر تحميل الأذان، تحقق من الاتصال"
                } else {
                    "Failed to download Adhan track."
                }
            }
        }
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    suspend fun loadHadithChapter(book: HadithBookInfo, chapter: Int): List<HadithItem> =
        repository.loadHadithChapter(book, chapter)

    suspend fun loadSurahAyahs(surahNumber: Int): Result<List<CachedSurahAyahEntity>> =
        repository.loadSurahFromNetworkIfNeeded(surahNumber)

    suspend fun loadMushafPage(pageNumber: Int): Result<List<CachedSurahAyahEntity>> =
        repository.loadMushafPage(pageNumber)

    suspend fun searchQuran(query: String): List<CachedSurahAyahEntity> =
        repository.searchQuranVerses(query)

    private fun syncProfileIfSignedIn() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            firebaseRepo.syncUserProfile(prefsState.value, user.displayName ?: "Muslim User")
        }
    }
}
