package com.example.prayer

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.AudioPlaybackService
import com.example.data.local.UserPrefsState
import com.example.data.local.UserPreferencesDataStore
import com.example.data.seed.AdhanCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

object PrayerNotificationScheduler {
    const val CHANNEL_ADHAN = "soufian_adhan_channel"
    const val CHANNEL_REMINDER = "soufian_reminder_channel"
    const val EXTRA_PRAYER_KEY = "extra_prayer_key"
    const val EXTRA_IS_REMINDER = "extra_is_reminder"
    const val EXTRA_ADHAN_ID = "extra_adhan_id"
    const val EXTRA_LANGUAGE = "extra_language"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val adhanChannel = NotificationChannel(
                CHANNEL_ADHAN,
                "Adhan Alerts • تنبيهات الأذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled prayer time Adhan notifications"
                enableVibration(true)
            }
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "Pre-Prayer Reminders • تذكير قبل الصلاة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "10-minute pre-prayer preparation reminders"
            }
            nm.createNotificationChannel(adhanChannel)
            nm.createNotificationChannel(reminderChannel)
        }
    }

    fun scheduleDailyPrayers(context: Context, prefs: UserPrefsState) {
        ensureChannels(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val method = CalculationMethod.fromId(prefs.calculationMethod)
        val now = System.currentTimeMillis()
        val schedule = PrayerTimeCalculator.calculateDailySchedule(
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            method = method,
            nowMillis = now,
            timeZone = com.example.data.seed.CitiesCatalog.findTimeZone(prefs.cityNameEn, prefs.latitude, prefs.longitude)
        )

        val prayerConfigs = listOf(
            Triple(PrayerType.FAJR, prefs.fajrEnabled, prefs.fajrAdhanId),
            Triple(PrayerType.DHUHR, prefs.dhuhrEnabled, prefs.dhuhrAdhanId),
            Triple(PrayerType.ASR, prefs.asrEnabled, prefs.asrAdhanId),
            Triple(PrayerType.MAGHRIB, prefs.maghribEnabled, prefs.maghribAdhanId),
            Triple(PrayerType.ISHA, prefs.ishaEnabled, prefs.ishaAdhanId)
        )

        prayerConfigs.forEachIndexed { idx, (type, enabled, adhanId) ->
            val entry = schedule.prayers.firstOrNull { it.type == type } ?: return@forEachIndexed
            var targetMillis = entry.timeMillis
            if (targetMillis <= now) {
                targetMillis += 86_400_000L // schedule for tomorrow
            }

            val adhanCode = 100 + idx
            val reminderCode = 200 + idx

            if (enabled) {
                scheduleAlarm(
                    context, alarmManager, adhanCode, targetMillis,
                    type.key, isReminder = false, adhanId = adhanId, language = prefs.language
                )
                if (prefs.prePrayerReminderEnabled) {
                    val reminderMillis = targetMillis - 10 * 60_000L
                    if (reminderMillis > now) {
                        scheduleAlarm(
                            context, alarmManager, reminderCode, reminderMillis,
                            type.key, isReminder = true, adhanId = adhanId, language = prefs.language
                        )
                    }
                } else {
                    cancelAlarm(context, alarmManager, reminderCode)
                }
            } else {
                cancelAlarm(context, alarmManager, adhanCode)
                cancelAlarm(context, alarmManager, reminderCode)
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerAtMillis: Long,
        prayerKey: String,
        isReminder: Boolean,
        adhanId: String,
        language: String
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_KEY, prayerKey)
            putExtra(EXTRA_IS_REMINDER, isReminder)
            putExtra(EXTRA_ADHAN_ID, adhanId)
            putExtra(EXTRA_LANGUAGE, language)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
        }
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PrayerNotificationScheduler.ensureChannels(context)
        val prayerKey = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_KEY) ?: "FAJR"
        val isReminder = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_IS_REMINDER, false)
        val adhanId = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_ADHAN_ID) ?: "adhan_makkah"
        val language = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_LANGUAGE) ?: "ar"

        val prayerType = PrayerType.entries.firstOrNull { it.key == prayerKey } ?: PrayerType.FAJR
        val prayerName = if (language == "ar") prayerType.nameAr else prayerType.nameEn

        val title = if (isReminder) {
            if (language == "ar") "اقتربت صلاة $prayerName" else "$prayerName Prayer in 10 Minutes"
        } else {
            if (language == "ar") "حان الآن موعد أذان $prayerName" else "Time for $prayerName Prayer"
        }
        val body = if (isReminder) {
            if (language == "ar") "توضأ واستعد للصلاة بارك الله فيك" else "Prepare for prayer with serenity."
        } else {
            val track = AdhanCatalog.findById(adhanId)
            if (language == "ar") "أذان: ${track.titleAr}" else "Adhan: ${track.titleEn}"
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context,
            prayerKey.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = if (isReminder) PrayerNotificationScheduler.CHANNEL_REMINDER else PrayerNotificationScheduler.CHANNEL_ADHAN
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_mosque_notif)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .build()
        nm.notify(prayerKey.hashCode() + if (isReminder) 1 else 0, notification)

        if (!isReminder) {
            val track = AdhanCatalog.findById(adhanId)
            val localFile = File(File(context.filesDir, "adhan_audio"), "${track.id}.mp3")
            val source = if (localFile.exists()) localFile.absolutePath else track.audioUrl
            AudioPlaybackService.startPlay(
                context = context,
                urlOrPath = source,
                title = title,
                subtitle = body
            )
            // Reschedule next day's alarms
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = UserPreferencesDataStore(context).prefsFlow.first()
                    PrayerNotificationScheduler.scheduleDailyPrayers(context, prefs)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = UserPreferencesDataStore(context).prefsFlow.first()
                    PrayerNotificationScheduler.scheduleDailyPrayers(context, prefs)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
