package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CachedSurahAyahEntity
import com.example.data.local.CachedSurahEntity
import com.example.data.local.DownloadedAudioEntity
import com.example.data.local.UserPreferencesDataStore
import com.example.data.remote.AlQuranSurahMetaDto
import com.example.data.remote.NetworkModule
import com.example.data.seed.AdhanTrack
import com.example.data.seed.HadithBookInfo
import com.example.data.seed.HadithItem
import com.example.data.seed.HadithSeedData
import com.example.data.seed.QuranCatalog
import com.example.data.seed.RuqyahAudioTrack
import com.example.data.seed.SurahInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLEncoder

class QuranAndHadithRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val prefsStore: UserPreferencesDataStore
) {
    private val quranDao = db.quranCacheDao()
    private val downloadedDao = db.downloadedAudioDao()
    private val verifiedFullPages = java.util.Collections.synchronizedSet(mutableSetOf(1, 604))

    @Volatile
    private var availableReciterSurahs: Set<Int>? = null

    suspend fun ensureStarterDataSeeded() = withContext(Dispatchers.IO) {
        if (quranDao.getCachedSurahsOnce().size < 114) {
            quranDao.upsertSurahs(
                QuranCatalog.surahs.map { s -> s.toCachedEntity(syncedAtMillis = 0L) }
            )
        }
        if (quranDao.getSurahAyahsOnce(1).isEmpty() || quranDao.getPageAyahsOnce(604).size < 15) {
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

    suspend fun loadSurahFromNetworkIfNeeded(surahNumber: Int, forceRefresh: Boolean = false): Result<List<CachedSurahAyahEntity>> =
        withContext(Dispatchers.IO) {
            val safeSurah = surahNumber.coerceIn(1, 114)
            val local = quranDao.getSurahAyahsOnce(safeSurah)
                .distinctBy { it.globalKey }
                .sortedBy { it.ayahNumber }
            val fallbackSurah = QuranCatalog.surahs.getOrNull(safeSurah - 1)
            val expectedCount = fallbackSurah?.ayahCount ?: 0
            if (!forceRefresh && local.size >= expectedCount && local.isNotEmpty()) {
                return@withContext Result.success(local)
            }
            try {
                var entities: List<CachedSurahAyahEntity> = emptyList()
                val response = try {
                    NetworkModule.alQuranApi.getSurahWithTafsirAndTranslation(safeSurah)
                } catch (_: Exception) {
                    null
                }
                val editions = response?.data.orEmpty()
                val uthmani = editions.getOrNull(0)?.ayahs.orEmpty()
                val english = editions.getOrNull(1)?.ayahs.orEmpty()
                val tafsir = editions.getOrNull(2)?.ayahs.orEmpty()

                if (uthmani.isNotEmpty()) {
                    entities = uthmani.mapIndexedNotNull { idx, ayah ->
                        val ayahNum = ayah.numberInSurah ?: 0
                        val text = ayah.text?.trim().orEmpty()
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

                // Fallback to single Uthmani edition if multi-edition call failed or returned empty
                if (entities.isEmpty()) {
                    val singleResp = NetworkModule.alQuranApi.getSingleSurahUthmani(safeSurah)
                    val singleAyahs = singleResp.data?.ayahs.orEmpty()
                    entities = singleAyahs.mapNotNull { ayah ->
                        val ayahNum = ayah.numberInSurah ?: 0
                        val text = ayah.text?.trim().orEmpty()
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
                    val merged = mergePreservingTextAndTafsir(
                        incoming = deduped,
                        existingByKey = local.associateBy { it.globalKey }
                    )
                    quranDao.upsertAyahs(merged)
                    Result.success(merged)
                } else if (local.isNotEmpty()) {
                    Result.success(local)
                } else {
                    Result.failure(IllegalStateException("Empty Quran API response"))
                }
            } catch (e: Exception) {
                if (local.isNotEmpty()) Result.success(local)
                else Result.failure(e)
            }
        }

    suspend fun loadMushafPage(pageNumber: Int, forceRefresh: Boolean = false): Result<List<CachedSurahAyahEntity>> =
        withContext(Dispatchers.IO) {
            val safePage = pageNumber.coerceIn(1, 604)
            val local = quranDao.getPageAyahsOnce(safePage)
                .distinctBy { it.globalKey }
                .sortedBy { it.globalAyahNumber }
            if (!forceRefresh && verifiedFullPages.contains(safePage) && local.isNotEmpty()) {
                return@withContext Result.success(local)
            }
            try {
                val resp = NetworkModule.alQuranApi.getMushafPage(safePage)
                val ayahs = resp.data?.ayahs.orEmpty()
                val rawMapped = ayahs.mapNotNull { dto ->
                    val ayahNum = dto.numberInSurah ?: 0
                    val text = dto.text?.trim().orEmpty()
                    if (ayahNum <= 0 || text.isBlank()) return@mapNotNull null
                    val sNum = (dto.surah?.number ?: 1).coerceIn(1, 114)
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
        try {
            val resp = NetworkModule.alQuranApi.searchQuran(trimmed)
            resp.data?.matches?.take(30)?.mapNotNull { m ->
                val ayahNum = m.numberInSurah ?: 0
                val text = m.text?.trim().orEmpty()
                if (ayahNum <= 0 || text.isBlank()) return@mapNotNull null
                val sNum = (m.surah?.number ?: 1).coerceIn(1, 114)
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
    }

    suspend fun getRuqyahPlaybackSource(track: RuqyahAudioTrack): Result<String> = withContext(Dispatchers.IO) {
        val local = downloadedDao.getDownloadByKey(track.id)
        if (local != null && File(local.localFilePath).exists()) {
            return@withContext Result.success(local.localFilePath)
        }
        resolveArchiveMp3Url(track.archiveItemId)
    }

    suspend fun downloadRuqyahAudio(track: RuqyahAudioTrack): Result<DownloadedAudioEntity> = withContext(Dispatchers.IO) {
        val urlRes = resolveArchiveMp3Url(track.archiveItemId)
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

    private suspend fun resolveArchiveMp3Url(identifier: String): Result<String> {
        return try {
            val meta = NetworkModule.archiveOrgApi.getItemMetadata(identifier)
            val mp3File = meta.files.firstOrNull {
                it.name.endsWith(".mp3", ignoreCase = true) && it.format.contains("MP3", ignoreCase = true)
            } ?: meta.files.firstOrNull { it.name.endsWith(".mp3", ignoreCase = true) }

            if (mp3File != null && meta.d1.isNotBlank() && meta.dir.isNotBlank()) {
                val encodedName = URLEncoder.encode(mp3File.name, "UTF-8").replace("+", "%20")
                Result.success("https://${meta.d1}${meta.dir}/$encodedName")
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
