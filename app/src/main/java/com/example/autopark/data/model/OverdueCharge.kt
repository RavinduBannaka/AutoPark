package com.example.autopark.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class OverdueCharge(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val invoiceId: String = "",
    val invoiceNumber: String = "",
    val originalAmount: Double = 0.0,
    val lateFeePercentage: Double = 0.0, // e.g., 10% late fee
    val lateFeeAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val totalDueAmount: Double = 0.0,
    val overdueDays: Int = 0,
    val daysOverdue: Int = 0,
    val dueDate: Long = 0,
    val status: String = "PENDING", // PENDING, PAID
    val paymentStatus: String = "PENDING", // PENDING, PAID
    val paymentDate: Long? = null,
    val amountPaid: Double = 0.0,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
