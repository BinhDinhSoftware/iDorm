package com.bdsoftware.idorm.feature.wificonfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.data.repository.WifiAuthRepository
import com.bdsoftware.idorm.core.data.repository.WifiConnectionManager
import com.bdsoftware.idorm.core.data.repository.WifiWorkerManager
import com.bdsoftware.idorm.core.model.WifiNetworkConfig
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WifiUiMessage {
    data class Resource(val resId: Int, val formatArgs: List<Any> = emptyList()) : WifiUiMessage()
    data class Raw(val message: String) : WifiUiMessage()
}

@HiltViewModel
class WifiConfigViewModel @Inject constructor(
    private val wifiAuthRepository: WifiAuthRepository,
    private val wifiWorkerManager: WifiWorkerManager,
    private val wifiConnectionManager: WifiConnectionManager
) : ViewModel() {

    val wifiConfigs: StateFlow<List<WifiNetworkConfig>> = wifiAuthRepository.wifiConfigs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isAutoBypassEnabled: StateFlow<Boolean> = wifiAuthRepository.isAutoBypassEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isWifiActive: StateFlow<Boolean> = wifiAuthRepository.isWifiActive
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _uiMessage = MutableSharedFlow<WifiUiMessage>()
    val uiMessage: SharedFlow<WifiUiMessage> = _uiMessage.asSharedFlow()

    fun toggleAutoBypass(enabled: Boolean) {
        viewModelScope.launch {
            wifiAuthRepository.setAutoBypassEnabled(enabled)
            if (enabled) {
                wifiWorkerManager.startAutoRenewWorker()
                _uiMessage.emit(WifiUiMessage.Resource(DesignR.string.wifi_config_msg_auto_bypass_enabled))
            } else {
                wifiWorkerManager.stopAutoRenewWorker()
                _uiMessage.emit(WifiUiMessage.Resource(DesignR.string.wifi_config_msg_auto_bypass_disabled))
            }
        }
    }

    fun addOrUpdateConfig(config: WifiNetworkConfig, oldSsid: String? = null) {
        viewModelScope.launch {
            val currentList = wifiConfigs.value.toMutableList()
            val targetSsid = oldSsid ?: config.ssid
            val existingIndex = currentList.indexOfFirst { it.ssid.equals(targetSsid, ignoreCase = true) }
            if (existingIndex >= 0) {
                currentList[existingIndex] = config
            } else {
                currentList.add(config)
            }
            wifiAuthRepository.saveWifiConfigs(currentList)
            _uiMessage.emit(WifiUiMessage.Resource(DesignR.string.wifi_config_msg_save_success, listOf(config.ssid)))
        }
    }

    fun removeConfig(ssid: String) {
        viewModelScope.launch {
            val currentList = wifiConfigs.value.filterNot { it.ssid.equals(ssid, ignoreCase = true) }
            wifiAuthRepository.saveWifiConfigs(currentList)
            _uiMessage.emit(WifiUiMessage.Resource(DesignR.string.wifi_config_msg_delete_success, listOf(ssid)))
        }
    }

    fun toggleConfigEnabled(config: WifiNetworkConfig, enabled: Boolean) {
        addOrUpdateConfig(config.copy(enabled = enabled))
    }

    fun manualConnectAndLogin(config: WifiNetworkConfig) {
        viewModelScope.launch {
            _uiMessage.emit(WifiUiMessage.Resource(DesignR.string.wifi_config_msg_logging_in, listOf(config.ssid)))
            wifiConnectionManager.bindProcessToWifi()
            try {
                val result = wifiAuthRepository.loginWifi(config.gatewayUrl, config.awingUrl)
                if (result.isSuccess) {
                    _uiMessage.emit(WifiUiMessage.Resource(DesignR.string.wifi_config_msg_login_success, listOf(config.ssid)))
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Thất bại"
                    _uiMessage.emit(WifiUiMessage.Resource(DesignR.string.wifi_config_msg_login_error, listOf(err)))
                }
            } finally {
                wifiConnectionManager.unbindProcess()
            }
        }
    }
}
