package com.example.prayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.seed.CitiesCatalog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs

data class DetectedLocation(
    val cityAr: String,
    val cityEn: String,
    val regionAr: String = "",
    val regionEn: String = "",
    val countryAr: String = "",
    val countryEn: String = "",
    val countryCode: String = "",
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String = "Africa/Casablanca",
    val suggestedMethodId: String = "MWL",
    val usedOfflineFallback: Boolean = false,
    val accuracyMeters: Float? = null
)

sealed interface GpsLocationResult {
    data class Success(val location: DetectedLocation) : GpsLocationResult
    data object PermissionMissing : GpsLocationResult
    data object WeakSignal : GpsLocationResult
    data class Error(val reason: String) : GpsLocationResult
}

private data class GeocodedDetails(
    val city: String,
    val region: String,
    val country: String,
    val countryCode: String
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        if (latitude.isNaN() || longitude.isNaN() || latitude.isInfinite() || longitude.isInfinite()) {
            return false
        }
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return false
        if (abs(latitude) < 0.0001 && abs(longitude) < 0.0001) return false
        return true
    }

    fun formatCoordinates(latitude: Double, longitude: Double): String {
        val latDir = if (latitude >= 0.0) "N" else "S"
        val lonDir = if (longitude >= 0.0) "E" else "W"
        return String.format(Locale.US, "%.3f° %s, %.3f° %s", abs(latitude), latDir, abs(longitude), lonDir)
    }

    fun hasMovedSignificantly(
        prevLat: Double,
        prevLon: Double,
        prevCityEn: String,
        next: DetectedLocation,
        thresholdKm: Double = 10.0
    ): Boolean {
        val movedKm = CitiesCatalog.distanceKm(prevLat, prevLon, next.latitude, next.longitude)
        val cityChanged = next.cityEn.isNotBlank() &&
            !next.usedOfflineFallback &&
            !next.cityEn.equals(prevCityEn, ignoreCase = true) &&
            movedKm > 3.0
        return movedKm >= thresholdKm || cityChanged
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocationWithStatus(context: Context): GpsLocationResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext GpsLocationResult.PermissionMissing
        }

        val cts = CancellationTokenSource()
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val loc = withTimeoutOrNull(12_000L) {
                suspendCancellableCoroutine { cont ->
                    cont.invokeOnCancellation { cts.cancel() }
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                        .addOnSuccessListener { current ->
                            if (current != null && isValidCoordinate(current.latitude, current.longitude)) {
                                if (cont.isActive) cont.resume(current)
                            } else {
                                client.lastLocation
                                    .addOnSuccessListener { last ->
                                        if (cont.isActive) cont.resume(last)
                                    }
                                    .addOnFailureListener {
                                        if (cont.isActive) cont.resume(null)
                                    }
                            }
                        }
                        .addOnFailureListener {
                            client.lastLocation
                                .addOnSuccessListener { last ->
                                    if (cont.isActive) cont.resume(last)
                                }
                                .addOnFailureListener {
                                    if (cont.isActive) cont.resume(null)
                                }
                        }
                }
            }

            if (loc == null || !isValidCoordinate(loc.latitude, loc.longitude)) {
                return@withContext GpsLocationResult.WeakSignal
            }

            val arDetails = withTimeoutOrNull(4_000L) {
                resolveAddressDetails(context, loc.latitude, loc.longitude, Locale.forLanguageTag("ar"))
            }
            val enDetails = withTimeoutOrNull(4_000L) {
                resolveAddressDetails(context, loc.latitude, loc.longitude, Locale.ENGLISH)
            }

            val nearestCatalog = CitiesCatalog.findNearestCity(loc.latitude, loc.longitude, maxDistanceKm = 75.0)
            val countryCode = (enDetails?.countryCode?.takeIf { it.isNotBlank() }
                ?: arDetails?.countryCode?.takeIf { it.isNotBlank() }
                ?: nearestCatalog?.countryCode.orEmpty()).uppercase(Locale.US)

            val usedOfflineFallback = enDetails == null && arDetails == null
            val coordsLabel = formatCoordinates(loc.latitude, loc.longitude)

            val cityAr = arDetails?.city?.takeIf { it.isNotBlank() }
                ?: nearestCatalog?.nameAr
                ?: enDetails?.city?.takeIf { it.isNotBlank() }
                ?: coordsLabel
            val cityEn = enDetails?.city?.takeIf { it.isNotBlank() }
                ?: nearestCatalog?.nameEn
                ?: arDetails?.city?.takeIf { it.isNotBlank() }
                ?: coordsLabel

            val regionAr = arDetails?.region?.takeIf { it.isNotBlank() && !it.equals(cityAr, ignoreCase = true) }.orEmpty()
            val regionEn = enDetails?.region?.takeIf { it.isNotBlank() && !it.equals(cityEn, ignoreCase = true) }.orEmpty()

            val countryAr = arDetails?.country?.takeIf { it.isNotBlank() }
                ?: nearestCatalog?.countryAr
                ?: enDetails?.country.orEmpty()
            val countryEn = enDetails?.country?.takeIf { it.isNotBlank() }
                ?: nearestCatalog?.countryEn
                ?: arDetails?.country.orEmpty()

            val tzId = CitiesCatalog.resolveTimeZoneId(
                cityNameEn = cityEn,
                countryCode = countryCode,
                latitude = loc.latitude,
                longitude = loc.longitude
            )
            val methodId = CitiesCatalog.suggestedMethodForCountry(countryCode, loc.latitude, loc.longitude)

            GpsLocationResult.Success(
                DetectedLocation(
                    cityAr = cityAr,
                    cityEn = cityEn,
                    regionAr = regionAr,
                    regionEn = regionEn,
                    countryAr = countryAr,
                    countryEn = countryEn,
                    countryCode = countryCode,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    timeZoneId = tzId,
                    suggestedMethodId = methodId,
                    usedOfflineFallback = usedOfflineFallback,
                    accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null
                )
            )
        } catch (e: SecurityException) {
            GpsLocationResult.PermissionMissing
        } catch (e: Exception) {
            GpsLocationResult.Error(e.localizedMessage ?: "Location error")
        } finally {
            cts.cancel()
        }
    }

    suspend fun detectCurrentLocation(context: Context): Result<DetectedLocation> {
        return when (val status = detectCurrentLocationWithStatus(context)) {
            is GpsLocationResult.Success -> Result.success(status.location)
            GpsLocationResult.PermissionMissing -> Result.failure(SecurityException("Location permission denied"))
            GpsLocationResult.WeakSignal -> Result.failure(IllegalStateException("Weak or unavailable GPS signal"))
            is GpsLocationResult.Error -> Result.failure(IllegalStateException(status.reason))
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveAddressDetails(
        context: Context,
        lat: Double,
        lon: Double,
        locale: Locale
    ): GeocodedDetails? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, locale)
            val addr: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        if (cont.isActive) cont.resume(addresses.firstOrNull())
                    }
                }
            } else {
                geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
            }
            if (addr == null) return@withContext null
            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
            val region = addr.adminArea ?: addr.subAdminArea ?: ""
            val country = addr.countryName ?: ""
            val code = addr.countryCode ?: ""
            if (city.isBlank() && country.isBlank()) null
            else GeocodedDetails(city = city, region = region, country = country, countryCode = code)
        } catch (_: Exception) {
            null
        }
    }
}
