package com.bdsoftware.idorm.feature.hcmc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.domain.GetHcmcStatisticsUseCase
import com.bdsoftware.idorm.core.domain.GetHcmcUserRequestsUseCase
import com.bdsoftware.idorm.core.domain.GetHcmcNotificationsUseCase
import com.bdsoftware.idorm.core.domain.GetHcmcNotificationDetailUseCase
import com.bdsoftware.idorm.core.domain.MarkHcmcNotificationAsReadUseCase
import com.bdsoftware.idorm.core.network.model.NetworkHcmcStatisticsSummary
import com.bdsoftware.idorm.core.network.model.NetworkHcmcUserRequestItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage

@HiltViewModel
class HcmcViewModel @Inject constructor(
    private val getStatisticsUseCase: GetHcmcStatisticsUseCase,
    private val getUserRequestsUseCase: GetHcmcUserRequestsUseCase,
    private val getNotificationsUseCase: GetHcmcNotificationsUseCase,
    private val getNotificationDetailUseCase: GetHcmcNotificationDetailUseCase,
    private val markNotificationAsReadUseCase: MarkHcmcNotificationAsReadUseCase,
    private val preferencesDataSource: IDormPreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(HcmcUiState())
    val uiState: StateFlow<HcmcUiState> = _uiState.asStateFlow()

    private val _notificationsState = MutableStateFlow(HcmcNotificationsState())
    val notificationsState: StateFlow<HcmcNotificationsState> = _notificationsState.asStateFlow()

    init {
        loadData()
    }

    fun loadData(showIndicator: Boolean = true) {
        viewModelScope.launch {
            val userId = preferencesDataSource.hcmcUserId.firstOrNull()
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "Chưa đăng nhập HCMC") }
                return@launch
            }

            if (showIndicator) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            } else {
                _uiState.update { it.copy(error = null) }
            }

            // Tải thống kê
            launch {
                getStatisticsUseCase(userId).onSuccess { data ->
                    _uiState.update { it.copy(
                        summary = data.summary ?: NetworkHcmcStatisticsSummary()
                    ) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.toUserMessage("Lỗi khi lấy thống kê yêu cầu")) }
                }
            }

            // Tải danh sách yêu cầu
            launch {
                getUserRequestsUseCase(userId, page = 1, limit = 50).onSuccess { list ->
                    _uiState.update { it.copy(requests = list, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toUserMessage("Lỗi khi lấy danh sách yêu cầu")) }
                }
            }

            // Tải số lượng chưa đọc
            launch {
                getNotificationsUseCase(userId, page = 1, limit = 100).onSuccess { list ->
                    val unreadCount = list.count { !it.is_read }
                    _uiState.update { it.copy(unreadNotificationsCount = unreadCount) }
                }
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            val userId = preferencesDataSource.hcmcUserId.firstOrNull()
            if (userId.isNullOrBlank()) {
                _notificationsState.update { it.copy(isLoading = false, error = "Chưa đăng nhập HCMC") }
                return@launch
            }

            _notificationsState.update { it.copy(isLoading = true, error = null) }

            getNotificationsUseCase(userId, page = 1, limit = 50).onSuccess { list ->
                _notificationsState.update { it.copy(notifications = list, isLoading = false) }
            }.onFailure { e ->
                _notificationsState.update { it.copy(isLoading = false, error = e.toUserMessage("Lỗi khi lấy thông báo")) }
            }
        }
    }

    fun selectNotification(notification: NetworkHcmcNotificationItem) {
        viewModelScope.launch {
            _notificationsState.update { it.copy(detailLoading = true) }
            val notifyIdStr = notification.id.toString()

            // 1. Fetch details
            getNotificationDetailUseCase(notifyIdStr).onSuccess { detail ->
                _notificationsState.update { it.copy(selectedDetail = detail, detailLoading = false) }

                // 2. Mark as read if not already read
                if (!notification.is_read) {
                    val userIdStr = preferencesDataSource.hcmcUserId.firstOrNull()
                    val userId = userIdStr?.toIntOrNull() ?: 0
                    if (userId > 0) {
                        markNotificationAsReadUseCase(userId, notification.id).onSuccess {
                            // Update local list state to show as read
                            _notificationsState.update { state ->
                                val updatedList = state.notifications.map { item ->
                                    if (item.id == notification.id) item.copy(is_read = true) else item
                                }
                                state.copy(notifications = updatedList)
                            }
                            // Also update unread count in main UI state
                            updateUnreadCount()
                        }
                    }
                }
            }.onFailure { e ->
                _notificationsState.update { it.copy(detailLoading = false, error = e.toUserMessage("Lỗi khi lấy chi tiết thông báo")) }
            }
        }
    }

    fun clearSelectedDetail() {
        _notificationsState.update { it.copy(selectedDetail = null) }
    }

    private suspend fun updateUnreadCount() {
        val userId = preferencesDataSource.hcmcUserId.firstOrNull()
        if (!userId.isNullOrBlank()) {
            getNotificationsUseCase(userId, page = 1, limit = 100).onSuccess { list ->
                val unreadCount = list.count { !it.is_read }
                _uiState.update { it.copy(unreadNotificationsCount = unreadCount) }
            }
        }
    }

    fun refresh(showIndicator: Boolean = true) {
        loadData(showIndicator)
    }
}

data class HcmcUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val summary: NetworkHcmcStatisticsSummary = NetworkHcmcStatisticsSummary(),
    val requests: List<NetworkHcmcUserRequestItem> = emptyList(),
    val unreadNotificationsCount: Int = 0
)

data class HcmcNotificationsState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notifications: List<NetworkHcmcNotificationItem> = emptyList(),
    val detailLoading: Boolean = false,
    val selectedDetail: NetworkHcmcNotificationDetailItem? = null
)
