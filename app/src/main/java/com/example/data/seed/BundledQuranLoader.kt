package com.example.data.seed

import android.content.Context
import android.util.JsonReader
import com.example.data.local.CachedSurahAyahEntity
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Loads the complete authentic Uthmani Quran dataset (114 Surahs, 6,236 Ayahs, 604 Mushaf pages)
 * bundled in assets/quran_uthmani_complete.json as a verified offline-first source.
 * Uses only authentic AlQuran.cloud (quran-uthmani) text and preserves starter Sahih International
 * translations & Tafsir Al-Muyassar from QuranCatalog.
 */
object BundledQuranLoader {
    private const val ASSET_NAME = "quran_uthmani_complete.json"

    private val BISMILLAH_PREFIXES = listOf(
        "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
        "بِّسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
    )

    @Volatile
    private var cachedBySurah: Map<Int, List<CachedSurahAyahEntity>>? = null

    @Volatile
    private var cachedByPage: Map<Int, List<CachedSurahAyahEntity>>? = null

    fun cleanUthmaniAyahText(surahNumber: Int, ayahNumber: Int, rawText: String): String {
        val cleaned = rawText.replace("\uFEFF", "").trim()
        if (cleaned.isEmpty()) return cleaned
        if (surahNumber != 1 && surahNumber != 9 && ayahNumber == 1) {
            for (prefix in BISMILLAH_PREFIXES) {
                if (cleaned.startsWith(prefix)) {
                    val remainder = cleaned.substring(prefix.length).trim()
                    if (remainder.isNotEmpty()) {
                        return remainder
                    }
                }
            }
        }
        return cleaned
    }

    private fun ensureLoaded(context: Context): Pair<Map<Int, List<CachedSurahAyahEntity>>, Map<Int, List<CachedSurahAyahEntity>>> {
        val existingSurahs = cachedBySurah
        val existingPages = cachedByPage
        if (existingSurahs != null && existingPages != null) {
            return existingSurahs to existingPages
        }
        synchronized(this) {
            val s2 = cachedBySurah
            val p2 = cachedByPage
            if (s2 != null && p2 != null) {
                return s2 to p2
            }
            val starterByKey = QuranCatalog.offlineStarterAyahs.associateBy { it.globalKey }
            val bySurah = HashMap<Int, List<CachedSurahAyahEntity>>(128)
            val byPage = HashMap<Int, MutableList<CachedSurahAyahEntity>>(640)

            try {
                context.assets.open(ASSET_NAME).use { rawStream ->
                    JsonReader(InputStreamReader(rawStream, StandardCharsets.UTF_8)).use { reader ->
                            reader.beginArray() // root array of surahs
                            while (reader.hasNext()) {
                                reader.beginArray() // [surahNumber, [[ayahNum, globalNum, juz, hizbQ, page, text], ...]]
                                val surahNum = reader.nextInt().coerceIn(1, 114)
                                val ayahs = ArrayList<CachedSurahAyahEntity>()
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    reader.beginArray()
                                    val ayahNum = reader.nextInt()
                                    val globalNum = reader.nextInt()
                                    val juz = reader.nextInt().coerceIn(1, 30)
                                    val hizbQ = reader.nextInt().coerceAtLeast(1)
                                    val page = reader.nextInt().coerceIn(1, 604)
                                    val text = cleanUthmaniAyahText(surahNum, ayahNum, reader.nextString())
                                    reader.endArray()

                                    val key = "${surahNum}_$ayahNum"
                                    val starter = starterByKey[key]
                                    val entity = CachedSurahAyahEntity(
                                        globalKey = key,
                                        surahNumber = surahNum,
                                        ayahNumber = ayahNum,
                                        globalAyahNumber = globalNum,
                                        juz = juz,
                                        hizbQuarter = hizbQ,
                                        page = page,
                                        arabicTextUthmani = text,
                                        englishTranslation = starter?.englishTranslation.orEmpty(),
                                        arabicTafsir = starter?.arabicTafsir.orEmpty()
                                    )
                                    ayahs.add(entity)
                                    byPage.getOrPut(page) { ArrayList() }.add(entity)
                                }
                                reader.endArray()
                                reader.endArray()
                                bySurah[surahNum] = ayahs
                            }
                        reader.endArray()
                    }
                }
            } catch (_: Exception) {
                // Fallback safely to built-in starter ayahs if assets are unavailable in isolated tests
                val groupedSurahs = QuranCatalog.offlineStarterAyahs.groupBy { it.surahNumber }
                val groupedPages = QuranCatalog.offlineStarterAyahs.groupBy { it.page }
                cachedBySurah = groupedSurahs
                cachedByPage = groupedPages
                return groupedSurahs to groupedPages
            }

            val finalizedPages = byPage.mapValues { (_, list) ->
                list.sortedBy { it.globalAyahNumber }
            }
            cachedBySurah = bySurah
            cachedByPage = finalizedPages
            return bySurah to finalizedPages
        }
    }

    fun getCachedSurahAyahsOrNull(surahNumber: Int): List<CachedSurahAyahEntity>? {
        val safeSurah = surahNumber.coerceIn(1, 114)
        return cachedBySurah?.get(safeSurah)
    }

    fun getCachedPageAyahsOrNull(pageNumber: Int): List<CachedSurahAyahEntity>? {
        val safePage = pageNumber.coerceIn(1, 604)
        return cachedByPage?.get(safePage)
    }

    fun getSurahAyahs(context: Context, surahNumber: Int): List<CachedSurahAyahEntity> {
        val safeSurah = surahNumber.coerceIn(1, 114)
        return ensureLoaded(context).first[safeSurah].orEmpty()
    }

    fun getPageAyahs(context: Context, pageNumber: Int): List<CachedSurahAyahEntity> {
        val safePage = pageNumber.coerceIn(1, 604)
        return ensureLoaded(context).second[safePage].orEmpty()
    }

    fun searchAyahs(context: Context, query: String, limit: Int = 40): List<CachedSurahAyahEntity> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        val results = ArrayList<CachedSurahAyahEntity>()
        val surahMap = ensureLoaded(context).first
        for (sNum in 1..114) {
            val list = surahMap[sNum] ?: continue
            for (ayah in list) {
                if (ayah.arabicTextUthmani.contains(q) || ayah.englishTranslation.contains(q, ignoreCase = true)) {
                    results.add(ayah)
                    if (results.size >= limit) return results
                }
            }
        }
        return results
    }
}
