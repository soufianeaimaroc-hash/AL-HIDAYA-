package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- AlQuran Cloud API ---

@JsonClass(generateAdapter = true)
data class AlQuranEditionsResponse(
    val code: Int = 0,
    val status: String = "",
    val data: List<AlQuranSurahEdition> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AlQuranPageResponse(
    val code: Int = 0,
    val status: String = "",
    val data: AlQuranPageData? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranPageData(
    val number: Int = 1,
    val ayahs: List<AlQuranAyahDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AlQuranSurahEdition(
    val number: Int = 0,
    val name: String = "",
    val englishName: String = "",
    val ayahs: List<AlQuranAyahDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AlQuranAyahDto(
    val number: Int = 0,
    val text: String = "",
    val numberInSurah: Int = 0,
    val juz: Int = 1,
    val hizbQuarter: Int = 1,
    val page: Int = 1,
    val surah: AlQuranMiniSurahDto? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranMiniSurahDto(
    val number: Int = 0,
    val name: String = "",
    val englishName: String = ""
)

@JsonClass(generateAdapter = true)
data class AlQuranSearchResponse(
    val code: Int = 0,
    val data: AlQuranSearchData? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranSearchData(
    val count: Int = 0,
    val matches: List<AlQuranSearchMatch> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AlQuranSearchMatch(
    val number: Int = 0,
    val text: String = "",
    val numberInSurah: Int = 1,
    val surah: AlQuranMiniSurahDto? = null
)

interface AlQuranCloudApi {
    @GET("surah/{surahNumber}/editions/quran-uthmani,en.sahih,ar.muyassar")
    suspend fun getSurahWithTafsirAndTranslation(
        @Path("surahNumber") surahNumber: Int
    ): AlQuranEditionsResponse

    @GET("page/{pageNumber}/quran-uthmani")
    suspend fun getMushafPage(
        @Path("pageNumber") pageNumber: Int
    ): AlQuranPageResponse

    @GET("search/{keyword}/all/quran-uthmani")
    suspend fun searchQuran(
        @Path("keyword") keyword: String
    ): AlQuranSearchResponse
}

// --- MP3Quran API v3 (for Abdullah Al-Mousa) ---

@JsonClass(generateAdapter = true)
data class Mp3QuranRecitersResponse(
    val reciters: List<Mp3QuranReciterDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Mp3QuranReciterDto(
    val id: Int = 0,
    val name: String = "",
    val moshaf: List<Mp3QuranMoshafDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Mp3QuranMoshafDto(
    val id: Int = 0,
    val name: String = "",
    val server: String = "",
    @Json(name = "surah_total") val surahTotal: Int = 0,
    @Json(name = "surah_list") val surahList: String = ""
)

interface Mp3QuranApi {
    @GET("reciters")
    suspend fun getReciters(@Query("language") language: String = "ar"): Mp3QuranRecitersResponse
}

// --- Hadith API (fawazahmed0/hadith-api via jsDelivr) ---

@JsonClass(generateAdapter = true)
data class HadithApiResponse(
    val metadata: HadithApiMetadata? = null,
    val hadiths: List<HadithApiEntry> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HadithApiMetadata(
    val name: String = "",
    val section: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class HadithApiEntry(
    val hadithnumber: Double = 0.0,
    val arabicnumber: Double = 0.0,
    val text: String = "",
    val grades: List<HadithApiGrade>? = null,
    val reference: HadithApiReference? = null
)

@JsonClass(generateAdapter = true)
data class HadithApiGrade(
    val name: String? = null,
    val grade: String? = null
)

@JsonClass(generateAdapter = true)
data class HadithApiReference(
    val book: Int = 0,
    val hadith: Int = 0
)

interface HadithCdnApi {
    @GET("editions/{edition}/sections/{section}.json")
    suspend fun getHadithSection(
        @Path("edition") edition: String,
        @Path("section") section: Int
    ): HadithApiResponse
}

// --- Internet Archive Metadata API (for verified Ruqyah Shariah audio tracks) ---

@JsonClass(generateAdapter = true)
data class ArchiveMetadataResponse(
    val d1: String = "",
    val dir: String = "",
    val files: List<ArchiveFileDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArchiveFileDto(
    val name: String = "",
    val format: String = ""
)

interface ArchiveOrgApi {
    @GET("metadata/{identifier}")
    suspend fun getItemMetadata(@Path("identifier") identifier: String): ArchiveMetadataResponse
}

