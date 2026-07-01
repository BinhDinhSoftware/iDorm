package com.bdsoftware.idorm.core.data.repository

import android.util.Log
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.network.model.NotificationData
import com.bdsoftware.idorm.core.network.model.NetworkUpdateNotificationRequest
import com.bdsoftware.idorm.core.network.retrofit.RetrofitDefaultNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val defaultNetwork: RetrofitDefaultNetwork,
    private val tokenManager: IDormPreferencesDataSource
) {
    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications: StateFlow<List<NotificationData>> = _notifications.asStateFlow()

    val unreadCount: Flow<Int> = notifications.map { list ->
        list.count { !it.IsRead }
    }

    suspend fun getNotifications(limit: Int? = null): List<NotificationData> {
        return try {
            val response = defaultNetwork.getNotifications()
            val sorted = response.sortedByDescending { it.CreatedDate }
            _notifications.value = sorted
            if (limit != null) sorted.take(limit) else sorted
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error fetching notifications", e)
            emptyList()
        }
    }

    suspend fun markAsRead(notificationId: Int) {
        try {
            val userId = tokenManager.userId.firstOrNull()
            defaultNetwork.updateNotification(
                NetworkUpdateNotificationRequest(
                    NotificationId = notificationId.toString(),
                    NotificationStudentId = userId
                )
            )
            val updated = _notifications.value.map {
                if (it.Id == notificationId) it.copy(IsRead = true) else it
            }
            _notifications.value = updated
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking notification as read", e)
        }
    }
}

