package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DriverStatusPanel(
    step: Int, // 0: Confirmed, 1: Arriving, 2: Started, 3: Completed
    driverName: String,
    vehiclePlate: String,
    phoneNumber: String,
    modifier: Modifier = Modifier
) {
    // Simulated live metrics based on progress step
    val driverLocation = remember(step) {
        when (step) {
            0 -> "Durbar Marg (Near Standard Chartered Bank)"
            1 -> "Bagbazar Intersection (Arriving at Pickup)"
            2 -> "Trip started - Crossing Koteshwor highway"
            3 -> "Trip completed - Dropped off securely"
            else -> "Simulating next street location..."
        }
    }

    val etaMinutes = remember(step) {
        when (step) {
            0 -> 8
            1 -> 2
            2 -> 11
            3 -> 0
            else -> 0
        }
    }

    val statusMessage = remember(step) {
        when (step) {
            0 -> "Driver has accepted your request"
            1 -> "Driver is arriving at your pick-up spot"
            2 -> "Heading swiftly to destination"
            3 -> "Arrived at destination"
            else -> ""
        }
    }

    val statusBadgeColor = remember(step) {
        when (step) {
            0 -> Color(0xFF1976D2) // Blue
            1 -> Color(0xFFFFB300) // Amber
            2 -> Color(0xFF4CAF50) // Green
            3 -> Color(0xFF757575) // Gray
            else -> Color.DarkGray
        }
    }

    // Interactive blinking visual locator radar dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_fade"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFFFFC107).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("driver_status_panel"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Live status & ETA Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Blinking radar locator icon
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusBadgeColor.copy(alpha = radarPulseAlpha), CircleShape)
                            .border(1.dp, statusBadgeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = statusBadgeColor
                    )
                }

                if (step < 3) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ETA: $etaMinutes MINS",
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("driver_status_eta_text"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF6F00)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ARRIVED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress text / state desc
            Text(
                text = driverLocation,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF212121),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_status_simulated_location")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = statusMessage,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mini visual location slider metric
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                val completionPercentage = when (step) {
                    0 -> 0.15f
                    1 -> 0.45f
                    2 -> 0.80f
                    3 -> 1.0f
                    else -> 0.0f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(completionPercentage)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(statusBadgeColor)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Driver details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFFFECB3), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👨", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = driverName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            modifier = Modifier.testTag("driver_status_name")
                        )
                        Text(
                            text = "Licensed: $vehiclePlate",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Call Action Button
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "📞 Call ($phoneNumber)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF6F00)
                    )
                }
            }
        }
    }
}
