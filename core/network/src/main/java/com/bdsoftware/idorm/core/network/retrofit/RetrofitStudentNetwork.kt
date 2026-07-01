package com.bdsoftware.idorm.core.network.retrofit

import com.bdsoftware.idorm.core.network.model.NetworkAuthResponse
import com.bdsoftware.idorm.core.network.model.NetworkForgetPinRequest
import com.bdsoftware.idorm.core.network.model.NetworkLoginRequest
import com.bdsoftware.idorm.core.network.model.NetworkUserProfileResponse
import com.bdsoftware.idorm.core.network.model.NetworkChangePinRequest
import com.bdsoftware.idorm.core.network.model.NetworkChangePinResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitStudentNetwork {
    @POST("api/Student/Login")
    suspend fun login(
        @Body request: NetworkLoginRequest
    ): NetworkAuthResponse

    @POST("api/Student/PostForgetPIN")
    suspend fun forgetPin(
        @Body request: NetworkForgetPinRequest
    )

    @POST("api/Student/PostChangePIN")
    suspend fun changePin(
        @Body request: NetworkChangePinRequest
    ): NetworkChangePinResponse

    @GET("api/Student/GetStudentInfo")
    suspend fun getProfile(): NetworkUserProfileResponse
}
