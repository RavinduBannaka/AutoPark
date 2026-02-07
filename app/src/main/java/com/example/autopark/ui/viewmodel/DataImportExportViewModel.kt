package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.*
import com.example.autopark.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataImportExportViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository,
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingRateRepository: ParkingRateRepository,
    private val transactionRepository: ParkingTransactionRepository,
    private val invoiceRepository: InvoiceRepository,
    private val overdueChargeRepository: OverdueChargeRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    data class ExportData(
        val users: List<User>,
        val vehicles: List<Vehicle>,
        val parkingLots: List<ParkingLot>,
        val parkingRates: List<ParkingRate>,
        val transactions: List<ParkingTransaction>,
        val invoices: List<Invoice>,
        val overdueCharges: List<OverdueCharge>
    )

    suspend fun exportAllData(): Result<ExportData> {
        return try {
            _isLoading.value = true
            _progress.value = 0

            _progress.value = 10
            val users = authRepository.getAllUsers().getOrDefault(emptyList())
            
            _progress.value = 25
            val vehicles = vehicleRepository.getAllVehicles().getOrDefault(emptyList())
            
            _progress.value = 40
            val parkingLots = parkingLotRepository.getAllParkingLots().getOrDefault(emptyList())
            
            _progress.value = 55
            val rates = parkingRateRepository.getAllParkingRates().getOrDefault(emptyList())
            
            _progress.value = 70
            val transactions = transactionRepository.getAllTransactions().getOrDefault(emptyList())
            
            _progress.value = 85
            val invoices = invoiceRepository.getAllInvoices().getOrDefault(emptyList())
            
            _progress.value = 100
            val overdueCharges = overdueChargeRepository.getAllOverdueCharges().getOrDefault(emptyList())

            Result.success(
                ExportData(
                    users = users,
                    vehicles = vehicles,
                    parkingLots = parkingLots,
                    parkingRates = rates,
                    transactions = transactions,
                    invoices = invoices,
                    overdueCharges = overdueCharges
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
            _progress.value = 0
        }
    }

    suspend fun importAllData(data: ExportData): ImportResult {
        var successCount = 0
        var errorCount = 0
        val errors = mutableListOf<String>()

        _isLoading.value = true
        _progress.value = 0

        // Import users
        data.users.forEach { user ->
            authRepository.addUser(user)
                .onSuccess { successCount++ }
                .onFailure { 
                    errorCount++
                    errors.add("User ${user.email}: ${it.message}")
                }
        }
        _progress.value = 15

        // Import vehicles
        data.vehicles.forEach { vehicle ->
            vehicleRepository.addVehicle(vehicle)
                .onSuccess { successCount++ }
                .onFailure { 
                    errorCount++
                    errors.add("Vehicle ${vehicle.vehicleNumber}: ${it.message}")
                }
        }
        _progress.value = 30

        // Import parking lots
        data.parkingLots.forEach { lot ->
            parkingLotRepository.addParkingLot(lot)
                .onSuccess { successCount++ }
                .onFailure { 
                    errorCount++
                    errors.add("Lot ${lot.name}: ${it.message}")
                }
        }
        _progress.value = 45

        // Import parking rates
        data.parkingRates.forEach { rate ->
            parkingRateRepository.addParkingRate(rate)
                .onSuccess { successCount++ }
                .onFailure { 
                    errorCount++
                    errors.add("Rate: ${it.message}")
                }
        }
        _progress.value = 60

        // Import transactions
        data.transactions.forEach { transaction ->
            transactionRepository.addTransaction(transaction)
                .onSuccess { successCount++ }
                .onFailure { 
                    errorCount++
                    errors.add("Transaction: ${it.message}")
                }
        }
        _progress.value = 75

        // Import invoices
        data.invoices.forEach { invoice ->
            invoiceRepository.addInvoice(invoice)
                .onSuccess { successCount++ }
                .onFailure { 
                    errorCount++
                    errors.add("Invoice ${invoice.invoiceNumber}: ${it.message}")
                }
        }
        _progress.value = 90

        // Import overdue charges
        data.overdueCharges.forEach { charge ->
            overdueChargeRepository.addOverdueCharge(charge)
                .onSuccess { successCount++ }
                .onFailure { 
                    errorCount++
                    errors.add("Overdue charge: ${it.message}")
                }
        }
        _progress.value = 100

        _isLoading.value = false
        
        return ImportResult(
            successCount = successCount,
            errorCount = errorCount,
            errors = errors
        )
    }

    data class ImportResult(
        val successCount: Int,
        val errorCount: Int,
        val errors: List<String>
    )

    fun showError(message: String) {
        _errorMessage.value = message
    }

    fun showSuccess(message: String) {
        _successMessage.value = message
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
