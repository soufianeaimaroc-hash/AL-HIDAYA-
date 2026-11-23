package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.HijriCalendarHelper
import com.example.data.local.BookmarkEntity
import com.example.data.local.UserPrefsState
import com.example.data.seed.AdhkarData
import com.example.data.seed.QuranCatalog
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.PrayerType
import com.example.ui.components.GoldOrnamentalDivider
import com.example.ui.components.IslamicHeroCanvasBackground
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import com.example.core.LocalAppStrings
import java.util.Calendar
import java.util.Locale

private data class QuickServiceItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    prefs: UserPrefsState,
    schedule: DailyPrayerSchedule,
    bookmarks: List<BookmarkEntity>,
    onNavigateQuran: () -> Unit,
    onNavigateSurah: (Int) -> Unit,
    onNavigateAdhkar: () -> Unit,
    onNavigateRuqyah: () -> Unit,
    onNavigateHadith: () -> Unit,
    onNavigateQibla: () -> Unit,
    onNavigateAdhan: () -> Unit,
    onNavigateCityPicker: () -> Unit,
    onToggleBookmark: (String, String, String, String, String) -> Unit,
    onShareText: (String) -> Unit
) {
    val s = LocalAppStrings.current
    val isAr = prefs.language == "ar"
    val context = LocalContext.current
    val dates = remember(prefs.language) { HijriCalendarHelper.getTodayDates(prefs.language) }
    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val ayahOfDay = remember(dayOfYear) { QuranCatalog.ayahOfTheDay(dayOfYear) }
    val duaOfDay = remember(dayOfYear) { AdhkarData.duaOfTheDay(dayOfYear) }

    val countdownText = remember(schedule.remainingMillis) {
        val totalSec = schedule.remainingMillis / 1000L
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val sec = totalSec % 60
        String.format(Locale.US, "%02d:%02d:%02d", h, m, sec)
    }

    val services = listOf(
        QuickServiceItem("quran", s.quranTitle, Icons.AutoMirrored.Filled.MenuBook, EmeraldPrimary, onNavigateQuran),
        QuickServiceItem("adhkar", s.adhkarTitle, Icons.Filled.VolunteerActivism, Color(0xFF147D52), onNavigateAdhkar),
        QuickServiceItem("hadith", s.hadithTitle, Icons.Outlined.AutoStories, Color(0xFF9E7B1C), onNavigateHadith),
        QuickServiceItem("qibla", s.qiblaTitle, Icons.Filled.Explore, Color(0xFF0E6E58), onNavigateQibla),
        QuickServiceItem("adhan", s.adhanTitle, Icons.Filled.NotificationsActive, GoldRoyal, onNavigateAdhan)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dedicated Islamic Top Bar: App Identity, Hijri / Gregorian Date, and Current City
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_top_bar"),
            color = EmeraldDeep,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_al_hidaya_emblem),
                            contentDescription = s.appName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = s.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldChampagne
                            )
                            Text(
                                text = dates.hijriText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = dates.gregorianText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onNavigateCityPicker)
                            .testTag("home_city_chip"),
                        color = Color.White.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, GoldChampagne.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = s.cityLabel,
                                tint = GoldChampagne,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val cityLabel = if (isAr) prefs.cityName else prefs.cityNameEn
                            val countryLabel = if (isAr) prefs.countryNameAr else prefs.countryNameEn
                            val combinedLoc = if (countryLabel.isNotBlank() && !cityLabel.contains(countryLabel)) {
                                "$cityLabel • $countryLabel"
                            } else {
                                cityLabel
                            }
                            Text(
                                text = combinedLoc,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Next Prayer Time & Countdown Timer Container (with Mosque/Crescent Islamic Canvas)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_next_prayer_container"),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.55f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IslamicHeroCanvasBackground(
                            modifier = Modifier.matchParentSize()
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = s.greetingSalam,
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldChampagne
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.Black.copy(alpha = 0.30f),
                                border = BorderStroke(1.dp, GoldChampagne.copy(alpha = 0.40f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("next_prayer_countdown_box")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = s.nextPrayer,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = GoldChampagne
                                        )
                                        val nextName = if (isAr) schedule.nextPrayer.type.nameAr else schedule.nextPrayer.type.nameEn
                                        Text(
                                            text = "$nextName • ${schedule.nextPrayer.formattedTime}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = s.remainingTime,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.78f)
                                        )
                                        Text(
                                            text = countdownText,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldChampagne
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // All 6 Prayer Times Row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(schedule.prayers) { entry ->
                                    val isNext = entry.type == schedule.nextPrayer.type
                                    val isCurrent = schedule.currentPrayer?.type == entry.type
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = when {
                                            isNext -> GoldRoyal
                                            isCurrent -> Color.White.copy(alpha = 0.24f)
                                            else -> Color.White.copy(alpha = 0.12f)
                                        },
                                        border = when {
                                            isNext -> null
                                            isCurrent -> BorderStroke(1.dp, GoldChampagne.copy(alpha = 0.7f))
                                            else -> BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                                        },
                                        modifier = Modifier.clickable(onClick = onNavigateAdhan)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (isAr) entry.type.nameAr else entry.type.nameEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isNext) EmeraldDeep else Color.White.copy(alpha = 0.9f)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = entry.formattedTime,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isNext) EmeraldDeep else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Quick Islamic Services Grid
            item {
                Text(
                    text = s.quickServices,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    services.take(3).forEach { item ->
                        QuickServiceTile(
                            item = item,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_btn_${item.id}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    services.drop(3).forEach { item ->
                        QuickServiceTile(
                            item = item,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_btn_${item.id}")
                        )
                    }
                    // Continue Reading shortcut tile
                    QuickServiceTile(
                        item = QuickServiceItem(
                            id = "continue_quran",
                            title = if (isAr) "سورة ${prefs.lastSurahName}" else "Resume ${prefs.lastSurahNumber}",
                            icon = Icons.Filled.AutoAwesome,
                            accent = EmeraldPrimary,
                            onClick = { onNavigateSurah(prefs.lastSurahNumber) }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_btn_resume_quran")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                QuickServiceTile(
                    item = QuickServiceItem(
                        id = "ruqyah",
                        title = s.ruqyahScreenTitle,
                        icon = Icons.Filled.Shield,
                        accent = EmeraldDeep,
                        onClick = onNavigateRuqyah
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_btn_ruqyah")
                )
            }

            // 3. Ayah of the Day
            item {
                val ayahKey = "quran_${ayahOfDay.surahNumber}_${ayahOfDay.ayahNumber}"
                val isAyahSaved = bookmarks.any { it.referenceKey == ayahKey }
                DailyWisdomCard(
                    badgeTitle = s.ayahOfTheDay,
                    arabicText = ayahOfDay.arabicText,
                    translationText = if (!isAr) ayahOfDay.englishText else "",
                    sourceText = if (isAr) {
                        "سورة ${ayahOfDay.surahNameAr} • الآية ${ayahOfDay.ayahNumber}"
                    } else {
                        "${ayahOfDay.surahNameEn} • Ayah ${ayahOfDay.ayahNumber}"
                    },
                    isBookmarked = isAyahSaved,
                    onClickCard = { onNavigateSurah(ayahOfDay.surahNumber) },
                    onBookmarkClick = {
                        onToggleBookmark(
                            ayahKey,
                            "QURAN",
                            "سورة ${ayahOfDay.surahNameAr} - ${ayahOfDay.ayahNumber}",
                            ayahOfDay.englishText,
                            ayahOfDay.arabicText
                        )
                    },
                    onShareClick = {
                        onShareText("${ayahOfDay.arabicText}\n\n${ayahOfDay.englishText}\n(${ayahOfDay.surahNameAr}: ${ayahOfDay.ayahNumber}) - AL HIDAYA – الهداية")
                    }
                )
            }

            // 4. Dua of the Day
            item {
                val duaKey = "dua_day_${dayOfYear}"
                val isDuaSaved = bookmarks.any { it.referenceKey == duaKey }
                DailyWisdomCard(
                    badgeTitle = s.duaOfTheDay,
                    arabicText = duaOfDay.arabicText,
                    translationText = if (!isAr) duaOfDay.englishText else "",
                    sourceText = if (isAr) duaOfDay.sourceAr else duaOfDay.sourceEn,
                    isBookmarked = isDuaSaved,
                    onClickCard = onNavigateAdhkar,
                    onBookmarkClick = {
                        onToggleBookmark(
                            duaKey,
                            "DHIKR",
                            s.duaOfTheDay,
                            duaOfDay.sourceEn,
                            duaOfDay.arabicText
                        )
                    },
                    onShareClick = {
                        onShareText("${duaOfDay.arabicText}\n\n${duaOfDay.englishText}\n(${duaOfDay.sourceAr}) - AL HIDAYA – الهداية")
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun QuickServiceTile(
    item: QuickServiceItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DailyWisdomCard(
    badgeTitle: String,
    arabicText: String,
    translationText: String,
    sourceText: String,
    isBookmarked: Boolean,
    onClickCard: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickCard),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = badgeTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Row {
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GoldRoyal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = arabicText,
                fontFamily = AmiriFontFamily,
                fontSize = 22.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            if (translationText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = translationText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            GoldOrnamentalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = sourceText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
