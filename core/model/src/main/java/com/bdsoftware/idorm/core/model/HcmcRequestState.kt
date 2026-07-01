package com.bdsoftware.idorm.core.model

enum class HcmcRequestState(val value: String) {
    NEW("new"),
    PENDING("pending"),
    ASSIGNED("assigned"),
    REPAIRING("repairing"),
    PROCESSING("processing"),
    DONE("done"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    OVERDUE("overdue");

    companion object {
        fun fromString(state: String?): HcmcRequestState? {
            val lower = state?.lowercase()?.trim() ?: return null
            return when {
                lower == "new" || lower == "mới" -> NEW
                lower == "pending" || lower == "waiting" || lower.contains("đang chờ") || lower.contains("chờ") || lower == "dang cho" || lower == "cho xu ly" -> PENDING
                lower == "assigned" || lower == "đã phân công" || lower == "phân công" -> ASSIGNED
                lower == "repairing" || lower == "đang sửa" || lower == "sửa chữa" || lower.contains("sửa") -> REPAIRING
                lower == "processing" || lower == "đang xử lý" || lower == "dang xu ly" || lower == "in_progress" -> PROCESSING
                lower == "done" || lower == "completed" || lower == "finished" || lower == "hoàn thành" || lower == "hoan thanh" || lower == "success" || lower == "thành công" || lower == "hoàn tất" -> DONE
                lower == "approved" || lower == "đã duyệt" || lower == "da duyet" || lower == "passed" || lower == "chấp nhận" || lower == "chap nhan" || lower == "đạt" || lower == "dat" -> APPROVED
                lower == "rejected" || lower == "từ chối" || lower == "tu choi" || lower == "denied" || lower == "failed" -> REJECTED
                lower == "cancelled" || lower == "đã hủy" || lower == "da huy" -> CANCELLED
                lower == "overdue" || lower == "quá hạn" || lower == "qua han" -> OVERDUE
                else -> values().firstOrNull { it.value == lower }
            }
        }
    }
}
