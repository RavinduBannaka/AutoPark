package com.example.autopark.data.repository

import com.example.autopark.data.model.ParkingRate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ParkingRateRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun addParkingRate(rate: ParkingRate): Result<String> {
        return try {
            val docRef = db.collection("parking_rates").add(rate).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateParkingRate(rate: ParkingRate): Result<Unit> {
        return try {
            db.collection("parking_rates").document(rate.id).set(rate).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteParkingRate(rateId: String): Result<Unit> {
        return try {
            db.collection("parking_rates").document(rateId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParkingRate(rateId: String): Result<ParkingRate> {
        return try {
            val doc = db.collection("parking_rates").document(rateId).get().await()
            val rate = doc.toObject(ParkingRate::class.java)
            if (rate != null) {
                Result.success(rate.copy(id = doc.id))
            } else {
                Result.failure(Exception("Parking rate not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatesForLot(parkingLotId: String): Result<List<ParkingRate>> {
        return try {
            val docs = db.collection("parking_rates")
                .whereEqualTo("parkingLotId", parkingLotId)
                .get()
                .await()
            val rates = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingRate::class.java)?.copy(id = doc.id)
            }
            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveRatesForLot(parkingLotId: String): Result<List<ParkingRate>> {
        return try {
            val docs = db.collection("parking_rates")
                .whereEqualTo("parkingLotId", parkingLotId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val rates = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingRate::class.java)?.copy(id = doc.id)
            }
            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRateByType(parkingLotId: String, rateType: String): Result<ParkingRate?> {
        return try {
            val docs = db.collection("parking_rates")
                .whereEqualTo("parkingLotId", parkingLotId)
                .whereEqualTo("rateType", rateType)
                .get()
                .await()
            val rate = docs.documents.mapNotNull { doc ->
                doc.toObject(ParkingRate::class.java)?.copy(id = doc.id)
            }.firstOrNull()
            Result.success(rate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
