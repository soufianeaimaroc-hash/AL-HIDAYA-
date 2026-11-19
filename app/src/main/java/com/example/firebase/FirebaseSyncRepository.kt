package com.example.firebase

import android.content.Context
import com.example.R
import com.example.data.local.AdhkarProgressEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserPrefsState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeBookmarks(userId: String): Flow<List<BookmarkDoc>> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId/bookmarks"
        emitAll(
            db.collection("users").document(userId).collection("bookmarks")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        BookmarkDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeUserProfile(userId: String): Flow<UserProfileDoc?> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfileDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun syncUserProfile(prefs: UserPrefsState, displayName: String): Result<Unit> {
        var docPath = "users/unauthenticated"
        return try {
            val uid = requireUserId()
            docPath = "users/$uid"
            val docRef = db.collection("users").document(uid)
            val existingSnap = docRef.get().await()
            val safeName = displayName.trim().ifBlank { "Muslim User" }.take(100)
            val safeCity = prefs.cityNameEn.trim().ifBlank { "Casablanca" }.take(100)
            val safeSurahName = prefs.lastSurahName.trim().take(100)

            if (!existingSnap.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "language" to if (prefs.language in listOf("ar", "en")) prefs.language else "ar",
                    "themeMode" to if (prefs.themeMode in listOf("system", "light", "dark")) prefs.themeMode else "system",
                    "cityName" to safeCity,
                    "latitude" to prefs.latitude.coerceIn(-90.0, 90.0),
                    "longitude" to prefs.longitude.coerceIn(-180.0, 180.0),
                    "calculationMethod" to prefs.calculationMethod,
                    "quranFontSize" to prefs.quranFontSize.coerceIn(16, 40).toLong(),
                    "lastSurahNumber" to prefs.lastSurahNumber.coerceIn(1, 114).toLong(),
                    "lastAyahNumber" to prefs.lastAyahNumber.coerceIn(1, 286).toLong(),
                    "lastSurahName" to safeSurahName,
                    "tasbihTotalCount" to prefs.tasbihTotalCount.coerceAtLeast(0).toLong(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "displayName" to safeName,
                    "language" to if (prefs.language in listOf("ar", "en")) prefs.language else "ar",
                    "themeMode" to if (prefs.themeMode in listOf("system", "light", "dark")) prefs.themeMode else "system",
                    "cityName" to safeCity,
                    "latitude" to prefs.latitude.coerceIn(-90.0, 90.0),
                    "longitude" to prefs.longitude.coerceIn(-180.0, 180.0),
                    "calculationMethod" to prefs.calculationMethod,
                    "quranFontSize" to prefs.quranFontSize.coerceIn(16, 40).toLong(),
                    "lastSurahNumber" to prefs.lastSurahNumber.coerceIn(1, 114).toLong(),
                    "lastAyahNumber" to prefs.lastAyahNumber.coerceIn(1, 286).toLong(),
                    "lastSurahName" to safeSurahName,
                    "tasbihTotalCount" to prefs.tasbihTotalCount.coerceAtLeast(0).toLong(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun upsertBookmark(bookmark: BookmarkEntity): Result<Unit> {
        val safeKey = sanitizeDocId(bookmark.referenceKey)
        var docPath = "users/unauthenticated/bookmarks/$safeKey"
        return try {
            val uid = requireUserId()
            docPath = "users/$uid/bookmarks/$safeKey"
            val docRef = db.collection("users").document(uid).collection("bookmarks").document(safeKey)
            val existingSnap = docRef.get().await()

            val safeTitle = bookmark.title.trim().ifBlank { "Bookmark" }.take(200)
            val safeSubtitle = bookmark.subtitle.trim().take(300)
            val safeArabic = bookmark.arabicText.trim().ifBlank { safeTitle }.take(4000)

            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "itemType" to if (bookmark.itemType in listOf("QURAN", "HADITH", "DHIKR")) bookmark.itemType else "QURAN",
                    "title" to safeTitle,
                    "subtitle" to safeSubtitle,
                    "arabicText" to safeArabic,
                    "referenceKey" to safeKey,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "title" to safeTitle,
                        "subtitle" to safeSubtitle,
                        "arabicText" to safeArabic,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteBookmark(referenceKey: String): Result<Unit> {
        val safeKey = sanitizeDocId(referenceKey)
        var docPath = "users/unauthenticated/bookmarks/$safeKey"
        return try {
            val uid = requireUserId()
            docPath = "users/$uid/bookmarks/$safeKey"
            db.collection("users").document(uid).collection("bookmarks").document(safeKey)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun syncAdhkarProgress(progress: AdhkarProgressEntity): Result<Unit> {
        val safeId = sanitizeDocId(progress.categoryId)
        var docPath = "users/unauthenticated/adhkar_progress/$safeId"
        return try {
            val uid = requireUserId()
            docPath = "users/$uid/adhkar_progress/$safeId"
            val docRef = db.collection("users").document(uid).collection("adhkar_progress").document(safeId)
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "categoryId" to safeId.take(60),
                        "completedCount" to progress.completedCount.coerceAtLeast(0).toLong(),
                        "targetCount" to progress.targetCount.coerceAtLeast(1).toLong(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "completedCount" to progress.completedCount.coerceAtLeast(0).toLong(),
                        "targetCount" to progress.targetCount.coerceAtLeast(1).toLong(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    private fun sanitizeDocId(raw: String): String {
        return raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120).ifBlank { "item_1" }
    }
}
