package com.example.ui

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom enum representing available map styling layers.
 */
enum class DolphinMapTheme {
    STANDARD,
    SATELLITE,
    TERRAIN,
    DARK_RETRO
}

/**
 * Companion object that provides mapping coordinates and projections for Kathmandu context.
 */
object DolphinMapEngine {
    data class MapPoint(val name: String, val latitude: Double, val longitude: Double)

    val registeredPoints = listOf(
        MapPoint("Kathmandu", 27.7172, 85.3240),
        MapPoint("Lalitpur", 27.6744, 85.3218),
        MapPoint("Bhaktapur", 27.6710, 85.4298),
        MapPoint("Pokhara", 28.2096, 83.9856),
        MapPoint("Kirtipur", 27.6799, 85.2754),
        MapPoint("Thamel", 27.7144, 85.3122),
        MapPoint("Bouddha", 27.7215, 85.3620),
        MapPoint("Jamsikhel", 27.6790, 85.3116),
        MapPoint("Chabahil", 27.7169, 85.3486),
        MapPoint("Kalanki", 27.6938, 85.2811),
        MapPoint("Baneshwor", 27.6915, 85.3422)
    )

    // Bounding coordinates of Kathmandu Valley for projection
    const val MIN_LAT = 27.63
    const val MAX_LAT = 27.75
    const val MIN_LNG = 85.24
    const val MAX_LNG = 85.44

    /**
     * Translates a textual location string into stable Latitude/Longitude coordinates.
     */
    fun getCoordinates(placeName: String): Pair<Double, Double> {
        val clean = placeName.trim().lowercase()
        if (clean.isEmpty()) {
            return Pair(27.7172, 85.3240) // Default centered in Kathmandu
        }
        val found = registeredPoints.find { clean.contains(it.name.lowercase()) || it.name.lowercase().contains(clean) }
        if (found != null) {
            return Pair(found.latitude, found.longitude)
        }
        // Fallback: stable hashing to map ANY user entered string to a consistent physical coordinate in the valley
        val hash = placeName.hashCode()
        val latRange = MAX_LAT - MIN_LAT
        val lngRange = MAX_LNG - MIN_LNG
        val latOffset = (abs(hash) % 1000) / 1000.0 * (latRange * 0.7) + (latRange * 0.15)
        val lngOffset = (abs(hash / 100) % 1000) / 1000.0 * (lngRange * 0.7) + (lngRange * 0.15)
        return Pair(MIN_LAT + latOffset, MIN_LNG + lngOffset)
    }

    /**
     * Linear Projection of Latitude/Longitude to 2D Screen Offset, factoring zoom and panning offset.
     */
    fun project(
        lat: Double,
        lng: Double,
        width: Float,
        height: Float,
        zoom: Float,
        panOffset: Offset
    ): Offset {
        val pctX = (lng - MIN_LNG) / (MAX_LNG - MIN_LNG)
        // Invert Y direction because screen coordinates increase downwards
        val pctY = 1.0 - (lat - MIN_LAT) / (MAX_LAT - MIN_LAT)

        val centerX = width / 2f
        val centerY = height / 2f

        // Apply zoom scaling centered on screen center, then translation offsets
        val x = centerX + (pctX.toFloat() * width - centerX) * zoom + panOffset.x
        val y = centerY + (pctY.toFloat() * height - centerY) * zoom + panOffset.y

        return Offset(x, y)
    }
}

/**
 * A highly polished, custom-coded interactive Map Component simulating a real mapping API library
 * (e.g. Google Maps or Mapbox). Supports standard map controls, styling layers, dynamic routing,
 * live animated GPS indicators, and simulated moving drivers.
 */
@Composable
fun DolphinMapView(
    modifier: Modifier = Modifier,
    fromLoc: String,
    toLoc: String,
    activeTracking: Boolean = false,
    selectedCategory: String = "bike"
) {
    val context = LocalContext.current

    // Map configuration state
    var selectedTheme by remember { mutableStateOf(DolphinMapTheme.STANDARD) }
    var zoomLevel by remember { mutableStateOf(1.22f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Retrieve projected geographical points
    val fromCoords = remember(fromLoc) { DolphinMapEngine.getCoordinates(fromLoc) }
    val toCoords = remember(toLoc) { DolphinMapEngine.getCoordinates(toLoc) }

    // Setup map animations (auto route animation loops)
    val infiniteTransition = rememberInfiniteTransition(label = "map_renders")
    
    // Wave pulse effect for the user's pins
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    // Moving riders/drivers along circular orbital paths around the viewport
    val riderProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rider_travel"
    )

    // Travel progress indicator along the route (when navigating)
    val tripProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Restart
        ),
        label = "trip_progress"
    )

    // Determine color schemes based on the selected map theme
    val mapColors = remember(selectedTheme) {
        when (selectedTheme) {
            DolphinMapTheme.STANDARD -> MapColors(
                land = Color(0xFFF2EFE9),
                water = Color(0xFFA5C9EB),
                majorRoad = Color(0xFFFFFFFF),
                secondaryRoad = Color(0xFFE4E1DB),
                parks = Color(0xFFD3ECD2),
                route = Color(0xFFFF6F00),
                contourLines = Color(0xFFE6E1D8),
                gridLines = Color(0xFFE0DBD3).copy(alpha = 0.5f)
            )
            DolphinMapTheme.SATELLITE -> MapColors(
                land = Color(0xFF223019),
                water = Color(0xFF0F1E36),
                majorRoad = Color(0xFF7F8C8D),
                secondaryRoad = Color(0xFF535C5E),
                parks = Color(0xFF14451D),
                route = Color(0xFFFF5252),
                contourLines = Color(0xFF2F3E26),
                gridLines = Color(0xFF384A30).copy(alpha = 0.4f)
            )
            DolphinMapTheme.TERRAIN -> MapColors(
                land = Color(0xFFEAE2D5),
                water = Color(0xFF7EBDC2),
                majorRoad = Color(0xFFFFFFFF),
                secondaryRoad = Color(0xFFE3DAC9),
                parks = Color(0xFFB1C89B),
                route = Color(0xFF22577A),
                contourLines = Color(0xFFCDBCAC),
                gridLines = Color(0xFFC4B2A0).copy(alpha = 0.6f)
            )
            DolphinMapTheme.DARK_RETRO -> MapColors(
                land = Color(0xFF14141E),
                water = Color(0xFF0F2C59),
                majorRoad = Color(0xFFFFAA00),
                secondaryRoad = Color(0xFF393E46),
                parks = Color(0xFF0F343A),
                route = Color(0xFF32FF6A),
                contourLines = Color(0xFF232332),
                gridLines = Color(0xFF24252F).copy(alpha = 0.5f)
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(mapColors.land)
            .border(1.5.dp, Color(0xFFFFC107).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panOffset = Offset(
                        x = (panOffset.x + dragAmount.x).coerceIn(-400f, 400f),
                        y = (panOffset.y + dragAmount.y).coerceIn(-400f, 400f)
                    )
                }
            }
            .testTag("interactive_dolphin_map")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Grid Lines (Coordinate Grid network)
            val gridStep = 60f
            for (x in 0..(w / gridStep).toInt()) {
                val lineX = x * gridStep + (panOffset.x % gridStep)
                drawLine(
                    color = mapColors.gridLines,
                    start = Offset(lineX, 0f),
                    end = Offset(lineX, h),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(h / gridStep).toInt()) {
                val lineY = y * gridStep + (panOffset.y % gridStep)
                drawLine(
                    color = mapColors.gridLines,
                    start = Offset(0f, lineY),
                    end = Offset(w, lineY),
                    strokeWidth = 1f
                )
            }

            // 2. Map Features: Water Bodies (Bagmati, Bishnumati Rivers representation)
            val waterPath = Path()
            val riverNodes = 6
            val nodeStepX = w / (riverNodes - 1)
            waterPath.moveTo(0f, h * 0.4f + panOffset.y)

            for (i in 0 until riverNodes) {
                val rx = i * nodeStepX + (panOffset.x * 0.15f)
                val ry = h * 0.45f + sin(i + panOffset.x * 0.005f) * 25f + panOffset.y
                waterPath.lineTo(rx, ry)
            }
            drawPath(
                path = waterPath,
                color = mapColors.water,
                style = Stroke(width = 24f * zoomLevel)
            )

            // Draw a big lake/pond representing Rani Pokhari
            val raniPokhariCenter = DolphinMapEngine.project(27.7078, 85.3149, w, h, zoomLevel, panOffset)
            drawRect(
                color = mapColors.water,
                topLeft = Offset(raniPokhariCenter.x - 16f * zoomLevel, raniPokhariCenter.y - 12f * zoomLevel),
                size = Size(32f * zoomLevel, 24f * zoomLevel)
            )

            // 3. Map Features: Green zones (Shivapuri National Park at top, Ratna Park inside)
            val ratnaParkCenter = DolphinMapEngine.project(27.7058, 85.3160, w, h, zoomLevel, panOffset)
            drawCircle(
                color = mapColors.parks,
                radius = 28f * zoomLevel,
                center = ratnaParkCenter
            )

            // Huge green reserve representing Shivapuri (North Kathmandu)
            val shivapuriCenter = DolphinMapEngine.project(27.7400, 85.3400, w, h, zoomLevel, panOffset)
            drawCircle(
                color = mapColors.parks,
                radius = 80f * zoomLevel,
                center = shivapuriCenter
            )

            // 4. Map Features: Contour topographical rings when in Terrain mode
            if (selectedTheme == DolphinMapTheme.TERRAIN) {
                // Draw three altitude rings simulating hills around Swoyambhu and Kopan
                val swoyambhuHills = DolphinMapEngine.project(27.7150, 85.2904, w, h, zoomLevel, panOffset)
                drawCircle(color = mapColors.contourLines, radius = 45f * zoomLevel, center = swoyambhuHills, style = Stroke(width = 2f))
                drawCircle(color = mapColors.contourLines, radius = 65f * zoomLevel, center = swoyambhuHills, style = Stroke(width = 1.5f))

                val kopanHills = DolphinMapEngine.project(27.7410, 85.3640, w, h, zoomLevel, panOffset)
                drawCircle(color = mapColors.contourLines, radius = 55f * zoomLevel, center = kopanHills, style = Stroke(width = 2f))
                drawCircle(color = mapColors.contourLines, radius = 75f * zoomLevel, center = kopanHills, style = Stroke(width = 1.5f))
            }

            // 5. Drawing major simulated Roads (Grid of Kathmandu Ring Road and Durbar Marg)
            // Ring road approximation: A vast giant rounded rectangle encircling Kathmandu
            val ringRoadCenter = DolphinMapEngine.project(27.7000, 85.3200, w, h, zoomLevel, panOffset)
            drawRoundRect(
                color = mapColors.majorRoad,
                topLeft = Offset(ringRoadCenter.x - w * 0.42f * zoomLevel, ringRoadCenter.y - h * 0.42f * zoomLevel),
                size = Size(w * 0.84f * zoomLevel, h * 0.84f * zoomLevel),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(120f * zoomLevel),
                style = Stroke(width = 14f * zoomLevel)
            )

            // Inside major avenues
            val thamelCenter = DolphinMapEngine.project(27.7144, 85.3122, w, h, zoomLevel, panOffset)
            val baneshworCenter = DolphinMapEngine.project(27.6915, 85.3422, w, h, zoomLevel, panOffset)
            drawLine(
                color = mapColors.majorRoad,
                start = Offset(thamelCenter.x, 0f),
                end = Offset(thamelCenter.x, h),
                strokeWidth = 9f * zoomLevel
            )
            drawLine(
                color = mapColors.majorRoad,
                start = Offset(0f, baneshworCenter.y),
                end = Offset(w, baneshworCenter.y),
                strokeWidth = 9f * zoomLevel
            )

            // Secondary neighborhood pathways
            for (idx in 1..4) {
                drawLine(
                    color = mapColors.secondaryRoad,
                    start = Offset(0f, h * 0.22f * idx + panOffset.y),
                    end = Offset(w, h * 0.22f * idx + panOffset.y),
                    strokeWidth = 4f * zoomLevel
                )
                drawLine(
                    color = mapColors.secondaryRoad,
                    start = Offset(w * 0.25f * idx + panOffset.x, 0f),
                    end = Offset(w * 0.25f * idx + panOffset.x, h),
                    strokeWidth = 4f * zoomLevel
                )
            }

            // 6. Draw dynamic Route visualization between FROM and TO coordinates
            val fromProj = DolphinMapEngine.project(fromCoords.first, fromCoords.second, w, h, zoomLevel, panOffset)
            val toProj = DolphinMapEngine.project(toCoords.first, toCoords.second, w, h, zoomLevel, panOffset)

            val hasDestination = toLoc.trim().isNotEmpty() && toLoc.lowercase() != fromLoc.lowercase()

            if (hasDestination) {
                // To look realistic, instead of a straight line, routes wrap or curve.
                // We create a bezier cubic path representing the optimized city streets sequence
                val routePath = Path().apply {
                    moveTo(fromProj.x, fromProj.y)
                    // First control point curves slightly to mimic taking a street turn
                    val ctrlX1 = fromProj.x + (toProj.x - fromProj.x) * 0.2f
                    val ctrlY1 = fromProj.y - (toProj.y - fromProj.y) * 0.15f
                    // Second control point curves around Ring road intersection
                    val ctrlX2 = fromProj.x + (toProj.x - fromProj.x) * 0.8f
                    val ctrlY2 = fromProj.y + (toProj.y - fromProj.y) * 1.15f
                    
                    cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, toProj.x, toProj.y)
                }

                // Draw the colorful route border underlay
                drawPath(
                    path = routePath,
                    color = mapColors.route.copy(alpha = 0.35f),
                    style = Stroke(width = 12f * zoomLevel, cap = StrokeCap.Round)
                )

                // Draw the main route line with dashes/glow effect
                drawPath(
                    path = routePath,
                    color = mapColors.route,
                    style = Stroke(
                        width = 6f * zoomLevel,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
                    )
                )

                // Dynamic pulsing visual dot representing active rider simulation driving along path
                // Get segment interpolation
                val riderOffset = getCubicCoordinate(fromProj, toProj, tripProgress)
                drawCircle(
                    color = Color.White,
                    radius = 8f * zoomLevel,
                    center = riderOffset
                )
                drawCircle(
                    color = mapColors.route,
                    radius = 5f * zoomLevel,
                    center = riderOffset
                )

                // Label on the rider
                val pilotEmoji = when (selectedCategory) {
                    "bike" -> "🏍️"
                    "auto" -> "🛺"
                    else -> "🚗"
                }
                // (Optional layout drawing could paint actual text inside canvas but can cause crash, so we stick to visual tags)
            }

            // 7. Visual Pins & Markers
            // User Location Pin
            drawCircle(
                color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                radius = pulseSize * zoomLevel,
                center = fromProj
            )
            drawCircle(
                color = Color.White,
                radius = 10f * zoomLevel,
                center = fromProj
            )
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 6f * zoomLevel,
                center = fromProj
            )

            // Destination Pin
            if (hasDestination) {
                drawCircle(
                    color = Color(0xFFF44336).copy(alpha = 0.3f),
                    radius = pulseSize * 0.8f * zoomLevel,
                    center = toProj
                )
                drawCircle(
                    color = Color.White,
                    radius = 10f * zoomLevel,
                    center = toProj
                )
                drawCircle(
                    color = Color(0xFFF44336),
                    radius = 6f * zoomLevel,
                    center = toProj
                )
            }

            // 8. Simulated surrounding drivers / riders moving about in orbit
            val nearbyDrivers = listOf(
                Pair(27.722, 85.321),
                Pair(27.685, 85.334),
                Pair(27.701, 85.295),
                Pair(27.728, 85.345)
            )

            nearbyDrivers.forEachIndexed { index, pair ->
                // Apply a small sinusoidal moving offset
                val latMove = pair.first + sin(riderProgress + index) * 0.003
                val lngMove = pair.second + cos(riderProgress + index) * 0.003
                val driverProj = DolphinMapEngine.project(latMove, lngMove, w, h, zoomLevel, panOffset)

                // Draw driver dot
                drawCircle(
                    color = Color(0xFFFFC107).copy(alpha = 0.35f),
                    radius = 14f * zoomLevel,
                    center = driverProj
                )
                drawCircle(
                    color = Color(0xFFFFC107),
                    radius = 6f * zoomLevel,
                    center = driverProj
                )
                drawCircle(
                    color = Color.Black,
                    radius = 2.5f * zoomLevel,
                    center = driverProj
                )
            }
        }

        // Floating compass needle in the top-left corner
        Box(
            modifier = Modifier
                .padding(12.dp)
                .size(34.dp)
                .background(Color.White.copy(alpha = 0.75f), CircleShape)
                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🧭", fontSize = 16.sp)
        }

        // Map layering options (Switch theme selector)
        Row(
            modifier = Modifier
                .padding(bottom = 12.dp, start = 12.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .align(Alignment.BottomStart)
        ) {
            MapLayerPill("🗺️ Std", selectedTheme == DolphinMapTheme.STANDARD) {
                selectedTheme = DolphinMapTheme.STANDARD
            }
            Spacer(modifier = Modifier.width(4.dp))
            MapLayerPill("🛰️ Sat", selectedTheme == DolphinMapTheme.SATELLITE) {
                selectedTheme = DolphinMapTheme.SATELLITE
            }
            Spacer(modifier = Modifier.width(4.dp))
            MapLayerPill("⛰️ Terr", selectedTheme == DolphinMapTheme.TERRAIN) {
                selectedTheme = DolphinMapTheme.TERRAIN
            }
            Spacer(modifier = Modifier.width(4.dp))
            MapLayerPill("🌃 Neon", selectedTheme == DolphinMapTheme.DARK_RETRO) {
                selectedTheme = DolphinMapTheme.DARK_RETRO
            }
        }

        // Zoom and location controls alignment column
        Column(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom In Button
            IconButton(
                onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(5.5f) },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Zoom Out Button
            IconButton(
                onClick = { zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(0.6f) },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Text(text = "−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Re-center on GPS Location Button
            IconButton(
                onClick = {
                    panOffset = Offset.Zero
                    zoomLevel = 1.22f
                    Toast.makeText(context, "Map recentered on current GPS point!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFFFC107), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            ) {
                Text(text = "🎯", fontSize = 16.sp)
            }
        }

        // Mini telemetry status overlay showing Satellite / GPS telemetry metadata
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GPS: ACCURATE  |  ZM: ${(zoomLevel * 100).toInt()}%  |  DRV: 4 ACTIVE",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MapLayerPill(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color(0xFFFFC107) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color(0xFF212121) else Color.Gray
        )
    }
}

/**
 * Helper to interpolate coordinate along a cubic Bezier path.
 */
private fun getCubicCoordinate(start: Offset, end: Offset, t: Float): Offset {
    val ctrlX1 = start.x + (end.x - start.x) * 0.2f
    val ctrlY1 = start.y - (end.y - start.y) * 0.15f
    val ctrlX2 = start.x + (end.x - start.x) * 0.8f
    val ctrlY2 = start.y + (end.y - start.y) * 1.15f

    val u = 1 - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t

    val x = uuu * start.x + 3 * uu * t * ctrlX1 + 3 * u * tt * ctrlX2 + ttt * end.x
    val y = uuu * start.y + 3 * uu * t * ctrlY1 + 3 * u * tt * ctrlY2 + ttt * end.y

    return Offset(x, y)
}

/**
 * Configuration schema for custom map color choices.
 */
private data class MapColors(
    val land: Color,
    val water: Color,
    val majorRoad: Color,
    val secondaryRoad: Color,
    val parks: Color,
    val route: Color,
    val contourLines: Color,
    val gridLines: Color
)
