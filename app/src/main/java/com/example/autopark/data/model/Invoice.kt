package com.example.autopark.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Invoice(
    val id: String = "",
    val ownerId: String = "",
    val invoiceNumber: String = "",
    val month: Int = 0, // 1-12
    val year: Int = 0,
    val fromDate: Long = 0,
    val toDate: Long = 0,
    val totalTransactions: Int = 0,
    val totalHours: Long = 0,
    val totalCharges: Double = 0.0,
    val overdueCharges: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentStatus: String = "", // PENDING, PAID, PARTIAL
    val paymentDate: Long? = null,
    val amountPaid: Double = 0.0,
    val transactionIds: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

enum class InvoicePaymentStatus {
    PENDING, PAID, PARTIAL
}
