package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.model.WifiNetworkConfig
import kotlinx.coroutines.flow.Flow

interface WifiAuthRepository {
    val isWifiActive: Flow<Boolean>
    val isAutoBypassEnabled: Flow<Boolean>
    val wifiConfigs: Flow<List<WifiNetworkConfig>>
    suspend fun loginWifi(): Result<Unit>
    suspend fun loginWifi(gatewayUrl: String, awingUrl: String): Result<Unit>
    suspend fun setWifiActiveState(isActive: Boolean)
    suspend fun setAutoBypassEnabled(enabled: Boolean)
    suspend fun saveWifiConfigs(configs: List<WifiNetworkConfig>)
}
