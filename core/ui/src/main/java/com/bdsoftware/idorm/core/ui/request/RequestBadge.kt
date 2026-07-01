package com.bdsoftware.idorm.core.ui.request

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bdsoftware.idorm.core.designsystem.component.AppBadge

import com.bdsoftware.idorm.core.model.HcmcRequestState

/**
 * Component hiển thị badge trạng thái cho các yêu cầu HCMC,
 * thiết kế dựa trên các style pill có chứa icon và màu nền dịu (tint) tương ứng.
 */
@Composable
fun RequestBadge(
    status: String,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val state = HcmcRequestState.fromString(status)
    
    // Ánh xạ trạng thái sang nhãn tiếng Việt
    val displayName = label ?: when (state) {
        HcmcRequestState.NEW -> "Mới"
        HcmcRequestState.PENDING -> "Chờ duyệt"
        HcmcRequestState.ASSIGNED -> "Đã phân công"
        HcmcRequestState.REPAIRING -> "Đang sửa chữa"
        HcmcRequestState.PROCESSING -> "Đang xử lý"
        HcmcRequestState.DONE -> "Hoàn thành"
        HcmcRequestState.APPROVED -> "Đã duyệt"
        HcmcRequestState.REJECTED -> "Từ chối"
        HcmcRequestState.CANCELLED -> "Đã hủy"
        HcmcRequestState.OVERDUE -> "Quá hạn"
        null -> status
    }

    // Ánh xạ màu sắc & icon
    val (bgAndText, icon) = when (state) {
        // Mới (Blue tint)
        HcmcRequestState.NEW -> Pair(
            Pair(Color(0xFFEBF3FC), Color(0xFF1E3A8A)),
            Icons.Default.Info
        )
        // Chờ duyệt / Chờ xử lý (Orange/Peach tint)
        HcmcRequestState.PENDING -> Pair(
            Pair(Color(0xFFFFF3E0), Color(0xFFE65100)),
            Icons.Default.Info
        )
        // Đã phân công (Cyan/Light Blue tint)
        HcmcRequestState.ASSIGNED -> Pair(
            Pair(Color(0xFFE0F7FA), Color(0xFF006064)),
            Icons.Default.Info
        )
        // Đang sửa chữa (Olive/Lime tint)
        HcmcRequestState.REPAIRING -> Pair(
            Pair(Color(0xFFF9FBE7), Color(0xFF33691E)),
            Icons.Default.Build
        )
        // Đang xử lý (Orange/Peach tint)
        HcmcRequestState.PROCESSING -> Pair(
            Pair(Color(0xFFFFF3E0), Color(0xFFE65100)),
            Icons.Default.PlayArrow
        )
        // Hoàn thành / Đã duyệt (Green tint)
        HcmcRequestState.DONE, HcmcRequestState.APPROVED -> Pair(
            Pair(Color(0xFFE8F5E9), Color(0xFF1B5E20)),
            Icons.Default.CheckCircle
        )
        // Từ chối / Blocked (Pink/Red tint)
        HcmcRequestState.REJECTED, HcmcRequestState.OVERDUE -> Pair(
            Pair(Color(0xFFFFEBEE), Color(0xFFC62828)),
            Icons.Default.Close
        )
        // Đã hủy / Cancelled (Gray tint)
        HcmcRequestState.CANCELLED -> Pair(
            Pair(Color(0xFFECEFF1), Color(0xFF37474F)),
            Icons.Default.Close
        )
        null -> Pair(
            Pair(Color(0xFFF5F5F5), Color(0xFF616161)),
            Icons.Default.Info
        )
    }

    AppBadge(
        text = displayName,
        backgroundColor = bgAndText.first,
        textColor = bgAndText.second,
        icon = icon,
        modifier = modifier
    )
}
