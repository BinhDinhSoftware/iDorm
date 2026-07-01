package com.bdsoftware.idorm.core.network.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

import com.bdsoftware.idorm.core.common.util.WifiLogCollector

@Singleton
class WifiAuthDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WifiAuth"
        private const val USER_AGENT = "PostmanRuntime/7.54.0"
    }

    private fun logD(msg: String) {
        Log.d(TAG, msg)
        WifiLogCollector.log(TAG, msg)
    }

    private fun logE(msg: String) {
        Log.e(TAG, msg, null)
        WifiLogCollector.log(TAG, msg, isError = true)
    }

    private var routerCookies: String = ""

    suspend fun loginWifi(
        gatewayUrl: String = "http://186.186.0.1",
        awingBaseUrl: String = "http://v1.awingconnect.vn",
        retryCount: Int = 0
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanGatewayUrl = gatewayUrl.trim().removeSuffix("/")
        val cleanAwingBaseUrl = awingBaseUrl.trim().removeSuffix("/")

        val gatewayLogin = "$cleanGatewayUrl/login"
        val gatewayLogout = "$cleanGatewayUrl/logout?"
        val gatewayStatus = "$cleanGatewayUrl/status"

        val awingLoginUrl = "$cleanAwingBaseUrl/login"
        val awingSuccessUrl = "$cleanAwingBaseUrl/Success"
        val awingVerifyUrl = "$cleanAwingBaseUrl/Home/VerifyUrl"
        val awingAnalyticUrl = "$cleanAwingBaseUrl/Analytic/Send"

        // ── Bước 0: Tìm card mạng Wi-Fi (theo cơ chế Japan Wi-Fi: SystemServiceNetworkPropertiesRepository.c()) ──
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        logD("[*] Bước 0: Tìm card mạng Wi-Fi và cấu hình định tuyến...")

        val allWifiNetworks = connectivityManager.allNetworks.mapNotNull { network ->
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return@mapNotNull null
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Pair(network, caps)
            } else null
        }

        // Ưu tiên: Captive Portal / Validated > Internet > bất kỳ Wi-Fi nào
        val selectedWifi = allWifiNetworks.firstOrNull { (_, caps) ->
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) ||
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: allWifiNetworks.firstOrNull { (_, caps) ->
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: allWifiNetworks.firstOrNull()

        val wifiNetwork = selectedWifi?.first

        if (wifiNetwork == null) {
            return@withContext Result.failure(Exception("Không tìm thấy kết nối Wi-Fi. Vui lòng bật Wi-Fi và kết nối vào mạng Wi-Fi KTX."))
        }

        logD("[*] Tìm thấy mạng Wi-Fi. Ràng buộc tiến trình vào mạng Wi-Fi (bindProcessToNetwork)...")
        // ── Ràng buộc tiến trình: đảm bảo DNS + socket đều đi qua Wi-Fi (theo cơ chế Japan Wi-Fi: r8.b + C8.B0) ──
        connectivityManager.bindProcessToNetwork(wifiNetwork)

        try {
            // Tạo OkHttpClient định tuyến trực tiếp qua Wi-Fi (double insurance: socketFactory + process binding)
            val client = okHttpClient.newBuilder()
                .socketFactory(wifiNetwork.socketFactory)
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .build()

            logD( "[*] Bước 1: Lấy thông tin từ Router...")
            
            val loginRequest = Request.Builder()
                .url(gatewayLogin)
                .build()

            var loginHtml = ""
            try {
                client.newCall(loginRequest).execute().use { response ->
                    val cookies = response.headers("set-cookie")
                    if (cookies.isNotEmpty()) {
                        routerCookies = cookies.joinToString("; ") { it.substringBefore(";") }
                    }
                    loginHtml = response.body?.string() ?: ""
                }
            } catch (e: Exception) {
                logE( "[-] Lỗi kết nối Router: ${e.message}")
                return@withContext Result.failure(Exception("Không thể kết nối đến Router. Hãy chắc chắn bạn đang kết nối đúng Wifi KTX (Wifi QC miễn phí)."))
            }

            val infoRegex = Regex("""const\s+wifiInfo\s*=\s*(\{[\s\S]*?\})\s*;""")
            val matchResult = infoRegex.find(loginHtml)
            
            if (matchResult == null) {
                logD( "[-] Không tìm thấy wifiInfo. Có thể do kẹt session hoặc đang ở trang status.")
                if (retryCount >= 2) {
                    return@withContext Result.failure(Exception("Không tìm thấy thông tin cấu hình Wifi. Hãy chắc chắn bạn đã kết nối vào đúng Wifi KTX."))
                }
                logD( "    -> Tiến hành Force Logout...")
                
                val logoutRequest = Request.Builder()
                    .url(gatewayLogout)
                    .header("Referer", gatewayStatus)
                    .header("Cookie", routerCookies)
                    .build()
                    
                try {
                    client.newCall(logoutRequest).execute().close()
                } catch (e: Exception) {
                    logE( "[-] Lỗi khi gửi lệnh Logout: ${e.message}")
                }
                logD( "    -> Đã gửi lệnh Logout thành công. Đang thử lại (Retry) sau 3s...")
                
                delay(3000)
                // Gọi đệ quy trong cùng scope binding (không unbind rồi rebind lại)
                return@withContext executeLoginRetry(client, gatewayUrl, awingBaseUrl, retryCount + 1)
            }

            val wifiInfoRawStr = matchResult.groupValues[1]
            logD( "    wifiInfo raw: $wifiInfoRawStr")

            // Parse JS object literal: handles both { mac: 'val' } and { "mac": "val" }
            // Regex matches: key (with or without quotes) : 'value' or "value"
            val kvRegex = Regex("""['"]?(\w[\w\-]*)['"]?\s*:\s*['"]([^'"]*?)['"]""")
            val wifiInfoMap = mutableMapOf<String, String>()
            kvRegex.findAll(wifiInfoRawStr).forEach { match ->
                wifiInfoMap[match.groupValues[1]] = match.groupValues[2]
            }
            
            logD( "    Parsed wifiInfo: $wifiInfoMap")

            if (wifiInfoMap["logged-in"] == "yes") {
                logD( "[+] Bạn đã đăng nhập Internet rồi!")
                return@withContext Result.success(Unit)
            }

            val mac = wifiInfoMap["mac"] ?: ""
            val ip = wifiInfoMap["ip"] ?: ""
            val chapIdRaw = wifiInfoMap["chap_id"] ?: ""
            val chapChallengeRaw = wifiInfoMap["chap_challenge"] ?: ""

            // Chuyển đổi sang byte và mã hóa lại dạng chuẩn 3 ký số như index.js
            val chapIdStr = toUrlOctal(parseOctalString(chapIdRaw))
            val chapChallengeStr = toUrlOctal(parseOctalString(chapChallengeRaw))

            logD( "    MAC: $mac, IP: $ip")
            logD( "    chap_id (raw): $chapIdRaw -> normalized: $chapIdStr")
            logD( "    chap_challenge (raw): $chapChallengeRaw -> normalized: $chapChallengeStr")

            val awingUrl = "$awingLoginUrl?serial=CC:2D:E0:1C:00:67&client_mac=$mac&client_ip=$ip&userurl=&login_url=$gatewayLogin&chap_id=$chapIdStr&chap_challenge=$chapChallengeStr"
            
            logD( "\n[*] Bước 3: Gọi API VerifyUrl của AWING để lấy Form...")

            val verifyRequest = Request.Builder()
                .url(awingVerifyUrl)
                .post("".toRequestBody("application/json".toMediaType()))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .header("Referer", awingUrl)
                .header("Origin", cleanAwingBaseUrl)
                .build()

            var verifyResponseJsonStr = ""
            client.newCall(verifyRequest).execute().use { response ->
                verifyResponseJsonStr = response.body?.string() ?: ""
            }

            val verifyResponseJson = json.parseToJsonElement(verifyResponseJsonStr).jsonObject
            val captiveContext = verifyResponseJson["captiveContext"]?.jsonObject
            val contentForm = captiveContext?.get("contentAuthenForm")?.jsonPrimitive?.content ?: ""

            if (contentForm.isEmpty()) {
                logE( "[-] Không tìm thấy contentAuthenForm trong phản hồi VerifyUrl.")
                return@withContext Result.failure(Exception("Không nhận được biểu mẫu đăng nhập từ hệ thống quảng cáo."))
            }

            logD( "\n[*] Bước 4: Trích xuất thông tin CHAP password...")
            val userMatch = Regex("""name="username"\s+value="([^"]+)"""").find(contentForm)
            val passMatch = Regex("""name="password"\s+value="([^"]+)"""").find(contentForm)

            if (userMatch == null || passMatch == null) {
                logE( "[-] Không thể parse username/password từ Form HTML.")
                return@withContext Result.failure(Exception("Không thể nhận dạng thông tin tài khoản quảng cáo."))
            }

            val username = userMatch.groupValues[1]
            val password = passMatch.groupValues[1]

            logD( "    Username: $username")
            logD( "    Password (from AWING Form): $password")

            logD( "\n[*] Bước 4.5: Gửi Analytic (Giả lập đã xem quảng cáo)...")
            
            val currentDTOStr = verifyResponseJsonStr.replace(""""userContext":\{[^\}]*\}""", """"userContext":{}""")

            val viewAnalyticBody = """
                {
                    "captiveContextDTO": $currentDTOStr,
                    "analyticType": "View",
                    "viewIndex": 1
                }
            """.trimIndent()

            val viewAnalyticReq = Request.Builder()
                .url(awingAnalyticUrl)
                .post(viewAnalyticBody.toRequestBody("application/json".toMediaType()))
                .header("User-Agent", USER_AGENT)
                .header("Referer", awingUrl)
                .header("Origin", cleanAwingBaseUrl)
                .build()

            var newTokenDTOStr = currentDTOStr
            try {
                client.newCall(viewAnalyticReq).execute().use { res ->
                    val viewRespStr = res.body?.string() ?: ""
                    if (viewRespStr.contains("token")) {
                        newTokenDTOStr = viewRespStr
                    }
                }
            } catch (e: Exception) {
                logE( "[-] Lỗi gửi Analytic View: ${e.message}")
            }

            val clickAnalyticBody = """
                {
                    "captiveContextDTO": $newTokenDTOStr,
                    "analyticType": "Click",
                    "viewIndex": 0
                }
            """.trimIndent()

            val clickAnalyticReq = Request.Builder()
                .url(awingAnalyticUrl)
                .post(clickAnalyticBody.toRequestBody("application/json".toMediaType()))
                .header("User-Agent", USER_AGENT)
                .header("Referer", awingUrl)
                .header("Origin", cleanAwingBaseUrl)
                .build()

            try {
                client.newCall(clickAnalyticReq).execute().close()
            } catch (e: Exception) {
                logE( "[-] Lỗi gửi Analytic Click: ${e.message}")
            }

            logD( "\n[*] Bước 5: Gửi POST đăng nhập đến Mikrotik Router...")
            
            val loginParams = FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("dst", awingSuccessUrl)
                .add("popup", "false")
                .build()

            val mikrotikLoginReq = Request.Builder()
                .url(gatewayLogin)
                .post(loginParams)
                .header("User-Agent", USER_AGENT)
                // KHÔNG gửi Cookie ở bước này theo đúng logic của index.js
                .build()

            var postHtml = ""
            client.newCall(mikrotikLoginReq).execute().use { res ->
                postHtml = res.body?.string() ?: ""
            }

            // The login page's JS code contains the word "success", so checking contains("success") is unreliable.
            // We should check for the hotspot status page title or the logged-in status variable.
            val resultMatch = infoRegex.find(postHtml)
            if (resultMatch != null) {
                val resultJsonStr = resultMatch.groupValues[1]
                val loggedIn = Regex("""['"]?logged-in['"]?\s*:\s*['"]([^'"]+)['"]""").find(resultJsonStr)?.groupValues?.get(1)
                
                if (loggedIn == "yes") {
                    logD( "\n[+] ĐĂNG NHẬP THÀNH CÔNG! Đã có Internet.")
                    Result.success(Unit)
                } else {
                    val errorMatch = Regex("""['"]?error['"]?\s*:\s*['"]([^'"]+)['"]""").find(resultJsonStr)?.groupValues?.get(1)
                    logD( "\n[-] Đăng nhập thất bại. Lỗi từ Router: ${errorMatch ?: "Không rõ"}")
                    Result.failure(Exception("Gia hạn thất bại: ${errorMatch ?: "Lỗi từ Router"}"))
                }
            } else if (postHtml.contains("<title>hotspot > status</title>", ignoreCase = true) || postHtml.contains("You are logged in", ignoreCase = true)) {
                logD( "\n[+] ĐĂNG NHẬP THÀNH CÔNG! Đã có Internet.")
                Result.success(Unit)
            } else {
                logD( "\n[?] Không xác định được phản hồi, nhưng request đã được gửi đi.")
                logD( "Nội dung phản hồi: \n${postHtml.take(500)}") // In ra 500 ký tự đầu để debug
                Result.success(Unit)
            }

        } catch (e: Exception) {
            logE("[-] Lỗi trong quá trình đăng nhập Wifi: ${e.message}")
            Result.failure(e)
        } finally {
            // ── Hủy ràng buộc tiến trình (theo cơ chế Japan Wi-Fi: C8.B0 line 68) ──
            logD("[*] Hủy ràng buộc tiến trình khỏi mạng Wi-Fi (bindProcessToNetwork(null))")
            connectivityManager.bindProcessToNetwork(null)
        }
    }

    /**
     * Thực hiện lại luồng đăng nhập trong cùng scope binding (không cần rebind mạng).
     * Được gọi khi cần retry sau khi force-logout session cũ.
     */
    private suspend fun executeLoginRetry(
        client: OkHttpClient,
        gatewayUrl: String,
        awingBaseUrl: String,
        retryCount: Int
    ): Result<Unit> {
        val cleanGatewayUrl = gatewayUrl.trim().removeSuffix("/")
        val cleanAwingBaseUrl = awingBaseUrl.trim().removeSuffix("/")

        val gatewayLogin = "$cleanGatewayUrl/login"
        val gatewayLogout = "$cleanGatewayUrl/logout?"
        val gatewayStatus = "$cleanGatewayUrl/status"

        val awingLoginUrl = "$cleanAwingBaseUrl/login"
        val awingSuccessUrl = "$cleanAwingBaseUrl/Success"
        val awingVerifyUrl = "$cleanAwingBaseUrl/Home/VerifyUrl"
        val awingAnalyticUrl = "$cleanAwingBaseUrl/Analytic/Send"

        logD("[*] Retry lần $retryCount: Bắt đầu lại luồng đăng nhập...")

        logD("[*] Bước 1: Lấy thông tin từ Router...")

        val loginRequest = Request.Builder()
            .url(gatewayLogin)
            .build()

        var loginHtml = ""
        try {
            client.newCall(loginRequest).execute().use { response ->
                val cookies = response.headers("set-cookie")
                if (cookies.isNotEmpty()) {
                    routerCookies = cookies.joinToString("; ") { it.substringBefore(";") }
                }
                loginHtml = response.body?.string() ?: ""
            }
        } catch (e: Exception) {
            logE("[-] Lỗi kết nối Router: ${e.message}")
            return Result.failure(Exception("Không thể kết nối đến Router. Hãy chắc chắn bạn đang kết nối đúng Wifi KTX (Wifi QC miễn phí)."))
        }

        val infoRegex = Regex("""const\s+wifiInfo\s*=\s*(\{[\s\S]*?\})\s*;""")
        val matchResult = infoRegex.find(loginHtml)

        if (matchResult == null) {
            logD("[-] Retry: Vẫn không tìm thấy wifiInfo sau retry lần $retryCount.")
            return if (retryCount >= 2) {
                Result.failure(Exception("Không tìm thấy thông tin cấu hình Wifi. Hãy chắc chắn bạn đã kết nối vào đúng Wifi KTX."))
            } else {
                logD("    -> Tiến hành Force Logout lần nữa...")
                val logoutRequest = Request.Builder()
                    .url(gatewayLogout)
                    .header("Referer", gatewayStatus)
                    .header("Cookie", routerCookies)
                    .build()
                try {
                    client.newCall(logoutRequest).execute().close()
                } catch (e: Exception) {
                    logE("[-] Lỗi khi gửi lệnh Logout: ${e.message}")
                }
                delay(3000)
                executeLoginRetry(client, gatewayUrl, awingBaseUrl, retryCount + 1)
            }
        }

        val wifiInfoRawStr = matchResult.groupValues[1]
        logD("    wifiInfo raw: $wifiInfoRawStr")

        val kvRegex = Regex("""['"]?(\w[\w\-]*)['"]?\s*:\s*['"]([^'"]*?)['"]""")
        val wifiInfoMap = mutableMapOf<String, String>()
        kvRegex.findAll(wifiInfoRawStr).forEach { match ->
            wifiInfoMap[match.groupValues[1]] = match.groupValues[2]
        }
        logD("    Parsed wifiInfo: $wifiInfoMap")

        if (wifiInfoMap["logged-in"] == "yes") {
            logD("[+] Bạn đã đăng nhập Internet rồi!")
            return Result.success(Unit)
        }

        val mac = wifiInfoMap["mac"] ?: ""
        val ip = wifiInfoMap["ip"] ?: ""
        val chapIdRaw = wifiInfoMap["chap_id"] ?: ""
        val chapChallengeRaw = wifiInfoMap["chap_challenge"] ?: ""

        val chapIdStr = toUrlOctal(parseOctalString(chapIdRaw))
        val chapChallengeStr = toUrlOctal(parseOctalString(chapChallengeRaw))

        logD("    MAC: $mac, IP: $ip")

        val awingUrl = "$awingLoginUrl?serial=CC:2D:E0:1C:00:67&client_mac=$mac&client_ip=$ip&userurl=&login_url=$gatewayLogin&chap_id=$chapIdStr&chap_challenge=$chapChallengeStr"

        logD("\n[*] Bước 3: Gọi API VerifyUrl của AWING để lấy Form...")

        val verifyRequest = Request.Builder()
            .url(awingVerifyUrl)
            .post("".toRequestBody("application/json".toMediaType()))
            .header("Content-Type", "application/json")
            .header("User-Agent", USER_AGENT)
            .header("Referer", awingUrl)
            .header("Origin", cleanAwingBaseUrl)
            .build()

        var verifyResponseJsonStr = ""
        client.newCall(verifyRequest).execute().use { response ->
            verifyResponseJsonStr = response.body?.string() ?: ""
        }

        val verifyResponseJson = json.parseToJsonElement(verifyResponseJsonStr).jsonObject
        val captiveContext = verifyResponseJson["captiveContext"]?.jsonObject
        val contentForm = captiveContext?.get("contentAuthenForm")?.jsonPrimitive?.content ?: ""

        if (contentForm.isEmpty()) {
            logE("[-] Không tìm thấy contentAuthenForm trong phản hồi VerifyUrl.")
            return Result.failure(Exception("Không nhận được biểu mẫu đăng nhập từ hệ thống quảng cáo."))
        }

        logD("\n[*] Bước 4: Trích xuất thông tin CHAP password...")
        val userMatch = Regex("""name="username"\s+value="([^"]+)"""").find(contentForm)
        val passMatch = Regex("""name="password"\s+value="([^"]+)"""").find(contentForm)

        if (userMatch == null || passMatch == null) {
            logE("[-] Không thể parse username/password từ Form HTML.")
            return Result.failure(Exception("Không thể nhận dạng thông tin tài khoản quảng cáo."))
        }

        val username = userMatch.groupValues[1]
        val password = passMatch.groupValues[1]

        logD("    Username: $username")
        logD("    Password (from AWING Form): $password")

        logD("\n[*] Bước 4.5: Gửi Analytic (Giả lập đã xem quảng cáo)...")

        val currentDTOStr = verifyResponseJsonStr.replace(""""userContext":\{[^\}]*\}""", """"userContext":{}""")

        val viewAnalyticBody = """
            {
                "captiveContextDTO": $currentDTOStr,
                "analyticType": "View",
                "viewIndex": 1
            }
        """.trimIndent()

        val viewAnalyticReq = Request.Builder()
            .url(awingAnalyticUrl)
            .post(viewAnalyticBody.toRequestBody("application/json".toMediaType()))
            .header("User-Agent", USER_AGENT)
            .header("Referer", awingUrl)
            .header("Origin", cleanAwingBaseUrl)
            .build()

        var newTokenDTOStr = currentDTOStr
        try {
            client.newCall(viewAnalyticReq).execute().use { res ->
                val viewRespStr = res.body?.string() ?: ""
                if (viewRespStr.contains("token")) {
                    newTokenDTOStr = viewRespStr
                }
            }
        } catch (e: Exception) {
            logE("[-] Lỗi gửi Analytic View: ${e.message}")
        }

        val clickAnalyticBody = """
            {
                "captiveContextDTO": $newTokenDTOStr,
                "analyticType": "Click",
                "viewIndex": 0
            }
        """.trimIndent()

        val clickAnalyticReq = Request.Builder()
            .url(awingAnalyticUrl)
            .post(clickAnalyticBody.toRequestBody("application/json".toMediaType()))
            .header("User-Agent", USER_AGENT)
            .header("Referer", awingUrl)
            .header("Origin", cleanAwingBaseUrl)
            .build()

        try {
            client.newCall(clickAnalyticReq).execute().close()
        } catch (e: Exception) {
            logE("[-] Lỗi gửi Analytic Click: ${e.message}")
        }

        logD("\n[*] Bước 5: Gửi POST đăng nhập đến Mikrotik Router...")

        val loginParams = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .add("dst", awingSuccessUrl)
            .add("popup", "false")
            .build()

        val mikrotikLoginReq = Request.Builder()
            .url(gatewayLogin)
            .post(loginParams)
            .header("User-Agent", USER_AGENT)
            .build()

        var postHtml = ""
        client.newCall(mikrotikLoginReq).execute().use { res ->
            postHtml = res.body?.string() ?: ""
        }

        val resultMatch = infoRegex.find(postHtml)
        return if (resultMatch != null) {
            val resultJsonStr = resultMatch.groupValues[1]
            val loggedIn = Regex("""['"]?logged-in['"]?\s*:\s*['"]([^'"]+)['"]""").find(resultJsonStr)?.groupValues?.get(1)

            if (loggedIn == "yes") {
                logD("\n[+] ĐĂNG NHẬP THÀNH CÔNG! Đã có Internet.")
                Result.success(Unit)
            } else {
                val errorMatch = Regex("""['"]?error['"]?\s*:\s*['"]([^'"]+)['"]""").find(resultJsonStr)?.groupValues?.get(1)
                logD("\n[-] Đăng nhập thất bại. Lỗi từ Router: ${errorMatch ?: "Không rõ"}")
                Result.failure(Exception("Gia hạn thất bại: ${errorMatch ?: "Lỗi từ Router"}"))
            }
        } else if (postHtml.contains("<title>hotspot > status</title>", ignoreCase = true) || postHtml.contains("You are logged in", ignoreCase = true)) {
            logD("\n[+] ĐĂNG NHẬP THÀNH CÔNG! Đã có Internet.")
            Result.success(Unit)
        } else {
            logD("\n[?] Không xác định được phản hồi, nhưng request đã được gửi đi.")
            logD("Nội dung phản hồi: \n${postHtml.take(500)}")
            Result.success(Unit)
        }
    }

    private fun parseOctalString(input: String): ByteArray {
        val bytes = java.io.ByteArrayOutputStream()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (c == '\\') {
                if (i + 1 < input.length) {
                    val next = input[i + 1]
                    if (next == '\\') {
                        bytes.write('\\'.code)
                        i += 2
                    } else if (next in '0'..'7') {
                        var numStr = ""
                        var j = i + 1
                        while (j < input.length && j < i + 4 && input[j] in '0'..'7') {
                            numStr += input[j]
                            j++
                        }
                        val byteValue = numStr.toInt(8).toByte()
                        bytes.write(byteValue.toInt())
                        i = j
                    } else {
                        bytes.write(c.code)
                        i++
                    }
                } else {
                    bytes.write(c.code)
                    i++
                }
            } else {
                bytes.write(c.code)
                i++
            }
        }
        return bytes.toByteArray()
    }

    private fun toUrlOctal(bytes: ByteArray): String {
        val sb = java.lang.StringBuilder()
        for (b in bytes) {
            val u = b.toInt() and 0xFF
            val octal = Integer.toOctalString(u)
            sb.append("\\").append(octal.padStart(3, '0'))
        }
        return sb.toString()
    }
}
