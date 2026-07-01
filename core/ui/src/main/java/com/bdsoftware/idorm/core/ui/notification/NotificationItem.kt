package com.bdsoftware.idorm.core.ui.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.text.HtmlCompat
import com.bdsoftware.idorm.core.designsystem.component.AppNotificationItem
import com.bdsoftware.idorm.core.network.model.NotificationData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log

@Composable
fun NotificationItem(
    notification: NotificationData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Email
) {
    val plainTextContent = remember(notification.Content) {
        HtmlCompat.fromHtml(
            notification.Content,
            HtmlCompat.FROM_HTML_MODE_COMPACT
        ).toString().trim()
    }

    val formattedDate = remember(notification.CreatedDate) {
        formatNotificationDate(notification.CreatedDate)
    }
    // log Titles and IsRead
    AppNotificationItem(
        title = notification.Titles,
        content = plainTextContent,
        date = formattedDate,
        isRead = true,
        onClick = onClick,
        icon = icon,
        modifier = modifier
    )
}

/**
 * Parses an ISO date string from the API and formats it as "HH:mm, dd/MM/yyyy".
 * Falls back to the raw string on parse errors.
 */
private fun formatNotificationDate(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString)
        DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").format(dateTime)
    } catch (e: Exception) {
        dateString
    }
}
