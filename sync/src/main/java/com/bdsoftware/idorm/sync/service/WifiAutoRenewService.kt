package com.bdsoftware.idorm.sync.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.bdsoftware.idorm.core.data.notification.WifiNotificationManager
import com.bdsoftware.idorm.core.data.notification.WifiNotificationStatus
import com.bdsoftware.idorm.core.data.repository.WifiAuthRepository
import com.bdsoftware.idorm.core.data.repository.WifiConnectionManager
import com.bdsoftware.idorm.core.model.WifiNetworkConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

import com.bdsoftware.idorm.core.common.util.WifiLogCollector

/**
 * Foreground Service quản lý vòng đời WiFi Free KTX.
 *
 * Cơ chế:
 * - onCreate(): đăng ký NetworkCallback (lắng nghe kết nối AWING)
 * - Vòng lặp: kiểm tra kết nối → bind process → login → gia hạn 30 phút
 * - KHÔNG tự động kết nối/ép kết nối vào WiFi
 * - onDestroy(): hủy tất cả callback
 */
@AndroidEntryPoint
class WifiAutoRenewService : Service() {

    @Inject lateinit var wifiConnectionManager: WifiConnectionManager
    @Inject lateinit var wifiAuthRepository: WifiAuthRepository
    @Inject lateinit var wifiNotificationManager: WifiNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var renewJob: Job? = null
    private var lastLoginTriggerTime = 0L
    private var wasAwingConnected = false

    companion object {
        private const val TAG = "WifiService"
        private const val NOTIFICATION_ID = 1001
        private const val RENEW_INTERVAL_MS = 30 * 60 * 1000L   // 30 phút
        private const val CHECK_INTERVAL_MS = 30 * 1000L         // 30 giây
    }

    private fun logD(msg: String) {
        Log.d(TAG, msg)
        WifiLogCollector.log(TAG, msg)
    }

    private fun logE(msg: String) {
        Log.e(TAG, msg, null)
        WifiLogCollector.log(TAG, msg, isError = true)
    }

    // ── BroadcastReceiver: Wi-Fi bật/tắt ──
    private val wifiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                when (state) {
                    WifiManager.WIFI_STATE_ENABLED -> {
                        logD("[Broadcast] Wi-Fi bật → kiểm tra kết nối AWING")
                        // Chỉ kiểm tra, KHÔNG ép kết nối
                        if (wifiConnectionManager.isConnectedToAwing()) {
                            logD("[Broadcast] Đã ở AWING → login ngay")
                            triggerImmediateLogin()
                        } else {
                            wifiNotificationManager.showWifiActiveNotification(WifiNotificationStatus.WaitingForConnection)
                        }
                    }
                    WifiManager.WIFI_STATE_DISABLED -> {
                        logD("[Broadcast] Wi-Fi tắt")
                        wifiNotificationManager.showWifiActiveNotification(WifiNotificationStatus.WifiDisabled)
                        triggerNetworkLost()
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════

    override fun onCreate() {
        super.onCreate()
        logD("[Service] onCreate")

        // Đồng bộ danh sách SSID và Gateway từ cấu hình
        serviceScope.launch {
            val configs = wifiAuthRepository.wifiConfigs.first()
            val enabledConfigs = configs.filter { it.enabled }
            wifiConnectionManager.updateAwingConfigs(
                ssids = enabledConfigs.map { it.ssid },
                gateways = enabledConfigs.map { it.gatewayUrl }
            )
        }

        // Đăng ký Wi-Fi on/off receiver
        registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))

        // Đăng ký NetworkCallback — phát hiện Wi-Fi connected/lost
        wifiConnectionManager.registerNetworkCallback(
            onAwingConnected = {
                logD("[Service] NetworkCallback → AWING kết nối! Login ngay...")
                triggerImmediateLogin()
            },
            onNetworkLost = {
                logD("[Service] NetworkCallback → Mất kết nối AWING")
                triggerNetworkLost()
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logD("[Service] onStartCommand")

        // Start Foreground
        val notification = wifiNotificationManager.createWifiActiveNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            logE("[Service] Lỗi startForeground: ${e.message}")
        }

        wifiNotificationManager.showWifiActiveNotification()

        // Bắt đầu vòng lặp giám sát
        if (renewJob == null || renewJob?.isCompleted == true) {
            startMainLoop()
        }

        return START_STICKY
    }

    // ═══════════════════════════════════════════════════════════════════
    // VÒNG LẶP CHÍNH — Giám sát kết nối và gia hạn
    // KHÔNG tự động kết nối, chỉ chờ người dùng kết nối WiFi Awing
    // ═══════════════════════════════════════════════════════════════════

    private fun triggerImmediateLogin() {
        val now = System.currentTimeMillis()
        if (now - lastLoginTriggerTime < 10_000L) {
            logD("[Service] Bỏ qua trigger login do khoảng cách quá ngắn (< 10s)")
            return
        }
        lastLoginTriggerTime = now
        renewJob?.cancel()
        startMainLoop()
    }

    private fun triggerNetworkLost() {
        if (wasAwingConnected) {
            logD("[Service] Phát hiện mất kết nối AWING → Cập nhật trạng thái ngay")
            wasAwingConnected = false
            renewJob?.cancel()
            startMainLoop()
        }
    }

    private fun startMainLoop() {
        renewJob = serviceScope.launch {
            while (isActive) {
                // Đồng bộ danh sách SSID và Gateway từ cấu hình
                val configs = wifiAuthRepository.wifiConfigs.first()
                val enabledConfigs = configs.filter { it.enabled }
                wifiConnectionManager.updateAwingConfigs(
                    ssids = enabledConfigs.map { it.ssid },
                    gateways = enabledConfigs.map { it.gatewayUrl }
                )

                val isAwing = wifiConnectionManager.isConnectedToAwing()

                if (isAwing) {
                    wasAwingConnected = true
                    // ══ ĐÃ KẾT NỐI AWING → đăng nhập / gia hạn ══
                    val currentSsid = wifiConnectionManager.getCurrentWifiSsid()
                    val gatewayIp = wifiConnectionManager.getGatewayIpAddress()
                    val matchedConfig = enabledConfigs.firstOrNull {
                        it.ssid.trim().equals(currentSsid?.trim(), ignoreCase = true)
                    } ?: enabledConfigs.firstOrNull { config ->
                        gatewayIp != null && config.gatewayUrl.contains(gatewayIp)
                    }

                    val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    logD("[Loop] ══ Gia hạn WiFi @ $timestamp — SSID: $currentSsid ══")
                    wifiNotificationManager.showWifiActiveNotification(WifiNotificationStatus.AwingAcquiring)

                    // Bind process → login → unbind
                    wifiConnectionManager.bindProcessToWifi()
                    try {
                        val result = if (matchedConfig != null) {
                            wifiAuthRepository.loginWifi(matchedConfig.gatewayUrl, matchedConfig.awingUrl)
                        } else {
                            wifiAuthRepository.loginWifi()
                        }

                        if (result.isSuccess) {
                            logD("[Loop] Gia hạn thành công!")
                            wifiNotificationManager.showWifiActiveNotification(WifiNotificationStatus.InternetOk)
                            delay(RENEW_INTERVAL_MS)
                        } else {
                            val err = result.exceptionOrNull()?.message ?: "Lỗi không rõ"
                            logE("[Loop] Gia hạn thất bại: $err")
                            wifiNotificationManager.showWifiActiveNotification(WifiNotificationStatus.RenewFailed(err))
                            wifiAuthRepository.setWifiActiveState(false)
                            delay(RENEW_INTERVAL_MS)
                        }
                    } finally {
                        wifiConnectionManager.unbindProcess()
                    }
                } else {
                    wasAwingConnected = false
                    // ══ CHƯA KẾT NỐI AWING → Chỉ chờ, không ép kết nối ══
                    logD("[Loop] Chưa kết nối AWING. Đang chờ...")
                    wifiNotificationManager.showWifiActiveNotification(WifiNotificationStatus.WaitingForConnection)
                    delay(CHECK_INTERVAL_MS)
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════════════

    override fun onDestroy() {
        super.onDestroy()
        logD("[Service] onDestroy")

        try { unregisterReceiver(wifiStateReceiver) } catch (_: Exception) {}

        wifiConnectionManager.unregisterNetworkCallback()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        wifiNotificationManager.cancelNotification()

        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
