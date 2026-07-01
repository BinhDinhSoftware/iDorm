package com.bdsoftware.idorm.core.domain

import android.content.Context
import com.bdsoftware.idorm.core.data.repository.ConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import com.bdsoftware.idorm.core.common.util.getAppVersion
import javax.inject.Inject

sealed interface AppStatus {
    data object Ready : AppStatus
    data class Maintenance(val message: String) : AppStatus
    data class ForceUpdate(val latestVersion: String) : AppStatus
}

class CheckAppStatusUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): Result<AppStatus> {
        return configRepository.getAppConfig().map { config ->
            if (config.maintenanceMode) {
                return@map AppStatus.Maintenance(
                    config.maintenanceMessage.ifBlank { "Hệ thống đang bảo trì định kỳ. Vui lòng quay lại sau." }
                )
            }

            val currentVersion = context.getAppVersion()
            val isForceUpdate = isVersionLessThan(currentVersion, config.androidMinVersion) || 
                    (config.androidForceUpdate && isVersionLessThan(currentVersion, config.androidLatestVersion))

            if (isForceUpdate) {
                AppStatus.ForceUpdate(config.androidLatestVersion)
            } else {
                AppStatus.Ready
            }
        }
    }

    private fun isVersionLessThan(current: String, target: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val targetParts = target.split(".").mapNotNull { it.toIntOrNull() }
        val maxLength = maxOf(currentParts.size, targetParts.size)
        for (i in 0 until maxLength) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val targetPart = targetParts.getOrElse(i) { 0 }
            if (currentPart < targetPart) return true
            if (currentPart > targetPart) return false
        }
        return false
    }
}
