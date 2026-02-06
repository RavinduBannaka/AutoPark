package com.example.autopark.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ParkingRate(
    val id: String = "",
    val parkingLotId: String = "",
    val rateType: String = "", // NORMAL, VIP, HOURLY, OVERNIGHT
    val pricePerHour: Double = 0.0,
    val pricePerDay: Double = 0.0, // 24 hours
    val overnightPrice: Double = 0.0, // 8 PM - 8 AM
    val minChargeAmount: Double = 0.0,
    val maxChargePerDay: Double = 0.0,
    val isActive: Boolean = true,
    val vipMultiplier: Double = 1.0, // For VIP rates
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

enum class RateType {
    NORMAL, VIP, HOURLY, OVERNIGHT
}
