package com.bdsoftware.idorm.core.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log
import com.bdsoftware.idorm.core.common.util.WifiLogCollector
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quản lý logic giám sát và xác thực WiFi miễn phí tại KTX.
 *
 * Cơ chế:
 * - Lắng nghe NetworkCallback khi thiết bị kết nối WiFi Awing
 * - Bind process vào mạng WiFi để thực hiện login captive portal
 * - Hỗ trợ danh sách SSID động (nhiều mesh WiFi khác nhau)
 *
 * KHÔNG tự động kết nối/ép kết nối vào WiFi.
 * Người dùng tự kết nối WiFi → Service phát hiện và login/gia hạn.
 */
@Singleton
class WifiConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WifiConnection"
    }

    /** Danh sách SSID Awing được cập nhật từ cấu hình */
    @Volatile
    private var awingSsids: List<String> = listOf("Free Wi-MESH", "Free Wi-MESH - Rescue")

    /** Danh sách IP Gateway Awing được cập nhật từ cấu hình */
    @Volatile
    private var awingGateways: List<String> = listOf("186.186.0.1")

    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private fun logD(msg: String) {
        Log.d(TAG, msg)
        WifiLogCollector.log(TAG, msg)
    }

    /**
     * Cập nhật danh sách SSID và Gateway Awing từ cấu hình người dùng.
     */
    fun updateAwingConfigs(ssids: List<String>, gateways: List<String>) {
        if (ssids.isNotEmpty()) {
            awingSsids = ssids
        }
        val extractedGateways = gateways.map { url ->
            url.replace("http://", "")
                .replace("https://", "")
                .split("/")
                .firstOrNull()
                ?.split(":")
                ?.firstOrNull()
                ?.trim() ?: ""
        }.filter { it.isNotEmpty() }
        if (extractedGateways.isNotEmpty()) {
            awingGateways = extractedGateways
        }
        logD("[Config] Cập nhật SSIDs: $awingSsids, Gateways: $awingGateways")
    }

    fun updateAwingSsids(ssids: List<String>) {
        if (ssids.isNotEmpty()) {
            awingSsids = ssids
        }
        logD("[Config] Cập nhật danh sách SSID: $awingSsids")
    }

    fun getAwingSsids(): List<String> = awingSsids

    // ═══════════════════════════════════════════════════════════════════
    // TÌM MẠNG WI-FI — Duyệt allNetworks, ưu tiên CAPTIVE_PORTAL/VALIDATED
    // ═══════════════════════════════════════════════════════════════════

    fun findWifiNetwork(): Network? {
        val allWifi = connectivityManager.allNetworks.mapNotNull { network ->
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return@mapNotNull null
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) Pair(network, caps) else null
        }

        // Ưu tiên 1: Captive Portal hoặc Validated
        val preferred = allWifi.firstOrNull { (_, caps) ->
            (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) ||
             caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        if (preferred != null) return preferred.first

        // Ưu tiên 2: Có Internet capability
        val withInternet = allWifi.firstOrNull { (_, caps) ->
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        if (withInternet != null) return withInternet.first

        // Fallback: bất kỳ Wi-Fi nào
        return allWifi.firstOrNull()?.first
    }

    // ═══════════════════════════════════════════════════════════════════
    // LẤY SSID HIỆN TẠI — Duyệt allNetworks + transportInfo
    // ═══════════════════════════════════════════════════════════════════

    @Suppress("DEPRECATION")
    fun getCurrentWifiSsid(): String? {
        // Cách 1: Duyệt allNetworks → transportInfo (Android 10+)
        try {
            for (network in connectivityManager.allNetworks) {
                val caps = connectivityManager.getNetworkCapabilities(network) ?: continue
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val transportInfo = caps.transportInfo
                    if (transportInfo is android.net.wifi.WifiInfo) {
                        val ssid = transportInfo.ssid?.replace("\"", "")
                        if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
                            return ssid
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logD("[SSID] Lỗi lấy qua transportInfo: ${e.message}")
        }

        // Cách 2: Fallback — wifiManager.connectionInfo
        try {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                val ssid = wifiInfo.ssid?.replace("\"", "")
                if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
                    return ssid
                }
            }
        } catch (e: Exception) {
            logD("[SSID] Lỗi lấy qua connectionInfo: ${e.message}")
        }

        return null
    }

    fun isAwingSsid(ssid: String?): Boolean {
        if (ssid == null) return false
        val cleanSsid = ssid.replace("\"", "").trim()
        return awingSsids.any { it.replace("\"", "").trim().equals(cleanSsid, ignoreCase = true) }
    }

    fun getGatewayIpAddress(): String? {
        try {
            val dhcpInfo = wifiManager.dhcpInfo
            if (dhcpInfo != null && dhcpInfo.gateway != 0) {
                val gatewayInt = dhcpInfo.gateway
                return "${gatewayInt and 0xFF}.${(gatewayInt shr 8) and 0xFF}.${(gatewayInt shr 16) and 0xFF}.${(gatewayInt shr 24) and 0xFF}"
            }
        } catch (e: Exception) {
            logD("[Gateway] Lỗi lấy IP Gateway: ${e.message}")
        }
        return null
    }

    fun isConnectedToAwing(): Boolean {
        val ssid = getCurrentWifiSsid()
        if (ssid != null && ssid.isNotEmpty() && ssid != "<unknown ssid>") {
            return isAwingSsid(ssid)
        }
        // Fallback: Check if the gateway IP matches any configured gateway URL/IP
        val gateway = getGatewayIpAddress()
        if (gateway != null && gateway.isNotEmpty()) {
            val matched = awingGateways.any { it.equals(gateway, ignoreCase = true) }
            if (matched) {
                logD("[Awing] SSID không đọc được hoặc unknown nhưng Gateway IP trùng khớp: $gateway")
                return true
            }
        }
        return false
    }

    // ═══════════════════════════════════════════════════════════════════
    // NETWORK CALLBACK — Lắng nghe khi Wi-Fi available/lost
    // ═══════════════════════════════════════════════════════════════════

    fun registerNetworkCallback(
        onAwingConnected: () -> Unit,
        onNetworkLost: () -> Unit
    ) {
        if (networkCallback != null) return

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val caps = connectivityManager.getNetworkCapabilities(network)
                if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    val ssid = getCurrentWifiSsid()
                    logD("[Callback] WiFi available: $ssid")
                    if (isConnectedToAwing()) {
                        logD("[Callback] ★ Đã kết nối AWING! Trigger login...")
                        onAwingConnected()
                    } else {
                        onNetworkLost()
                    }
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    // Nếu mạng đã có Internet (VALIDATED), không cần trigger login
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        return
                    }

                    if (isConnectedToAwing()) {
                        logD("[Callback] ★ Phát hiện SSID AWING hoặc Gateway Awing. Trigger login...")
                        onAwingConnected()
                    } else {
                        onNetworkLost()
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                logD("[Callback] Mất kết nối WiFi.")
                onNetworkLost()
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
        logD("[Callback] Đã đăng ký NetworkCallback")
    }

    fun unregisterNetworkCallback() {
        networkCallback?.let {
            try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            networkCallback = null
            logD("[Callback] Đã hủy NetworkCallback")
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // BIND PROCESS TO NETWORK — Ép toàn bộ luồng mạng qua Wi-Fi
    // ═══════════════════════════════════════════════════════════════════

    fun bindProcessToWifi(): Boolean {
        val wifiNetwork = findWifiNetwork()
        if (wifiNetwork != null) {
            connectivityManager.bindProcessToNetwork(wifiNetwork)
            logD("[Bind] Đã ràng buộc tiến trình vào Wi-Fi")
            return true
        }
        logD("[Bind] Không tìm thấy mạng Wi-Fi để ràng buộc")
        return false
    }

    fun unbindProcess() {
        connectivityManager.bindProcessToNetwork(null)
        logD("[Bind] Đã hủy ràng buộc tiến trình")
    }
}
