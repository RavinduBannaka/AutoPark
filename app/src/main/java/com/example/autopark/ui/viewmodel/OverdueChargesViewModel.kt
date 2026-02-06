package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.OverdueCharge
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.OverdueChargeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverdueChargesViewModel @Inject constructor(
    private val overdueChargeRepository: OverdueChargeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _overdueCharges = MutableStateFlow<List<OverdueCharge>>(emptyList())
    val overdueCharges: StateFlow<List<OverdueCharge>> = _overdueCharges.asStateFlow()

    private val _totalOverdue = MutableStateFlow(0.0)
    val totalOverdue: StateFlow<Double> = _totalOverdue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadOverdueCharges() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = overdueChargeRepository.getPendingOverdueCharges(userId)
                result.onSuccess { charges ->
                    _overdueCharges.value = charges
                    _totalOverdue.value = charges.sumOf { it.totalDueAmount }
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load overdue charges"
                }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
