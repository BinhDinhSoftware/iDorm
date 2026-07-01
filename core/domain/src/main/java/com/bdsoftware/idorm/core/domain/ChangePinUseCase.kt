package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.AuthRepository
import javax.inject.Inject

class ChangePinUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(oldPin: String, newPin: String, confirmPin: String): Result<Unit> {
        if (oldPin.isBlank()) {
            return Result.failure(IllegalArgumentException("Mật khẩu cũ không được để trống"))
        }
        if (newPin.isBlank()) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới không được để trống"))
        }
        if (confirmPin.isBlank()) {
            return Result.failure(IllegalArgumentException("Xác nhận mật khẩu mới không được để trống"))
        }
        if (newPin != confirmPin) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu mới không trùng khớp"))
        }
        return authRepository.changePin(oldPin, newPin, confirmPin)
    }
}
