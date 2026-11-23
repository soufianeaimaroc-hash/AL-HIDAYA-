package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.LocalAppStrings
import com.example.data.seed.CitiesCatalog
import com.example.prayer.LocationHelper
import com.example.prayer.PrayerTimeCalculator
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private data class GeoPoly(val points: List<Pair<Double, Double>>)

// Stylized continental polygons (lat, lon) for rich offline/online vector geo context
private val WORLD_CONTINENTS = listOf(
    // Africa
    GeoPoly(
        listOf(
            35.8 to -5.5, 37.3 to 9.8, 31.2 to 32.3, 22.0 to 36.8, 11.8 to 51.2,
            -1.0 to 41.5, -26.0 to 32.9, -34.8 to 20.0, -22.5 to 14.4, 4.5 to 9.6,
            6.3 to 3.4, 5.0 to -7.5, 14.7 to -17.5, 21.0 to -17.0, 27.6 to -13.2, 33.6 to -7.6
        )
    ),
    // Europe
    GeoPoly(
        listOf(
            36.0 to -5.6, 43.3 to -8.4, 48.4 to -4.5, 51.0 to 2.5, 55.0 to 8.5,
            60.0 to 5.0, 70.0 to 25.0, 68.0 to 44.0, 55.0 to 55.0, 45.0 to 40.0,
            41.0 to 29.0, 38.0 to 23.7, 40.5 to 18.0, 44.0 to 12.5, 38.0 to 13.0, 41.4 to 2.1, 36.7 to -4.4
        )
    ),
    // Arabian Peninsula & Asia
    GeoPoly(
        listOf(
            36.0 to 36.0, 41.0 to 29.0, 42.0 to 41.5, 45.0 to 55.0, 65.0 to 70.0,
            72.0 to 130.0, 62.0 to 170.0, 40.0 to 140.0, 22.0 to 114.0, 1.3 to 103.8,
            8.0 to 98.0, 22.0 to 90.0, 8.0 to 77.5, 23.0 to 68.5, 25.2 to 55.3,
            24.5 to 54.4, 12.8 to 45.0, 21.5 to 39.2, 29.5 to 34.9
        )
    ),
    // Indonesia / SE Islands
    GeoPoly(
        listOf(-6.2 to 105.8, -8.5 to 115.5, -7.2 to 112.7, -1.0 to 104.0)
    ),
    // North America
    GeoPoly(
        listOf(
            68.0 to -165.0, 70.0 to -100.0, 62.0 to -65.0, 45.0 to -60.0, 25.0 to -80.0,
            20.0 to -90.0, 15.0 to -92.0, 32.5 to -117.0, 49.0 to -125.0, 60.0 to -145.0
        )
    ),
    // South America
    GeoPoly(
        listOf(
            11.0 to -75.0, 6.0 to -55.0, -5.0 to -35.0, -23.0 to -43.0, -54.0 to -68.0,
            -40.0 to -73.5, -18.0 to -70.5, 0.0 to -80.0, 9.0 to -79.5
        )
    ),
    // Australia
    GeoPoly(
        listOf(-12.0 to 130.0, -18.0 to 146.0, -37.0 to 150.0, -35.0 to 115.0, -22.0 to 114.0)
    )
)

@Composable
fun LocationQiblaMapCard(
    cityLabel: String,
    countryRegionLabel: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    isDstActive: Boolean,
    qiblaBearing: Double,
    isRefreshingGps: Boolean,
    onRefreshGps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isAr = s.langCode == "ar"
    var worldZoom by remember { mutableStateOf(false) }

    val distanceKm = remember(latitude, longitude) {
        PrayerTimeCalculator.calculateDistanceToKaabaKm(latitude, longitude)
    }
    val coordsText = remember(latitude, longitude) {
        LocationHelper.formatCoordinates(latitude, longitude)
    }

    val pulseTransition = rememberInfiniteTransition(label = "gps_pin_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Precompute spherical great-circle intermediate points between User and Kaaba
    val greatCircleArc = remember(latitude, longitude) {
        computeGreatCirclePoints(
            lat1Deg = latitude,
            lon1Deg = longitude,
            lat2Deg = CitiesCatalog.KAABA_LAT,
            lon2Deg = CitiesCatalog.KAABA_LON,
            segments = 28
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_qibla_map_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Interactive Vector Geo-Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(EmeraldDeep)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF072D21), Color(0xFF0B4230), Color(0xFF06251B))
                        )
                    )

                    val (minLon, maxLon, minLat, maxLat) = if (worldZoom) {
                        listOf(-180.0, 180.0, -65.0, 78.0)
                    } else {
                        val rawMinLon = min(longitude, CitiesCatalog.KAABA_LON)
                        val rawMaxLon = max(longitude, CitiesCatalog.KAABA_LON)
                        val rawMinLat = min(latitude, CitiesCatalog.KAABA_LAT)
                        val rawMaxLat = max(latitude, CitiesCatalog.KAABA_LAT)
                        val lonSpan = max(24.0, (rawMaxLon - rawMinLon) * 1.45)
                        val latSpan = max(16.0, (rawMaxLat - rawMinLat) * 1.65)
                        val cLon = (rawMinLon + rawMaxLon) / 2.0
                        val cLat = (rawMinLat + rawMaxLat) / 2.0
                        listOf(
                            (cLon - lonSpan / 2.0).coerceAtLeast(-180.0),
                            (cLon + lonSpan / 2.0).coerceAtMost(180.0),
                            (cLat - latSpan / 2.0).coerceAtLeast(-70.0),
                            (cLat + latSpan / 2.0).coerceAtMost(80.0)
                        )
                    }

                    fun project(lat: Double, lon: Double): Offset {
                        val x = ((lon - minLon) / (maxLon - minLon).coerceAtLeast(1.0)).toFloat() * size.width
                        val y = (1.0 - (lat - minLat) / (maxLat - minLat).coerceAtLeast(1.0)).toFloat() * size.height
                        return Offset(x, y)
                    }

                    // Graticule grid lines
                    val gridStepLon = if (worldZoom) 30 else 10
                    val gridStepLat = if (worldZoom) 30 else 10
                    for (lon in -180..180 step gridStepLon) {
                        if (lon.toDouble() in minLon..maxLon) {
                            val p1 = project(minLat, lon.toDouble())
                            val p2 = project(maxLat, lon.toDouble())
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = p1,
                                end = p2,
                                strokeWidth = 1f
                            )
                        }
                    }
                    for (lat in -60..75 step gridStepLat) {
                        if (lat.toDouble() in minLat..maxLat) {
                            val p1 = project(lat.toDouble(), minLon)
                            val p2 = project(lat.toDouble(), maxLon)
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = p1,
                                end = p2,
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Stylized landmasses
                    WORLD_CONTINENTS.forEach { poly ->
                        val path = Path()
                        poly.points.forEachIndexed { index, (lat, lon) ->
                            val pt = project(lat, lon)
                            if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = EmeraldVibrant.copy(alpha = 0.34f)
                        )
                        drawPath(
                            path = path,
                            color = GoldChampagne.copy(alpha = 0.22f),
                            style = Stroke(width = 1.4f)
                        )
                    }

                    // Great-circle geodesic path from User to Makkah (avoid wrap artifacts)
                    val pathArc = Path()
                    var prevOffset: Offset? = null
                    greatCircleArc.forEachIndexed { idx, (lat, lon) ->
                        val pt = project(lat, lon)
                        val prev = prevOffset
                        if (idx == 0 || prev == null || abs(pt.x - prev.x) > size.width * 0.55f) {
                            pathArc.moveTo(pt.x, pt.y)
                        } else {
                            pathArc.lineTo(pt.x, pt.y)
                        }
                        prevOffset = pt
                    }

                    drawPath(
                        path = pathArc,
                        color = GoldChampagne.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 4f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )

                    // Kaaba Pin (21.4225, 39.8262)
                    val kaabaPt = project(CitiesCatalog.KAABA_LAT, CitiesCatalog.KAABA_LON)
                    drawCircle(
                        color = GoldRoyal.copy(alpha = 0.28f),
                        radius = 22f,
                        center = kaabaPt
                    )
                    drawRect(
                        color = Color(0xFF111111),
                        topLeft = Offset(kaabaPt.x - 10f, kaabaPt.y - 10f),
                        size = Size(20f, 20f)
                    )
                    drawRect(
                        color = GoldRoyal,
                        topLeft = Offset(kaabaPt.x - 10f, kaabaPt.y - 5f),
                        size = Size(20f, 4f)
                    )

                    // User GPS Pin with Animated Pulse
                    val userPt = project(latitude, longitude)
                    drawCircle(
                        color = GoldChampagne.copy(alpha = pulseAlpha),
                        radius = 12f * pulseScale,
                        center = userPt
                    )
                    drawCircle(
                        color = EmeraldPrimary,
                        radius = 11f,
                        center = userPt
                    )
                    drawCircle(
                        color = GoldChampagne,
                        radius = 6f,
                        center = userPt
                    )
                }

                // Top overlay badges (Title + Zoom toggle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(0.8.dp, GoldChampagne.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Explore, null, tint = GoldChampagne, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "${s.mapCardTitle} • ${String.format(Locale.US, "%.1f", qiblaBearing)}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        onClick = { worldZoom = !worldZoom },
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.58f),
                        border = BorderStroke(0.8.dp, GoldChampagne.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("map_zoom_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (worldZoom) Icons.Filled.ZoomIn else Icons.Filled.Public,
                                contentDescription = null,
                                tint = GoldChampagne,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (worldZoom) s.mapZoomRoute else s.mapZoomWorld,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                // Bottom-left coordinate overlay
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.58f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "$coordsText • ${distanceKm.toInt()} km ${if (isAr) "للكعبة" else "to Kaaba"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldChampagne,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // Location summary & Refresh CTA footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.LocationOn, null, tint = GoldRoyal, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = cityLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (countryRegionLabel.isNotBlank()) {
                            Text(
                                text = countryRegionLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = if (isDstActive) "$timeZoneId • ${s.dstActiveLabel}" else timeZoneId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onRefreshGps,
                    enabled = !isRefreshingGps,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.65f)),
                    modifier = Modifier.testTag("map_refresh_location_button")
                ) {
                    if (isRefreshingGps) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.MyLocation, null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(s.refreshLocationLabel)
                }
            }
        }
    }
}

private fun computeGreatCirclePoints(
    lat1Deg: Double,
    lon1Deg: Double,
    lat2Deg: Double,
    lon2Deg: Double,
    segments: Int
): List<Pair<Double, Double>> {
    val lat1 = Math.toRadians(lat1Deg)
    val lon1 = Math.toRadians(lon1Deg)
    val lat2 = Math.toRadians(lat2Deg)
    val lon2 = Math.toRadians(lon2Deg)

    val x1 = cos(lat1) * cos(lon1)
    val y1 = cos(lat1) * sin(lon1)
    val z1 = sin(lat1)

    val x2 = cos(lat2) * cos(lon2)
    val y2 = cos(lat2) * sin(lon2)
    val z2 = sin(lat2)

    val dot = (x1 * x2 + y1 * y2 + z1 * z2).coerceIn(-1.0, 1.0)
    val omega = acos(dot)
    if (omega < 1e-4) return listOf(lat1Deg to lon1Deg, lat2Deg to lon2Deg)
    val sinOmega = sin(omega)

    return (0..segments).map { i ->
        val t = i.toDouble() / segments.toDouble()
        val a = sin((1.0 - t) * omega) / sinOmega
        val b = sin(t * omega) / sinOmega
        val x = a * x1 + b * x2
        val y = a * y1 + b * y2
        val z = a * z1 + b * z2
        val lat = Math.toDegrees(atan2(z, sqrt(x * x + y * y)))
        val lon = Math.toDegrees(atan2(y, x))
        val normLon = ((lon + 540.0) % 360.0) - 180.0
        lat to normLon
    }
}
