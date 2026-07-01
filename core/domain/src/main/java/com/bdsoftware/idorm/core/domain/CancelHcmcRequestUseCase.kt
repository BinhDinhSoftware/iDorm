package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import javax.inject.Inject

class CancelHcmcRequestUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(
        requestId: Int,
        userId: Int,
        cancelReason: String
    ): Result<Unit> {
        return repository.cancelRequest(requestId, userId, cancelReason)
    }
}
