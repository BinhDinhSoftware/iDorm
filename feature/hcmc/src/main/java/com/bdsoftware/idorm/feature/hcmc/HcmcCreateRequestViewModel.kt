package com.bdsoftware.idorm.feature.hcmc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.domain.GetHcmcServiceGroupsUseCase
import com.bdsoftware.idorm.core.network.model.NetworkHcmcService
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage

@HiltViewModel
class HcmcCreateRequestViewModel @Inject constructor(
    private val getServiceGroupsUseCase: GetHcmcServiceGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HcmcCreateRequestUiState())
    val uiState: StateFlow<HcmcCreateRequestUiState> = _uiState.asStateFlow()

    init {
        loadServiceGroups()
    }

    fun loadServiceGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getServiceGroupsUseCase().onSuccess { groups ->
                _uiState.update { it.copy(serviceGroups = groups, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.toUserMessage("Không thể tải danh sách dịch vụ"), isLoading = false) }
            }
        }
    }

    fun selectGroup(group: NetworkHcmcServiceGroup) {
        _uiState.update {
            it.copy(
                selectedGroup = group,
                selectedService = null // Reset selected service when parent group changes
            )
        }
    }

    fun selectService(service: NetworkHcmcService) {
        _uiState.update { it.copy(selectedService = service) }
    }

    fun submitRequest() {
        val state = _uiState.value
        val group = state.selectedGroup
        val service = state.selectedService
        if (group == null || service == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            // Simulating API call for request creation
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}

data class HcmcCreateRequestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val serviceGroups: List<NetworkHcmcServiceGroup> = emptyList(),
    val selectedGroup: NetworkHcmcServiceGroup? = null,
    val selectedService: NetworkHcmcService? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
