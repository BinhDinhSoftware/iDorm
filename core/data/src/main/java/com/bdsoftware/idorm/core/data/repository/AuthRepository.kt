package com.bdsoftware.idorm.core.data.repository

interface AuthRepository {
    suspend fun login(studentCode: String, pin: String): Result<String>
    suspend fun forgetPin(email: String, studentCode: String): Result<Unit>
    suspend fun changePin(oldPin: String, newPin: String, confirmPin: String): Result<Unit>
}
