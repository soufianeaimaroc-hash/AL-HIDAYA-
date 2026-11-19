package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.audio.AudioPlaybackService
import com.example.audio.AudioPlaybackState
import com.example.core.LocalAppStrings
import com.example.data.local.UserPrefsState
import com.example.data.seed.AdhanCatalog
import com.example.data.seed.AdhanTrack
import com.example.data.seed.CitiesCatalog
import com.example.data.seed.CityOption
import com.example.prayer.CalculationMethod
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.PrayerType
import com.example.ui.theme.GoldRoyal

@Composable
fun PrayerAndAdhanScreen(
    initialTab: Int = 0,
    prefs: UserPrefsState,
    schedule: DailyPrayerSchedule,
    audioState: AudioPlaybackState,
    downloadedKeys: Set<String>,
    downloadingKeys: Set<String>,
    onDetectGps: () -> Unit,
    onSelectCity: (CityOption) -> Unit,
    onSelectMethod: (String) -> Unit,
    onTogglePrayerAlert: (String, Boolean) -> Unit,
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
    var assignTargetTrack by remember { mutableStateOf<AdhanTrack?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onDetectGps() else showCityDialog = true
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    // Location & Calculation Method Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.LocationOn, null, tint = GoldRoyal)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(s.cityLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = if (isAr) prefs.cityName else prefs.cityNameEn,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val granted = ContextCompat.checkSelfPermission(
                                                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                                                ) == PackageManager.PERMISSION_GRANTED
                                                if (granted) onDetectGps()
                                                else locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                            },
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
                                        .padding(vertical = 4.dp),
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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(s.prePrayerReminder, style = MaterialTheme.typography.bodyMedium)
                                    Switch(
                                        checked = prefs.prePrayerReminderEnabled,
                                        onCheckedChange = { onTogglePrayerAlert("REMINDER", it) },
                                        modifier = Modifier.testTag("pre_prayer_switch")
                                    )
                                }
                            }
                        }
                    }

                    // Daily Prayer Times with independent toggles & assigned Adhan
                    items(schedule.prayers, key = { it.type.key }) { entry ->
                        val isNext = entry.type == schedule.nextPrayer.type
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
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (isNext) 1.5.dp else 0.8.dp,
                                color = if (isNext) GoldRoyal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
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
                                    Text(
                                        text = if (isAr) entry.type.nameAr else entry.type.nameEn,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
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

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
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
