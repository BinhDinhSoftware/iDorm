package com.bdsoftware.idorm.core.network.retrofit

import com.bdsoftware.idorm.core.network.model.NotificationData
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceResponse
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceEWResponse
import com.bdsoftware.idorm.core.network.model.NetworkRentItem
import com.bdsoftware.idorm.core.network.model.NetworkGenerateQrResponse
import com.bdsoftware.idorm.core.network.model.NetworkUpdateNotificationRequest
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field

interface RetrofitDefaultNetwork {
    @GET("api/Default/GetListNotification")
    suspend fun getNotifications(): List<NotificationData>

    @GET("api/Default/GetInvoiceList")
    suspend fun getInvoices(): List<NetworkInvoiceResponse>

    @GET("api/Default/GetInvoiceEWList")
    suspend fun getInvoiceEWs(): List<NetworkInvoiceEWResponse>

    @GET("api/Default/GetRentList")
    suspend fun getRentList(): List<NetworkRentItem>

    @POST("Payment/GenerateQrCode")
    @FormUrlEncoded
    suspend fun generateQrCode(
        @Field("param") param: String
    ): NetworkGenerateQrResponse

    @POST("api/Default/PostUpdateNotification")
    suspend fun updateNotification(
        @Body request: NetworkUpdateNotificationRequest
    )
}
