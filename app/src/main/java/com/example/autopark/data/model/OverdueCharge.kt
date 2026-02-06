package com.example.autopark.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class OverdueCharge(
    val id: String = "",
    val ownerId: String = "",
    val invoiceId: String = "",
    val originalAmount: Double = 0.0,
    val latnesFeePercentage: Double = 0.0, // e.g., 10% late fee
    val lateFeeAmount: Double = 0.0,
    val totalDueAmount: Double = 0.0,
    val daysOverdue: Int = 0,
    val dueDate: Long = 0,
    val paymentStatus: String = "", // PENDING, PAID
    val paymentDate: Long? = null,
    val amountPaid: Double = 0.0,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
