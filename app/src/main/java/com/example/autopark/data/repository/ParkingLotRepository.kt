package com.example.autopark.data.repository

import com.example.autopark.data.model.ParkingLot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkingLotRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addParkingLot(lot: ParkingLot): Result<String> {
        return try {
            val docRef = db.collection("parking_lots").add(lot).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateParkingLot(lot: ParkingLot): Result<Unit> {
        return try {
            db.collection("parking_lots").document(lot.id).set(lot).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteParkingLot(lotId: String): Result<Unit> {
        return try {
            db.collection("parking_lots").document(lotId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParkingLot(lotId: String): Result<ParkingLot> {
        return try {
            val doc = db.collection("parking_lots").document(lotId).get().await()
            val lot = doc.toObject(ParkingLot::class.java)
            if (lot != null) {
                Result.success(lot.copy(id = doc.id))
            } else {
                Result.failure(Exception("Parking lot not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllParkingLots(): Result<List<ParkingLot>> {
        return try {
            val docs = db.collection("parking_lots").get().await()
            val lots = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingLot::class.java)?.copy(id = doc.id)
            }
            Result.success(lots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParkingLotsByCity(city: String): Result<List<ParkingLot>> {
        return try {
            val docs = db.collection("parking_lots")
                .whereEqualTo("city", city)
                .get()
                .await()
            val lots = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingLot::class.java)?.copy(id = doc.id)
            }
            Result.success(lots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvailableSpots(lotId: String, availableSpots: Int): Result<Unit> {
        return try {
            db.collection("parking_lots").document(lotId)
                .update("availableSpots", availableSpots).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
