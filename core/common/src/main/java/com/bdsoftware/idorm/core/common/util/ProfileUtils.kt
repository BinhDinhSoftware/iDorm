package com.bdsoftware.idorm.core.common.util

/**
 * Format ISO datetime (e.g. "2004-10-10T00:00:00") to "dd/MM/yyyy".
 * Returns empty string if input is null/blank or unparseable.
 */
fun formatDateString(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val datePart = if (isoDate.contains("T")) {
            isoDate.substringBefore("T")
        } else if (isoDate.length >= 10) {
            isoDate.substring(0, 10)
        } else {
            isoDate
        }
        val parts = datePart.split("-")
        if (parts.size == 3 && parts[0].length == 4) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            isoDate
        }
    } catch (_: Exception) {
        isoDate
    }
}

/**
 * Normalize address strings — splits by comma, trims whitespace,
 * and joins with a single comma and space.
 * e.g. "xã Tuy Phước Đông,tỉnh Bình Định" → "xã Tuy Phước Đông, tỉnh Bình Định"
 */
fun normalizeAddressString(address: String?): String {
    if (address.isNullOrBlank()) return ""
    return address
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(", ")
}
