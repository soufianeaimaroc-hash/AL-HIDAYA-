package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.audio.QueueTrack
import com.example.data.local.AppDatabase
import com.example.data.local.CachedSurahAyahEntity
import com.example.data.local.CachedSurahEntity
import com.example.data.local.DownloadedAudioEntity
import com.example.data.local.UserPreferencesDataStore
import com.example.data.remote.AlQuranSurahMetaDto
import com.example.data.remote.ArchiveMetadataResponse
import com.example.data.remote.NetworkModule
import com.example.data.seed.AdhanTrack
import com.example.data.seed.BundledQuranLoader
import com.example.data.seed.HadithBookInfo
import com.example.data.seed.HadithItem
import com.example.data.seed.HadithSeedData
import com.example.data.seed.QuranCatalog
import com.example.data.seed.RuqyahAudioSource
import com.example.data.seed.RuqyahAudioTrack
import com.example.data.seed.RuqyahCatalog
import com.example.data.seed.SurahInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.URLEncoder

data class RuqyahQueuePlan(
    val tracks: List<QueueTrack>,
    val resolvedReciterNameAr: String,
    val resolvedReciterNameEn: String,
    val loopWholeQueue: Boolean
)

class QuranAndHadithRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val prefsStore: UserPreferencesDataStore
) {
    private val quranDao = db.quranCacheDao()
    private val downloadedDao = db.downloadedAudioDao()
    private val verifiedFullPages = java.util.Collections.synchronizedSet(mutableSetOf<Int>())

    @Volatile
    private var availableReciterSurahs: Set<Int>? = null

    suspend fun ensureStarterDataSeeded() = withContext(Dispatchers.IO) {
        if (quranDao.getCachedSurahsOnce().size < 114) {
            quranDao.upsertSurahs(
                QuranCatalog.surahs.map { s -> s.toCachedEntity(syncedAtMillis = 0L) }
            )
        }
        val fatihahLocal = quranDao.getSurahAyahsOnce(1)
        val page604Local = quranDao.getPageAyahsOnce(604)
        if (
            fatihahLocal.size < 7 ||
            fatihahLocal.any { it.arabicTextUthmani.isBlank() } ||
            page604Local.size < 15 ||
            page604Local.any { it.arabicTextUthmani.isBlank() }
        ) {
            quranDao.upsertAyahs(QuranCatalog.offlineStarterAyahs)
        }
    }

    fun observeSurahs(): Flow<List<SurahInfo>> =
        quranDao.observeCachedSurahs().map { entities ->
            if (entities.size >= 114) {
                entities.map { it.toDomainInfo() }
            } else if (entities.isEmpty()) {
                emptyList()
            } else {
                val byNum = entities.associateBy { it.number }
                QuranCatalog.surahs.map { fallback ->
                    byNum[fallback.number]?.toDomainInfo() ?: fallback
                }
            }
        }

    suspend fun syncSurahCatalog(forceRefresh: Boolean = false): Result<List<SurahInfo>> =
        withContext(Dispatchers.IO) {
            val cached = quranDao.getCachedSurahsOnce().map { it.toDomainInfo() }
            try {
                val resp = NetworkModule.alQuranApi.getSurahList()
                val code = resp.code ?: 0
                if (code != 200) {
                    throw IllegalStateException("Quran API returned status code $code")
                }
                val synced = parseSurahListFromApiDtos(resp.data, QuranCatalog.surahs)
                if (synced.size < 114) {
                    throw IllegalStateException("Incomplete Surah catalog received (${synced.size}/114)")
                }
                val now = System.currentTimeMillis()
                quranDao.upsertSurahs(synced.map { it.toCachedEntity(syncedAtMillis = now) })
                Result.success(synced)
            } catch (e: Exception) {
                if (!forceRefresh && cached.size >= 114) {
                    Result.success(cached)
                } else {
                    Result.failure(e)
                }
            }
        }

    fun observeSurahAyahs(surahNumber: Int): Flow<List<CachedSurahAyahEntity>> =
        quranDao.observeSurahAyahs(surahNumber)

    fun observeCachedSurahNumbers(): Flow<List<Int>> =
        quranDao.observeCachedSurahNumbers()

    fun observeAllDownloads(): Flow<List<DownloadedAudioEntity>> =
        downloadedDao.observeAllDownloads()

    suspend fun getInstantSurahAyahs(surahNumber: Int): List<CachedSurahAyahEntity> =
        withContext(Dispatchers.IO) {
            val safeSurah = surahNumber.coerceIn(1, 114)
            val expectedCount = QuranCatalog.surahs.getOrNull(safeSurah - 1)?.ayahCount ?: 0
            val local = quranDao.getSurahAyahsOnce(safeSurah)
                .distinctBy { it.globalKey }
                .sortedBy { it.ayahNumber }
                .map { entity ->
                    val cleaned = BundledQuranLoader.cleanUthmaniAyahText(
                        entity.surahNumber,
                        entity.ayahNumber,
                        entity.arabicTextUthmani
                    )
                    if (cleaned == entity.arabicTextUthmani) entity else entity.copy(arabicTextUthmani = cleaned)
                }
            val localComplete = local.size >= expectedCount &&
                local.isNotEmpty() &&
                local.all { it.arabicTextUthmani.isNotBlank() }
            if (localComplete) {
                return@withContext local
            }
            val bundled = BundledQuranLoader.getSurahAyahs(context, safeSurah)
            if (bundled.isEmpty()) return@withContext local.filter { it.arabicTextUthmani.isNotBlank() }
            val merged = mergePreservingTextAndTafsir(
                incoming = bundled,
                existingByKey = local.associateBy { it.globalKey }
            )
            quranDao.upsertAyahs(merged)
            merged
        }

    suspend fun loadSurahFromNetworkIfNeeded(surahNumber: Int, forceRefresh: Boolean = false): Result<List<CachedSurahAyahEntity>> =
        withContext(Dispatchers.IO) {
            val safeSurah = surahNumber.coerceIn(1, 114)
            val fallbackSurah = QuranCatalog.surahs.getOrNull(safeSurah - 1)
            val expectedCount = fallbackSurah?.ayahCount ?: 0
            val local = quranDao.getSurahAyahsOnce(safeSurah)
                .distinctBy { it.globalKey }
                .sortedBy { it.ayahNumber }
                .map { entity ->
                    val cleaned = BundledQuranLoader.cleanUthmaniAyahText(
                        entity.surahNumber,
                        entity.ayahNumber,
                        entity.arabicTextUthmani
                    )
                    if (cleaned == entity.arabicTextUthmani) entity else entity.copy(arabicTextUthmani = cleaned)
                }
            val localComplete = local.size >= expectedCount &&
                local.isNotEmpty() &&
                local.all { it.arabicTextUthmani.isNotBlank() }
            val bundled = BundledQuranLoader.getSurahAyahs(context, safeSurah)
            val instantBaseline = if (localComplete) {
                local
            } else if (bundled.isNotEmpty()) {
                val mergedBundled = mergePreservingTextAndTafsir(bundled, local.associateBy { it.globalKey })
                quranDao.upsertAyahs(mergedBundled)
                mergedBundled
            } else {
                local.filter { it.arabicTextUthmani.isNotBlank() }
            }

            val hasAllEnrichment = instantBaseline.size >= expectedCount &&
                instantBaseline.isNotEmpty() &&
                instantBaseline.all { it.englishTranslation.isNotBlank() && it.arabicTafsir.isNotBlank() }

            if (!forceRefresh && hasAllEnrichment) {
                return@withContext Result.success(instantBaseline)
            }
            if (!isNetworkConnected()) {
                return@withContext if (instantBaseline.isNotEmpty()) Result.success(instantBaseline)
                else Result.failure(IllegalStateException("Offline"))
            }

            try {
                var entities: List<CachedSurahAyahEntity> = emptyList()
                val response = withTimeoutOrNull(8_000L) {
                    try {
                        NetworkModule.alQuranApi.getSurahWithTafsirAndTranslation(safeSurah)
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (_: Exception) {
                        null
                    }
                }
                val editions = response?.data.orEmpty()
                val uthmani = editions.getOrNull(0)?.ayahs.orEmpty()
                val english = editions.getOrNull(1)?.ayahs.orEmpty()
                val tafsir = editions.getOrNull(2)?.ayahs.orEmpty()

                if (uthmani.isNotEmpty()) {
                    entities = uthmani.mapIndexedNotNull { idx, ayah ->
                        val ayahNum = ayah.numberInSurah ?: 0
                        val text = BundledQuranLoader.cleanUthmaniAyahText(safeSurah, ayahNum, ayah.text.orEmpty())
                        if (ayahNum <= 0 || text.isBlank()) return@mapIndexedNotNull null
                        CachedSurahAyahEntity(
                            globalKey = "${safeSurah}_$ayahNum",
                            surahNumber = safeSurah,
                            ayahNumber = ayahNum,
                            globalAyahNumber = ayah.number ?: ayahNum,
                            juz = (ayah.juz ?: fallbackSurah?.startJuz ?: 1).coerceIn(1, 30),
                            hizbQuarter = (ayah.hizbQuarter ?: 1).coerceAtLeast(1),
                            page = (ayah.page ?: fallbackSurah?.startPage ?: 1).coerceIn(1, 604),
                            arabicTextUthmani = text,
                            englishTranslation = english.getOrNull(idx)?.text?.trim().orEmpty(),
                            arabicTafsir = tafsir.getOrNull(idx)?.text?.trim().orEmpty()
                        )
                    }
                }

                // Fallback to single Uthmani edition only if baseline is somehow empty
                if (entities.isEmpty() && instantBaseline.isEmpty()) {
                    val singleResp = withTimeoutOrNull(6_000L) {
                        NetworkModule.alQuranApi.getSingleSurahUthmani(safeSurah)
                    }
                    val singleAyahs = singleResp?.data?.ayahs.orEmpty()
                    entities = singleAyahs.mapNotNull { ayah ->
                        val ayahNum = ayah.numberInSurah ?: 0
                        val text = BundledQuranLoader.cleanUthmaniAyahText(safeSurah, ayahNum, ayah.text.orEmpty())
                        if (ayahNum <= 0 || text.isBlank()) return@mapNotNull null
                        CachedSurahAyahEntity(
                            globalKey = "${safeSurah}_$ayahNum",
                            surahNumber = safeSurah,
                            ayahNumber = ayahNum,
                            globalAyahNumber = ayah.number ?: ayahNum,
                            juz = (ayah.juz ?: fallbackSurah?.startJuz ?: 1).coerceIn(1, 30),
                            hizbQuarter = (ayah.hizbQuarter ?: 1).coerceAtLeast(1),
                            page = (ayah.page ?: fallbackSurah?.startPage ?: 1).coerceIn(1, 604),
                            arabicTextUthmani = text,
                            englishTranslation = "",
                            arabicTafsir = ""
                        )
                    }
                }

                val deduped = entities.distinctBy { it.globalKey }.sortedBy { it.ayahNumber }
                if (deduped.isNotEmpty()) {
                    val baseList = if (instantBaseline.size > deduped.size) {
                        val dedupedByKey = deduped.associateBy { it.globalKey }
                        mergePreservingTextAndTafsir(
                            incoming = instantBaseline,
                            existingByKey = dedupedByKey
                        )
                    } else {
                        mergePreservingTextAndTafsir(
                            incoming = deduped,
                            existingByKey = instantBaseline.associateBy { it.globalKey }
                        )
                    }
                    quranDao.upsertAyahs(baseList)
                    Result.success(baseList)
                } else if (instantBaseline.isNotEmpty()) {
                    Result.success(instantBaseline)
                } else {
                    Result.failure(IllegalStateException("Empty Quran API response"))
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                if (instantBaseline.isNotEmpty()) Result.success(instantBaseline)
                else Result.failure(e)
            }
        }

    suspend fun loadMushafPage(pageNumber: Int, forceRefresh: Boolean = false): Result<List<CachedSurahAyahEntity>> =
        withContext(Dispatchers.IO) {
            val safePage = pageNumber.coerceIn(1, 604)
            val local = quranDao.getPageAyahsOnce(safePage)
                .distinctBy { it.globalKey }
                .sortedBy { it.globalAyahNumber }
            if (!forceRefresh && verifiedFullPages.contains(safePage) && local.isNotEmpty() && local.all { it.arabicTextUthmani.isNotBlank() }) {
                return@withContext Result.success(local)
            }

            val bundledPage = BundledQuranLoader.getPageAyahs(context, safePage)
            if (!forceRefresh && bundledPage.isNotEmpty()) {
                val existingByKey = quranDao.getAyahsByKeys(bundledPage.map { it.globalKey })
                    .associateBy { it.globalKey }
                val merged = mergePreservingTextAndTafsir(bundledPage, existingByKey)
                quranDao.upsertAyahs(merged)
                verifiedFullPages.add(safePage)
                return@withContext Result.success(merged)
            }

            try {
                val resp = withTimeoutOrNull(8_000L) {
                    NetworkModule.alQuranApi.getMushafPage(safePage)
                } ?: return@withContext if (local.isNotEmpty()) Result.success(local)
                else Result.failure(IllegalStateException("Timed out loading Mushaf page"))

                val ayahs = resp.data?.ayahs.orEmpty()
                val rawMapped = ayahs.mapNotNull { dto ->
                    val ayahNum = dto.numberInSurah ?: 0
                    val sNum = (dto.surah?.number ?: 1).coerceIn(1, 114)
                    val text = BundledQuranLoader.cleanUthmaniAyahText(sNum, ayahNum, dto.text.orEmpty())
                    if (ayahNum <= 0 || text.isBlank()) return@mapNotNull null
                    CachedSurahAyahEntity(
                        globalKey = "${sNum}_$ayahNum",
                        surahNumber = sNum,
                        ayahNumber = ayahNum,
                        globalAyahNumber = dto.number ?: ayahNum,
                        juz = (dto.juz ?: 1).coerceIn(1, 30),
                        hizbQuarter = (dto.hizbQuarter ?: 1).coerceAtLeast(1),
                        page = dto.page?.takeIf { it in 1..604 } ?: safePage,
                        arabicTextUthmani = text,
                        englishTranslation = "",
                        arabicTafsir = ""
                    )
                }.distinctBy { it.globalKey }.sortedBy { it.globalAyahNumber }

                if (rawMapped.isNotEmpty()) {
                    val existingByKey = quranDao.getAyahsByKeys(rawMapped.map { it.globalKey })
                        .associateBy { it.globalKey }
                    val merged = mergePreservingTextAndTafsir(rawMapped, existingByKey)
                    quranDao.upsertAyahs(merged)
                    verifiedFullPages.add(safePage)
                    Result.success(merged)
                } else if (local.isNotEmpty()) {
                    Result.success(local)
                } else {
                    Result.failure(IllegalStateException("Empty Mushaf page response"))
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                if (local.isNotEmpty()) Result.success(local)
                else Result.failure(e)
            }
        }

    suspend fun searchQuranVerses(query: String): List<CachedSurahAyahEntity> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()
        val localMatches = quranDao.searchLocalAyahs(trimmed)
        if (localMatches.isNotEmpty()) return@withContext localMatches
        val bundledMatches = BundledQuranLoader.searchAyahs(context, trimmed)
        if (bundledMatches.isNotEmpty()) return@withContext bundledMatches
        try {
            val resp = NetworkModule.alQuranApi.searchQuran(trimmed)
            resp.data?.matches?.take(30)?.mapNotNull { m ->
                val ayahNum = m.numberInSurah ?: 0
                val sNum = (m.surah?.number ?: 1).coerceIn(1, 114)
                val text = BundledQuranLoader.cleanUthmaniAyahText(sNum, ayahNum, m.text.orEmpty())
                if (ayahNum <= 0 || text.isBlank()) return@mapNotNull null
                CachedSurahAyahEntity(
                    globalKey = "${sNum}_$ayahNum",
                    surahNumber = sNum,
                    ayahNumber = ayahNum,
                    globalAyahNumber = m.number ?: ayahNum,
                    juz = 1,
                    hizbQuarter = 1,
                    page = QuranCatalog.surahs.getOrNull(sNum - 1)?.startPage ?: 1,
                    arabicTextUthmani = text,
                    englishTranslation = "",
                    arabicTafsir = ""
                )
            }.orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun resolveAbdullahMousaServerUrl(): Result<String> = withContext(Dispatchers.IO) {
        val cached = normalizeHttpsUrl(prefsStore.prefsFlow.first().reciterServerUrl)
        if (cached.isNotBlank()) return@withContext Result.success(cached)

        try {
            val resp = NetworkModule.mp3QuranApi.getReciters("ar")
            val reciters = resp.reciters.orEmpty()
            val match = reciters.firstOrNull { rec ->
                val n = rec.name.orEmpty()
                (n.contains("عبد") && n.contains("موسى")) ||
                    n.contains("عبدالله الموسى") ||
                    n.contains("عبد الله الموسى") ||
                    n.contains("عبدالله بن موسى")
            } ?: reciters.firstOrNull { rec ->
                rec.name.orEmpty().contains("الموسى")
            }
            val bestMoshaf = match?.moshaf
                ?.filter { !it.server.isNullOrBlank() }
                ?.maxByOrNull { it.surahTotal ?: 0 }
            val parsedSurahSet = parseAvailableSurahList(bestMoshaf?.surahList)
            if (parsedSurahSet.isNotEmpty()) {
                availableReciterSurahs = parsedSurahSet
            }
            val server = normalizeHttpsUrl(bestMoshaf?.server.orEmpty())
            if (server.isNotBlank()) {
                prefsStore.setReciterServerUrl(server)
                Result.success(server)
            } else {
                Result.failure(IllegalStateException("Reciter server not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSurahAudioKey(surahNumber: Int): String = "surah_${surahNumber.toString().padStart(3, '0')}_abdullah_mousa"

    suspend fun getSurahPlaybackSource(surahNumber: Int): Result<String> = withContext(Dispatchers.IO) {
        val safeSurah = surahNumber.coerceIn(1, 114)
        val key = getSurahAudioKey(safeSurah)
        val local = downloadedDao.getDownloadByKey(key)
        if (local != null && File(local.localFilePath).exists()) {
            return@withContext Result.success(local.localFilePath)
        }
        val serverResult = resolveAbdullahMousaServerUrl()
        val available = availableReciterSurahs
        if (available != null && available.isNotEmpty() && !available.contains(safeSurah)) {
            return@withContext Result.failure(NoSuchElementException(RECITER_SURAH_UNAVAILABLE_CODE))
        }
        serverResult.map { server ->
            val padded = safeSurah.toString().padStart(3, '0')
            "$server/$padded.mp3"
        }
    }

    suspend fun downloadSurahAudio(surahNumber: Int): Result<DownloadedAudioEntity> = withContext(Dispatchers.IO) {
        val safeSurah = surahNumber.coerceIn(1, 114)
        val serverRes = resolveAbdullahMousaServerUrl()
        val server = serverRes.getOrElse { return@withContext Result.failure(it) }
        val available = availableReciterSurahs
        if (available != null && available.isNotEmpty() && !available.contains(safeSurah)) {
            return@withContext Result.failure(NoSuchElementException(RECITER_SURAH_UNAVAILABLE_CODE))
        }
        val padded = safeSurah.toString().padStart(3, '0')
        val url = "$server/$padded.mp3"
        val destFile = File(File(context.filesDir, "quran_audio"), "$padded.mp3")

        val downloadRes = NetworkModule.downloadAudioFile(url, destFile)
        downloadRes.map { bytes ->
            val surah = QuranCatalog.surahs.getOrNull(safeSurah - 1)
            val entity = DownloadedAudioEntity(
                audioKey = getSurahAudioKey(safeSurah),
                title = surah?.arabicName ?: "سورة $safeSurah",
                localFilePath = destFile.absolutePath,
                sizeBytes = bytes,
                downloadedAtMillis = System.currentTimeMillis()
            )
            downloadedDao.upsertDownload(entity)
            entity
        }
    }

    suspend fun downloadAdhanAudio(track: AdhanTrack): Result<DownloadedAudioEntity> = withContext(Dispatchers.IO) {
        val destFile = File(File(context.filesDir, "adhan_audio"), "${track.id}.mp3")
        val downloadRes = NetworkModule.downloadAudioFile(track.audioUrl, destFile)
        downloadRes.map { bytes ->
            val entity = DownloadedAudioEntity(
                audioKey = track.id,
                title = track.titleAr,
                localFilePath = destFile.absolutePath,
                sizeBytes = bytes,
                downloadedAtMillis = System.currentTimeMillis()
            )
            downloadedDao.upsertDownload(entity)
            entity
        }
    }

    suspend fun getAdhanPlaybackSource(track: AdhanTrack): String = withContext(Dispatchers.IO) {
        val local = downloadedDao.getDownloadByKey(track.id)
        if (local != null && File(local.localFilePath).exists()) local.localFilePath else track.audioUrl
    }

    suspend fun getSurahQueueSources(startSurah: Int): Result<List<Pair<Int, String>>> = withContext(Dispatchers.IO) {
        val safeStart = startSurah.coerceIn(1, 114)
        val cachedDownloads = downloadedDao.observeAllDownloads().first().associateBy { it.audioKey }
        val serverRes = resolveAbdullahMousaServerUrl()
        val server = serverRes.getOrNull()

        val resultList = buildSurahQueueList(
            startSurah = safeStart,
            serverUrl = server,
            localFiles = cachedDownloads.mapValues { it.value.localFilePath }.filterValues { File(it).exists() },
            availableSurahs = availableReciterSurahs
        )
        if (resultList.isNotEmpty()) Result.success(resultList)
        else Result.failure(serverRes.exceptionOrNull() ?: IllegalStateException("Reciter server unavailable"))
    }

    companion object {
        const val RECITER_SURAH_UNAVAILABLE_CODE = "RECITER_SURAH_UNAVAILABLE"
        const val OFFLINE_AUDIO_CODE = "OFFLINE_AUDIO"

        private fun SurahInfo.toCachedEntity(syncedAtMillis: Long) = CachedSurahEntity(
            number = number,
            arabicName = arabicName,
            englishName = englishName,
            englishTranslation = englishTranslation,
            ayahCount = ayahCount,
            isMeccan = isMeccan,
            startJuz = startJuz,
            startPage = startPage,
            syncedAtMillis = syncedAtMillis
        )

        private fun CachedSurahEntity.toDomainInfo() = SurahInfo(
            number = number,
            arabicName = arabicName,
            englishName = englishName,
            englishTranslation = englishTranslation,
            ayahCount = ayahCount,
            isMeccan = isMeccan,
            startJuz = startJuz,
            startPage = startPage
        )

        fun normalizeArabicSurahDisplayName(rawName: String?, fallbackArabicName: String): String {
            val cleaned = rawName?.trim()
                ?.removePrefix("سُورَةُ ")
                ?.removePrefix("سورة ")
                ?.trim()
                .orEmpty()
            // Prefer canonical clean Arabic name from QuranCatalog when API name is blank or heavily diacritized
            return if (fallbackArabicName.isNotBlank()) fallbackArabicName else cleaned
        }

        fun parseSurahListFromApiDtos(
            dtos: List<AlQuranSurahMetaDto>?,
            fallbackCatalog: List<SurahInfo> = QuranCatalog.surahs
        ): List<SurahInfo> {
            if (dtos.isNullOrEmpty()) return emptyList()
            val dtoByNum = dtos.mapNotNull { dto ->
                val num = dto.number ?: return@mapNotNull null
                if (num in 1..114) num to dto else null
            }.toMap()
            if (dtoByNum.isEmpty()) return emptyList()

            return fallbackCatalog.map { base ->
                val dto = dtoByNum[base.number]
                if (dto == null) {
                    base
                } else {
                    val count = dto.numberOfAyahs?.takeIf { it > 0 } ?: base.ayahCount
                    val revType = dto.revelationType?.trim().orEmpty()
                    val isMeccan = when {
                        revType.equals("Meccan", ignoreCase = true) -> true
                        revType.equals("Medinan", ignoreCase = true) -> false
                        else -> base.isMeccan
                    }
                    base.copy(
                        arabicName = normalizeArabicSurahDisplayName(dto.name, base.arabicName),
                        englishName = dto.englishName?.trim()?.takeIf { it.isNotBlank() } ?: base.englishName,
                        englishTranslation = dto.englishNameTranslation?.trim()?.takeIf { it.isNotBlank() } ?: base.englishTranslation,
                        ayahCount = count,
                        isMeccan = isMeccan
                    )
                }
            }
        }

        fun parseAvailableSurahList(surahListCsv: String?): Set<Int> {
            if (surahListCsv.isNullOrBlank()) return emptySet()
            return surahListCsv.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..114 }
                .toSet()
        }

        fun normalizeHttpsUrl(rawUrl: String): String {
            val trimmed = rawUrl.trim().trimEnd('/')
            return if (trimmed.startsWith("http://", ignoreCase = true)) {
                "https://" + trimmed.removePrefix("http://")
            } else {
                trimmed
            }
        }

        fun mushafPageImageUrl(pageNumber: Int): String {
            val padded = pageNumber.coerceIn(1, 604).toString().padStart(3, '0')
            return "https://android.quran.com/data/width_1024/page$padded.png"
        }

        fun mergePreservingTextAndTafsir(
            incoming: List<CachedSurahAyahEntity>,
            existingByKey: Map<String, CachedSurahAyahEntity>
        ): List<CachedSurahAyahEntity> {
            return incoming.map { entity ->
                val existing = existingByKey[entity.globalKey]
                if (existing == null) entity
                else entity.copy(
                    arabicTextUthmani = entity.arabicTextUthmani.ifBlank { existing.arabicTextUthmani },
                    englishTranslation = entity.englishTranslation.ifBlank { existing.englishTranslation },
                    arabicTafsir = entity.arabicTafsir.ifBlank { existing.arabicTafsir }
                )
            }
        }

        fun buildSurahQueueList(
            startSurah: Int,
            serverUrl: String?,
            localFiles: Map<String, String>,
            availableSurahs: Set<Int>? = null
        ): List<Pair<Int, String>> {
            val safeStart = startSurah.coerceIn(1, 114)
            val normalizedServer = serverUrl?.let(::normalizeHttpsUrl)?.takeIf { it.isNotBlank() }
            val resultList = mutableListOf<Pair<Int, String>>()
            for (sNum in safeStart..114) {
                val key = "surah_${sNum.toString().padStart(3, '0')}_abdullah_mousa"
                val localPath = localFiles[key]
                val hasRemote = normalizedServer != null &&
                    (availableSurahs == null || availableSurahs.isEmpty() || availableSurahs.contains(sNum))
                val src = when {
                    !localPath.isNullOrBlank() -> localPath
                    hasRemote -> "$normalizedServer/${sNum.toString().padStart(3, '0')}.mp3"
                    else -> null
                }
                if (src != null) {
                    resultList.add(sNum to src)
                }
            }
            return resultList
        }

        fun buildIslamicNetworkAyahAudioUrl(edition: String, globalAyahNumber: Int): String {
            val safeEdition = edition.trim().ifBlank { "ar.alafasy" }
            val safeNum = globalAyahNumber.coerceIn(1, 6236)
            return "https://cdn.islamic.network/quran/audio/128/$safeEdition/$safeNum.mp3"
        }

        fun selectPreferredArchiveMp3Url(meta: ArchiveMetadataResponse): String? {
            val d1 = meta.d1?.trim().orEmpty()
            val dir = meta.dir?.trim().orEmpty()
            if (d1.isBlank() || dir.isBlank()) return null

            val files = meta.files.orEmpty().filter { dto ->
                val name = dto.name?.trim().orEmpty()
                name.endsWith(".mp3", ignoreCase = true) &&
                    !name.startsWith("._") &&
                    !name.contains("__MACOSX", ignoreCase = true)
            }
            if (files.isEmpty()) return null

            val chosen = files.firstOrNull { dto ->
                dto.format.orEmpty().contains("MP3", ignoreCase = true) &&
                    !dto.name.orEmpty().contains("64kb", ignoreCase = true)
            } ?: files.firstOrNull { dto ->
                dto.format.orEmpty().contains("MP3", ignoreCase = true)
            } ?: files.first()

            val fileName = chosen.name?.trim().orEmpty()
            if (fileName.isBlank()) return null
            val encodedName = fileName.split("/")
                .joinToString("/") { segment ->
                    URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
                }
            val cleanDir = if (dir.startsWith("/")) dir else "/$dir"
            return normalizeHttpsUrl("https://$d1$cleanDir/$encodedName")
        }
    }

    private fun isNetworkConnected(): Boolean {
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

    suspend fun getRuqyahPlaybackSource(
        track: RuqyahAudioTrack,
        reciterEdition: String = "ar.alafasy"
    ): Result<String> = withContext(Dispatchers.IO) {
        val local = downloadedDao.getDownloadByKey(track.id)
        if (local != null && File(local.localFilePath).exists()) {
            return@withContext Result.success(local.localFilePath)
        }
        when (val src = track.audioSource) {
            is RuqyahAudioSource.ArchiveItem -> resolveArchiveMp3Url(src.identifier)
            is RuqyahAudioSource.SyncedQuranSequence -> {
                val firstAyah = src.ayahRefs.firstOrNull { it.isQuranAyah }
                    ?: return@withContext Result.failure(IllegalStateException("No Quranic ayahs configured"))
                if (!isNetworkConnected()) {
                    return@withContext Result.failure(IllegalStateException(OFFLINE_AUDIO_CODE))
                }
                Result.success(buildIslamicNetworkAyahAudioUrl(reciterEdition, firstAyah.globalAyahNumber))
            }
            is RuqyahAudioSource.FullSurahMp3Quran -> {
                resolveMp3QuranSurahAudioUrl(src.surahNumber, reciterEdition).map { it.first }
            }
        }
    }

    suspend fun resolveRuqyahPlaybackPlan(
        track: RuqyahAudioTrack,
        reciterEdition: String,
        isAr: Boolean
    ): Result<RuqyahQueuePlan> = withContext(Dispatchers.IO) {
        val profile = RuqyahCatalog.findReciterByEdition(reciterEdition)
        when (val src = track.audioSource) {
            is RuqyahAudioSource.ArchiveItem -> {
                val local = downloadedDao.getDownloadByKey(track.id)
                val resolvedUrl = if (local != null && File(local.localFilePath).exists()) {
                    local.localFilePath
                } else {
                    if (!isNetworkConnected()) {
                        return@withContext Result.failure(IllegalStateException(OFFLINE_AUDIO_CODE))
                    }
                    resolveArchiveMp3Url(src.identifier).getOrElse {
                        return@withContext Result.failure(it)
                    }
                }
                val trackObj = QueueTrack(
                    trackId = track.id,
                    source = resolvedUrl,
                    title = if (isAr) track.titleAr else track.titleEn,
                    subtitle = if (isAr) track.reciterAr else track.reciterEn
                )
                Result.success(
                    RuqyahQueuePlan(
                        tracks = listOf(trackObj),
                        resolvedReciterNameAr = track.reciterAr,
                        resolvedReciterNameEn = track.reciterEn,
                        loopWholeQueue = false
                    )
                )
            }

            is RuqyahAudioSource.SyncedQuranSequence -> {
                val quranRefs = src.ayahRefs.filter { it.isQuranAyah }
                if (quranRefs.isEmpty()) {
                    return@withContext Result.failure(IllegalStateException("Empty Quran Ruqyah sequence"))
                }
                val edition = profile.cdnEdition ?: "ar.alafasy"
                val offlineDir = File(File(context.filesDir, "ruqyah_audio"), "${track.id}_$edition")
                val hasAllOffline = quranRefs.all { ref ->
                    File(offlineDir, "${ref.globalAyahNumber}.mp3").exists()
                }
                if (!hasAllOffline && !isNetworkConnected()) {
                    return@withContext Result.failure(IllegalStateException(OFFLINE_AUDIO_CODE))
                }
                val queue = quranRefs.mapIndexed { idx, ref ->
                    val localFile = File(offlineDir, "${ref.globalAyahNumber}.mp3")
                    val audioUri = if (localFile.exists()) {
                        localFile.absolutePath
                    } else {
                        buildIslamicNetworkAyahAudioUrl(edition, ref.globalAyahNumber)
                    }
                    val sInfo = QuranCatalog.surahs.getOrNull(ref.surahNumber - 1)
                    val ayahBadge = if (isAr) {
                        "سورة ${sInfo?.arabicName ?: ref.surahNumber} • الآية ${ref.ayahNumber}"
                    } else {
                        "${sInfo?.englishName ?: "Surah ${ref.surahNumber}"} • Ayah ${ref.ayahNumber}"
                    }
                    QueueTrack(
                        trackId = "${track.id}_${edition}_${idx}_${ref.globalAyahNumber}",
                        source = audioUri,
                        title = if (isAr) "${track.titleAr} • $ayahBadge" else "${track.titleEn} • $ayahBadge",
                        subtitle = if (isAr) profile.nameAr else profile.nameEn,
                        surahNumber = ref.surahNumber,
                        ayahNumber = ref.ayahNumber
                    )
                }
                Result.success(
                    RuqyahQueuePlan(
                        tracks = queue,
                        resolvedReciterNameAr = profile.nameAr,
                        resolvedReciterNameEn = profile.nameEn,
                        loopWholeQueue = true
                    )
                )
            }

            is RuqyahAudioSource.FullSurahMp3Quran -> {
                val localFile = File(File(context.filesDir, "ruqyah_audio"), "${track.id}_${profile.id}.mp3")
                val legacyLocal = downloadedDao.getDownloadByKey(track.id)
                val resolvedPair: Pair<String, String> = when {
                    localFile.exists() -> localFile.absolutePath to (if (isAr) profile.nameAr else profile.nameEn)
                    legacyLocal != null && File(legacyLocal.localFilePath).exists() -> {
                        legacyLocal.localFilePath to (if (isAr) profile.nameAr else profile.nameEn)
                    }
                    else -> {
                        if (!isNetworkConnected()) {
                            return@withContext Result.failure(IllegalStateException(OFFLINE_AUDIO_CODE))
                        }
                        resolveMp3QuranSurahAudioUrl(src.surahNumber, reciterEdition).getOrElse {
                            return@withContext Result.failure(it)
                        }
                    }
                }
                val qTrack = QueueTrack(
                    trackId = "${track.id}_${profile.id}",
                    source = resolvedPair.first,
                    title = if (isAr) track.titleAr else track.titleEn,
                    subtitle = resolvedPair.second,
                    surahNumber = src.surahNumber
                )
                Result.success(
                    RuqyahQueuePlan(
                        tracks = listOf(qTrack),
                        resolvedReciterNameAr = resolvedPair.second,
                        resolvedReciterNameEn = profile.nameEn,
                        loopWholeQueue = false
                    )
                )
            }
        }
    }

    suspend fun downloadRuqyahAudio(
        track: RuqyahAudioTrack,
        reciterEdition: String = "ar.alafasy"
    ): Result<DownloadedAudioEntity> = withContext(Dispatchers.IO) {
        val profile = RuqyahCatalog.findReciterByEdition(reciterEdition)
        when (val src = track.audioSource) {
            is RuqyahAudioSource.ArchiveItem -> {
                val urlRes = resolveArchiveMp3Url(src.identifier)
                val url = urlRes.getOrElse { return@withContext Result.failure(it) }
                val destFile = File(File(context.filesDir, "ruqyah_audio"), "${track.id}.mp3")
                val downloadRes = NetworkModule.downloadAudioFile(url, destFile)
                downloadRes.map { bytes ->
                    val entity = DownloadedAudioEntity(
                        audioKey = track.id,
                        title = track.titleAr,
                        localFilePath = destFile.absolutePath,
                        sizeBytes = bytes,
                        downloadedAtMillis = System.currentTimeMillis()
                    )
                    downloadedDao.upsertDownload(entity)
                    entity
                }
            }

            is RuqyahAudioSource.FullSurahMp3Quran -> {
                val urlRes = resolveMp3QuranSurahAudioUrl(src.surahNumber, reciterEdition)
                val url = urlRes.getOrElse { return@withContext Result.failure(it) }.first
                val destFile = File(File(context.filesDir, "ruqyah_audio"), "${track.id}_${profile.id}.mp3")
                val downloadRes = NetworkModule.downloadAudioFile(url, destFile)
                downloadRes.map { bytes ->
                    val entity = DownloadedAudioEntity(
                        audioKey = track.id,
                        title = track.titleAr,
                        localFilePath = destFile.absolutePath,
                        sizeBytes = bytes,
                        downloadedAtMillis = System.currentTimeMillis()
                    )
                    downloadedDao.upsertDownload(entity)
                    entity
                }
            }

            is RuqyahAudioSource.SyncedQuranSequence -> {
                val quranRefs = src.ayahRefs.filter { it.isQuranAyah }
                if (quranRefs.isEmpty()) {
                    return@withContext Result.failure(IllegalStateException("No audio verses in suite"))
                }
                val edition = profile.cdnEdition ?: "ar.alafasy"
                val offlineDir = File(File(context.filesDir, "ruqyah_audio"), "${track.id}_$edition")
                offlineDir.mkdirs()
                var totalBytes = 0L
                for (ref in quranRefs) {
                    val destFile = File(offlineDir, "${ref.globalAyahNumber}.mp3")
                    if (!destFile.exists() || destFile.length() == 0L) {
                        val url = buildIslamicNetworkAyahAudioUrl(edition, ref.globalAyahNumber)
                        val bytes = NetworkModule.downloadAudioFile(url, destFile)
                            .getOrElse { return@withContext Result.failure(it) }
                        totalBytes += bytes
                    } else {
                        totalBytes += destFile.length()
                    }
                }
                val entity = DownloadedAudioEntity(
                    audioKey = track.id,
                    title = track.titleAr,
                    localFilePath = offlineDir.absolutePath,
                    sizeBytes = totalBytes,
                    downloadedAtMillis = System.currentTimeMillis()
                )
                downloadedDao.upsertDownload(entity)
                Result.success(entity)
            }
        }
    }

    private suspend fun resolveMp3QuranSurahAudioUrl(
        surahNumber: Int,
        reciterEdition: String
    ): Result<Pair<String, String>> {
        return try {
            val profile = RuqyahCatalog.findReciterByEdition(reciterEdition)
            val resp = NetworkModule.mp3QuranApi.getReciters("ar")
            val match = RuqyahCatalog.findBestMoshafForReciter(
                reciters = resp.reciters.orEmpty(),
                profile = profile,
                targetSurah = surahNumber
            )
            if (match != null) {
                val padded = surahNumber.coerceIn(1, 114).toString().padStart(3, '0')
                Result.success("${normalizeHttpsUrl(match.first)}/$padded.mp3" to match.second)
            } else {
                Result.failure(IllegalStateException("MP3Quran reciter server unavailable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveArchiveMp3Url(identifier: String): Result<String> {
        return try {
            val meta = NetworkModule.archiveOrgApi.getItemMetadata(identifier)
            val url = selectPreferredArchiveMp3Url(meta)
            if (url != null) {
                Result.success(url)
            } else {
                Result.failure(IllegalStateException("MP3 entry not found in Archive metadata"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadHadithChapter(book: HadithBookInfo, chapterNumber: Int): List<HadithItem> =
        withContext(Dispatchers.IO) {
            val fallback = HadithSeedData.starterHadiths.filter {
                it.bookId == book.id && (chapterNumber == 0 || it.chapterNumber == chapterNumber)
            }
            val safeChapter = chapterNumber.coerceAtLeast(1)
            try {
                val araResp = NetworkModule.hadithApi.getHadithSection(book.arabicEdition, safeChapter)
                val engResp = try {
                    NetworkModule.hadithApi.getHadithSection(book.englishEdition, safeChapter)
                } catch (_: Exception) {
                    null
                }
                val engMap = engResp?.hadiths?.associateBy { it.hadithnumber.toInt() }.orEmpty()
                val chapterTitle = araResp.metadata?.section?.values?.firstOrNull()
                    ?: HadithSeedData.chapters.getOrNull(safeChapter - 1)?.titleAr
                    ?: "باب $safeChapter"

                val remoteItems = araResp.hadiths.take(40).mapIndexed { index, entry ->
                    val num = entry.hadithnumber.toInt().takeIf { it > 0 } ?: (index + 1)
                    val engEntry = engMap[num]
                    val gradeText = engEntry?.grades?.firstOrNull()?.grade
                        ?: if (book.id == "bukhari" || book.id == "muslim") "Sahih" else "Verified Collection"
                    val gradeAr = when {
                        gradeText.contains("Sahih", ignoreCase = true) -> "صحيح"
                        gradeText.contains("Hasan", ignoreCase = true) -> "حسن"
                        else -> "مخرج في ${book.titleAr}"
                    }
                    HadithItem(
                        id = "${book.id}_$num",
                        bookId = book.id,
                        bookNameAr = book.titleAr,
                        bookNameEn = book.titleEn,
                        chapterNumber = safeChapter,
                        chapterNameAr = chapterTitle,
                        chapterNameEn = "Chapter $safeChapter",
                        hadithNumber = num,
                        narratorAr = book.authorAr,
                        narratorEn = book.authorEn,
                        arabicText = entry.text.trim(),
                        englishText = engEntry?.text?.trim().orEmpty(),
                        gradeAr = gradeAr,
                        gradeEn = gradeText
                    )
                }.filter { it.arabicText.isNotBlank() }

                remoteItems.ifEmpty { fallback }
            } catch (_: Exception) {
                fallback.ifEmpty { HadithSeedData.starterHadiths.filter { it.bookId == book.id } }
            }
        }
}
