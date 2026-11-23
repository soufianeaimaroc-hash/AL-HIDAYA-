package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.LocalAppStrings
import com.example.firebase.AuthManager
import com.example.ui.MainViewModel
import com.example.ui.components.GoldOrnamentalDivider
import com.example.ui.components.IslamicHeroCanvasBackground
import com.example.ui.components.MiniAudioPlayerBar
import com.example.ui.screens.AboutDeveloperScreen
import com.example.ui.screens.AdhkarAndTasbihScreen
import com.example.ui.screens.HadithScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrayerAndAdhanScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.RuqyahScreen
import com.example.ui.screens.SettingsAndAccountScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.theme.AlHidayaTheme
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import com.example.ui.theme.PlayfairFontFamily
import kotlinx.coroutines.delay

object Routes {
    const val HOME = "home"
    const val QURAN = "quran"
    const val PRAYER = "prayer?tab={tab}"
    const val QIBLA = "qibla"
    const val ADHKAR = "adhkar?tab={tab}"
    const val RUQYAH = "ruqyah"
    const val HADITH = "hadith"
    const val SETTINGS = "settings"
    const val SURAH_READER = "surah_reader/{surahNumber}/{pageMode}"
    const val ABOUT_DEVELOPER = "about_developer"

    fun prayer(tab: Int = 0) = "prayer?tab=$tab"
    fun adhkar(tab: Int = 0) = "adhkar?tab=$tab"
    fun surahReader(surahNumber: Int, pageMode: Boolean = false) = "surah_reader/$surahNumber/$pageMode"
}

private data class NavTab(
    val route: String,
    val baseRoute: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            AlHidayaApp(viewModel)
        }
    }
}

@Composable
fun AlHidayaApp(viewModel: MainViewModel) {
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val schedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
    val qiblaBearing by viewModel.qiblaBearing.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val adhkarProgress by viewModel.adhkarProgress.collectAsStateWithLifecycle()
    val downloadedKeys by viewModel.downloadedAudioKeys.collectAsStateWithLifecycle()
    val downloadingKeys by viewModel.downloadingKeys.collectAsStateWithLifecycle()
    val cachedSurahs by viewModel.cachedSurahNumbers.collectAsStateWithLifecycle()
    val surahListState by viewModel.surahListState.collectAsStateWithLifecycle()
    val locationUiState by viewModel.locationUiState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val selectedRuqyahCategory by viewModel.selectedRuqyahCategory.collectAsStateWithLifecycle()
    val activeRuqyahTrackId by viewModel.activeRuqyahTrackId.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1100L)
        showSplash = false
    }

    LaunchedEffect(Unit) {
        val cm = CredentialManager.create(context)
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = cm,
            scope = scope,
            onAuthSuccess = {},
            onUnauthenticated = {}
        )
    }

    AlHidayaTheme(
        themeMode = prefs.themeMode,
        language = prefs.language
    ) {
        val s = LocalAppStrings.current
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(statusBanner) {
            statusBanner?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearBanner()
            }
        }

        val tabs = listOf(
            NavTab(Routes.HOME, Routes.HOME, s.navHome, Icons.Filled.Home, Icons.Outlined.Home),
            NavTab(Routes.QURAN, Routes.QURAN, s.navQuran, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
            NavTab(Routes.prayer(0), "prayer", s.navPrayer, Icons.Filled.AccessTimeFilled, Icons.Outlined.AccessTime),
            NavTab(Routes.QIBLA, Routes.QIBLA, s.navQibla, Icons.Filled.Explore, Icons.Outlined.Explore),
            NavTab(Routes.adhkar(0), "adhkar", s.navAdhkar, Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism),
            NavTab(Routes.RUQYAH, Routes.RUQYAH, s.ruqyahTitle, Icons.Filled.Shield, Icons.Outlined.Shield),
            NavTab(Routes.HADITH, Routes.HADITH, s.navHadith, Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
            NavTab(Routes.SETTINGS, Routes.SETTINGS, s.navSettings, Icons.Filled.Settings, Icons.Outlined.Settings)
        )

        val hideBottomNav = currentRoute.startsWith("surah_reader") || currentRoute == Routes.ABOUT_DEVELOPER

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!useRail && !hideBottomNav) {
                        Column {
                            MiniAudioPlayerBar(
                                audioState = audioState,
                                onSelectSurah = viewModel::jumpToSurahInPlayer,
                                onSetRepeatConfig = viewModel::setRepeatConfig
                            )
                            NavigationBar {
                                // Show primary tabs on compact phone bottom bar; Ruqyah, Hadith & Adhkar accessible via Quick Cards & Rail
                                val compactTabs = listOf(tabs[0], tabs[1], tabs[2], tabs[3], tabs[4], tabs[7])
                                compactTabs.forEach { tab ->
                                    val selected = currentRoute.startsWith(tab.baseRoute)
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo(Routes.HOME) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_${tab.baseRoute}")
                                    )
                                }
                            }
                        }
                    } else if (hideBottomNav) {
                        MiniAudioPlayerBar(
                            audioState = audioState,
                            onSelectSurah = viewModel::jumpToSurahInPlayer,
                            onSetRepeatConfig = viewModel::setRepeatConfig
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail && !hideBottomNav) {
                        NavigationRail(modifier = Modifier.fillMaxHeight()) {
                            tabs.forEach { tab ->
                                val selected = currentRoute.startsWith(tab.baseRoute)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(Routes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label, maxLines = 1) },
                                    modifier = Modifier.testTag("rail_${tab.baseRoute}")
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        if (useRail && !hideBottomNav) {
                            MiniAudioPlayerBar(
                                audioState = audioState,
                                onSelectSurah = viewModel::jumpToSurahInPlayer,
                                onSetRepeatConfig = viewModel::setRepeatConfig
                            )
                        }

                        NavHost(
                            navController = navController,
                            startDestination = Routes.HOME,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(Routes.HOME) {
                                HomeScreen(
                                    prefs = prefs,
                                    schedule = schedule,
                                    bookmarks = bookmarks,
                                    onNavigateQuran = { navController.navigate(Routes.QURAN) },
                                    onNavigateSurah = { surahNum ->
                                        navController.navigate(Routes.surahReader(surahNum, false))
                                    },
                                    onNavigateAdhkar = { navController.navigate(Routes.adhkar(0)) },
                                    onNavigateRuqyah = { navController.navigate(Routes.RUQYAH) },
                                    onNavigateHadith = { navController.navigate(Routes.HADITH) },
                                    onNavigateQibla = { navController.navigate(Routes.QIBLA) },
                                    onNavigateAdhan = { navController.navigate(Routes.prayer(1)) },
                                    onNavigateCityPicker = { navController.navigate(Routes.prayer(0)) },
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onShareText = { txt -> viewModel.shareText(context, txt) }
                                )
                            }

                            composable(Routes.QURAN) {
                                QuranScreen(
                                    prefs = prefs,
                                    surahListState = surahListState,
                                    bookmarks = bookmarks,
                                    cachedSurahs = cachedSurahs,
                                    downloadedKeys = downloadedKeys,
                                    downloadingKeys = downloadingKeys,
                                    onRetryLoadSurahs = viewModel::retryLoadSurahs,
                                    onDismissSurahSyncError = viewModel::dismissSurahSyncError,
                                    onOpenSurah = { surahNum, pageMode ->
                                        navController.navigate(Routes.surahReader(surahNum, pageMode))
                                    },
                                    onPlaySurah = viewModel::playSurahAbdullahMousa,
                                    onPlayQuranOnceFromSurah = viewModel::playQuranOnceFromSurah,
                                    onDownloadSurah = viewModel::downloadSurahForOffline,
                                    onSearchVerses = viewModel::searchQuran
                                )
                            }

                            composable(
                                route = Routes.SURAH_READER,
                                arguments = listOf(
                                    navArgument("surahNumber") { type = NavType.IntType },
                                    navArgument("pageMode") { type = NavType.BoolType }
                                )
                            ) { backStack ->
                                val surahNum = backStack.arguments?.getInt("surahNumber") ?: 1
                                val pageMode = backStack.arguments?.getBoolean("pageMode") ?: false
                                SurahReaderScreen(
                                    surahNumber = surahNum,
                                    initialPageMode = pageMode,
                                    prefs = prefs,
                                    bookmarks = bookmarks,
                                    audioState = audioState,
                                    downloadedKeys = downloadedKeys,
                                    downloadingKeys = downloadingKeys,
                                    loadInstantSurahAyahs = viewModel::getInstantSurahAyahs,
                                    loadSurahAyahs = viewModel::loadSurahAyahs,
                                    loadMushafPage = viewModel::loadMushafPage,
                                    onUpdateLastRead = viewModel::updateLastRead,
                                    onPlaySurah = viewModel::playSurahAbdullahMousa,
                                    onPlayQuranOnceFromSurah = viewModel::playQuranOnceFromSurah,
                                    onPlayPageOnce = viewModel::playPageAyahsOnce,
                                    onPlayAyah = viewModel::playSingleAyahAudio,
                                    onSetRepeatConfig = viewModel::setRepeatConfig,
                                    onDownloadSurah = viewModel::downloadSurahForOffline,
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onShareText = { txt -> viewModel.shareText(context, txt) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Routes.PRAYER,
                                arguments = listOf(
                                    navArgument("tab") {
                                        type = NavType.IntType
                                        defaultValue = 0
                                    }
                                )
                            ) { backStack ->
                                val tab = backStack.arguments?.getInt("tab") ?: 0
                                PrayerAndAdhanScreen(
                                    initialTab = tab,
                                    prefs = prefs,
                                    schedule = schedule,
                                    audioState = audioState,
                                    downloadedKeys = downloadedKeys,
                                    downloadingKeys = downloadingKeys,
                                    locationUiState = locationUiState,
                                    qiblaBearing = qiblaBearing,
                                    onDetectGps = { viewModel.detectGpsLocation(isAutoCheck = false) },
                                    onPermissionDenied = viewModel::markGpsPermissionDenied,
                                    onDismissLocationStatus = viewModel::dismissLocationStatus,
                                    onSelectCity = viewModel::selectCity,
                                    onSelectMethod = viewModel::setCalculationMethod,
                                    onSelectAsrMadhab = viewModel::setAsrMadhab,
                                    onSetPrayerOffset = viewModel::setPrayerOffset,
                                    onResetOffsets = viewModel::resetPrayerOffsets,
                                    onTogglePrayerAlert = viewModel::setPrayerEnabled,
                                    onTogglePlayAdhanOnNotification = viewModel::setPlayAdhanOnNotification,
                                    onPreviewAdhan = viewModel::previewAdhan,
                                    onDownloadAdhan = viewModel::downloadAdhanForOffline,
                                    onAssignAdhan = viewModel::assignPrayerAdhan
                                )
                            }

                            composable(Routes.QIBLA) {
                                QiblaScreen(
                                    prefs = prefs,
                                    qiblaBearing = qiblaBearing
                                )
                            }

                            composable(
                                route = Routes.ADHKAR,
                                arguments = listOf(
                                    navArgument("tab") {
                                        type = NavType.IntType
                                        defaultValue = 0
                                    }
                                )
                            ) { backStack ->
                                val tab = backStack.arguments?.getInt("tab") ?: 0
                                AdhkarAndTasbihScreen(
                                    initialTab = tab,
                                    prefs = prefs,
                                    bookmarks = bookmarks,
                                    adhkarProgress = adhkarProgress,
                                    audioState = audioState,
                                    downloadedKeys = downloadedKeys,
                                    downloadingKeys = downloadingKeys,
                                    selectedRuqyahCategory = selectedRuqyahCategory,
                                    activeRuqyahTrackId = activeRuqyahTrackId,
                                    onUpdateCategoryProgress = viewModel::updateAdhkarProgress,
                                    onIncrementTasbih = viewModel::incrementTasbih,
                                    onSetTasbihTarget = viewModel::setTasbihTarget,
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onPlayRuqyah = viewModel::playRuqyahTrack,
                                    onDownloadRuqyah = viewModel::downloadRuqyahTrack,
                                    onSelectRuqyahCategory = viewModel::selectRuqyahCategory,
                                    onSelectRuqyahReciterEdition = viewModel::selectRuqyahReciterEdition,
                                    onResumeLastRuqyah = viewModel::resumeLastRuqyah,
                                    onSeekAudioTo = viewModel::seekAudioTo,
                                    onSeekAudioBy = viewModel::seekAudioBy,
                                    onSetAudioVolume = viewModel::setAudioVolume,
                                    onSetSleepTimerMinutes = viewModel::setSleepTimerMinutes,
                                    onSetRepeatConfig = viewModel::setRepeatConfig,
                                    onShareText = { txt -> viewModel.shareText(context, txt) }
                                )
                            }

                            composable(Routes.RUQYAH) {
                                RuqyahScreen(
                                    prefs = prefs,
                                    audioState = audioState,
                                    bookmarks = bookmarks,
                                    downloadedKeys = downloadedKeys,
                                    downloadingKeys = downloadingKeys,
                                    selectedCategory = selectedRuqyahCategory,
                                    activeRuqyahTrackId = activeRuqyahTrackId,
                                    onSelectCategory = viewModel::selectRuqyahCategory,
                                    onSelectReciterEdition = viewModel::selectRuqyahReciterEdition,
                                    onPlayRuqyah = viewModel::playRuqyahTrack,
                                    onResumeLastRuqyah = viewModel::resumeLastRuqyah,
                                    onDownloadRuqyah = viewModel::downloadRuqyahTrack,
                                    onSeekTo = viewModel::seekAudioTo,
                                    onSeekBy = viewModel::seekAudioBy,
                                    onSetVolume = viewModel::setAudioVolume,
                                    onSetSleepTimerMinutes = viewModel::setSleepTimerMinutes,
                                    onSetRepeatConfig = viewModel::setRepeatConfig,
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onShareText = { txt -> viewModel.shareText(context, txt) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.HADITH) {
                                HadithScreen(
                                    prefs = prefs,
                                    bookmarks = bookmarks,
                                    loadHadithChapter = viewModel::loadHadithChapter,
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onShareText = { txt -> viewModel.shareText(context, txt) }
                                )
                            }

                            composable(Routes.SETTINGS) {
                                SettingsAndAccountScreen(
                                    prefs = prefs,
                                    currentUser = currentUser,
                                    bookmarks = bookmarks,
                                    onLanguageChange = viewModel::setLanguage,
                                    onThemeChange = viewModel::setThemeMode,
                                    onFontSizeChange = viewModel::setQuranFontSize,
                                    onOpenPrayerCity = { navController.navigate(Routes.prayer(0)) },
                                    onOpenAboutDeveloper = { navController.navigate(Routes.ABOUT_DEVELOPER) }
                                )
                            }

                            composable(Routes.ABOUT_DEVELOPER) {
                                AboutDeveloperScreen(onBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showSplash,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AlHidayaSplashOverlay(
                    appName = s.appName,
                    appSubtitle = s.appSubtitle,
                    developerCredit = "${s.developerProfessionalName} • ${s.developerRole}",
                    onDismiss = { showSplash = false }
                )
            }
        }
    }
}

@Composable
private fun AlHidayaSplashOverlay(
    appName: String,
    appSubtitle: String,
    developerCredit: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(EmeraldDeep, EmeraldPrimary, EmeraldDeep)))
            .clickable(onClick = onDismiss)
            .testTag("al_hidaya_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        IslamicHeroCanvasBackground(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = EmeraldDeep,
                shadowElevation = 14.dp,
                border = BorderStroke(2.5.dp, GoldChampagne),
                modifier = Modifier.size(136.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_al_hidaya_emblem),
                    contentDescription = appName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = appName,
                fontFamily = PlayfairFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GoldChampagne,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.90f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
            GoldOrnamentalDivider(modifier = Modifier.padding(horizontal = 40.dp))
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = developerCredit,
                style = MaterialTheme.typography.labelMedium,
                color = GoldChampagne.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
