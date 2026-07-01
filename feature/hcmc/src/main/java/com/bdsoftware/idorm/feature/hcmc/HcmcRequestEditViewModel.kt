package com.bdsoftware.idorm.feature.hcmc

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.bdsoftware.idorm.core.data.repository.SelectedFile
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.domain.GetHcmcServiceFormUseCase
import com.bdsoftware.idorm.core.domain.UpdateHcmcRequestUseCase
import com.bdsoftware.idorm.core.network.model.NetworkHcmcAttachment
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRequestDetail
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceFormResponse
import com.bdsoftware.idorm.feature.hcmc.navigation.HcmcRequestEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage
import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.retrofit.HcmcAuthTokenProvider
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

data class HcmcRequestEditUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val form: NetworkHcmcServiceFormResponse? = null,
    val fieldValues: Map<Int, Any> = emptyMap(),
    val requestDetails: String = "",
    // Ảnh cũ đã lưu trên server
    val existingImages: List<NetworkHcmcAttachment> = emptyList(),
    // ID ảnh cũ bị xóa (gửi lên API)
    val removedImageIds: List<Int> = emptyList(),
    // Ảnh mới thêm từ thiết bị
    val newImages: List<Uri> = emptyList(),
    val files: List<SelectedFile> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val submitError: String? = null
)

@HiltViewModel
class HcmcRequestEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHcmcServiceFormUseCase: GetHcmcServiceFormUseCase,
    private val updateHcmcRequestUseCase: UpdateHcmcRequestUseCase,
    private val repository: HcmcRepository,
    private val preferencesDataSource: IDormPreferencesDataSource,
    hcmcAuthTokenProvider: HcmcAuthTokenProvider,
    @com.bdsoftware.idorm.core.network.di.HcmcRetrofit val okHttpClient: okhttp3.OkHttpClient
) : ViewModel() {

    private val route = savedStateHandle.toRoute<HcmcRequestEditRoute>()
    private val requestId = route.requestId
    private val serviceId = route.serviceId

    private val _uiState = MutableStateFlow(HcmcRequestEditUiState())
    val uiState: StateFlow<HcmcRequestEditUiState> = _uiState.asStateFlow()

    val hcmcAccessToken: StateFlow<String?> = hcmcAuthTokenProvider.hcmcAccessToken
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val formDeferred = async { getHcmcServiceFormUseCase(serviceId) }
            val detailDeferred = async { repository.getRequestDetail(requestId) }

            val formResult = formDeferred.await()
            val detailResult = detailDeferred.await()

            when {
                formResult.isFailure -> {
                    _uiState.update { it.copy(isLoading = false, error = formResult.exceptionOrNull()?.toUserMessage("Không thể tải form")) }
                }
                detailResult.isFailure -> {
                    _uiState.update { it.copy(isLoading = false, error = detailResult.exceptionOrNull()?.toUserMessage("Không thể tải thông tin yêu cầu")) }
                }
                else -> {
                    val form = formResult.getOrNull()!!
                    val detail = detailResult.getOrNull()!!
                    val prefilled = prefillFromDetail(form, detail)
                    _uiState.update { prefilled.copy(isLoading = false) }
                }
            }
        }
    }

    private fun prefillFromDetail(
        form: NetworkHcmcServiceFormResponse,
        detail: NetworkHcmcRequestDetail
    ): HcmcRequestEditUiState {
        val fieldValues = mutableMapOf<Int, Any>()

        detail.inputs?.forEach { input ->
            val field = form.fields.find { it.name == input.name } ?: return@forEach
            val rawValue = input.value_display ?: return@forEach

            val parsedValue: Any? = when (field.type) {
                "textarea", "text" -> rawValue

                "date" -> parseDisplayDateToIso(rawValue)

                "date_multi" -> rawValue.split(", ").map { it.trim() }.mapNotNull { parseDisplayDateToIso(it) }

                "select" -> {
                    val trimmed = rawValue.trim()
                    val idFromNumber = trimmed.toIntOrNull()
                    if (idFromNumber != null && field.options.any { it.id == idFromNumber }) {
                        idFromNumber
                    } else {
                        field.options.find { it.name.trim().equals(trimmed, ignoreCase = true) }?.id
                    }
                }

                "select_multi" -> {
                    val trimmedVal = rawValue.trim()
                    if (trimmedVal.startsWith("[") && trimmedVal.endsWith("]")) {
                        val ids = trimmedVal.replace("[", "").replace("]", "")
                            .split(",")
                            .map { it.trim().toIntOrNull() }
                            .filterNotNull()
                        ids.filter { id -> field.options.any { it.id == id } }
                    } else if (trimmedVal.split(",").all { it.trim().toIntOrNull() != null }) {
                        trimmedVal.split(",")
                            .map { it.trim().toIntOrNull() }
                            .filterNotNull()
                            .filter { id -> field.options.any { it.id == id } }
                    } else {
                        val sortedOptions = field.options.sortedByDescending { it.name.length }
                        val matchedIds = mutableListOf<Int>()
                        var remainingText = trimmedVal
                        
                        sortedOptions.forEach { opt ->
                            val optName = opt.name.trim()
                            if (optName.isNotEmpty() && remainingText.contains(optName, ignoreCase = true)) {
                                matchedIds.add(opt.id)
                                val index = remainingText.indexOf(optName, ignoreCase = true)
                                if (index != -1) {
                                    remainingText = remainingText.substring(0, index) + 
                                            remainingText.substring(index + optName.length)
                                }
                            }
                        }
                        matchedIds
                    }
                }

                "checkbox" -> rawValue.lowercase() == "true" || rawValue == "1"

                else -> null
            }

            if (parsedValue != null) {
                fieldValues[field.id] = parsedValue
            }
        }

        return HcmcRequestEditUiState(
            form = form,
            fieldValues = fieldValues,
            requestDetails = detail.note ?: "",
            existingImages = detail.image_attachment_ids ?: emptyList()
        )
    }

    private fun parseDisplayDateToIso(displayDate: String): String? {
        val parts = displayDate.trim().split("/")
        if (parts.size == 3) {
            val day = parts[0].padStart(2, '0')
            val month = parts[1].padStart(2, '0')
            val year = parts[2]
            return "$year-$month-$day"
        }
        if (displayDate.contains("-") && displayDate.split("-").size == 3) {
            return displayDate.trim()
        }
        return null
    }

    fun onFieldValueChange(fieldId: Int, value: Any) {
        _uiState.update {
            val updated = it.fieldValues.toMutableMap().also { m -> m[fieldId] = value }
            it.copy(fieldValues = updated)
        }
    }

    fun onRequestDetailsChange(text: String) {
        _uiState.update { it.copy(requestDetails = text) }
    }

    // Xóa ảnh cũ (từ server)
    fun onRemoveExistingImage(id: Int) {
        _uiState.update {
            it.copy(
                existingImages = it.existingImages.filterNot { img -> img.id == id },
                removedImageIds = it.removedImageIds + id
            )
        }
    }

    // Thêm ảnh mới từ thiết bị
    fun onAddNewImage(uri: Uri, size: Long): String? {
        if (size > 3145728) return "Dung lượng ảnh vượt quá 3MB"
        val total = _uiState.value.existingImages.size + _uiState.value.newImages.size
        if (total >= 5) return "Chỉ cho phép đính kèm tối đa 5 hình ảnh"
        _uiState.update { it.copy(newImages = it.newImages + uri) }
        return null
    }

    fun onRemoveNewImage(uri: Uri) {
        _uiState.update { it.copy(newImages = it.newImages.filterNot { u -> u == uri }) }
    }

    fun onAddFile(uri: Uri, name: String, size: Long): String? {
        if (size > 3145728) return "Dung lượng tệp vượt quá 3MB"
        if (_uiState.value.files.size >= 3) return "Chỉ cho phép đính kèm tối đa 3 tệp tin"
        _uiState.update { it.copy(files = it.files + SelectedFile(uri, name, size)) }
        return null
    }

    fun onRemoveFile(uri: Uri) {
        _uiState.update { it.copy(files = it.files.filterNot { f -> f.uri != uri }) }
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value
        val form = state.form ?: return false
        if (state.requestDetails.isBlank()) return false
        for (field in form.fields) {
            if (field.required) {
                val value = state.fieldValues[field.id] ?: return false
                when (field.type) {
                    "textarea", "text", "date" -> if ((value as? String).isNullOrBlank()) return false
                    "checkbox" -> if (value as? Boolean != true) return false
                    "select_multi", "date_multi" -> if ((value as? List<*>).isNullOrEmpty()) return false
                    "select" -> if (value !is Int) return false
                }
            }
        }
        return true
    }

    fun submitUpdate() {
        if (!isFormValid()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val userId = preferencesDataSource.hcmcUserId.firstOrNull()
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(isSubmitting = false, submitError = "Chưa đăng nhập HCMC") }
                return@launch
            }

            val state = _uiState.value
            val form = state.form

            val dynamicDataJson = if (form != null && state.fieldValues.isNotEmpty()) {
                try {
                    buildJsonObject {
                        form.fields.forEach { field ->
                            val value = state.fieldValues[field.id]
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
                            } else if (value != null) {
                                when (value) {
                                    is String -> put(field.name, value)
                                    is Boolean -> put(field.name, value)
                                    is Number -> put(field.name, value)
                                }
                            }
                        }
                    }.toString()
                } catch (e: Exception) { null }
            } else null

            val newAttachments = mutableListOf<SelectedFile>()
            state.newImages.forEachIndexed { index, uri ->
                newAttachments.add(SelectedFile(uri, "image_$index.jpg", 0))
            }
            newAttachments.addAll(state.files)

            updateHcmcRequestUseCase(
                requestId = requestId,
                userId = userId,
                note = state.requestDetails,
                dynamicData = dynamicDataJson,
                removedImageIds = state.removedImageIds,
                newAttachments = newAttachments
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isSubmitting = false, submitError = exception.toUserMessage("Cập nhật yêu cầu thất bại")) }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(submitError = null) }
    }
}
