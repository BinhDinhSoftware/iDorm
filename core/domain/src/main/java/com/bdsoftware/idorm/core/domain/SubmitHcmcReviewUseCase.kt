package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import javax.inject.Inject

class SubmitHcmcReviewUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(userId: Int, requestId: Int, rating: String, comments: String): Result<Unit> {
        return repository.createReview(userId, requestId, rating, comments)
    }
}
