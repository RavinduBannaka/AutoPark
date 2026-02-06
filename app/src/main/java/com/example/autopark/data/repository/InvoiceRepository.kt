package com.example.autopark.data.repository

import com.example.autopark.data.model.Invoice
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvoiceRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addInvoice(invoice: Invoice): Result<String> {
        return try {
            val docRef = db.collection("invoices").add(invoice).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInvoice(invoice: Invoice): Result<Unit> {
        return try {
            db.collection("invoices").document(invoice.id).set(invoice).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvoice(invoiceId: String): Result<Invoice> {
        return try {
            val doc = db.collection("invoices").document(invoiceId).get().await()
            val invoice = doc.toObject(Invoice::class.java)
            if (invoice != null) {
                Result.success(invoice.copy(id = doc.id))
            } else {
                Result.failure(Exception("Invoice not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerInvoices(ownerId: String): Result<List<Invoice>> {
        return try {
            val docs = db.collection("invoices")
                .whereEqualTo("ownerId", ownerId)
                .orderBy("month")
                .orderBy("year")
                .get()
                .await()
            val invoices = docs.documents.mapNotNull { doc ->
                doc.toObject(Invoice::class.java)?.copy(id = doc.id)
            }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyInvoice(ownerId: String, month: Int, year: Int): Result<Invoice?> {
        return try {
            val docs = db.collection("invoices")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .await()
            val invoice = docs.documents.mapNotNull { doc ->
                doc.toObject(Invoice::class.java)?.copy(id = doc.id)
            }.firstOrNull()
            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingInvoices(ownerId: String): Result<List<Invoice>> {
        return try {
            val docs = db.collection("invoices")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("paymentStatus", "PENDING")
                .get()
                .await()
            val invoices = docs.documents.mapNotNull { doc ->
                doc.toObject(Invoice::class.java)?.copy(id = doc.id)
            }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
