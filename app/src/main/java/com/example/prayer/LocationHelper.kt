package com.example.prayer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class DetectedLocation(
    val cityAr: String,
    val cityEn: String,
    val latitude: Double,
    val longitude: Double
)

object LocationHelper {
    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): Result<DetectedLocation> = withContext(Dispatchers.IO) {
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val loc = suspendCancellableCoroutine { cont ->
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            cont.resume(location)
                        } else {
                            client.lastLocation
                                .addOnSuccessListener { last -> cont.resume(last) }
                                .addOnFailureListener { cont.resume(null) }
                        }
                    }
                    .addOnFailureListener { cont.resume(null) }
            }

            if (loc == null) {
                return@withContext Result.failure(IllegalStateException("Unable to read location"))
            }

            val cityAr = resolveCityName(context, loc.latitude, loc.longitude, Locale("ar")) ?: "الموقع الحالي"
            val cityEn = resolveCityName(context, loc.latitude, loc.longitude, Locale.ENGLISH) ?: "Current Location"

            Result.success(
                DetectedLocation(
                    cityAr = cityAr,
                    cityEn = cityEn,
                    latitude = loc.latitude,
                    longitude = loc.longitude
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveCityName(
        context: Context,
        lat: Double,
        lon: Double,
        locale: Locale
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        val addr = addresses.firstOrNull()
                        cont.resume(addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea)
                    }
                }
            } else {
                val list = geocoder.getFromLocation(lat, lon, 1)
                val addr = list?.firstOrNull()
                addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
            }
        } catch (_: Exception) {
            null
        }
    }
}
