package com.bdsoftware.idorm.core.common.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WifiLogEntry(
    val id: Long,
    val timestamp: String,
    val tag: String,
    val message: String,
    val isError: Boolean = false,
    val isSuccess: Boolean = false
)

object WifiLogCollector {
    private var idCounter = 0L
    private val _logs = MutableStateFlow<List<WifiLogEntry>>(emptyList())
    val logs: StateFlow<List<WifiLogEntry>> = _logs.asStateFlow()

    fun log(tag: String, message: String, isError: Boolean = false, isSuccess: Boolean = false) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = WifiLogEntry(
            id = ++idCounter,
            timestamp = timestamp,
            tag = tag,
            message = message,
            isError = isError || message.contains("[-] ") || message.contains("thất bại", ignoreCase = true),
            isSuccess = isSuccess || message.contains("[+]") || message.contains("thành công", ignoreCase = true)
        )
        _logs.value = (_logs.value + entry).takeLast(300) // Store last 300 logs
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
