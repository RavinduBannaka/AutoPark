package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.Vehicle
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadOwnerVehicles()
    }

    fun loadOwnerVehicles() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = vehicleRepository.getOwnerVehicles(userId)
                result.onSuccess { vehicles ->
                    _vehicles.value = vehicles
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load vehicles"
                }
            }
            _isLoading.value = false
        }
    }

    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vehicleRepository.addVehicle(vehicle)
            result.onSuccess {
                loadOwnerVehicles()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to add vehicle"
            }
            _isLoading.value = false
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vehicleRepository.updateVehicle(vehicle)
            result.onSuccess {
                loadOwnerVehicles()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update vehicle"
            }
            _isLoading.value = false
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vehicleRepository.deleteVehicle(vehicleId)
            result.onSuccess {
                loadOwnerVehicles()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to delete vehicle"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
