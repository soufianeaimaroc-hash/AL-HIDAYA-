package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.example.ui.components.MiniAudioPlayerBar
import com.example.ui.screens.AboutDeveloperScreen
import com.example.ui.screens.AdhkarAndTasbihScreen
import com.example.ui.screens.HadithScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrayerAndAdhanScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsAndAccountScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.theme.SoufianIslamTheme

object Routes {
    const val HOME = "home"
    const val QURAN = "quran"
    const val PRAYER = "prayer?tab={tab}"
    const val QIBLA = "qibla"
    const val ADHKAR = "adhkar?tab={tab}"
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
            SoufianIslamApp(viewModel)
        }
    }
}

@Composable
fun SoufianIslamApp(viewModel: MainViewModel) {
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
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    SoufianIslamTheme(
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
            NavTab(Routes.QURAN, Routes.QURAN, s.navQuran, Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
            NavTab(Routes.prayer(0), "prayer", s.navPrayer, Icons.Filled.AccessTimeFilled, Icons.Outlined.AccessTime),
            NavTab(Routes.QIBLA, Routes.QIBLA, s.navQibla, Icons.Filled.Explore, Icons.Outlined.Explore),
            NavTab(Routes.adhkar(0), "adhkar", s.navAdhkar, Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism),
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
                                // Show 5 primary tabs on compact phone bottom bar; Hadith & Adhkar accessible via Quick Cards & Settings/Tabs
                                val compactTabs = listOf(tabs[0], tabs[1], tabs[2], tabs[3], tabs[4], tabs[6])
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
                                    onNavigateRuqyah = { navController.navigate(Routes.adhkar(2)) },
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
                                    onDetectGps = viewModel::detectGpsLocation,
                                    onSelectCity = viewModel::selectCity,
                                    onSelectMethod = viewModel::setCalculationMethod,
                                    onTogglePrayerAlert = viewModel::setPrayerEnabled,
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
                                    onUpdateCategoryProgress = viewModel::updateAdhkarProgress,
                                    onIncrementTasbih = viewModel::incrementTasbih,
                                    onSetTasbihTarget = viewModel::setTasbihTarget,
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onPlayRuqyah = viewModel::playRuqyahTrack,
                                    onDownloadRuqyah = viewModel::downloadRuqyahTrack
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
        }
    }
}
