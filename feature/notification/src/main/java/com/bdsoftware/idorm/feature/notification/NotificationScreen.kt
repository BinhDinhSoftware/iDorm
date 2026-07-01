package com.bdsoftware.idorm.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import com.bdsoftware.idorm.core.designsystem.component.topbar.CenterTopBar
import com.bdsoftware.idorm.core.ui.notification.NotificationItem
import com.bdsoftware.idorm.core.ui.notification.NotificationDetailBottomSheet
import com.bdsoftware.idorm.core.network.model.NotificationData
import java.time.LocalDateTime
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import java.time.format.DateTimeFormatter

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }

    androidx.compose.runtime.LaunchedEffect(selectedNotification) {
        selectedNotification?.let {
            if (!it.IsRead) {
                viewModel.markNotificationAsRead(it.Id)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ComponentStyles.PrimaryBlue)
    ) {
        CenterTopBar(
            title = stringResource(DesignR.string.notification_screen_title),
            modifier = Modifier.statusBarsPadding()
        )

        // ── Content: White container with rounded top corners ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color.White)
        ) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading && notifications.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = ComponentStyles.PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    notifications.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(DesignR.string.notification_empty_text),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                        ) {
                            itemsIndexed(notifications) { index, notification ->
                                NotificationItem(
                                    notification = notification,
                                    onClick = { selectedNotification = notification }
                                )

                                // Divider between items (not after the last one)
                                if (index < notifications.lastIndex) {
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
    }

    selectedNotification?.let { notification ->
        NotificationDetailBottomSheet(
            notification = notification,
            onDismiss = { selectedNotification = null }
        )
    }
}


