package com.bdsoftware.idorm.sync.di

import com.bdsoftware.idorm.core.data.repository.WifiWorkerManager
import com.bdsoftware.idorm.sync.worker.WorkManagerWifiWorkerManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    abstract fun bindWifiWorkerManager(
        impl: WorkManagerWifiWorkerManager
    ): WifiWorkerManager
}
