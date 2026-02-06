package com.example.autopark.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ParkingLot(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val totalSpots: Int = 0,
    val availableSpots: Int = 0,
    val description: String = "",
    val contactNumber: String = "",
    val openingTime: String = "", // HH:mm format
    val closingTime: String = "", // HH:mm format
    val is24Hours: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
