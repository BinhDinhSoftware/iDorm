package com.bdsoftware.idorm.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.data.repository.InvoiceRepository
import com.bdsoftware.idorm.core.domain.GetPaymentParamsUseCase
import com.bdsoftware.idorm.core.model.PaymentDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage

data class PaymentUiState(
    val isLoading: Boolean = true,
    val bidvQrBase64: String = "",
    val vcbQrBase64: String = "",
    val amount: Double = 0.0,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val getPaymentParamsUseCase: GetPaymentParamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun loadQrCodes(invoiceId: Int, amount: Double, isEw: Boolean) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState(isLoading = true, amount = amount)
            val result = getPaymentParamsUseCase(invoiceId, amount, isEw)
            result.onSuccess { details ->
                _uiState.value = PaymentUiState(
                    isLoading = false,
                    bidvQrBase64 = details.bidvQrBase64,
                    vcbQrBase64 = details.vcbQrBase64,
                    amount = amount
                )
            }.onFailure { e ->
                _uiState.value = PaymentUiState(
                    isLoading = false,
                    amount = amount,
                    errorMessage = e.toUserMessage("Không thể tải mã QR")
                )
            }
        }
    }

    suspend fun checkPaymentStatus(invoiceId: Int, isEw: Boolean): Boolean {
        return try {
            if (isEw) {
                val list = invoiceRepository.getInvoiceEWs()
                list.any { it.Id == invoiceId && it.IsPayment }
            } else {
                val list = invoiceRepository.getInvoices()
                list.any { it.Id == invoiceId && it.IsPayment }
            }
        } catch (e: Exception) {
            false
        }
    }
}
