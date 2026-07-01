package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.model.FeedbackStatus
import com.bdsoftware.idorm.core.network.firebase.FirebaseFeedbackDataSource
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class OfflineFirstFeedbackRepository @Inject constructor(
    private val feedbackDataSource: FirebaseFeedbackDataSource,
    private val tokenManager: IDormPreferencesDataSource
) : FeedbackRepository {

    override suspend fun submitFeedback(description: String, email: String?): Result<Unit> {
        return try {
            val studentCode = tokenManager.userStudentCode.firstOrNull().orEmpty()
            val studentName = tokenManager.userFullName.firstOrNull().orEmpty()
            val resolvedEmail = if (!email.isNullOrBlank()) email else tokenManager.userEmail.firstOrNull().orEmpty()

            feedbackDataSource.submitFeedback(
                studentCode = studentCode,
                studentName = studentName,
                email = resolvedEmail,
                description = description,
                status = FeedbackStatus.PENDING.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
