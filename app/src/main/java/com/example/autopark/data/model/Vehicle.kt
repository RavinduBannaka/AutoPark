package com.example.autopark.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Vehicle(
    val id: String = "",
    val ownerId: String = "",
    val vehicleNumber: String = "",
    val vehicleType: String = "", // Car, Bike, Truck, etc.
    val color: String = "",
    val brand: String = "",
    val model: String = "",
    val parkingLicenseValid: Boolean = true,
    val registrationExpiry: Long = 0,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
