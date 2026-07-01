package com.bdsoftware.idorm.core.network.di

import com.bdsoftware.idorm.core.network.retrofit.AuthTokenProvider
import com.bdsoftware.idorm.core.network.retrofit.HcmcAuthTokenProvider
import com.bdsoftware.idorm.core.network.retrofit.RetrofitDefaultNetwork
import com.bdsoftware.idorm.core.network.retrofit.RetrofitHcmcNetwork
import com.bdsoftware.idorm.core.network.retrofit.RetrofitStudentNetwork
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRefreshTokenRequest
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import com.google.firebase.firestore.FirebaseFirestore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HcmcRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://sv.ktxhcm.edu.vn/"
    private const val HCMC_BASE_URL = "https://hanhchinhmotcua.ktxhcm.edu.vn/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Tạo SSL trust manager dùng chung cho cả 2 OkHttpClient.
     */
    private fun createTrustAllCerts(): Array<TrustManager> = arrayOf(
        object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        }
    )

    private fun createSslOkHttpBuilder(): OkHttpClient.Builder {
        val trustAllCerts = createTrustAllCerts()
        val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
            .hostnameVerifier { _, _ -> true }
    }

    // ───── Student/Default OkHttpClient ─────

    @Provides
    @Singleton
    fun provideOkHttpClient(authTokenProvider: AuthTokenProvider): OkHttpClient {
        try {
            return createSslOkHttpBuilder()
                .addInterceptor { chain ->
                    val requestBuilder = chain.request().newBuilder()
                    val token = runBlocking {
                        authTokenProvider.token.firstOrNull()
                    }
                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "$token")
                    }
                    chain.proceed(requestBuilder.build())
                }
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                )
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // ───── HCMC OkHttpClient với Bearer token + Refresh Authenticator ─────

    @Provides
    @Singleton
    @HcmcRetrofit
    fun provideHcmcOkHttpClient(
        hcmcAuthTokenProvider: HcmcAuthTokenProvider,
        json: Json
    ): OkHttpClient {
        try {
            return createSslOkHttpBuilder()
                // Interceptor: tự động thêm Authorization header cho HCMC
                .addInterceptor { chain ->
                    val requestBuilder = chain.request().newBuilder()
                    val accessToken = runBlocking {
                        hcmcAuthTokenProvider.hcmcAccessToken.firstOrNull()
                    }
                    if (!accessToken.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $accessToken")
                    }
                    chain.proceed(requestBuilder.build())
                }
                // Authenticator: xử lý tự động refresh khi nhận 401
                .authenticator { _, response ->
                    // Tránh vòng lặp refresh vô tận
                    if (response.request.url.encodedPath.contains("refresh_token")) {
                        return@authenticator null
                    }

                    val refreshToken = runBlocking {
                        hcmcAuthTokenProvider.hcmcRefreshToken.firstOrNull()
                    }
                    if (refreshToken.isNullOrEmpty()) {
                        return@authenticator null
                    }

                    // Gọi đồng bộ api/public_user/refresh_token
                    try {
                        val refreshBody = json.encodeToString(
                            NetworkHcmcRefreshTokenRequest.serializer(),
                            NetworkHcmcRefreshTokenRequest(refresh_token = refreshToken)
                        )
                        val mediaType = "application/json".toMediaType()
                        val refreshRequest = okhttp3.Request.Builder()
                            .url(HCMC_BASE_URL + "api/public_user/refresh_token")
                            .post(okhttp3.RequestBody.create(mediaType, refreshBody))
                            .build()

                        val refreshClient = createSslOkHttpBuilder().build()
                        val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                        if (refreshResponse.isSuccessful) {
                            val bodyString = refreshResponse.body?.string()
                            if (bodyString != null) {
                                val parsed = json.decodeFromString(
                                    com.bdsoftware.idorm.core.network.model.NetworkHcmcAuthResponse.serializer(),
                                    bodyString
                                )
                                val newAccessToken = parsed.data?.access_token
                                if (!newAccessToken.isNullOrEmpty()) {
                                    // Retry request gốc với token mới
                                    return@authenticator response.request.newBuilder()
                                        .header("Authorization", "Bearer $newAccessToken")
                                        .build()
                                }
                            }
                        }
                        null
                    } catch (e: Exception) {
                        null
                    }
                }
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                )
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // ───── Retrofit instances ─────

    @Provides
    @Singleton
    fun provideRetrofitStudentNetwork(
        json: Json,
        okHttpClient: OkHttpClient
    ): RetrofitStudentNetwork = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RetrofitStudentNetwork::class.java)

    @Provides
    @Singleton
    fun provideRetrofitDefaultNetwork(
        json: Json,
        okHttpClient: OkHttpClient
    ): RetrofitDefaultNetwork = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RetrofitDefaultNetwork::class.java)

    @Provides
    @Singleton
    fun provideRetrofitHcmcNetwork(
        json: Json,
        @HcmcRetrofit hcmcOkHttpClient: OkHttpClient
    ): RetrofitHcmcNetwork = Retrofit.Builder()
        .baseUrl(HCMC_BASE_URL)
        .client(hcmcOkHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RetrofitHcmcNetwork::class.java)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
