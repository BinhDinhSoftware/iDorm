package com.bdsoftware.idorm.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bdsoftware.idorm.core.common.util.splitDateTime

/**
 * Dữ liệu cho mỗi mục trong timeline.
 *
 * @param title Tiêu đề (ví dụ: tên bước xử lý, trạng thái thuê)
 * @param date Chuỗi ngày giờ (dạng "yyyy-MM-dd HH:mm:ss")
 * @param subtitle Phụ đề (ví dụ: người thực hiện, tên phòng)
 * @param description Mô tả / ghi chú
 * @param isActive Đánh dấu item đang active (hiển thị nổi bật)
 */
data class TimelineItemData(
    val title: String,
    val date: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    val isActive: Boolean = false
)

/**
 * Component timeline tái sử dụng - hiển thị danh sách sự kiện theo trục dọc.
 *
 * @param items Danh sách dữ liệu timeline
 * @param modifier Modifier tùy chỉnh
 * @param activeColor Màu cho item active (mặc định xanh lá)
 * @param itemContent Slot mở rộng nội dung cho từng item
 */
@Composable
fun AppTimeline(
    items: List<TimelineItemData>,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF4CAF50),
    titleContent: @Composable ((index: Int, item: TimelineItemData) -> Unit)? = null,
    itemContent: @Composable (ColumnScope.(index: Int, item: TimelineItemData) -> Unit)? = null
) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            TimelineItemRow(
                item = item,
                isFirst = index == 0,
                isLast = index == items.lastIndex,
                activeColor = activeColor,
                titleContent = titleContent?.let { { it(index, item) } },
                content = itemContent?.let { { it(index, item) } }
            )
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: TimelineItemData,
    isFirst: Boolean,
    isLast: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    titleContent: @Composable (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val inactiveColor = Color(0xFFBDBDBD)
    val isHighlighted = item.isActive || isFirst
    val dotColor = if (isHighlighted) activeColor else inactiveColor
    val textColor = if (isHighlighted) Color(0xFF1E293B) else Color(0xFF78909C)
    val titleWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal

    val (datePart, timePart) = splitDateTime(item.date)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // ── Cột trái: Ngày & Giờ ──
        Column(
            modifier = Modifier.width(64.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = datePart,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 12.sp
                ),
                color = textColor,
                maxLines = 1
            )
            if (timePart.isNotBlank()) {
                Text(
                    text = timePart,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp
                    ),
                    color = if (isHighlighted) Color(0xFF78909C) else Color(0xFFB0BEC5),
                    maxLines = 1
                )
            }
        }

        // ── Cột giữa: Chấm tròn + Đường kẻ dọc ──
        Column(
            modifier = Modifier
                .width(32.dp)
                .padding(top = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (isHighlighted) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .defaultMinSize(minHeight = 44.dp)
                        .background(Color(0xFFE0E0E0))
                )
            }
        }

        // ── Cột phải: Nội dung ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 24.dp)
        ) {
            // Tiêu đề
            if (titleContent != null) {
                titleContent()
            } else {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = titleWeight,
                        fontSize = 15.sp
                    ),
                    color = textColor
                )
            }

            // Phụ đề
            val subtitle = item.subtitle
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp
                    ),
                    color = if (isHighlighted) Color(0xFF546E7A) else Color(0xFFB0BEC5),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Mô tả / Ghi chú
            val description = item.description
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    color = if (isHighlighted) Color(0xFF546E7A) else Color(0xFFB0BEC5),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            content?.invoke(this)
        }
    }
}
