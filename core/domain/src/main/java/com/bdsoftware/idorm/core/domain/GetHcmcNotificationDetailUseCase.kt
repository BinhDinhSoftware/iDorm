package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailItem
import javax.inject.Inject

class GetHcmcNotificationDetailUseCase @Inject constructor(
    private val hcmcRepository: HcmcRepository
) {
    suspend operator fun invoke(notifyId: String): Result<NetworkHcmcNotificationDetailItem> {
        if (notifyId.isBlank()) {
            return Result.failure(IllegalArgumentException("Notification ID không hợp lệ"))
        }
        return hcmcRepository.getNotificationDetail(notifyId)
    }
}
