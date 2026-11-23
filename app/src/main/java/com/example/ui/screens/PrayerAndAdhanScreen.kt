package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale
import com.example.audio.AudioPlaybackService
import com.example.audio.AudioPlaybackState
import com.example.core.HijriCalendarHelper
import com.example.core.LocalAppStrings
import com.example.data.local.UserPrefsState
import com.example.data.seed.AdhanCatalog
import com.example.data.seed.AdhanTrack
import com.example.data.seed.CitiesCatalog
import com.example.data.seed.CityOption
import com.example.prayer.AsrMadhab
import com.example.prayer.CalculationMethod
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.LocationHelper
import com.example.prayer.PrayerNotificationScheduler
import com.example.prayer.PrayerType
import com.example.ui.LocationUiState
import com.example.ui.components.IslamicHeroCanvasBackground
import com.example.ui.components.LocationQiblaMapCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun PrayerAndAdhanScreen(
    initialTab: Int = 0,
    prefs: UserPrefsState,
    schedule: DailyPrayerSchedule,
    audioState: AudioPlaybackState,
    downloadedKeys: Set<String>,
    downloadingKeys: Set<String>,
    locationUiState: LocationUiState = LocationUiState.Idle,
    qiblaBearing: Double = 94.0,
    onDetectGps: () -> Unit,
    onPermissionDenied: () -> Unit = {},
    onDismissLocationStatus: () -> Unit = {},
    onSelectCity: (CityOption) -> Unit,
    onSelectMethod: (String) -> Unit,
    onSelectAsrMadhab: (String) -> Unit = {},
    onSetPrayerOffset: (String, Int) -> Unit = { _, _ -> },
    onResetOffsets: () -> Unit = {},
    onTogglePrayerAlert: (String, Boolean) -> Unit,
    onTogglePlayAdhanOnNotification: (Boolean) -> Unit = {},
    onPreviewAdhan: (AdhanTrack) -> Unit,
    onDownloadAdhan: (AdhanTrack) -> Unit,
    onAssignAdhan: (String, String) -> Unit
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val isAr = prefs.language == "ar"
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showGpsConsentDialog by remember { mutableStateOf(false) }
    var showOffsetsDialog by remember { mutableStateOf(false) }
    var assignTargetTrack by remember { mutableStateOf<AdhanTrack?>(null) }

    val dates = remember(prefs.language) { HijriCalendarHelper.getTodayDates(prefs.language) }
    val countdownText = remember(schedule.remainingMillis) {
        val totalSec = schedule.remainingMillis / 1000L
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val sec = totalSec % 60
        String.format(Locale.US, "%02d:%02d:%02d", h, m, sec)
    }

    val cityDisplay = if (isAr) prefs.cityName else prefs.cityNameEn
    val regionCountryDisplay = remember(prefs, isAr) {
        val region = if (isAr) prefs.regionNameAr else prefs.regionNameEn
        val country = if (isAr) prefs.countryNameAr else prefs.countryNameEn
        listOf(region, country).filter { it.isNotBlank() }.distinct().joinToString(" • ")
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onDetectGps()
        } else {
            onPermissionDenied()
        }
    }

    fun triggerGpsRequest() {
        val granted = LocationHelper.hasLocationPermission(context)
        if (granted) {
            onDetectGps()
        } else {
            showGpsConsentDialog = true
        }
    }

    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (granted) {
            PrayerNotificationScheduler.scheduleDailyPrayers(context, prefs)
        }
    }

    var canExactAlarm by remember { mutableStateOf(PrayerNotificationScheduler.canScheduleExactAlarms(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, prefs) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
                val exactGranted = PrayerNotificationScheduler.canScheduleExactAlarms(context)
                if (exactGranted && !canExactAlarm) {
                    PrayerNotificationScheduler.scheduleDailyPrayers(context, prefs)
                }
                hasNotifPermission = notifGranted
                canExactAlarm = exactGranted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(s.navPrayer, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_prayer_times")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(s.adhanLibraryTitle, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_adhan_library")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("prayer_times_lazy_list"),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. GPS / Location Status Banners
                    if (locationUiState is LocationUiState.Detecting) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gps_detecting_banner"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                    Text(
                                        text = s.gpsDetecting,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (locationUiState is LocationUiState.PermissionDenied || locationUiState is LocationUiState.WeakSignal || locationUiState is LocationUiState.Error) {
                        val msg = when (locationUiState) {
                            LocationUiState.PermissionDenied -> s.gpsPermissionDeniedBanner
                            LocationUiState.WeakSignal -> s.gpsWeakSignalBanner
                            is LocationUiState.Error -> locationUiState.message
                            LocationUiState.Idle,
                            LocationUiState.Detecting,
                            is LocationUiState.Updated -> ""
                        }
                        val bannerTag = if (locationUiState == LocationUiState.PermissionDenied) {
                            "gps_permission_denied_banner"
                        } else {
                            "gps_weak_signal_banner"
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(bannerTag),
                                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.14f)),
                                border = BorderStroke(1.dp, WarningAmber),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Warning, null, tint = WarningAmber)
                                            Spacer(Modifier.width(8.dp))
                                            Text(text = msg, style = MaterialTheme.typography.bodySmall)
                                        }
                                        IconButton(onClick = onDismissLocationStatus, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Filled.Close, contentDescription = s.backLabel, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { triggerGpsRequest() }) {
                                            Text(s.retry)
                                        }
                                        Button(onClick = { showCityDialog = true }) {
                                            Text(s.selectCityManually)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (locationUiState is LocationUiState.Updated && (locationUiState.usedOfflineFallback || locationUiState.movedCity)) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gps_status_info_banner"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (locationUiState.movedCity) {
                                            "${s.gpsMovedUpdatedBanner}: ${locationUiState.cityLabel}"
                                        } else {
                                            s.gpsOfflineCityFallbackBanner
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = onDismissLocationStatus, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Filled.Close, contentDescription = s.backLabel, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (!schedule.isTimeZoneVerified) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("timezone_mismatch_banner"),
                                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.14f)),
                                border = BorderStroke(1.dp, WarningAmber),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Schedule, null, tint = WarningAmber)
                                    Text(
                                        text = "${s.timezoneMismatchWarning} (${schedule.timeZoneId})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = s.notificationPermissionNeeded,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                        Text(s.grantPermission)
                                    }
                                }
                            }
                        }
                    }

                    if (!canExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = s.exactAlarmWarning,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                        }
                                    }) {
                                        Text(s.grantPermission)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Prayer Summary Hero Card (City + Dates + Current Prayer + Next Prayer Countdown)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prayer_summary_hero_card"),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.55f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                IslamicHeroCanvasBackground(modifier = Modifier.matchParentSize())
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = dates.hijriText,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldChampagne
                                            )
                                            Text(
                                                text = dates.gregorianText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color.Black.copy(alpha = 0.35f),
                                            border = BorderStroke(0.8.dp, GoldChampagne.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = if (prefs.locationSource == "GPS") s.gpsSourceLabel else s.manualSourceLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldChampagne,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            if (schedule.currentPrayer != null) {
                                                val currName = if (isAr) schedule.currentPrayer.type.nameAr else schedule.currentPrayer.type.nameEn
                                                Text(
                                                    text = "${s.currentPrayerLabel}: $currName",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = GoldChampagne
                                                )
                                            }
                                            val nextName = if (isAr) schedule.nextPrayer.type.nameAr else schedule.nextPrayer.type.nameEn
                                            Text(
                                                text = "${s.nextPrayer}: $nextName",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = schedule.nextPrayer.formattedTime,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = GoldChampagne,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = s.remainingTime,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = countdownText,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldChampagne,
                                                modifier = Modifier.testTag("prayer_hero_countdown")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Interactive Location & Qibla Vector Map Card
                    item {
                        LocationQiblaMapCard(
                            cityLabel = cityDisplay,
                            countryRegionLabel = regionCountryDisplay,
                            latitude = prefs.latitude,
                            longitude = prefs.longitude,
                            timeZoneId = schedule.timeZoneId,
                            isDstActive = schedule.isDstActive,
                            qiblaBearing = qiblaBearing,
                            isRefreshingGps = locationUiState is LocationUiState.Detecting,
                            onRefreshGps = { triggerGpsRequest() }
                        )
                    }

                    // 4. Location Controls, Method, Asr Madhab, Offsets, Adhan Consent Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.LocationOn, null, tint = GoldRoyal)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(s.cityLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = cityDisplay,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { triggerGpsRequest() },
                                            modifier = Modifier.testTag("gps_detect_button")
                                        ) {
                                            Icon(Icons.Filled.MyLocation, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("GPS")
                                        }
                                        Button(
                                            onClick = { showCityDialog = true },
                                            modifier = Modifier.testTag("manual_city_button")
                                        ) {
                                            Text(if (isAr) "تغيير" else "City")
                                        }
                                    }
                                }

                                val currentMethod = CalculationMethod.fromId(prefs.calculationMethod)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showMethodDialog = true }
                                        .padding(vertical = 4.dp)
                                        .testTag("calculation_method_row"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(s.calculationMethodLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = if (isAr) currentMethod.nameAr else currentMethod.nameEn,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    TextButton(onClick = { showMethodDialog = true }) {
                                        Text(if (isAr) "تبديل" else "Change")
                                    }
                                }

                                // Asr Madhab Selector (Shafi vs Hanafi)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = s.asrMadhabLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val selectedMadhab = AsrMadhab.fromId(prefs.asrMadhab)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = selectedMadhab == AsrMadhab.SHAFI,
                                            onClick = { onSelectAsrMadhab(AsrMadhab.SHAFI.id) },
                                            label = { Text(s.asrMadhabShafi, style = MaterialTheme.typography.labelMedium) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldPrimary,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("madhab_shafi_chip")
                                        )
                                        FilterChip(
                                            selected = selectedMadhab == AsrMadhab.HANAFI,
                                            onClick = { onSelectAsrMadhab(AsrMadhab.HANAFI.id) },
                                            label = { Text(s.asrMadhabHanafi, style = MaterialTheme.typography.labelMedium) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldPrimary,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("madhab_hanafi_chip")
                                        )
                                    }
                                }

                                // Manual Minute Offsets Button
                                OutlinedButton(
                                    onClick = { showOffsetsDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("prayer_offsets_button")
                                ) {
                                    Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(s.manualOffsetsLabel)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(s.prePrayerReminder, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = prefs.prePrayerReminderEnabled,
                                        onCheckedChange = { onTogglePrayerAlert("REMINDER", it) },
                                        modifier = Modifier.testTag("pre_prayer_switch")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(s.playAdhanOnNotifLabel, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = prefs.playAdhanOnNotification,
                                        onCheckedChange = onTogglePlayAdhanOnNotification,
                                        modifier = Modifier.testTag("play_adhan_consent_switch")
                                    )
                                }
                            }
                        }
                    }

                    // 5. Daily Prayer Times List (Highlighting Current Prayer & Next Prayer)
                    val offsets = prefs.toPrayerMinuteOffsets()
                    items(schedule.prayers, key = { it.type.key }) { entry ->
                        val isNext = entry.type == schedule.nextPrayer.type
                        val isCurrent = schedule.currentPrayer?.type == entry.type
                        val offsetMin = offsets.offsetFor(entry.type)

                        val enabled = when (entry.type) {
                            PrayerType.FAJR -> prefs.fajrEnabled
                            PrayerType.DHUHR -> prefs.dhuhrEnabled
                            PrayerType.ASR -> prefs.asrEnabled
                            PrayerType.MAGHRIB -> prefs.maghribEnabled
                            PrayerType.ISHA -> prefs.ishaEnabled
                            PrayerType.SUNRISE -> false
                        }
                        val assignedAdhanId = when (entry.type) {
                            PrayerType.FAJR -> prefs.fajrAdhanId
                            PrayerType.DHUHR -> prefs.dhuhrAdhanId
                            PrayerType.ASR -> prefs.asrAdhanId
                            PrayerType.MAGHRIB -> prefs.maghribAdhanId
                            PrayerType.ISHA -> prefs.ishaAdhanId
                            PrayerType.SUNRISE -> ""
                        }
                        val assignedTrack = if (assignedAdhanId.isNotBlank()) AdhanCatalog.findById(assignedAdhanId) else null

                        Card(
                            modifier = Modifier.testTag("prayer_card_${entry.type.key}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isNext -> MaterialTheme.colorScheme.primaryContainer
                                    isCurrent -> SuccessGreen.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                width = if (isNext || isCurrent) 1.5.dp else 0.8.dp,
                                color = when {
                                    isNext -> GoldRoyal
                                    isCurrent -> SuccessGreen
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isAr) entry.type.nameAr else entry.type.nameEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isCurrent) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = SuccessGreen.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = s.currentPrayerLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SuccessGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (isNext) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = GoldRoyal.copy(alpha = 0.22f)
                                            ) {
                                                Text(
                                                    text = s.nextPrayer,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EmeraldDeep,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (offsetMin != 0) {
                                            val sign = if (offsetMin > 0) "+" else ""
                                            Text(
                                                text = "($sign$offsetMin${s.ruqyahMinutesShort})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    if (assignedTrack != null) {
                                        Text(
                                            text = if (isAr) assignedTrack.titleAr else assignedTrack.titleEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = entry.formattedTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                if (entry.type != PrayerType.SUNRISE) {
                                    Switch(
                                        checked = enabled,
                                        onCheckedChange = { onTogglePrayerAlert(entry.type.key, it) },
                                        modifier = Modifier.testTag("prayer_switch_${entry.type.key}")
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Adhan Library Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(AdhanCatalog.tracks, key = { it.id }) { track ->
                        val isPlayingThis = audioState.isPlaying &&
                            (audioState.currentSource == track.audioUrl || audioState.currentSource.endsWith("${track.id}.mp3"))
                        val isDownloaded = downloadedKeys.contains(track.id)
                        val isDownloading = downloadingKeys.contains(track.id)

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAr) track.titleAr else track.titleEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${if (isAr) track.locationAr else track.locationEn} • ${if (isAr) track.styleAr else track.styleEn}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (isAr) track.attributionAr else track.attributionEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDownloadAdhan(track) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        when {
                                            isDownloading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                            isDownloaded -> Icon(Icons.Filled.CheckCircle, s.downloadedOffline, tint = GoldRoyal)
                                            else -> Icon(Icons.Filled.CloudDownload, s.downloadOffline)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (isPlayingThis) AudioPlaybackService.stop(context)
                                            else onPreviewAdhan(track)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (isPlayingThis) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            null,
                                            Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isPlayingThis) s.stopPreview else s.previewAdhan)
                                    }

                                    Button(
                                        onClick = { assignTargetTrack = track },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.NotificationsActive, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(s.assignToPrayer)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // GPS Privacy & Consent Rationale Dialog
    if (showGpsConsentDialog) {
        AlertDialog(
            onDismissRequest = { showGpsConsentDialog = false },
            modifier = Modifier.testTag("gps_consent_dialog"),
            title = { Text(s.gpsConsentTitle, fontWeight = FontWeight.Bold) },
            text = { Text(s.gpsConsentBody, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        showGpsConsentDialog = false
                        locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    },
                    modifier = Modifier.testTag("gps_consent_accept_button")
                ) {
                    Text(s.gpsConsentAccept)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGpsConsentDialog = false
                        showCityDialog = true
                    },
                    modifier = Modifier.testTag("gps_consent_manual_button")
                ) {
                    Text(s.gpsConsentDecline)
                }
            }
        )
    }

    // Manual Minute Offsets Dialog
    if (showOffsetsDialog) {
        val offsets = prefs.toPrayerMinuteOffsets()
        AlertDialog(
            onDismissRequest = { showOffsetsDialog = false },
            modifier = Modifier.testTag("prayer_offsets_dialog"),
            title = { Text(s.manualOffsetsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrayerType.entries.forEach { pType ->
                        val currentOffset = offsets.offsetFor(pType)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAr) pType.nameAr else pType.nameEn,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onSetPrayerOffset(pType.key, (currentOffset - 1).coerceAtLeast(-30)) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("offset_minus_${pType.key}")
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "-1")
                                }
                                val sign = if (currentOffset > 0) "+" else ""
                                Text(
                                    text = "$sign$currentOffset",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(36.dp)
                                )
                                IconButton(
                                    onClick = { onSetPrayerOffset(pType.key, (currentOffset + 1).coerceAtMost(30)) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("offset_plus_${pType.key}")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "+1")
                                }
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onResetOffsets,
                    modifier = Modifier.testTag("offset_reset_all")
                ) {
                    Text(s.resetOffsetsLabel)
                }
            },
            confirmButton = {
                Button(onClick = { showOffsetsDialog = false }) {
                    Text(s.backLabel)
                }
            }
        )
    }

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            modifier = Modifier.testTag("manual_city_dialog"),
            title = { Text(s.selectCityManually) },
            text = {
                LazyColumn(modifier = Modifier.height(340.dp)) {
                    items(CitiesCatalog.cities) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCity(city)
                                    showCityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isAr) city.nameAr else city.nameEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isAr) city.countryAr else city.countryEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (prefs.cityNameEn == city.nameEn) {
                                Icon(Icons.Filled.CheckCircle, null, tint = GoldRoyal)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) { Text(s.backLabel) }
            }
        )
    }

    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = { Text(s.calculationMethodLabel) },
            text = {
                Column {
                    CalculationMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectMethod(method.id)
                                    showMethodDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAr) method.nameAr else method.nameEn,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (prefs.calculationMethod == method.id) {
                                Icon(Icons.Filled.CheckCircle, null, tint = GoldRoyal)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) { Text(s.backLabel) }
            }
        )
    }

    assignTargetTrack?.let { track ->
        AlertDialog(
            onDismissRequest = { assignTargetTrack = null },
            title = { Text(if (isAr) track.titleAr else track.titleEn) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "ALL" to s.assignAllPrayers,
                        PrayerType.FAJR.key to s.fajr,
                        PrayerType.DHUHR.key to s.dhuhr,
                        PrayerType.ASR.key to s.asr,
                        PrayerType.MAGHRIB.key to s.maghrib,
                        PrayerType.ISHA.key to s.isha
                    ).forEach { (key, label) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAssignAdhan(key, track.id)
                                    assignTargetTrack = null
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { assignTargetTrack = null }) { Text(s.backLabel) }
            }
        )
    }
}
