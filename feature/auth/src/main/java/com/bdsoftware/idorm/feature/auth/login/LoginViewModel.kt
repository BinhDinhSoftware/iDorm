package com.bdsoftware.idorm.feature.auth.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.domain.LoginUseCase
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
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCccdChanged(cccd: String) {
        _uiState.update { it.copy(cccd = cccd, cccdError = null, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validate fields using InputValidator
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
        val passwordResult = InputValidator.validateNotBlank(
            currentState.password,
            application.getString(DesignR.string.validation_password_empty)
        )

        var cccdError: String? = null
        var passwordError: String? = null

        if (cccdResult is ValidationResult.Invalid) {
            cccdError = cccdResult.errorMessage
        }
        if (passwordResult is ValidationResult.Invalid) {
            passwordError = passwordResult.errorMessage
        }

        if (cccdError != null || passwordError != null) {
            _uiState.update { it.copy(cccdError = cccdError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = loginUseCase(currentState.cccd, currentState.password)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage(application.getString(DesignR.string.login_error_fallback))) }
            }
        }
    }
}

data class LoginUiState(
    val cccd: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cccdError: String? = null,
    val passwordError: String? = null
)
