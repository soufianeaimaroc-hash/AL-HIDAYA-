package com.example.firebase

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Muslim User",
    val language: String = "ar",
    val themeMode: String = "system",
    val cityName: String = "Casablanca",
    val latitude: Double = 33.5731,
    val longitude: Double = -7.5898,
    val calculationMethod: String = "MOROCCO",
    val quranFontSize: Long = 24L,
    val lastSurahNumber: Long = 1L,
    val lastAyahNumber: Long = 1L,
    val lastSurahName: String = "الفاتحة",
    val tasbihTotalCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookmarkDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val itemType: String = "QURAN",
    val title: String = "",
    val subtitle: String = "",
    val arabicText: String = "",
    val referenceKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdhkarProgressDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val categoryId: String = "",
    val completedCount: Long = 0L,
    val targetCount: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
