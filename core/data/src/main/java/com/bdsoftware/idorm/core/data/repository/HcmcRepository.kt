package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.network.model.NetworkHcmcStatisticsData
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcUserRequestItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceGroup
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceFormResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRequestDetail
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcComplaintItem

import android.net.Uri

data class SelectedFile(
    val uri: Uri,
    val name: String,
    val size: Long
)

interface HcmcRepository {
    suspend fun getStatistics(userId: String): Result<NetworkHcmcStatisticsData>
    suspend fun getNotifications(userId: String, page: Int, limit: Int): Result<List<NetworkHcmcNotificationItem>>
    suspend fun getUserRequests(userId: String, page: Int, limit: Int): Result<List<NetworkHcmcUserRequestItem>>
    suspend fun getNotificationDetail(notifyId: String): Result<NetworkHcmcNotificationDetailItem>
    suspend fun readNotification(userId: Int, notifyId: Int): Result<Unit>
    suspend fun getServiceGroups(): Result<List<NetworkHcmcServiceGroup>>
    suspend fun getServiceForm(serviceId: Int): Result<NetworkHcmcServiceFormResponse>
    suspend fun createRequest(
        serviceId: Int,
        userId: String,
        note: String,
        dynamicData: String?,
        attachments: List<SelectedFile>
    ): Result<Unit>

    suspend fun getRequestDetail(requestId: Int): Result<NetworkHcmcRequestDetail>

    suspend fun createReview(userId: Int, requestId: Int, rating: String, comments: String): Result<Unit>
    suspend fun getReviews(requestId: Int): Result<List<NetworkHcmcReviewItem>>
    suspend fun createComplaint(requestId: Int, userId: String, content: String, attachments: List<SelectedFile>): Result<Unit>
    suspend fun getComplaints(requestId: Int, userId: Int): Result<List<NetworkHcmcComplaintItem>>
    suspend fun updateRequest(
        requestId: Int,
        userId: String,
        note: String,
        dynamicData: String?,
        removedImageIds: List<Int>,
        newAttachments: List<SelectedFile>
    ): Result<Unit>

    suspend fun cancelRequest(requestId: Int, userId: Int, cancelReason: String): Result<Unit>
}

