package com.bdsoftware.idorm.feature.account

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.domain.ChangePinUseCase
import com.bdsoftware.idorm.core.domain.ForgetPinUseCase
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
class AccountSettingsViewModel @Inject constructor(
    private val changePinUseCase: ChangePinUseCase,
    private val forgetPinUseCase: ForgetPinUseCase,
    private val application: Application
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow(ChangePasswordUiState())
    val changePasswordState: StateFlow<ChangePasswordUiState> = _changePasswordState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    // --- Change Password Operations ---

    fun onOldPinChanged(value: String) {
        _changePasswordState.update { it.copy(oldPin = value, oldPinError = null) }
    }

    fun onNewPinChanged(value: String) {
        _changePasswordState.update { it.copy(newPin = value, newPinError = null) }
    }

    fun onConfirmPinChanged(value: String) {
        _changePasswordState.update { it.copy(confirmPin = value, confirmPinError = null) }
    }

    fun dismissChangePasswordAlerts() {
        _changePasswordState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun clearChangePasswordForm() {
        _changePasswordState.update { ChangePasswordUiState() }
    }

    fun onChangePinClick(onSuccess: () -> Unit) {
        val state = _changePasswordState.value
        var hasError = false
        var oldPinError: String? = null
        var newPinError: String? = null
        var confirmPinError: String? = null

        if (state.oldPin.isBlank()) {
            oldPinError = application.getString(DesignR.string.validation_old_password_empty)
            hasError = true
        }
        if (state.newPin.isBlank()) {
            newPinError = application.getString(DesignR.string.validation_new_password_empty)
            hasError = true
        }
        if (state.confirmPin.isBlank()) {
            confirmPinError = application.getString(DesignR.string.validation_confirm_password_empty)
            hasError = true
        } else if (state.newPin != state.confirmPin) {
            confirmPinError = application.getString(DesignR.string.validation_password_mismatch)
            hasError = true
        }

        if (hasError) {
            _changePasswordState.update {
                it.copy(
                    oldPinError = oldPinError,
                    newPinError = newPinError,
                    confirmPinError = confirmPinError
                )
            }
            return
        }

        _changePasswordState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = changePinUseCase(
                oldPin = state.oldPin,
                newPin = state.newPin,
                confirmPin = state.confirmPin
            )
            result.onSuccess {
                _changePasswordState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = application.getString(DesignR.string.change_password_success)
                    )
                }
                onSuccess()
            }.onFailure { error ->
                _changePasswordState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.toUserMessage(application.getString(DesignR.string.change_password_error))
                    )
                }
            }
        }
    }

    // --- Forgot Password Operations ---

    fun onCccdChanged(value: String) {
        _forgotPasswordState.update { it.copy(cccd = value, cccdError = null) }
    }

    fun onEmailChanged(value: String) {
        _forgotPasswordState.update { it.copy(email = value, emailError = null) }
    }

    fun dismissForgotPasswordAlerts() {
        _forgotPasswordState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun clearForgotPasswordForm() {
        _forgotPasswordState.update { ForgotPasswordUiState() }
    }

    fun onForgetPinClick(onSuccess: () -> Unit) {
        val state = _forgotPasswordState.value
        var hasError = false
        var cccdError: String? = null
        var emailError: String? = null

        if (state.cccd.isBlank()) {
            cccdError = application.getString(DesignR.string.validation_cccd_empty)
            hasError = true
        } else if (!state.cccd.matches("^[0-9]{9,12}$".toRegex())) {
            cccdError = application.getString(DesignR.string.validation_cccd_format)
            hasError = true
        }

        if (state.email.isBlank()) {
            emailError = application.getString(DesignR.string.validation_email_empty)
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            emailError = application.getString(DesignR.string.validation_email_invalid)
            hasError = true
        }

        if (hasError) {
            _forgotPasswordState.update {
                it.copy(
                    cccdError = cccdError,
                    emailError = emailError
                )
            }
            return
        }

        _forgotPasswordState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = forgetPinUseCase(
                email = state.email,
                studentCode = state.cccd
            )
            result.onSuccess {
                _forgotPasswordState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = application.getString(DesignR.string.forgot_password_success)
                    )
                }
                onSuccess()
            }.onFailure { error ->
                _forgotPasswordState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.toUserMessage(application.getString(DesignR.string.forgot_password_error))
                    )
                }
            }
        }
    }
}

data class ChangePasswordUiState(
    val oldPin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val oldPinError: String? = null,
    val newPinError: String? = null,
    val confirmPinError: String? = null
)

data class ForgotPasswordUiState(
    val cccd: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val cccdError: String? = null,
    val emailError: String? = null
)
