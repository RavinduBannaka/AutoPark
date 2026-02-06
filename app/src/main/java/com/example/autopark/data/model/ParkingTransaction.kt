package com.example.autopark.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ParkingTransaction(
    val id: String = "",
    val parkingLotId: String = "",
    val vehicleId: String = "",
    val ownerId: String = "",
    val vehicleNumber: String = "",
    val entryTime: Long = 0, // timestamp in milliseconds
    val exitTime: Long? = null, // timestamp in milliseconds
    val duration: Long = 0, // in minutes
    val rateType: String = "", // NORMAL, VIP, HOURLY, OVERNIGHT
    val chargeAmount: Double = 0.0,
    val status: String = "", // ACTIVE, COMPLETED, PENDING_PAYMENT
    val paymentMethod: String = "", // CARD, CASH, UPI
    val paymentStatus: String = "", // PENDING, COMPLETED, FAILED
    val notes: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

enum class TransactionStatus {
    ACTIVE, COMPLETED, PENDING_PAYMENT
}

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED
}
