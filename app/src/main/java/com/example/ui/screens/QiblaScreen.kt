package com.example.ui.screens

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppStrings
import com.example.data.local.UserPrefsState
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun QiblaScreen(
    prefs: UserPrefsState,
    qiblaBearing: Double
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val isAr = prefs.language == "ar"

    var azimuthDeg by remember { mutableFloatStateOf(0f) }
    var magneticFieldMicroTesla by remember { mutableFloatStateOf(45f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }

    val declination = remember(prefs.latitude, prefs.longitude) {
        try {
            GeomagneticField(
                prefs.latitude.toFloat(),
                prefs.longitude.toFloat(),
                0f,
                System.currentTimeMillis()
            ).declination
        } catch (_: Exception) {
            0f
        }
    }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rotMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        SensorManager.getOrientation(rotMatrix, orientation)
                        val rawDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuthDeg = (rawDeg + declination + 360f) % 360f
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        if (rotationSensor == null && SensorManager.getRotationMatrix(rotMatrix, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val rawDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            azimuthDeg = (rawDeg + declination + 360f) % 360f
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        val bx = event.values[0]
                        val by = event.values[1]
                        val bz = event.values[2]
                        magneticFieldMicroTesla = sqrt(bx * bx + by * by + bz * bz)
                        if (rotationSensor == null && SensorManager.getRotationMatrix(rotMatrix, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val rawDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            azimuthDeg = (rawDeg + declination + 360f) % 360f
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    sensorAccuracy = accuracy
                }
            }
        }

        if (rotationSensor != null) {
            sm.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelSensor != null) {
            sm.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if (magSensor != null) {
            sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sm.unregisterListener(listener)
        }
    }

    val deltaToQibla = remember(azimuthDeg, qiblaBearing) {
        var diff = (qiblaBearing.toFloat() - azimuthDeg) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        diff
    }

    val isAligned = abs(deltaToQibla) <= 5f
    val hasMagneticInterference = magneticFieldMicroTesla < 22f || magneticFieldMicroTesla > 68f
    val needsCalibration = sensorAccuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW || hasMagneticInterference

    val animatedDialRotation by animateFloatAsState(
        targetValue = -azimuthDeg,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "compass_dial"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // City & Qibla Degree Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldRoyal.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAr) prefs.cityName else prefs.cityNameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${s.qiblaAngleLabel}: ${String.format("%.1f", qiblaBearing)}°",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${s.magneticFieldLabel}: ${String.format("%.0f", magneticFieldMicroTesla)} µT",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (needsCalibration) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qibla_calibration_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.16f)),
                    border = BorderStroke(1.dp, WarningAmber)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = WarningAmber)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = s.compassCalibrationWarning,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = s.compassCalibrationTip,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Luxury Emerald & Gold Compass Dial
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .aspectRatio(1f)
                    .testTag("qibla_compass_dial"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Outer dark emerald circle with gold rim
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(EmeraldPrimary, EmeraldDeep),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = if (isAligned) SuccessGreen else GoldRoyal,
                        radius = radius - 6f,
                        center = center,
                        style = Stroke(width = 8f)
                    )

                    // Rotate dial by -azimuthDeg so North points to magnetic/true North
                    rotate(degrees = animatedDialRotation, pivot = center) {
                        for (deg in 0 until 360 step 10) {
                            val rad = Math.toRadians(deg.toDouble() - 90.0)
                            val inner = if (deg % 30 == 0) radius * 0.78f else radius * 0.86f
                            val outer = radius * 0.92f
                            drawLine(
                                color = if (deg == 0) GoldChampagne else Color.White.copy(alpha = 0.55f),
                                start = Offset(
                                    center.x + inner * cos(rad).toFloat(),
                                    center.y + inner * sin(rad).toFloat()
                                ),
                                end = Offset(
                                    center.x + outer * cos(rad).toFloat(),
                                    center.y + outer * sin(rad).toFloat()
                                ),
                                strokeWidth = if (deg % 30 == 0) 4f else 2f
                            )
                        }

                        // Kaaba Gold Marker & Qibla Arrow at qiblaBearing
                        rotate(degrees = qiblaBearing.toFloat(), pivot = center) {
                            val needlePath = Path().apply {
                                moveTo(center.x, center.y - radius * 0.72f)
                                lineTo(center.x - 18f, center.y)
                                lineTo(center.x, center.y + radius * 0.25f)
                                lineTo(center.x + 18f, center.y)
                                close()
                            }
                            drawPath(
                                path = needlePath,
                                color = if (isAligned) SuccessGreen else GoldChampagne
                            )
                            // Kaaba icon cube at tip
                            val kaabaTop = center.y - radius * 0.85f
                            drawRect(
                                color = Color(0xFF121212),
                                topLeft = Offset(center.x - 16f, kaabaTop),
                                size = androidx.compose.ui.geometry.Size(32f, 32f)
                            )
                            drawRect(
                                color = GoldRoyal,
                                topLeft = Offset(center.x - 16f, kaabaTop + 6f),
                                size = androidx.compose.ui.geometry.Size(32f, 6f)
                            )
                        }
                    }

                    // Center hub
                    drawCircle(color = GoldRoyal, radius = 14f, center = center)
                    drawCircle(color = EmeraldDeep, radius = 7f, center = center)
                }
            }

            // Alignment Status Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isAligned) SuccessGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, if (isAligned) SuccessGreen else GoldRoyal.copy(alpha = 0.5f))
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isAligned) Icons.Filled.CheckCircle else Icons.Filled.Explore,
                        contentDescription = null,
                        tint = if (isAligned) SuccessGreen else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when {
                            isAligned -> s.qiblaAligned
                            deltaToQibla > 0 -> s.qiblaTurnRight
                            else -> s.qiblaTurnLeft
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
