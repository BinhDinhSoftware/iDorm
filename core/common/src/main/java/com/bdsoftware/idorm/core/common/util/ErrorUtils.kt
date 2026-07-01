package com.bdsoftware.idorm.core.common.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Converts a [Throwable] into a user-friendly error message,
 * sanitizing technical details like hostnames, stack traces, etc.
 *
 * This should be used in ViewModels before exposing error messages to the UI
 * to avoid leaking internal infrastructure details to end users.
 */
fun Throwable.toUserMessage(fallback: String = "Đã xảy ra lỗi, vui lòng thử lại sau"): String {
    // Tự động ghi nhận lỗi phi hệ thống (Non-fatal) trừ các lỗi về mạng/kết nối thông thường
    if (this !is UnknownHostException &&
        this !is ConnectException &&
        this !is SocketTimeoutException &&
        this !is SSLException
    ) {
        CrashlyticsUtils.recordException(this)
    }

    return when (this) {
        is UnknownHostException ->
            "Không có kết nối mạng. Vui lòng kiểm tra kết nối Internet và thử lại."

        is ConnectException ->
            "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại."

        is SocketTimeoutException ->
            "Kết nối đã hết thời gian chờ. Vui lòng thử lại sau."

        is SSLException ->
            "Lỗi bảo mật kết nối. Vui lòng thử lại sau."

        else -> {
            // For non-network exceptions, use the message if it looks safe
            // (i.e. it was likely set by our own code, not a system exception).
            // If the message looks like it contains a hostname or technical detail, use fallback.
            val msg = message
            if (msg.isNullOrBlank() || looksLikeTechnicalMessage(msg)) {
                fallback
            } else {
                msg
            }
        }
    }
}

/**
 * Simple heuristic to detect technical exception messages that should not
 * be shown to users. Returns true if the message likely contains
 * hostnames, IP addresses, stack traces, or Java class references.
 */
private fun looksLikeTechnicalMessage(message: String): Boolean {
    // Contains common patterns from system/network exceptions
    return message.contains("Unable to resolve host", ignoreCase = true) ||
        message.contains("failed to connect to", ignoreCase = true) ||
        message.contains("CLEARTEXT", ignoreCase = true) ||
        message.contains("EHOSTUNREACH", ignoreCase = true) ||
        message.contains("ECONNREFUSED", ignoreCase = true) ||
        message.contains("ENETUNREACH", ignoreCase = true) ||
        message.matches(Regex(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) || // IP address
        message.contains("java.", ignoreCase = false) ||
        message.contains("javax.", ignoreCase = false) ||
        message.contains("okhttp", ignoreCase = true) ||
        message.contains("retrofit", ignoreCase = true)
}
