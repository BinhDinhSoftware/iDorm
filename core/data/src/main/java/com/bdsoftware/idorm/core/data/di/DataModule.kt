package com.bdsoftware.idorm.core.data.di

import android.content.Context
import com.bdsoftware.idorm.core.data.repository.AuthRepository
import com.bdsoftware.idorm.core.data.repository.OfflineFirstAuthRepository
import com.bdsoftware.idorm.core.data.repository.WifiAuthRepository
import com.bdsoftware.idorm.core.data.repository.OfflineFirstWifiAuthRepository
import com.bdsoftware.idorm.core.data.repository.FeedbackRepository
import com.bdsoftware.idorm.core.data.repository.OfflineFirstFeedbackRepository
import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.data.repository.OfflineFirstHcmcRepository
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.network.retrofit.AuthTokenProvider
import com.bdsoftware.idorm.core.network.retrofit.HcmcAuthTokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindAuthRepository(
        impl: OfflineFirstAuthRepository
    ): AuthRepository

    @Binds
    abstract fun bindWifiAuthRepository(
        impl: OfflineFirstWifiAuthRepository
    ): WifiAuthRepository

    @Binds
    abstract fun bindFeedbackRepository(
        impl: OfflineFirstFeedbackRepository
    ): FeedbackRepository

    @Binds
    abstract fun bindAuthTokenProvider(
        impl: IDormPreferencesDataSource
    ): AuthTokenProvider

    @Binds
    abstract fun bindHcmcAuthTokenProvider(
        impl: IDormPreferencesDataSource
    ): HcmcAuthTokenProvider

    @Binds
    abstract fun bindHcmcRepository(
        impl: OfflineFirstHcmcRepository
    ): HcmcRepository
}
