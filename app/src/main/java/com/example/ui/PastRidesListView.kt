package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ride
import java.util.Locale

@Composable
fun PastRidesListView(
    rides: List<Ride>,
    onRideClick: (Ride) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "bike", "auto", "car"
    var selectedDateRange by remember { mutableStateOf("all") } // "all", "7days", "30days"
    var selectedCostRange by remember { mutableStateOf("all") } // "all", "under150", "150to400", "above400"

    // Parse date helper inside Composable
    val isWithinDays: (String, Int) -> Boolean = { dateStr, maxDays ->
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayDate = try {
            sdf.parse("2026-06-08")!!
        } catch (e: Exception) {
            java.util.Date()
        }
        val rideDate = when (dateStr.lowercase()) {
            "today" -> todayDate
            "yesterday" -> java.util.Date(todayDate.time - 24 * 60 * 60 * 1000L)
            else -> try {
                sdf.parse(dateStr) ?: todayDate
            } catch (e: Exception) {
                todayDate
            }
        }
        val diffMillis = todayDate.time - rideDate.time
        val diffDays = diffMillis / (24 * 60 * 60 * 1000L)
        diffDays >= 0 && diffDays <= maxDays
    }

    // Filtered rides
    val filteredRides = remember(rides, searchQuery, selectedFilter, selectedDateRange, selectedCostRange) {
        rides.filter { ride ->
            val matchesQuery = ride.fromLocation.contains(searchQuery, ignoreCase = true) ||
                    ride.toLocation.contains(searchQuery, ignoreCase = true) ||
                    ride.driverName.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = selectedFilter == "all" || ride.type.lowercase() == selectedFilter.lowercase()
            
            val matchesDate = when (selectedDateRange) {
                "7days" -> isWithinDays(ride.dateStr, 7)
                "30days" -> isWithinDays(ride.dateStr, 30)
                else -> true
            }

            val matchesCost = when (selectedCostRange) {
                "under150" -> ride.price < 150.0
                "150to400" -> ride.price in 150.0..400.0
                "above400" -> ride.price > 400.0
                else -> true
            }

            matchesQuery && matchesFilter && matchesDate && matchesCost
        }
    }

    // Historical spend calculations
    val totalSpend = remember(rides) {
        rides.filter { it.status == "completed" }.sumOf { it.price }
    }
    val completedCount = remember(rides) {
        rides.count { it.status == "completed" }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("past_rides_history_container")
    ) {
        // Quick Stats Summary Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("rides_stats_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Spent",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${totalSpend.toInt()}",
                        fontSize = 18.sp,
                        color = Color(0xFFFF6F00),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("total_spent_text")
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Completed",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount Rides",
                        fontSize = 18.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("completed_rides_count_text")
                    )
                }
            }
        }

        // Search & Filtering Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search location or driver...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("past_rides_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        // Filter: Category
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(42.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                FilterCategoryPill("All", selectedFilter == "all") { selectedFilter = "all" }
                FilterCategoryPill("🏍️ Bikes", selectedFilter == "bike") { selectedFilter = "bike" }
                FilterCategoryPill("🛺 Autos", selectedFilter == "auto") { selectedFilter = "auto" }
                FilterCategoryPill("🚗 Cars", selectedFilter == "car") { selectedFilter = "car" }
            }
        }

        // Filter: Date Range
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Date:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(42.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                FilterCategoryPill("All Time", selectedDateRange == "all") { selectedDateRange = "all" }
                FilterCategoryPill("7 Days", selectedDateRange == "7days") { selectedDateRange = "7days" }
                FilterCategoryPill("30 Days", selectedDateRange == "30days") { selectedDateRange = "30days" }
            }
        }

        // Filter: Cost range
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Price:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(42.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                FilterCategoryPill("Any", selectedCostRange == "all") { selectedCostRange = "all" }
                FilterCategoryPill("< Rs.150", selectedCostRange == "under150") { selectedCostRange = "under150" }
                FilterCategoryPill("Rs.150-400", selectedCostRange == "150to400") { selectedCostRange = "150to400" }
                FilterCategoryPill("> Rs.400", selectedCostRange == "above400") { selectedCostRange = "above400" }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredRides.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No matching rides found." else "No rides in this category.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .testTag("past_rides_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRides) { ride ->
                    PastRideCard(ride = ride, onClick = { onRideClick(ride) })
                }
            }
        }
    }
}

@Composable
fun FilterCategoryPill(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Color(0xFFFFC107) else Color.White)
            .border(1.dp, if (isActive) Color(0xFFFFC107) else Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color(0xFF212121) else Color.Gray
        )
    }
}

@Composable
fun PastRideCard(
    ride: Ride,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("past_ride_item_${ride.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Vehicle details + Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFF8E1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (ride.type.lowercase()) {
                                "bike" -> "🏍️"
                                "auto" -> "🛺"
                                else -> "🚗"
                            },
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = ride.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } +
                                    " (${ride.subtype.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "${ride.dateStr} • ${ride.timeStr}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = "Rs. ${ride.price.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF6F00),
                    modifier = Modifier.testTag("ride_price_text_${ride.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route representation (Timeline Style dots)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Pickup Area
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = ride.fromLocation,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                }

                // Dot space connector
                Box(
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .height(12.dp)
                        .width(2.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )

                // Dropoff Area
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFF44336), RoundedCornerShape(1.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = ride.toLocation,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = Color(0xFFFAFAFA), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Footer metadata row: Driver + Rating star and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Driver profile
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Pilot: ", fontSize = 11.sp, color = Color.Gray)
                    Text(text = ride.driverName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))

                    if (ride.rating > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⭐ " + "★".repeat(ride.rating),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA000)
                        )
                    }
                }

                // Status Pill
                val isCompleted = ride.status.lowercase() == "completed"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ride.status.uppercase(),
                        fontSize = 9.sp,
                        color = if (isCompleted) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
