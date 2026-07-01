package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.network.model.NetworkChangePinRequest
import com.bdsoftware.idorm.core.network.model.NetworkForgetPinRequest
import com.bdsoftware.idorm.core.network.model.NetworkLoginRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcLoginRequest
import com.bdsoftware.idorm.core.network.retrofit.RetrofitStudentNetwork
import com.bdsoftware.idorm.core.network.retrofit.RetrofitHcmcNetwork
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class OfflineFirstAuthRepository @Inject constructor(
    private val network: RetrofitStudentNetwork,
    private val hcmcNetwork: RetrofitHcmcNetwork,
    private val tokenManager: IDormPreferencesDataSource
) : AuthRepository {
    override suspend fun login(studentCode: String, pin: String): Result<String> {
        return try {
            // Gọi đồng thời cả 2 API đăng nhập
            coroutineScope {
                val studentLoginDeferred = async {
                    network.login(
                        NetworkLoginRequest(
                            StudentCode = studentCode,
                            PIN = pin
                        )
                    )
                }

                val hcmcLoginDeferred = async {
                    try {
                        hcmcNetwork.login(
                            NetworkHcmcLoginRequest(
                                username = studentCode,
                                password = pin,
                                fcm_device_token = "android_mock_token",
                                device_id = "android_device_${System.currentTimeMillis()}"
                            )
                        )
                    } catch (e: Exception) {
                        null // HCMC login failure không ảnh hưởng login chính
                    }
                }

                val studentResponse = studentLoginDeferred.await()
                val hcmcResponse = hcmcLoginDeferred.await()

                val data = studentResponse.Data
                if (studentResponse.Success && data != null) {
                    // Lưu token cổng sinh viên
                    tokenManager.saveToken(data.Token)

                    // Lưu token HCMC nếu đăng nhập thành công
                    val hcmcData = hcmcResponse?.data
                    if (hcmcResponse?.success == true && hcmcData != null) {
                        tokenManager.saveHcmcAuth(
                            accessToken = hcmcData.access_token,
                            refreshToken = hcmcData.refresh_token,
                            userId = hcmcData.id.toString()
                        )
                    }

                    Result.success(data.Token)
                } else {
                    Result.failure(Exception(studentResponse.Message ?: "Login failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgetPin(email: String, studentCode: String): Result<Unit> {
        return try {
            network.forgetPin(
                NetworkForgetPinRequest(
                    Email = email,
                    StudentCode = studentCode
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePin(oldPin: String, newPin: String, confirmPin: String): Result<Unit> {
        return try {
            val email = tokenManager.userEmail.firstOrNull().orEmpty()
            val response = network.changePin(
                NetworkChangePinRequest(
                    Email = email,
                    OldPIN = oldPin,
                    NewPIN = newPin,
                    ConfirmPIN = confirmPin
                )
            )
            if (response.Status) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.Message ?: "Thay đổi mật khẩu thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
