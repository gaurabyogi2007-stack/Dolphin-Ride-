package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class Ride(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromLocation: String,
    val toLocation: String,
    val type: String, // bike, auto, car
    val subtype: String, // standard, premium, electric, economy, comfort, share, xl
    val price: Double,
    val dateStr: String,
    val timeStr: String,
    val status: String, // completed, cancelled, active
    val driverName: String,
    val rating: Int // 1-5
)
