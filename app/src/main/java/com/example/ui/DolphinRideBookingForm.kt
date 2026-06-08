package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.util.Locale

@Composable
fun DolphinRideBookingForm(
    viewModel: DolphinViewModel,
    t: (String) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Local fields synchronized with viewmodel
    val fromLoc by viewModel.fromLocation.collectAsState()
    val toLoc by viewModel.toLocation.collectAsState()
    val isEstimating by viewModel.isEstimatingFare.collectAsState()
    val aiEstimation by viewModel.geminiFareEstimation.collectAsState()

    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("bike") } // "bike", "auto", "car"
    var selectedPaymentMethod by remember { mutableStateOf("cash") } // "cash", "esewa", "khalti", "dolphin_wallet"

    val favoriteAddresses = remember {
        mutableStateListOf(
            Triple("Home", "Kathmandu", "🏠"),
            Triple("Work", "Lalitpur", "💼"),
            Triple("College", "Pokhara", "🎓"),
            Triple("Market", "Thamel", "🛍️")
        )
    }

    val nepalCities = listOf(
        "Kathmandu", "Pokhara", "Lalitpur", "Bhaktapur", "Chitwan",
        "Janakpur", "Butwal", "Dharan", "Nepalgunj", "Birgunj", "Biratnagar", "Kirtipur", "Thamel", "Bouddha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dolphin_booking_form"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Plan & Estimate Ride",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 1. Pick-up spot field
            Box(modifier = Modifier.fillMaxWidth().zIndex(110f)) {
                Column {
                    OutlinedTextField(
                        value = fromLoc,
                        onValueChange = {
                            viewModel.updateFromLocation(it)
                            showFromDropdown = true
                        },
                        label = { Text("Pick-up Location", fontSize = 12.sp) },
                        placeholder = { Text("Enter current spot...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFC107),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFFFF8E1).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("pickup_address_input"),
                        singleLine = true
                    )

                    // Autocomplete drop-down matching nepal cities
                    if (showFromDropdown && fromLoc.length >= 2) {
                        val matching = nepalCities.filter { it.contains(fromLoc, ignoreCase = true) }
                        if (matching.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    matching.take(4).forEach { city ->
                                        Text(
                                            text = city,
                                            fontSize = 13.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.updateFromLocation(city)
                                                    showFromDropdown = false
                                                }
                                                .padding(12.dp)
                                        )
                                        Divider(color = Color(0xFFF5F5F5))
                                    }
                                }
                            }
                        }
                    }

                    // Favorite Addresses Selector for Pick-up
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favorites:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        favoriteAddresses.forEach { (name, addr, icon) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF8E1))
                                    .clickable {
                                        viewModel.updateFromLocation(addr)
                                        Toast.makeText(context, "Loaded Pick-up: $addr", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "$icon $name", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                            }
                        }
                        if (fromLoc.isNotBlank() && favoriteAddresses.none { it.second.lowercase() == fromLoc.lowercase() }) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .clickable {
                                        favoriteAddresses.add(Triple("Fav ${favoriteAddresses.size + 1}", fromLoc, "⭐"))
                                        Toast.makeText(context, "Saved Pick-up to favorites!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "⭐ Save", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Drop-off spot field
            Box(modifier = Modifier.fillMaxWidth().zIndex(100f)) {
                Column {
                    OutlinedTextField(
                        value = toLoc,
                        onValueChange = {
                            viewModel.updateToLocation(it)
                            showToDropdown = true
                        },
                        label = { Text("Drop-off Destination", fontSize = 12.sp) },
                        placeholder = { Text("Search where to drop...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFC107),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFFFF8E1).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("dropoff_address_input"),
                        singleLine = true
                    )

                    // Autocomplete dropdown
                    if (showToDropdown && toLoc.length >= 2) {
                        val matching = nepalCities.filter { it.contains(toLoc, ignoreCase = true) }
                        if (matching.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    matching.take(4).forEach { city ->
                                        Text(
                                            text = city,
                                            fontSize = 13.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.updateToLocation(city)
                                                    showToDropdown = false
                                                }
                                                .padding(12.dp)
                                        )
                                        Divider(color = Color(0xFFF5F5F5))
                                    }
                                }
                            }
                        }
                    }

                    // Favorite Addresses Selector for Drop-off
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favorites:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        favoriteAddresses.forEach { (name, addr, icon) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF8E1))
                                    .clickable {
                                        viewModel.updateToLocation(addr)
                                        Toast.makeText(context, "Loaded Drop-off: $addr", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "$icon $name", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                            }
                        }
                        if (toLoc.isNotBlank() && favoriteAddresses.none { it.second.lowercase() == toLoc.lowercase() }) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFEBEE))
                                    .clickable {
                                        favoriteAddresses.add(Triple("Fav ${favoriteAddresses.size + 1}", toLoc, "⭐"))
                                        Toast.makeText(context, "Saved Drop-off to favorites!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "⭐ Save", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Simple Visual Category Selection Segment
            Text(
                text = "Vehicle Category",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("bike", "🏍️ Bike", "Fast & Agile"),
                    Triple("auto", "🛺 Auto", "Local Space"),
                    Triple("car", "🚗 Car", "Premium AC")
                ).forEach { (catID, label, subtitle) ->
                    val isSelected = selectedCategory == catID
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFFFF8E1) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedCategory = catID
                                // Auto recalibrate if and only if we already have estimation
                                if (fromLoc.isNotEmpty() && toLoc.isNotEmpty()) {
                                    viewModel.estimateFare(fromLoc, toLoc, catID)
                                }
                            }
                            .testTag("booking_category_selector_$catID")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF212121))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = subtitle, fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Booking action buttons
            if (aiEstimation == null) {
                // If we don't have an active estimation, query Gemini first
                Button(
                    onClick = {
                        if (fromLoc.isBlank() || toLoc.isBlank()) {
                            Toast.makeText(context, "Please enter both Pick-up and Drop-off locations first!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.estimateFare(fromLoc, toLoc, selectedCategory)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("estimate_fare_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isEstimating
                ) {
                    if (isEstimating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF212121)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Consulting Gemini AI...", color = Color(0xFF212121), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "🔮 Estimate Fare with Gemini", color = Color(0xFF212121), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // AI Estimation is present! Display full AI breakdown
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFFFF8E1), RoundedCornerShape(12.dp))
                                .testTag("ai_estimation_display_panel")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (aiEstimation?.isAiEstimated == true) "🤖 GEMINI AI PREDICTION" else "⚙️ LOCAL SPEED ESTIMATOR",
                                            color = Color(0xFF1565C0),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", aiEstimation?.distance)} KM",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Estimated Cost", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            text = "Rs. ${aiEstimation?.fare?.toInt()}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            color = Color(0xFFFF6F00),
                                            modifier = Modifier.testTag("ai_estimated_cost_text")
                                        )
                                    }

                                    // Clear current estimation button to re-plan
                                    TextButton(onClick = { viewModel.clearFareEstimation() }) {
                                        Text(text = "❌ Edit Route", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.LightGray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = aiEstimation?.reason ?: "Estimated via fast city avenue",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.testTag("ai_estimation_reason_text")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Nepal Payment selection view block
                        Text(
                            text = "💳 Nepal Payment Method",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple("cash", "💵 Cash", Color(0xFF424242)),
                                Triple("esewa", "🟢 eSewa", Color(0xFF4CAF50)),
                                Triple("khalti", "🟣 Khalti", Color(0xFF5E35B1)),
                                Triple("dolphin_wallet", "🐬 Balance", Color(0xFFFFB300))
                            ).forEach { (pmID, label, color) ->
                                val isSelected = selectedPaymentMethod == pmID
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) color else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedPaymentMethod = pmID
                                            if (pmID == "dolphin_wallet") {
                                                val appBal = viewModel.walletBalance.value
                                                val fareAmt = aiEstimation?.fare ?: 120.0
                                                if (appBal < fareAmt) {
                                                    Toast.makeText(context, "Insufficient app balance: Rs. ${appBal.toInt()}. Please choose Cash, Khalti, or eSewa.", Toast.LENGTH_LONG).show()
                                                    selectedPaymentMethod = "cash"
                                                }
                                            }
                                        }
                                        .testTag("payment_method_chip_$pmID")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            color = if (isSelected) color else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Book Now Button
                        Button(
                            onClick = {
                                val est = aiEstimation
                                if (est != null) {
                                    if (selectedPaymentMethod == "esewa") {
                                        Toast.makeText(context, "🟢 eSewa Secure checkout linked! Initiating quick pay...", Toast.LENGTH_SHORT).show()
                                    } else if (selectedPaymentMethod == "khalti") {
                                        Toast.makeText(context, "🟣 Khalti secure gateway launched! Approving ride secure token...", Toast.LENGTH_SHORT).show()
                                    }
                                    viewModel.confirmRideBooking(selectedCategory, "standard")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("book_estimated_ride_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "🚀 Confirm & Book Now (" + selectedPaymentMethod.uppercase() + ")", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
