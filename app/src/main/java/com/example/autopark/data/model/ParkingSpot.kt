package com.example.autopark.data.model

data class ParkingSpot(
    val id: String = "",
    val spotNumber: String = "",
    val floor: Int = 1,
    val isOccupied: Boolean = false,
    val occupiedBy: String? = null,
    val occupiedSince: Long? = null,
    val vehicleNumber: String? = null
)