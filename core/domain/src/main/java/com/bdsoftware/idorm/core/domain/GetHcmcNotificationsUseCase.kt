package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationItem
import javax.inject.Inject

class GetHcmcNotificationsUseCase @Inject constructor(
    private val hcmcRepository: HcmcRepository
) {
    suspend operator fun invoke(userId: String, page: Int = 1, limit: Int = 20): Result<List<NetworkHcmcNotificationItem>> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID không hợp lệ"))
        }
        return hcmcRepository.getNotifications(userId, page, limit)
    }
}
