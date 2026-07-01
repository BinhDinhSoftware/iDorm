package com.bdsoftware.idorm.feature.hcmc

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.bdsoftware.idorm.core.data.repository.SelectedFile
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.domain.GetHcmcRequestDetailUseCase
import com.bdsoftware.idorm.core.domain.GetHcmcReviewsUseCase
import com.bdsoftware.idorm.core.domain.SubmitHcmcReviewUseCase
import com.bdsoftware.idorm.core.domain.GetHcmcComplaintsUseCase
import com.bdsoftware.idorm.core.domain.SubmitHcmcComplaintUseCase
import com.bdsoftware.idorm.core.domain.CancelHcmcRequestUseCase
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRequestDetail
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcComplaintItem
import com.bdsoftware.idorm.core.network.retrofit.HcmcAuthTokenProvider
import com.bdsoftware.idorm.feature.hcmc.navigation.HcmcRequestDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
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

data class HcmcRequestDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val detail: NetworkHcmcRequestDetail? = null,
    val reviews: List<NetworkHcmcReviewItem> = emptyList(),
    val complaints: List<NetworkHcmcComplaintItem> = emptyList(),
    val isReviewSubmitting: Boolean = false,
    val isComplaintSubmitting: Boolean = false,
    val isCancelling: Boolean = false,
    val submitError: String? = null,
    val submitSuccessMessage: String? = null
)

@HiltViewModel
class HcmcRequestDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHcmcRequestDetailUseCase: GetHcmcRequestDetailUseCase,
    private val getHcmcReviewsUseCase: GetHcmcReviewsUseCase,
    private val submitHcmcReviewUseCase: SubmitHcmcReviewUseCase,
    private val getHcmcComplaintsUseCase: GetHcmcComplaintsUseCase,
    private val submitHcmcComplaintUseCase: SubmitHcmcComplaintUseCase,
    private val cancelHcmcRequestUseCase: CancelHcmcRequestUseCase,
    private val preferencesDataSource: IDormPreferencesDataSource,
    hcmcAuthTokenProvider: HcmcAuthTokenProvider,
    @com.bdsoftware.idorm.core.network.di.HcmcRetrofit val okHttpClient: okhttp3.OkHttpClient
) : ViewModel() {

    private val route = savedStateHandle.toRoute<HcmcRequestDetailRoute>()
    val requestId = route.requestId

    private val _uiState = MutableStateFlow(HcmcRequestDetailUiState())
    val uiState: StateFlow<HcmcRequestDetailUiState> = _uiState.asStateFlow()

    val hcmcAccessToken: StateFlow<String?> = hcmcAuthTokenProvider.hcmcAccessToken
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val detailResult = getHcmcRequestDetailUseCase(requestId)
            val userIdStr = preferencesDataSource.hcmcUserId.firstOrNull() ?: ""
            val userId = userIdStr.toIntOrNull() ?: 0

            val reviewsResult = getHcmcReviewsUseCase(requestId)
            val complaintsResult = getHcmcComplaintsUseCase(requestId, userId)

            detailResult.onSuccess { detailResponse ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        detail = detailResponse,
                        reviews = reviewsResult.getOrNull() ?: emptyList(),
                        complaints = complaintsResult.getOrNull() ?: emptyList()
                    )
                }
            }.onFailure { exception ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = exception.toUserMessage("Không thể tải chi tiết yêu cầu"),
                        reviews = reviewsResult.getOrNull() ?: emptyList(),
                        complaints = complaintsResult.getOrNull() ?: emptyList()
                    )
                }
            }
        }
    }

    fun submitReview(rating: String, comments: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewSubmitting = true, submitError = null, submitSuccessMessage = null) }
            val userIdStr = preferencesDataSource.hcmcUserId.firstOrNull() ?: ""
            val userId = userIdStr.toIntOrNull() ?: 0

            submitHcmcReviewUseCase(userId, requestId, rating, comments)
                .onSuccess {
                    _uiState.update { it.copy(isReviewSubmitting = false, submitSuccessMessage = "Gửi đánh giá thành công") }
                    loadDetail()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isReviewSubmitting = false, submitError = exception.toUserMessage("Gửi đánh giá thất bại")) }
                }
        }
    }

    fun submitComplaint(content: String, attachments: List<SelectedFile>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isComplaintSubmitting = true, submitError = null, submitSuccessMessage = null) }
            val userIdStr = preferencesDataSource.hcmcUserId.firstOrNull() ?: ""

            submitHcmcComplaintUseCase(requestId, userIdStr, content, attachments)
                .onSuccess {
                    _uiState.update { it.copy(isComplaintSubmitting = false, submitSuccessMessage = "Gửi khiếu nại thành công") }
                    loadDetail()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isComplaintSubmitting = false, submitError = exception.toUserMessage("Gửi khiếu nại thất bại")) }
                }
        }
    }

    fun cancelRequest(cancelReason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, submitError = null, submitSuccessMessage = null) }
            val userIdStr = preferencesDataSource.hcmcUserId.firstOrNull() ?: ""
            val userId = userIdStr.toIntOrNull() ?: 0

            cancelHcmcRequestUseCase(requestId, userId, cancelReason)
                .onSuccess {
                    _uiState.update { it.copy(isCancelling = false, submitSuccessMessage = "Hủy yêu cầu thành công") }
                    loadDetail()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isCancelling = false, submitError = exception.toUserMessage("Hủy yêu cầu thất bại")) }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(submitError = null, submitSuccessMessage = null) }
    }
}
