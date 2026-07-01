package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.FeedbackRepository
import javax.inject.Inject

class SubmitFeedbackUseCase @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) {
    suspend operator fun invoke(description: String, email: String? = null): Result<Unit> {
        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("Mô tả chi tiết không được để trống"))
        }
        if (description.length > 500) {
            return Result.failure(IllegalArgumentException("Mô tả không được vượt quá 500 ký tự"))
        }
        return feedbackRepository.submitFeedback(description, email)
    }
}
