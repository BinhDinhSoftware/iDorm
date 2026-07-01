package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.AuthRepository
import javax.inject.Inject

class ForgetPinUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, studentCode: String): Result<Unit> {
        if (studentCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Số CCCD không được để trống"))
        }
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email không được để trống"))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("Định dạng email không hợp lệ"))
        }
        return authRepository.forgetPin(email, studentCode)
    }
}
