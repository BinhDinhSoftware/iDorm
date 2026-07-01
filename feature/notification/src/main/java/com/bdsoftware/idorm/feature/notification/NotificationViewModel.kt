package com.bdsoftware.idorm.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.data.repository.NotificationRepository
import com.bdsoftware.idorm.core.network.model.NotificationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationData>> = notificationRepository.notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAllNotifications()
    }

    private fun fetchAllNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            notificationRepository.getNotifications()
            _isLoading.value = false
        }
    }

    fun refresh() {
        fetchAllNotifications()
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }
}
