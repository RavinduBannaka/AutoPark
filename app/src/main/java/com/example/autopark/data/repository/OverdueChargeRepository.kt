package com.example.autopark.data.repository

import com.example.autopark.data.model.OverdueCharge
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OverdueChargeRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addOverdueCharge(charge: OverdueCharge): Result<String> {
        return try {
            val docRef = db.collection("overdue_charges").add(charge).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOverdueCharge(charge: OverdueCharge): Result<Unit> {
        return try {
            db.collection("overdue_charges").document(charge.id).set(charge).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOverdueCharge(chargeId: String): Result<OverdueCharge> {
        return try {
            val doc = db.collection("overdue_charges").document(chargeId).get().await()
            val charge = doc.toObject(OverdueCharge::class.java)
            if (charge != null) {
                Result.success(charge.copy(id = doc.id))
            } else {
                Result.failure(Exception("Overdue charge not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerOverdueCharges(ownerId: String): Result<List<OverdueCharge>> {
        return try {
            val docs = db.collection("overdue_charges")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            val charges = docs.documents.mapNotNull { doc ->
                doc.toObject(OverdueCharge::class.java)?.copy(id = doc.id)
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingOverdueCharges(ownerId: String): Result<List<OverdueCharge>> {
        return try {
            val docs = db.collection("overdue_charges")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("paymentStatus", "PENDING")
                .get()
                .await()
            val charges = docs.documents.mapNotNull { doc ->
                doc.toObject(OverdueCharge::class.java)?.copy(id = doc.id)
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChargesByInvoice(invoiceId: String): Result<List<OverdueCharge>> {
        return try {
            val docs = db.collection("overdue_charges")
                .whereEqualTo("invoiceId", invoiceId)
                .get()
                .await()
            val charges = docs.documents.mapNotNull { doc ->
                doc.toObject(OverdueCharge::class.java)?.copy(id = doc.id)
            }
            Result.success(charges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
