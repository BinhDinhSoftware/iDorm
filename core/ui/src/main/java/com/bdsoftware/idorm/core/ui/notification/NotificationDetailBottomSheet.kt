package com.bdsoftware.idorm.core.ui.notification

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.bdsoftware.idorm.core.network.model.NotificationData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailBottomSheet(
    notification: NotificationData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp)
        ) {
            // Title - Bold, UPPERCASE, Black
            Text(
                text = notification.Titles.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time - Gray
            Text(
                text = formatNotificationDate(notification.CreatedDate),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(16.dp))

            // HTML Scrollable content
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            textSize = 15f
                            setTextColor(android.graphics.Color.parseColor("#333333"))
                            setLineSpacing(0f, 1.2f)
                            // Enable clicking links inside HTML content
                            movementMethod = android.text.method.LinkMovementMethod.getInstance()
                        }
                    },
                    update = { textView ->
                        textView.text = HtmlCompat.fromHtml(
                            notification.Content,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatNotificationDate(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString)
        DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").format(dateTime)
    } catch (e: Exception) {
        dateString
    }
}
