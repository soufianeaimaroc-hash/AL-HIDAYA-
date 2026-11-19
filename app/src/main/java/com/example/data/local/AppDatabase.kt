package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CachedSurahEntity::class,
        CachedSurahAyahEntity::class,
        BookmarkEntity::class,
        AdhkarProgressEntity::class,
        DownloadedAudioEntity::class
    ],
    version = 2,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cached_surahs` (
                        `number` INTEGER NOT NULL,
                        `arabicName` TEXT NOT NULL,
                        `englishName` TEXT NOT NULL,
                        `englishTranslation` TEXT NOT NULL,
                        `ayahCount` INTEGER NOT NULL,
                        `isMeccan` INTEGER NOT NULL,
                        `startJuz` INTEGER NOT NULL,
                        `startPage` INTEGER NOT NULL,
                        `syncedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`number`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soufian_islam_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
