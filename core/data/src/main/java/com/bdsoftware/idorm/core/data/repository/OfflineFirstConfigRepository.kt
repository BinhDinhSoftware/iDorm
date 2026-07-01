package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.model.AppConfig
import com.bdsoftware.idorm.core.network.firebase.FirebaseConfigDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstConfigRepository @Inject constructor(
    private val firebaseConfigDataSource: FirebaseConfigDataSource
) : ConfigRepository {
    override suspend fun getAppConfig(): Result<AppConfig> {
        val config = firebaseConfigDataSource.getAppConfig()
        return if (config != null) {
            Result.success(config)
        } else {
            Result.failure(Exception("Failed to fetch app configuration"))
        }
    }
}
