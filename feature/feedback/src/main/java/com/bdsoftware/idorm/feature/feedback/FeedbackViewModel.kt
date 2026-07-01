package com.bdsoftware.idorm.feature.feedback

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.domain.SubmitFeedbackUseCase
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val tokenManager: IDormPreferencesDataSource,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val email = tokenManager.userEmail.first() ?: ""
                _uiState.update { it.copy(
                    email = email,
                    showEmailField = email.isBlank()
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(showEmailField = true) }
            }
        }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value, descriptionError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(success = false, description = "") }
    }

    fun onAddImage(uri: Uri) {
        if (_uiState.value.images.size >= 5) return
        _uiState.update { it.copy(images = it.images + uri) }
    }

    fun onRemoveImage(uri: Uri) {
        _uiState.update { it.copy(images = it.images.filter { item -> item != uri }) }
    }

    fun submitFeedback(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = application.getString(DesignR.string.validation_description_empty)) }
            return
        }
        if (state.description.length > 500) {
            _uiState.update { it.copy(descriptionError = application.getString(DesignR.string.validation_description_max)) }
            return
        }
        if (state.showEmailField) {
            if (state.email.isBlank()) {
                _uiState.update { it.copy(emailError = application.getString(DesignR.string.validation_email_empty)) }
                return
            }
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
            if (!state.email.matches(emailRegex)) {
                _uiState.update { it.copy(emailError = application.getString(DesignR.string.validation_email_format)) }
                return
            }
        }

        _uiState.update { it.copy(isLoading = true, error = null, success = false) }

        viewModelScope.launch {
            val result = submitFeedbackUseCase(
                description = state.description,
                email = if (state.showEmailField) state.email else null
            )
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, success = true) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.toUserMessage(application.getString(DesignR.string.feedback_error_fallback))) }
            }
        }
    }
}

data class FeedbackUiState(
    val description: String = "",
    val email: String = "",
    val showEmailField: Boolean = false,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val descriptionError: String? = null,
    val emailError: String? = null,
    val images: List<Uri> = emptyList()
)
