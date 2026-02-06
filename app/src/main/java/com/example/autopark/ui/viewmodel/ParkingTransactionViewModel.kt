package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.ParkingTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParkingTransactionViewModel @Inject constructor(
    private val transactionRepository: ParkingTransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<ParkingTransaction>>(emptyList())
    val transactions: StateFlow<List<ParkingTransaction>> = _transactions.asStateFlow()

    private val _activeParking = MutableStateFlow<ParkingTransaction?>(null)
    val activeParking: StateFlow<ParkingTransaction?> = _activeParking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadOwnerTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = transactionRepository.getOwnerTransactions(userId)
                result.onSuccess { trans ->
                    _transactions.value = trans
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load transactions"
                }
            }
            _isLoading.value = false
        }
    }

    fun loadVehicleTransactions(vehicleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = transactionRepository.getVehicleTransactions(vehicleId)
            result.onSuccess { trans ->
                _transactions.value = trans
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load transactions"
            }
            _isLoading.value = false
        }
    }

    fun loadMonthlyTransactions(month: Int, year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = transactionRepository.getMonthlyTransactions(userId, month, year)
                result.onSuccess { trans ->
                    _transactions.value = trans
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load transactions"
                }
            }
            _isLoading.value = false
        }
    }

    fun checkActiveParking(vehicleId: String) {
        viewModelScope.launch {
            val result = transactionRepository.getActiveParkingByVehicle(vehicleId)
            result.onSuccess { transaction ->
                _activeParking.value = transaction
            }.onFailure {
                _activeParking.value = null
            }
        }
    }

    fun addTransaction(transaction: ParkingTransaction) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = transactionRepository.addTransaction(transaction)
            result.onSuccess {
                loadOwnerTransactions()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to add transaction"
            }
            _isLoading.value = false
        }
    }

    fun updateTransaction(transaction: ParkingTransaction) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = transactionRepository.updateTransaction(transaction)
            result.onSuccess {
                loadOwnerTransactions()
                checkActiveParking(transaction.vehicleId)
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update transaction"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun processVehicleEntry(
        vehicleId: String,
        parkingLotId: String = "default_lot",
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // First check if vehicle already has active parking
                val activeResult = transactionRepository.getActiveParkingByVehicle(vehicleId)
                activeResult.getOrNull()?.let { activeTransaction ->
                    callback(Result.success(activeTransaction))
                    return@launch
                }

                // Get vehicle details (you'll need to inject VehicleRepository)
                // For now, create transaction with vehicleId
                val transaction = ParkingTransaction(
                    parkingLotId = parkingLotId,
                    vehicleId = vehicleId,
                    ownerId = "", // Will be filled from vehicle data
                    vehicleNumber = vehicleId, // Simplified for now
                    entryTime = System.currentTimeMillis(),
                    status = "ACTIVE"
                )

                val result = transactionRepository.addTransaction(transaction)
                result.onSuccess { transactionId ->
                    val completedTransaction = transaction.copy(id = transactionId)
                    callback(Result.success(completedTransaction))
                }.onFailure { error ->
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    fun processVehicleExit(
        vehicleId: String,
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val activeResult = transactionRepository.getActiveParkingByVehicle(vehicleId)
                val activeTransaction = activeResult.getOrNull()
                
                if (activeTransaction == null) {
                    callback(Result.failure(Exception("No active parking found for this vehicle")))
                    return@launch
                }

                val exitTime = System.currentTimeMillis()
                val duration = (exitTime - activeTransaction.entryTime) / (1000 * 60) // in minutes

                val updatedTransaction = activeTransaction.copy(
                    exitTime = exitTime,
                    duration = duration,
                    status = "COMPLETED"
                )

                val result = transactionRepository.updateTransaction(updatedTransaction)
                result.onSuccess {
                    callback(Result.success(updatedTransaction))
                }.onFailure { error ->
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
}
