package com.bdsoftware.idorm.feature.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.data.repository.InvoiceRepository
import com.bdsoftware.idorm.core.data.repository.UserRepository
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceEWResponse
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val roomInvoices: StateFlow<List<NetworkInvoiceResponse>> = invoiceRepository.roomInvoices
    val serviceInvoices: StateFlow<List<NetworkInvoiceEWResponse>> = invoiceRepository.serviceInvoices

    private val _studentName = MutableStateFlow("")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchInvoices()
        fetchStudentName()
    }

    private fun fetchStudentName() {
        viewModelScope.launch {
            try {
                val profile = userRepository.getStudentInfo()
                _studentName.value = profile.fullName
            } catch (e: Exception) {
                // Fallback or ignore
            }
        }
    }

    fun fetchInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            invoiceRepository.getInvoices()
            invoiceRepository.getInvoiceEWs()
            _isLoading.value = false
        }
    }
}
