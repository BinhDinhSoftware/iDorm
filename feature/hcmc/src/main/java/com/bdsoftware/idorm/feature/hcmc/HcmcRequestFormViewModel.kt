package com.bdsoftware.idorm.feature.hcmc

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.bdsoftware.idorm.core.data.repository.SelectedFile
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.domain.GetHcmcServiceFormUseCase
import com.bdsoftware.idorm.core.domain.SubmitHcmcRequestUseCase
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceFormResponse
import com.bdsoftware.idorm.feature.hcmc.navigation.HcmcRequestFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

data class HcmcRequestFormUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val form: NetworkHcmcServiceFormResponse? = null,
    val fieldValues: Map<Int, Any> = emptyMap(),
    val requestDetails: String = "",
    val images: List<Uri> = emptyList(),
    val files: List<SelectedFile> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)

@HiltViewModel
class HcmcRequestFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHcmcServiceFormUseCase: GetHcmcServiceFormUseCase,
    private val submitHcmcRequestUseCase: SubmitHcmcRequestUseCase,
    private val preferencesDataSource: IDormPreferencesDataSource
) : ViewModel() {

    private val route = savedStateHandle.toRoute<HcmcRequestFormRoute>()
    private val serviceId = route.serviceId

    private val _uiState = MutableStateFlow(HcmcRequestFormUiState())
    val uiState: StateFlow<HcmcRequestFormUiState> = _uiState.asStateFlow()

    init {
        loadServiceForm()
    }

    fun loadServiceForm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getHcmcServiceFormUseCase(serviceId)
                .onSuccess { formResponse ->
                    _uiState.update { it.copy(isLoading = false, form = formResponse) }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.toUserMessage("Không thể tải cấu trúc form")) }
                }
        }
    }

    fun onFieldValueChange(fieldId: Int, value: Any) {
        _uiState.update {
            val updatedValues = it.fieldValues.toMutableMap()
            updatedValues[fieldId] = value
            it.copy(fieldValues = updatedValues)
        }
    }

    fun onRequestDetailsChange(text: String) {
        _uiState.update { it.copy(requestDetails = text) }
    }

    fun onAddImage(uri: Uri, size: Long): String? {
        if (size > 3145728) {
            return "Dung lượng ảnh vượt quá 3MB"
        }
        if (_uiState.value.images.size >= 5) {
            return "Chỉ cho phép đính kèm tối đa 5 hình ảnh"
        }
        _uiState.update {
            it.copy(images = it.images + uri)
        }
        return null
    }

    fun onRemoveImage(uri: Uri) {
        _uiState.update {
            it.copy(images = it.images.filter { item -> item != uri })
        }
    }

    fun onAddFile(uri: Uri, name: String, size: Long): String? {
        if (size > 3145728) {
            return "Dung lượng tệp vượt quá 3MB"
        }
        if (_uiState.value.files.size >= 3) {
            return "Chỉ cho phép đính kèm tối đa 3 tệp tin"
        }
        val file = SelectedFile(uri, name, size)
        _uiState.update {
            it.copy(files = it.files + file)
        }
        return null
    }

    fun onRemoveFile(uri: Uri) {
        _uiState.update {
            it.copy(files = it.files.filter { item -> item.uri != uri })
        }
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value
        val form = state.form ?: return false

        if (state.requestDetails.isBlank()) return false

        for (field in form.fields) {
            if (field.required) {
                val value = state.fieldValues[field.id]
                if (value == null) return false
                when (field.type) {
                    "textarea", "text", "date" -> {
                        if ((value as? String).isNullOrBlank()) return false
                    }
                    "checkbox" -> {
                        if (value as? Boolean != true) return false
                    }
                    "select_multi", "date_multi" -> {
                        val list = value as? List<*>
                        if (list.isNullOrEmpty()) return false
                    }
                    "select" -> {
                        if (value !is Int) return false
                    }
                }
            }
        }
        return true
    }

    fun submitRequest() {
        if (!isFormValid()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val userId = preferencesDataSource.hcmcUserId.firstOrNull()
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(isSubmitting = false, error = "Chưa đăng nhập HCMC") }
                return@launch
            }

            val form = _uiState.value.form
            val dynamicDataJson = if (form != null && _uiState.value.fieldValues.isNotEmpty()) {
                try {
                    buildJsonObject {
                        form.fields.forEach { field ->
                            val value = _uiState.value.fieldValues[field.id]
                            val isMulti = field.type.contains("multi")
                            if (isMulti) {
                                val list = value as? List<*> ?: emptyList<Any>()
                                put(field.name, buildJsonArray {
                                    list.forEach { item ->
                                        when (item) {
                                            is String -> add(item)
                                            is Number -> add(item)
                                            is Boolean -> add(item)
                                        }
                                    }
                                })
                            } else {
                                if (value != null) {
                                    when (value) {
                                        is String -> put(field.name, value)
                                        is Boolean -> put(field.name, value)
                                        is Number -> put(field.name, value)
                                    }
                                }
                            }
                        }
                    }.toString()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            val allAttachments = mutableListOf<SelectedFile>()
            _uiState.value.images.forEachIndexed { index, uri ->
                allAttachments.add(SelectedFile(uri, "image_$index.jpg", 0))
            }
            allAttachments.addAll(_uiState.value.files)

            submitHcmcRequestUseCase(
                serviceId = serviceId,
                userId = userId,
                note = _uiState.value.requestDetails,
                dynamicData = dynamicDataJson,
                attachments = allAttachments
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isSubmitting = false, error = exception.toUserMessage("Gửi yêu cầu thất bại")) }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
