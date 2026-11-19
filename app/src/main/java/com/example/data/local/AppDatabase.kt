package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedSurahAyahEntity::class,
        BookmarkEntity::class,
        AdhkarProgressEntity::class,
        DownloadedAudioEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quranCacheDao(): QuranCacheDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun adhkarProgressDao(): AdhkarProgressDao
    abstract fun downloadedAudioDao(): DownloadedAudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soufian_islam_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
