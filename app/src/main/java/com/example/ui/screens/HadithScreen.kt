package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppStrings
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserPrefsState
import com.example.data.seed.HadithBookInfo
import com.example.data.seed.HadithItem
import com.example.data.seed.HadithSeedData
import com.example.ui.components.GoldOrnamentalDivider
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldRoyal
import com.example.ui.theme.SuccessGreen

@Composable
fun HadithScreen(
    prefs: UserPrefsState,
    bookmarks: List<BookmarkEntity>,
    loadHadithChapter: suspend (HadithBookInfo, Int) -> List<HadithItem>,
    onToggleBookmark: (String, String, String, String, String) -> Unit,
    onShareText: (String) -> Unit
) {
    val s = LocalAppStrings.current
    val isAr = prefs.language == "ar"

    var selectedBook by remember { mutableStateOf(HadithSeedData.books.first()) }
    var selectedChapter by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    var hadiths by remember { mutableStateOf<List<HadithItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedBook, selectedChapter) {
        isLoading = true
        hadiths = loadHadithChapter(selectedBook, selectedChapter)
        isLoading = false
    }

    val filteredHadiths = remember(hadiths, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) hadiths
        else hadiths.filter {
            it.arabicText.contains(q) ||
                it.englishText.contains(q, ignoreCase = true) ||
                it.chapterNameAr.contains(q) ||
                it.hadithNumber.toString() == q
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = s.hadithCollectionsTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            // 6 Canonical Hadith Books
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HadithSeedData.books, key = { it.id }) { book ->
                    FilterChip(
                        selected = selectedBook.id == book.id,
                        onClick = { selectedBook = book },
                        label = {
                            Text(
                                text = if (isAr) book.titleAr else book.titleEn,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag("hadith_book_${book.id}")
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Chapters (الأبواب)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HadithSeedData.chapters, key = { it.number }) { ch ->
                    FilterChip(
                        selected = selectedChapter == ch.number,
                        onClick = { selectedChapter = ch.number },
                        label = {
                            Text(if (isAr) ch.titleAr else ch.titleEn, style = MaterialTheme.typography.labelMedium)
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(s.searchHadithHint) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hadith_search_input")
            )

            Spacer(Modifier.height(10.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldRoyal)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHadiths, key = { it.id }) { item ->
                        val bmKey = "hadith_${item.id}"
                        val isSaved = bookmarks.any { it.referenceKey == bmKey }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = SuccessGreen.copy(alpha = 0.14f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Verified, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (isAr) item.gradeAr else item.gradeEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                onToggleBookmark(
                                                    bmKey,
                                                    "HADITH",
                                                    "${item.bookNameAr} #${item.hadithNumber}",
                                                    item.englishText,
                                                    item.arabicText
                                                )
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                contentDescription = s.saveBookmark,
                                                tint = if (isSaved) GoldRoyal else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                onShareText("${item.arabicText}\n\n${item.englishText}\n(${item.bookNameAr} - ${item.hadithNumber}) - AL HIDAYA – الهداية")
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Filled.Share, contentDescription = s.shareText)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = item.arabicText,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 20.sp,
                                    lineHeight = 34.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (item.englishText.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = item.englishText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(10.dp))
                                GoldOrnamentalDivider()
                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = if (isAr) {
                                        "${s.hadithSourceLabel}: ${item.bookNameAr} • حديث رقم ${item.hadithNumber}"
                                    } else {
                                        "${s.hadithSourceLabel}: ${item.bookNameEn} • Hadith #${item.hadithNumber}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
