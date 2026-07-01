package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.network.model.NetworkHcmcStatisticsData
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcUserRequestItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationReadRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceGroup
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceFormResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRequestDetail
import com.bdsoftware.idorm.core.network.model.NetworkHcmcAttachment
import com.bdsoftware.idorm.core.network.retrofit.RetrofitHcmcNetwork
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcComplaintItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcCancelRequest
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class OfflineFirstHcmcRepository @Inject constructor(
    private val hcmcNetwork: RetrofitHcmcNetwork,
    @ApplicationContext private val context: Context
) : HcmcRepository {

    override suspend fun getStatistics(userId: String): Result<NetworkHcmcStatisticsData> {
        return try {
            val response = hcmcNetwork.getStatistics(userId)
            val statsData = response.data
            if (response.success && statsData != null) {
                Result.success(statsData)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy thống kê yêu cầu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotifications(
        userId: String,
        page: Int,
        limit: Int
    ): Result<List<NetworkHcmcNotificationItem>> {
        return try {
            val response = hcmcNetwork.getNotifications(userId, page, limit)
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy thông báo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserRequests(
        userId: String,
        page: Int,
        limit: Int
    ): Result<List<NetworkHcmcUserRequestItem>> {
        return try {
            val response = hcmcNetwork.getUserRequests(userId, page, limit)
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy danh sách yêu cầu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotificationDetail(notifyId: String): Result<NetworkHcmcNotificationDetailItem> {
        return try {
            val response = hcmcNetwork.getNotificationDetail(NetworkHcmcNotificationDetailRequest(notifyId))
            val detailData = response.data
            if (response.success && detailData != null) {
                Result.success(detailData)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy chi tiết thông báo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun readNotification(userId: Int, notifyId: Int): Result<Unit> {
        return try {
            val response = hcmcNetwork.readNotification(NetworkHcmcNotificationReadRequest(user_id = userId, notify_id = notifyId))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi đánh dấu thông báo đã đọc"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceGroups(): Result<List<NetworkHcmcServiceGroup>> {
        return try {
            val response = hcmcNetwork.getServiceGroups()
            val groupsData = response.data
            if (response.success && groupsData != null) {
                Result.success(groupsData)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy danh sách dịch vụ"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceForm(serviceId: Int): Result<NetworkHcmcServiceFormResponse> {
        return try {
            val response = hcmcNetwork.getServiceForm(serviceId)
            val formData = response.data
            if (response.success && formData != null) {
                Result.success(formData)
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy cấu trúc form"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRequest(
        serviceId: Int,
        userId: String,
        note: String,
        dynamicData: String?,
        attachments: List<SelectedFile>
    ): Result<Unit> {
        return try {
            val requestIdBody = "0".toRequestBody("text/plain".toMediaTypeOrNull())
            val serviceIdBody = serviceId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val noteBody = note.toRequestBody("text/plain".toMediaTypeOrNull())
            val dynamicDataBody = dynamicData?.toRequestBody("text/plain".toMediaTypeOrNull())

            val multipartParts = attachments.mapNotNull { file ->
                try {
                    val inputStream = context.contentResolver.openInputStream(file.uri)
                    val bytes = inputStream?.use { it.readBytes() } ?: return@mapNotNull null
                    val mimeType = context.contentResolver.getType(file.uri) ?: "application/octet-stream"
                    val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("attachment", file.name, requestFile)
                } catch (e: Exception) {
                    null
                }
            }

            val response = hcmcNetwork.createRequest(
                requestId = requestIdBody,
                serviceId = serviceIdBody,
                requestUserId = userIdBody,
                note = noteBody,
                dynamicData = dynamicDataBody,
                attachments = multipartParts.ifEmpty { null }
            )

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Gửi yêu cầu thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRequestDetail(requestId: Int): Result<NetworkHcmcRequestDetail> {
        return try {
            val response = hcmcNetwork.getRequestDetail(requestId)
            val detail = response.data
            if (response.success && detail != null) {
                Result.success(detail)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải chi tiết yêu cầu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReview(
        userId: Int,
        requestId: Int,
        rating: String,
        comments: String
    ): Result<Unit> {
        return try {
            val response = hcmcNetwork.createReview(NetworkHcmcReviewRequest(userId, requestId, rating, comments))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Gửi đánh giá thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReviews(requestId: Int): Result<List<NetworkHcmcReviewItem>> {
        return try {
            val response = hcmcNetwork.getReviews(requestId)
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy danh sách đánh giá"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createComplaint(
        requestId: Int,
        userId: String,
        content: String,
        attachments: List<SelectedFile>
    ): Result<Unit> {
        return try {
            val requestIdBody = requestId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())

            val multipartParts = attachments.mapNotNull { file ->
                try {
                    val inputStream = context.contentResolver.openInputStream(file.uri)
                    val bytes = inputStream?.use { it.readBytes() } ?: return@mapNotNull null
                    val mimeType = context.contentResolver.getType(file.uri) ?: "application/octet-stream"
                    val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("attachment", file.name, requestFile)
                } catch (e: Exception) {
                    null
                }
            }

            val response = hcmcNetwork.createComplaint(
                requestId = requestIdBody,
                userId = userIdBody,
                content = contentBody,
                attachments = multipartParts.ifEmpty { null }
            )

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Gửi khiếu nại thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getComplaints(requestId: Int, userId: Int): Result<List<NetworkHcmcComplaintItem>> {
        return try {
            val response = hcmcNetwork.getComplaints(requestId, userId)
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Lỗi khi lấy danh sách khiếu nại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelRequest(requestId: Int, userId: Int, cancelReason: String): Result<Unit> {
        return try {
            val response = hcmcNetwork.cancelRequest(
                NetworkHcmcCancelRequest(
                    request_id = requestId,
                    user_id = userId,
                    cancel_reason = cancelReason
                )
            )
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Hủy yêu cầu thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRequest(
        requestId: Int,
        userId: String,
        note: String,
        dynamicData: String?,
        removedImageIds: List<Int>,
        newAttachments: List<SelectedFile>
    ): Result<Unit> {
        return try {
            val requestIdBody = requestId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val noteBody = note.toRequestBody("text/plain".toMediaTypeOrNull())
            val isReplaceBody = "false".toRequestBody("text/plain".toMediaTypeOrNull())
            val removedIdsBody = if (removedImageIds.isNotEmpty()) {
                "[${removedImageIds.joinToString(",")}]"
                    .toRequestBody("text/plain".toMediaTypeOrNull())
            } else null
            val dynamicDataBody = dynamicData?.toRequestBody("text/plain".toMediaTypeOrNull())

            val multipartParts = newAttachments.mapNotNull { file ->
                try {
                    val inputStream = context.contentResolver.openInputStream(file.uri)
                    val bytes = inputStream?.use { it.readBytes() } ?: return@mapNotNull null
                    val mimeType = context.contentResolver.getType(file.uri) ?: "application/octet-stream"
                    val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("attachment", file.name, requestFile)
                } catch (e: Exception) {
                    null
                }
            }

            val response = hcmcNetwork.updateRequest(
                requestId = requestIdBody,
                requestUserId = userIdBody,
                note = noteBody,
                isReplaceImages = isReplaceBody,
                removedImageIds = removedIdsBody,
                dynamicData = dynamicDataBody,
                attachments = multipartParts.ifEmpty { null }
            )

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Cập nhật yêu cầu thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
