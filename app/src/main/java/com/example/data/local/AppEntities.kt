package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_surahs")
data class CachedSurahEntity(
    @PrimaryKey val number: Int,
    val arabicName: String,
    val englishName: String,
    val englishTranslation: String,
    val ayahCount: Int,
    val isMeccan: Boolean,
    val startJuz: Int,
    val startPage: Int,
    val syncedAtMillis: Long = 0L
)

@Entity(tableName = "cached_ayahs")
data class CachedSurahAyahEntity(
    @PrimaryKey val globalKey: String, // "{surah}_{ayah}"
    val surahNumber: Int,
    val ayahNumber: Int,
    val globalAyahNumber: Int,
    val juz: Int,
    val hizbQuarter: Int,
    val page: Int,
    val arabicTextUthmani: String,
    val englishTranslation: String,
    val arabicTafsir: String
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val referenceKey: String, // e.g., "quran_2_255", "hadith_bukhari_1"
    val itemType: String, // "QURAN", "HADITH", "DHIKR"
    val title: String,
    val subtitle: String,
    val arabicText: String,
    val updatedAtMillis: Long
)

@Entity(tableName = "adhkar_progress")
data class AdhkarProgressEntity(
    @PrimaryKey val categoryId: String,
    val completedCount: Int,
    val targetCount: Int,
    val updatedAtMillis: Long
)

@Entity(tableName = "downloaded_audio")
data class DownloadedAudioEntity(
    @PrimaryKey val audioKey: String, // e.g., "surah_001_abdullah_mousa" or "adhan_makkah"
    val title: String,
    val localFilePath: String,
    val sizeBytes: Long,
    val downloadedAtMillis: Long
)
