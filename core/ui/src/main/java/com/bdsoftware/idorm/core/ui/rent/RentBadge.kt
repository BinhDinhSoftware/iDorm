package com.bdsoftware.idorm.core.ui.rent

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bdsoftware.idorm.core.designsystem.component.AppBadge

/**
 * Component RentBadge chuyên dùng cho lịch sử thuê, sử dụng lại AppBadge.
 */
@Composable
fun RentBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val statusLower = status.trim().lowercase()
    val (backgroundColor, textColor) = when {
        statusLower.contains("gia hạn") -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // Xanh lá
        statusLower.contains("thuê phòng") -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0)) // Xanh dương
        statusLower.contains("tạm ngưng") -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // Đỏ
        statusLower.contains("chuyển phòng") -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A)) // Tím
        else -> Pair(Color(0xFFF5F5F5), Color(0xFF616161)) // Xám
    }

    AppBadge(
        text = status,
        backgroundColor = backgroundColor,
        textColor = textColor,
        modifier = modifier,
        shape = CircleShape
    )
}
