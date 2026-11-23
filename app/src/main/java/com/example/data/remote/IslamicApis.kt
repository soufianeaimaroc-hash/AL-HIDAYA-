package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- AlQuran Cloud API ---

@JsonClass(generateAdapter = true)
data class AlQuranSurahListResponse(
    val code: Int? = null,
    val status: String? = null,
    val data: List<AlQuranSurahMetaDto>? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranSurahMetaDto(
    val number: Int? = null,
    val name: String? = null,
    val englishName: String? = null,
    val englishNameTranslation: String? = null,
    val numberOfAyahs: Int? = null,
    val revelationType: String? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranEditionsResponse(
    val code: Int? = null,
    val status: String? = null,
    val data: List<AlQuranSurahEdition>? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranSingleSurahResponse(
    val code: Int? = null,
    val status: String? = null,
    val data: AlQuranSurahEdition? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranPageResponse(
    val code: Int? = null,
    val status: String? = null,
    val data: AlQuranPageData? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranPageData(
    val number: Int? = null,
    val ayahs: List<AlQuranAyahDto>? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranSurahEdition(
    val number: Int? = null,
    val name: String? = null,
    val englishName: String? = null,
    val ayahs: List<AlQuranAyahDto>? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranAyahDto(
    val number: Int? = null,
    val text: String? = null,
    val numberInSurah: Int? = null,
    val juz: Int? = null,
    val hizbQuarter: Int? = null,
    val page: Int? = null,
    val surah: AlQuranMiniSurahDto? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranMiniSurahDto(
    val number: Int? = null,
    val name: String? = null,
    val englishName: String? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranSearchResponse(
    val code: Int? = null,
    val data: AlQuranSearchData? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranSearchData(
    val count: Int? = null,
    val matches: List<AlQuranSearchMatch>? = null
)

@JsonClass(generateAdapter = true)
data class AlQuranSearchMatch(
    val number: Int? = null,
    val text: String? = null,
    val numberInSurah: Int? = null,
    val surah: AlQuranMiniSurahDto? = null
)

interface AlQuranCloudApi {
    @GET("surah")
    suspend fun getSurahList(): AlQuranSurahListResponse

    @GET("surah/{surahNumber}/editions/quran-uthmani,en.sahih,ar.muyassar")
    suspend fun getSurahWithTafsirAndTranslation(
        @Path("surahNumber") surahNumber: Int
    ): AlQuranEditionsResponse

    @GET("surah/{surahNumber}/quran-uthmani")
    suspend fun getSingleSurahUthmani(
        @Path("surahNumber") surahNumber: Int
    ): AlQuranSingleSurahResponse

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
    val reciters: List<Mp3QuranReciterDto>? = null
)

@JsonClass(generateAdapter = true)
data class Mp3QuranReciterDto(
    val id: Int? = null,
    val name: String? = null,
    val moshaf: List<Mp3QuranMoshafDto>? = null
)

@JsonClass(generateAdapter = true)
data class Mp3QuranMoshafDto(
    val id: Int? = null,
    val name: String? = null,
    val server: String? = null,
    @Json(name = "surah_total") val surahTotal: Int? = null,
    @Json(name = "surah_list") val surahList: String? = null
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
    val d1: String? = null,
    val dir: String? = null,
    val files: List<ArchiveFileDto>? = null
)

@JsonClass(generateAdapter = true)
data class ArchiveFileDto(
    val name: String? = null,
    val format: String? = null,
    val length: String? = null,
    val size: String? = null
)

interface ArchiveOrgApi {
    @GET("metadata/{identifier}")
    suspend fun getItemMetadata(@Path("identifier") identifier: String): ArchiveMetadataResponse
}


