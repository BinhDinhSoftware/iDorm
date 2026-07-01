package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewItem
import javax.inject.Inject

class GetHcmcReviewsUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(requestId: Int): Result<List<NetworkHcmcReviewItem>> {
        return repository.getReviews(requestId)
    }
}
