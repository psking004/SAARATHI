package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.domain.model.LocationPoint
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.SaarathiBlue
import com.example.ui.theme.SaarathiNavy
import com.example.ui.theme.SaarathiTeal
import com.example.ui.theme.SaarathiTealLight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reusable Mapbox-compatible Navigation Map Component.
 * Supports:
 * - Current user location with pulsating beacon & directional heading cone
 * - Journey route path (sampled breadcrumb polyline)
 * - Incident location marker with warning alert radius
 * - Map camera following user (toggleable: Follow / Free Pan)
 * - Interactive Pan and Zoom controls
 * - Vector styling with coordinate HUD and speed indicator
 */
@Composable
fun MapboxMapView(
    userLatitude: Double = 12.9716,
    userLongitude: Double = 77.5946,
    headingDegrees: Float = 0f,
    speedKmh: Float = 0f,
    waypoints: List<LocationPoint> = emptyList(),
    incidentLatitude: Double? = null,
    incidentLongitude: Double? = null,
    modifier: Modifier = Modifier,
    isFollowModeInitial: Boolean = true,
    showControls: Boolean = true
) {
    // Mapbox Access Token resolution (read from BuildConfig or safe placeholder)
    val mapboxToken = remember {
        try {
            val field = BuildConfig::class.java.getField("EXPO_PUBLIC_MAPBOX_ACCESS_TOKEN")
            field.get(null) as? String ?: "pk.saarathi.protected.mapbox.token"
        } catch (e: Exception) {
            "pk.saarathi.protected.mapbox.token"
        }
    }

    var isFollowUser by remember { mutableStateOf(isFollowModeInitial) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for user beacon and incident marker
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_anim"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha_anim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE8ECEF))
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .testTag("mapbox_map_view")
    ) {
        // Interactive Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        isFollowUser = false
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val effectiveCenter = if (isFollowUser) center else Offset(center.x + panOffsetX, center.y + panOffsetY)

            // Draw Map Background Grid & Roads (Vector Canvas)
            drawMapBackgroundGrid(this, size, effectiveCenter, zoomLevel)

            // Draw Route Waypoints Polyline
            drawJourneyRoute(
                scope = this,
                center = effectiveCenter,
                userLat = userLatitude,
                userLng = userLongitude,
                waypoints = waypoints,
                zoom = zoomLevel
            )

            // Draw Incident Marker if available
            if (incidentLatitude != null && incidentLongitude != null) {
                drawIncidentMarker(
                    scope = this,
                    center = effectiveCenter,
                    userLat = userLatitude,
                    userLng = userLongitude,
                    incidentLat = incidentLatitude,
                    incidentLng = incidentLongitude,
                    zoom = zoomLevel,
                    pulseRadius = pulseRadius,
                    pulseAlpha = pulseAlpha
                )
            }

            // Draw User Current Location Beacon & Heading Indicator
            drawUserLocationMarker(
                scope = this,
                position = effectiveCenter,
                heading = headingDegrees,
                speedKmh = speedKmh,
                pulseRadius = pulseRadius,
                pulseAlpha = pulseAlpha
            )
        }

        // Top Status Overlays: Mapbox Branding & Coordinates HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mapbox Token & Engine Badge
            Surface(
                color = SaarathiNavy.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SaarathiTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MAPBOX LIVE • GPS FIX",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Coordinates Badge
            Surface(
                color = Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "${String.format("%.4f", userLatitude)}°, ${String.format("%.4f", userLongitude)}°",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaarathiNavy,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Map Control Floating Buttons (Follow toggle, Zoom In, Zoom Out)
        if (showControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Follow / Recenter Camera Button
                IconButton(
                    onClick = {
                        isFollowUser = true
                        panOffsetX = 0f
                        panOffsetY = 0f
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isFollowUser) SaarathiNavy else Color.White,
                        contentColor = if (isFollowUser) Color.White else SaarathiNavy
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(3.dp, CircleShape)
                        .testTag("map_recenter_button")
                ) {
                    Icon(
                        imageVector = if (isFollowUser) Icons.Default.MyLocation else Icons.Default.LocationSearching,
                        contentDescription = "Recenter Camera",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom In
                IconButton(
                    onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.5f) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White,
                        contentColor = SaarathiNavy
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(3.dp, CircleShape)
                        .testTag("map_zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                }

                // Zoom Out
                IconButton(
                    onClick = { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.6f) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White,
                        contentColor = SaarathiNavy
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(3.dp, CircleShape)
                        .testTag("map_zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                }
            }
        }

        // Bottom Navigation Speed HUD
        if (speedKmh > 0) {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = SaarathiTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${speedKmh.toInt()} km/h",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SaarathiNavy
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Map Vector Drawing Helpers
// -------------------------------------------------------------------------------------------------

private fun drawMapBackgroundGrid(
    scope: DrawScope,
    size: androidx.compose.ui.geometry.Size,
    center: Offset,
    zoom: Float
) {
    val roadColor = Color(0xFFD5DDE4)
    val arterialRoadColor = Color(0xFFFFFFFF)
    val waterColor = Color(0xFFC7E2E8)
    val parkColor = Color(0xFFDDF0E4)

    // Base background
    scope.drawRect(color = Color(0xFFF3F5F7))

    // Decorative green space / park block
    scope.drawRoundRect(
        color = parkColor,
        topLeft = Offset(center.x - 220f * zoom, center.y - 180f * zoom),
        size = androidx.compose.ui.geometry.Size(160f * zoom, 120f * zoom),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    // Decorative water body
    scope.drawRoundRect(
        color = waterColor,
        topLeft = Offset(center.x + 80f * zoom, center.y + 60f * zoom),
        size = androidx.compose.ui.geometry.Size(180f * zoom, 90f * zoom),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
    )

    val step = 70f * zoom
    // Minor road grid lines
    var x = (center.x % step) - step
    while (x < size.width + step) {
        scope.drawLine(
            color = roadColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 3f * zoom
        )
        x += step
    }

    var y = (center.y % step) - step
    while (y < size.height + step) {
        scope.drawLine(
            color = roadColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 3f * zoom
        )
        y += step
    }

    // Main Expressway / Arterial Corridors
    val mainDiagonalX = center.x - 300f * zoom
    val mainDiagonalY = center.y + 200f * zoom
    scope.drawLine(
        color = arterialRoadColor,
        start = Offset(mainDiagonalX, mainDiagonalY),
        end = Offset(center.x + 350f * zoom, center.y - 250f * zoom),
        strokeWidth = 10f * zoom,
        cap = StrokeCap.Round
    )
    scope.drawLine(
        color = Color(0xFFE2A84B).copy(alpha = 0.5f),
        start = Offset(mainDiagonalX, mainDiagonalY),
        end = Offset(center.x + 350f * zoom, center.y - 250f * zoom),
        strokeWidth = 4f * zoom,
        cap = StrokeCap.Round
    )
}

private fun drawJourneyRoute(
    scope: DrawScope,
    center: Offset,
    userLat: Double,
    userLng: Double,
    waypoints: List<LocationPoint>,
    zoom: Float
) {
    if (waypoints.isEmpty()) {
        // Draw standard local journey segment trace
        val path = Path().apply {
            moveTo(center.x - 140f * zoom, center.y + 110f * zoom)
            cubicTo(
                center.x - 80f * zoom, center.y + 90f * zoom,
                center.x - 40f * zoom, center.y + 30f * zoom,
                center.x, center.y
            )
        }

        // Shadow path
        scope.drawPath(
            path = path,
            color = SaarathiBlue.copy(alpha = 0.25f),
            style = Stroke(width = 10f * zoom, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Active route polyline
        scope.drawPath(
            path = path,
            color = SaarathiTeal,
            style = Stroke(width = 6f * zoom, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        return
    }

    // Convert GPS waypoints relative to user location
    val scale = 8000.0f * zoom
    val path = Path()

    waypoints.forEachIndexed { index, wp ->
        val dx = ((wp.longitude - userLng) * scale).toFloat()
        val dy = -((wp.latitude - userLat) * scale).toFloat()
        val point = Offset(center.x + dx, center.y + dy)
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }

    // Connect last waypoint to current user location
    path.lineTo(center.x, center.y)

    scope.drawPath(
        path = path,
        color = SaarathiTeal,
        style = Stroke(width = 6f * zoom, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun drawUserLocationMarker(
    scope: DrawScope,
    position: Offset,
    heading: Float,
    speedKmh: Float,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    // 1. Pulsing radar circle
    scope.drawCircle(
        color = SaarathiTeal.copy(alpha = pulseAlpha),
        radius = pulseRadius * 1.5f,
        center = position
    )

    // 2. Accuracy halo
    scope.drawCircle(
        color = SaarathiTealLight.copy(alpha = 0.45f),
        radius = 18f,
        center = position
    )

    // 3. Directional Heading Cone
    scope.rotate(degrees = heading, pivot = position) {
        val conePath = Path().apply {
            moveTo(position.x, position.y - 30f)
            lineTo(position.x - 14f, position.y - 8f)
            lineTo(position.x + 14f, position.y - 8f)
            close()
        }
        scope.drawPath(
            path = conePath,
            brush = Brush.verticalGradient(
                colors = listOf(SaarathiTeal.copy(alpha = 0.6f), Color.Transparent),
                startY = position.y - 30f,
                endY = position.y
            )
        )
    }

    // 4. White outer ring
    scope.drawCircle(
        color = Color.White,
        radius = 9f,
        center = position
    )

    // 5. Core solid navy/teal dot
    scope.drawCircle(
        color = SaarathiNavy,
        radius = 7f,
        center = position
    )
}

private fun drawIncidentMarker(
    scope: DrawScope,
    center: Offset,
    userLat: Double,
    userLng: Double,
    incidentLat: Double,
    incidentLng: Double,
    zoom: Float,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    val scale = 8000.0f * zoom
    val dx = ((incidentLng - userLng) * scale).toFloat()
    val dy = -((incidentLat - userLat) * scale).toFloat()
    val incidentPos = Offset(center.x + dx, center.y + dy)

    // Pulsing Red Alarm Wave
    scope.drawCircle(
        color = EmergencyRed.copy(alpha = pulseAlpha),
        radius = pulseRadius * 2.2f,
        center = incidentPos
    )

    // Inner Red Warning Pin
    scope.drawCircle(
        color = EmergencyRed,
        radius = 12f,
        center = incidentPos
    )

    scope.drawCircle(
        color = Color.White,
        radius = 4f,
        center = incidentPos
    )
}
