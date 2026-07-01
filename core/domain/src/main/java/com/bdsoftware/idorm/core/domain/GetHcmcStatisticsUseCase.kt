package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcStatisticsData
import javax.inject.Inject

class GetHcmcStatisticsUseCase @Inject constructor(
    private val hcmcRepository: HcmcRepository
) {
    suspend operator fun invoke(userId: String): Result<NetworkHcmcStatisticsData> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID không hợp lệ"))
        }
        return hcmcRepository.getStatistics(userId)
    }
}
