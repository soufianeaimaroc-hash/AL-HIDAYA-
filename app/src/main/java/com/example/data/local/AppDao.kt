package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranCacheDao {
    @Query("SELECT * FROM cached_surahs ORDER BY number ASC")
    fun observeCachedSurahs(): Flow<List<CachedSurahEntity>>

    @Query("SELECT * FROM cached_surahs ORDER BY number ASC")
    suspend fun getCachedSurahsOnce(): List<CachedSurahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSurahs(surahs: List<CachedSurahEntity>)

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun observeSurahAyahs(surahNumber: Int): Flow<List<CachedSurahAyahEntity>>

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    suspend fun getSurahAyahsOnce(surahNumber: Int): List<CachedSurahAyahEntity>

    @Query("SELECT * FROM cached_ayahs WHERE page = :page ORDER BY globalAyahNumber ASC")
    suspend fun getPageAyahsOnce(page: Int): List<CachedSurahAyahEntity>

    @Query("SELECT * FROM cached_ayahs WHERE globalKey IN (:keys)")
    suspend fun getAyahsByKeys(keys: List<String>): List<CachedSurahAyahEntity>

    @Query("SELECT DISTINCT surahNumber FROM cached_ayahs")
    fun observeCachedSurahNumbers(): Flow<List<Int>>

    @Query("SELECT * FROM cached_ayahs WHERE arabicTextUthmani LIKE '%' || :query || '%' OR englishTranslation LIKE '%' || :query || '%' LIMIT 50")
    suspend fun searchLocalAyahs(query: String): List<CachedSurahAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAyahs(ayahs: List<CachedSurahAyahEntity>)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY updatedAtMillis DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE referenceKey = :key)")
    fun observeIsBookmarked(key: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmarks(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE referenceKey = :key")
    suspend fun deleteBookmarkByKey(key: String)
}

@Dao
interface AdhkarProgressDao {
    @Query("SELECT * FROM adhkar_progress")
    fun observeAllProgress(): Flow<List<AdhkarProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: AdhkarProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AdhkarProgressEntity>)
}

@Dao
interface DownloadedAudioDao {
    @Query("SELECT * FROM downloaded_audio")
    fun observeAllDownloads(): Flow<List<DownloadedAudioEntity>>

    @Query("SELECT * FROM downloaded_audio WHERE audioKey = :key LIMIT 1")
    suspend fun getDownloadByKey(key: String): DownloadedAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(download: DownloadedAudioEntity)

    @Query("DELETE FROM downloaded_audio WHERE audioKey = :key")
    suspend fun deleteByKey(key: String)
}
