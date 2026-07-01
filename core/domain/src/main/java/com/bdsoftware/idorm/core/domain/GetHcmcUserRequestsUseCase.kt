package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcUserRequestItem
import javax.inject.Inject

class GetHcmcUserRequestsUseCase @Inject constructor(
    private val hcmcRepository: HcmcRepository
) {
    suspend operator fun invoke(userId: String, page: Int = 1, limit: Int = 20): Result<List<NetworkHcmcUserRequestItem>> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID không hợp lệ"))
        }
        return hcmcRepository.getUserRequests(userId, page, limit)
    }
}
