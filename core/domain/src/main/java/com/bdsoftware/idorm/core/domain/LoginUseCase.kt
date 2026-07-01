package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(studentCode: String, pin: String): Result<String> {
        if (studentCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Số CCCD không được để trống"))
        }
        if (pin.isBlank()) {
            return Result.failure(IllegalArgumentException("Mật khẩu không được để trống"))
        }
        return authRepository.login(studentCode, pin)
    }
}
