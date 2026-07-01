package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.model.AppConfig

interface ConfigRepository {
    suspend fun getAppConfig(): Result<AppConfig>
}
