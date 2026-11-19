package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeveloperBadgeBlue
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldRoyal
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicHeroCanvasBackground(
    modifier: Modifier = Modifier,
    goldTint: Color = GoldChampagne,
    deepColor: Color = EmeraldDeep,
    midColor: Color = EmeraldPrimary
) {
    Canvas(
        modifier = modifier.background(
            Brush.linearGradient(listOf(deepColor, midColor, deepColor))
        )
    ) {
        val w = size.width
        val h = size.height

        // Subtle geometric star lattice across upper region
        val step = 96f
        var x = 0f
        while (x < w + step) {
            var y = 0f
            while (y < h * 0.75f) {
                drawEightPointStar(
                    center = Offset(x, y),
                    radius = 24f,
                    color = goldTint.copy(alpha = 0.08f)
                )
                y += step
            }
            x += step
        }

        // Glowing crescent moon in top corner
        val moonCenter = Offset(w * 0.84f, h * 0.25f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(goldTint.copy(alpha = 0.25f), Color.Transparent),
                center = moonCenter,
                radius = 90f
            ),
            radius = 90f,
            center = moonCenter
        )
        drawCircle(color = goldTint.copy(alpha = 0.90f), radius = 26f, center = moonCenter)
        drawCircle(
            color = midColor,
            radius = 22f,
            center = Offset(moonCenter.x - 10f, moonCenter.y - 6f)
        )

        // Small stars
        listOf(
            Offset(w * 0.72f, h * 0.16f),
            Offset(w * 0.65f, h * 0.30f),
            Offset(w * 0.91f, h * 0.40f),
            Offset(w * 0.15f, h * 0.20f)
        ).forEach { pt ->
            drawCircle(color = goldTint.copy(alpha = 0.55f), radius = 3.2f, center = pt)
        }

        // Mosque dome & minarets silhouette along bottom edge
        drawMosqueSilhouette(w, h, goldTint.copy(alpha = 0.16f))
    }
}

private fun DrawScope.drawEightPointStar(center: Offset, radius: Float, color: Color) {
    val half = radius * 0.72f
    drawRect(
        color = color,
        topLeft = Offset(center.x - half, center.y - half),
        size = Size(half * 2f, half * 2f),
        style = Stroke(width = 1.4f)
    )
    rotate(degrees = 45f, pivot = center) {
        drawRect(
            color = color,
            topLeft = Offset(center.x - half, center.y - half),
            size = Size(half * 2f, half * 2f),
            style = Stroke(width = 1.4f)
        )
    }
}

private fun DrawScope.drawMosqueSilhouette(w: Float, h: Float, color: Color) {
    val base = h
    val path = Path().apply {
        moveTo(0f, base)
        lineTo(0f, base - h * 0.16f)
        // Left minaret
        lineTo(w * 0.10f, base - h * 0.16f)
        lineTo(w * 0.10f, base - h * 0.40f)
        lineTo(w * 0.12f, base - h * 0.48f)
        lineTo(w * 0.14f, base - h * 0.40f)
        lineTo(w * 0.14f, base - h * 0.18f)
        // Left small dome
        quadraticTo(w * 0.24f, base - h * 0.34f, w * 0.34f, base - h * 0.18f)
        // Main central grand dome
        quadraticTo(w * 0.50f, base - h * 0.52f, w * 0.66f, base - h * 0.18f)
        // Right small dome
        quadraticTo(w * 0.76f, base - h * 0.34f, w * 0.86f, base - h * 0.18f)
        // Right minaret
        lineTo(w * 0.86f, base - h * 0.40f)
        lineTo(w * 0.88f, base - h * 0.48f)
        lineTo(w * 0.90f, base - h * 0.40f)
        lineTo(w * 0.90f, base - h * 0.16f)
        lineTo(w, base - h * 0.16f)
        lineTo(w, base)
        close()
    }
    drawPath(path, color = color)
}

@Composable
fun RubElHizbNumberBadge(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val primary = MaterialTheme.colorScheme.primary
    val gold = GoldRoyal
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension * 0.36f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            drawRect(
                color = gold,
                topLeft = Offset(center.x - r, center.y - r),
                size = Size(r * 2f, r * 2f),
                style = Stroke(width = 2.4f)
            )
            rotate(45f, center) {
                drawRect(
                    color = primary,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(r * 2f, r * 2f),
                    style = Stroke(width = 2.2f)
                )
            }
        }
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GoldOrnamentalDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldRoyal.copy(alpha = 0.6f))
                    )
                )
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(GoldRoyal)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldRoyal.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun DeveloperIdentityBadgeBlock(
    name: String,
    professionalName: String,
    role: String,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Verified Developer Badge",
                tint = DeveloperBadgeBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = DeveloperBadgeBlue.copy(alpha = 0.14f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, DeveloperBadgeBlue.copy(alpha = 0.45f))
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = DeveloperBadgeBlue,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$professionalName • $role",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
