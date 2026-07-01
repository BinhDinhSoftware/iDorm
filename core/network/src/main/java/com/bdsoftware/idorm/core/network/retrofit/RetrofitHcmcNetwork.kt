package com.bdsoftware.idorm.core.network.retrofit

import com.bdsoftware.idorm.core.network.model.NetworkHcmcLoginRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcAuthResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRefreshTokenRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcCancelRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcStatisticsResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcUserRequestsResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationReadRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationReadResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcBaseResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceGroup
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceFormResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRequestDetailResponse
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewRequest
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcComplaintItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part

interface RetrofitHcmcNetwork {
    @GET("api/service/groups")
    suspend fun getServiceGroups(): NetworkHcmcBaseResponse<List<NetworkHcmcServiceGroup>>

    @GET("api/service/{id}/form")
    suspend fun getServiceForm(
        @Path("id") serviceId: Int
    ): NetworkHcmcBaseResponse<NetworkHcmcServiceFormResponse>

    @POST("api/public_user/login")
    suspend fun login(
        @Body request: NetworkHcmcLoginRequest
    ): NetworkHcmcAuthResponse

    @POST("api/public_user/refresh_token")
    suspend fun refreshToken(
        @Body request: NetworkHcmcRefreshTokenRequest
    ): NetworkHcmcAuthResponse

    @GET("api/service/request/statistics")
    suspend fun getStatistics(
        @Query("user_id") userId: String
    ): NetworkHcmcStatisticsResponse

    @GET("api/notifications/my")
    suspend fun getNotifications(
        @Query("user_id") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): NetworkHcmcNotificationResponse

    @GET("api/service/request/user")
    suspend fun getUserRequests(
        @Query("user_id") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): NetworkHcmcUserRequestsResponse

    @POST("api/notifications/detail")
    suspend fun getNotificationDetail(
        @Body request: NetworkHcmcNotificationDetailRequest
    ): NetworkHcmcNotificationDetailResponse

    @POST("api/notifications/read")
    suspend fun readNotification(
        @Body request: NetworkHcmcNotificationReadRequest
    ): NetworkHcmcNotificationReadResponse

    @Multipart
    @POST("api/service/request/create")
    suspend fun createRequest(
        @Part("request_id") requestId: okhttp3.RequestBody,
        @Part("service_id") serviceId: okhttp3.RequestBody,
        @Part("request_user_id") requestUserId: okhttp3.RequestBody,
        @Part("note") note: okhttp3.RequestBody,
        @Part("dynamic_data") dynamicData: okhttp3.RequestBody?,
        @Part attachments: List<okhttp3.MultipartBody.Part>?
    ): NetworkHcmcBaseResponse<Unit>

    @GET("api/service/request/detail/{id}")
    suspend fun getRequestDetail(
        @Path("id") requestId: Int
    ): NetworkHcmcRequestDetailResponse

    @POST("api/service/request/review/create")
    suspend fun createReview(
        @Body request: NetworkHcmcReviewRequest
    ): NetworkHcmcBaseResponse<Unit>

    @GET("api/service/request/review/list")
    suspend fun getReviews(
        @Query("request_id") requestId: Int
    ): NetworkHcmcBaseResponse<List<NetworkHcmcReviewItem>>

    @Multipart
    @POST("api/service/request/complaint/create")
    suspend fun createComplaint(
        @Part("request_id") requestId: okhttp3.RequestBody,
        @Part("user_id") userId: okhttp3.RequestBody,
        @Part("content") content: okhttp3.RequestBody,
        @Part attachments: List<okhttp3.MultipartBody.Part>?
    ): NetworkHcmcBaseResponse<Unit>

    @GET("api/service/request/complaint/list")
    suspend fun getComplaints(
        @Query("request_id") requestId: Int,
        @Query("user_id") userId: Int
    ): NetworkHcmcBaseResponse<List<NetworkHcmcComplaintItem>>

    @POST("api/service/request/cancel")
    suspend fun cancelRequest(
        @Body request: NetworkHcmcCancelRequest
    ): NetworkHcmcBaseResponse<Unit>

    @Multipart
    @POST("api/service/request/update")
    suspend fun updateRequest(
        @Part("request_id") requestId: okhttp3.RequestBody,
        @Part("request_user_id") requestUserId: okhttp3.RequestBody,
        @Part("note") note: okhttp3.RequestBody,
        @Part("is_replace_images") isReplaceImages: okhttp3.RequestBody,
        @Part("removed_image_ids") removedImageIds: okhttp3.RequestBody?,
        @Part("dynamic_data") dynamicData: okhttp3.RequestBody?,
        @Part attachments: List<okhttp3.MultipartBody.Part>?
    ): NetworkHcmcBaseResponse<Unit>
}
