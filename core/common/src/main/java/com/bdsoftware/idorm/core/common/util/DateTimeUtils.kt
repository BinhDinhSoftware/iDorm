package com.bdsoftware.idorm.core.common.util

/**
 * Tách chuỗi ngày giờ (dạng "yyyy-MM-dd HH:mm:ss") thành cặp (ngày, giờ).
 * Ngày trả về dạng "dd/MM", giờ trả về dạng "HH:mm".
 * Nếu chuỗi rỗng hoặc null, trả về ("---", "").
 */
fun splitDateTime(dateStr: String?): Pair<String, String> {
    if (dateStr.isNullOrBlank()) return Pair("---", "")
    val trimmed = dateStr.trim().replace("T", " ")
    val datePart = trimmed.substringBefore(" ")
    val timePart = trimmed.substringAfter(" ", "")

    val formattedDate = try {
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}"
        } else {
            datePart
        }
    } catch (e: Exception) {
        datePart
    }

    val formattedTime = try {
        if (timePart.isNotBlank()) {
            val cleanTime = timePart.substringBefore(".")
            val parts = cleanTime.split(":")
            if (parts.size >= 2) {
                "${parts[0]}:${parts[1]}"
            } else {
                cleanTime
            }
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }

    return Pair(formattedDate, formattedTime)
}

/**
 * Format chuỗi ngày giờ thành dạng "HH:mm dd/MM/yyyy".
 * Nếu chuỗi rỗng hoặc null, trả về "---".
 */
fun formatRequestDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "---"
    return try {
        val trimmed = dateString.trim()
        val datePart = trimmed.substringBefore(" ")
        val timePart = trimmed.substringAfter(" ")

        val dateParts = datePart.split("-")
        val dateStr = if (dateParts.size == 3) {
            "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}"
        } else {
            datePart
        }

        val timeParts = timePart.split(":")
        val timeStr = if (timeParts.size >= 2) {
            "${timeParts[0]}:${timeParts[1]}"
        } else {
            timePart
        }

        if (timeStr.isNotBlank() && timeStr != dateStr) {
            "$timeStr $dateStr"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Format chuỗi ngày ISO (dạng "yyyy-MM-ddTHH:mm:ss") thành "dd/MM/yyyy".
 * Hỗ trợ cả dạng có chữ "T" và dạng có khoảng trắng.
 */
fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "---"
    return try {
        val datePart = when {
            isoDate.contains("T") -> isoDate.substringBefore("T")
            isoDate.contains(" ") -> isoDate.substringBefore(" ")
            else -> isoDate
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
 * Format chuỗi ngày ISO (dạng "yyyy-MM-ddTHH:mm:ss") thành "dd/MM/yy".
 */
fun formatShortDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "---"
    return try {
        val datePart = when {
            isoDate.contains("T") -> isoDate.substringBefore("T")
            isoDate.contains(" ") -> isoDate.substringBefore(" ")
            else -> isoDate
        }
        val parts = datePart.split("-")
        if (parts.size == 3 && parts[0].length == 4) {
            val shortYear = parts[0].substring(2)
            "${parts[2]}/${parts[1]}/$shortYear"
        } else {
            isoDate
        }
    } catch (_: Exception) {
        isoDate
    }
}
