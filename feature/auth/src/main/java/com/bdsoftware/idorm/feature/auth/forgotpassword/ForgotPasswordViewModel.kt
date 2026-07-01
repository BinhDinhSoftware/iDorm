package com.bdsoftware.idorm.feature.auth.forgotpassword

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.domain.ForgetPinUseCase
import com.bdsoftware.idorm.feature.auth.util.InputValidator
import com.bdsoftware.idorm.feature.auth.util.ValidationResult
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgetPinUseCase: ForgetPinUseCase,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onCccdChanged(cccd: String) {
        _uiState.update { it.copy(cccd = cccd, cccdError = null, errorMessage = null, successMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null, successMessage = null) }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onDismissSuccess() {
        _uiState.update {
            it.copy(
                successMessage = null,
                cccd = "",
                email = ""
            )
        }
    }

    fun onSendRequestClick() {
        val currentState = _uiState.value

        val cccdResult = when (val notBlank = InputValidator.validateNotBlank(
            currentState.cccd,
            application.getString(DesignR.string.validation_cccd_empty)
        )) {
            is ValidationResult.Invalid -> notBlank
            ValidationResult.Valid -> {
                InputValidator.validateCustomRegex(
                    value = currentState.cccd,
                    regex = "^[0-9]{9,12}$".toRegex(),
                    emptyMessage = application.getString(DesignR.string.validation_cccd_empty),
                    formatMessage = application.getString(DesignR.string.validation_cccd_format)
                )
            }
        }
        val emailResult = InputValidator.validateEmail(
            currentState.email,
            emptyMessage = application.getString(DesignR.string.validation_email_empty),
            formatMessage = application.getString(DesignR.string.validation_email_format)
        )

        var cccdError: String? = null
        var emailError: String? = null

        if (cccdResult is ValidationResult.Invalid) cccdError = cccdResult.errorMessage
        if (emailResult is ValidationResult.Invalid) emailError = emailResult.errorMessage

        if (cccdError != null || emailError != null) {
            _uiState.update { it.copy(cccdError = cccdError, emailError = emailError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            val result = forgetPinUseCase(currentState.email, currentState.cccd)
            result.onSuccess {
                _uiState.update {
                    it.copy(isLoading = false, successMessage = application.getString(DesignR.string.forgot_password_success))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.toUserMessage(application.getString(DesignR.string.forgot_password_error)))
                }
            }
        }
    }
}

data class ForgotPasswordUiState(
    val cccd: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val cccdError: String? = null,
    val emailError: String? = null
)
