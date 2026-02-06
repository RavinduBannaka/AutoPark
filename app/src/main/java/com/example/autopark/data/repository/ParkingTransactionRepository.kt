package com.example.autopark.data.repository

import com.example.autopark.data.model.ParkingTransaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkingTransactionRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addTransaction(transaction: ParkingTransaction): Result<String> {
        return try {
            val docRef = db.collection("parking_transactions").add(transaction).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: ParkingTransaction): Result<Unit> {
        return try {
            db.collection("parking_transactions").document(transaction.id)
                .set(transaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransaction(transactionId: String): Result<ParkingTransaction> {
        return try {
            val doc = db.collection("parking_transactions").document(transactionId).get().await()
            val transaction = doc.toObject(ParkingTransaction::class.java)
            if (transaction != null) {
                Result.success(transaction.copy(id = doc.id))
            } else {
                Result.failure(Exception("Transaction not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerTransactions(ownerId: String): Result<List<ParkingTransaction>> {
        return try {
            val docs = db.collection("parking_transactions")
                .whereEqualTo("ownerId", ownerId)
                .orderBy("entryTime")
                .get()
                .await()
            val transactions = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingTransaction::class.java)?.copy(id = doc.id)
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVehicleTransactions(vehicleId: String): Result<List<ParkingTransaction>> {
        return try {
            val docs = db.collection("parking_transactions")
                .whereEqualTo("vehicleId", vehicleId)
                .orderBy("entryTime")
                .get()
                .await()
            val transactions = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingTransaction::class.java)?.copy(id = doc.id)
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveParkingByVehicle(vehicleId: String): Result<ParkingTransaction?> {
        return try {
            val docs = db.collection("parking_transactions")
                .whereEqualTo("vehicleId", vehicleId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            val transaction = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingTransaction::class.java)?.copy(id = doc.id)
            }.firstOrNull()
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyTransactions(ownerId: String, month: Int, year: Int): Result<List<ParkingTransaction>> {
        return try {
            // Get start and end of month
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month - 1, 1, 0, 0, 0)
            val startOfMonth = calendar.timeInMillis
            calendar.set(year, month, 1, 0, 0, 0)
            val startOfNextMonth = calendar.timeInMillis

            val docs = db.collection("parking_transactions")
                .whereEqualTo("ownerId", ownerId)
                .whereGreaterThanOrEqualTo("entryTime", startOfMonth)
                .whereLessThan("entryTime", startOfNextMonth)
                .get()
                .await()
            
            val transactions = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingTransaction::class.java)?.copy(id = doc.id)
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLotTransactions(lotId: String): Result<List<ParkingTransaction>> {
        return try {
            val docs = db.collection("parking_transactions")
                .whereEqualTo("parkingLotId", lotId)
                .orderBy("entryTime")
                .get()
                .await()
            val transactions = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingTransaction::class.java)?.copy(id = doc.id)
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyLotTransactions(lotId: String, month: Int, year: Int): Result<List<ParkingTransaction>> {
        return try {
            // Get start and end of month
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month - 1, 1, 0, 0, 0)
            val startOfMonth = calendar.timeInMillis
            calendar.set(year, month, 1, 0, 0, 0)
            val startOfNextMonth = calendar.timeInMillis

            val docs = db.collection("parking_transactions")
                .whereEqualTo("parkingLotId", lotId)
                .whereGreaterThanOrEqualTo("entryTime", startOfMonth)
                .whereLessThan("entryTime", startOfNextMonth)
                .get()
                .await()
            
            val transactions = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingTransaction::class.java)?.copy(id = doc.id)
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
