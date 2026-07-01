package com.bdsoftware.idorm.core.data.repository

interface FeedbackRepository {
    suspend fun submitFeedback(description: String, email: String? = null): Result<Unit>
}
