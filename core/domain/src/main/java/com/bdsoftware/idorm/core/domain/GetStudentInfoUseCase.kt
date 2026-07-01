package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.UserRepository
import com.bdsoftware.idorm.core.model.StudentProfile
import javax.inject.Inject

/**
 * Use case that fetches student profile info from the API (api/Student/GetStudentInfo).
 * Returns [Result] to let the ViewModel handle success/error states cleanly.
 */
class GetStudentInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<StudentProfile> {
        return try {
            val profile = userRepository.getStudentInfo()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
