package com.bdsoftware.idorm.core.data.repository

import android.content.Context
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.model.WifiNetworkConfig
import com.bdsoftware.idorm.core.network.wifi.WifiAuthDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstWifiAuthRepository @Inject constructor(
    private val networkDataSource: WifiAuthDataSource,
    private val tokenManager: IDormPreferencesDataSource,
    @ApplicationContext private val context: Context
) : WifiAuthRepository {

    private val _isWifiActive = MutableStateFlow(false)
    override val isWifiActive: Flow<Boolean> = _isWifiActive.asStateFlow()

    override val isAutoBypassEnabled: Flow<Boolean> = tokenManager.wifiAutoBypassEnabled

    override val wifiConfigs: Flow<List<WifiNetworkConfig>> = tokenManager.wifiConfigsJson.map { json ->
        IDormPreferencesDataSource.deserializeWifiConfigs(json)
    }

    override suspend fun loginWifi(): Result<Unit> {
        val gatewayUrl = tokenManager.wifiGatewayUrl.first()
        val awingBaseUrl = tokenManager.wifiAwingUrl.first()
        return loginWifi(gatewayUrl, awingBaseUrl)
    }

    override suspend fun loginWifi(gatewayUrl: String, awingUrl: String): Result<Unit> {
        val result = networkDataSource.loginWifi(gatewayUrl, awingUrl)
        if (result.isSuccess) {
            _isWifiActive.value = true
        } else {
            _isWifiActive.value = false
        }
        return result
    }

    override suspend fun setWifiActiveState(isActive: Boolean) {
        _isWifiActive.value = isActive
    }

    override suspend fun setAutoBypassEnabled(enabled: Boolean) {
        tokenManager.setWifiAutoBypassEnabled(enabled)
    }

    override suspend fun saveWifiConfigs(configs: List<WifiNetworkConfig>) {
        val json = IDormPreferencesDataSource.serializeWifiConfigs(configs)
        tokenManager.saveWifiConfigsJson(json)
    }
}
