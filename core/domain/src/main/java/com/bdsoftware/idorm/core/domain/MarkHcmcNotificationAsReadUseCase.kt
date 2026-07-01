package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import javax.inject.Inject

class MarkHcmcNotificationAsReadUseCase @Inject constructor(
    private val hcmcRepository: HcmcRepository
) {
    suspend operator fun invoke(userId: Int, notifyId: Int): Result<Unit> {
        if (userId <= 0 || notifyId <= 0) {
            return Result.failure(IllegalArgumentException("Thông số không hợp lệ"))
        }
        return hcmcRepository.readNotification(userId, notifyId)
    }
}
