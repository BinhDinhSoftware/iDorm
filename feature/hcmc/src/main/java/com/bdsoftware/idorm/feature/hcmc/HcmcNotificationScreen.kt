package com.bdsoftware.idorm.feature.hcmc

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.designsystem.component.AppNotificationItem
import com.bdsoftware.idorm.core.designsystem.component.topbar.CenterTopBar
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationDetailItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcNotificationItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HcmcNotificationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HcmcViewModel = hiltViewModel()
) {
    val state by viewModel.notificationsState.collectAsStateWithLifecycle()
    val primaryBlue = ComponentStyles.PrimaryBlue
    var clickedNotification by remember { mutableStateOf<NetworkHcmcNotificationItem?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(primaryBlue)
    ) {
        CenterTopBar(
            title = stringResource(DesignR.string.hcmc_notifications_title),
            onBack = onBack,
            modifier = Modifier.statusBarsPadding()
        )

        // ── Content: White container with rounded top corners ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                // .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color.White)
        ) {
            when {
                state.isLoading && state.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryBlue,
                            strokeWidth = 2.dp
                        )
                    }
                }
                state.error != null && state.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error ?: stringResource(DesignR.string.hcmc_error_fallback),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.loadNotifications() }) {
                                Text(stringResource(DesignR.string.hcmc_retry_button), color = primaryBlue)
                            }
                        }
                    }
                }
                state.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(DesignR.string.hcmc_notifications_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        itemsIndexed(state.notifications) { index, notification ->
                            val fallbackTitle = stringResource(DesignR.string.hcmc_notifications_fallback_title)
                            AppNotificationItem(
                                title = notification.title ?: fallbackTitle,
                                content = notification.body ?: "",
                                date = formatNotificationDate(notification.create_date ?: ""),
                                isRead = notification.is_read,
                                onClick = {
                                    clickedNotification = notification
                                    viewModel.selectNotification(notification)
                                }
                            )

                            // Divider between items
                            if (index < state.notifications.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    thickness = 0.5.dp,
                                    color = Color(0xFFEEEEEE)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    clickedNotification?.let { item ->
        HcmcNotificationDetailBottomSheet(
            notification = item,
            detail = state.selectedDetail,
            isLoading = state.detailLoading,
            onDismiss = {
                clickedNotification = null
                viewModel.clearSelectedDetail()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HcmcNotificationDetailBottomSheet(
    notification: NetworkHcmcNotificationItem,
    detail: NetworkHcmcNotificationDetailItem?,
    isLoading: Boolean,
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
            val fallbackTitle = stringResource(DesignR.string.hcmc_notifications_fallback_title)
            Text(
                text = (notification.title ?: fallbackTitle).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time - Gray (formatted style)
            Text(
                text = formatNotificationDate(notification.create_date ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(16.dp))

            // HTML Scrollable content matching notification detail style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .weight(1f, fill = false),
                contentAlignment = Alignment.TopStart
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = ComponentStyles.PrimaryBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (detail != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AndroidView(
                            factory = { context ->
                                TextView(context).apply {
                                    textSize = 15f
                                    setTextColor(android.graphics.Color.parseColor("#333333"))
                                    setLineSpacing(0f, 1.2f)
                                    movementMethod = android.text.method.LinkMovementMethod.getInstance()
                                }
                            },
                            update = { textView ->
                                textView.text = HtmlCompat.fromHtml(
                                    detail.body ?: "",
                                    HtmlCompat.FROM_HTML_MODE_COMPACT
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = notification.body ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF333333),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun formatNotificationDate(dateString: String): String {
    if (dateString.isBlank()) return ""
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateString.trim(), formatter)
        DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").format(dateTime)
    } catch (e: Exception) {
        try {
            val dateTime = LocalDateTime.parse(dateString.trim().replace(" ", "T"))
            DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").format(dateTime)
        } catch (e2: Exception) {
            dateString
        }
    }
}
