package com.bdsoftware.idorm.feature.hcmc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import com.bdsoftware.idorm.core.designsystem.component.topbar.CenterTopBar
import com.bdsoftware.idorm.core.network.model.NetworkHcmcUserRequestItem
import com.bdsoftware.idorm.core.ui.request.RequestBadge
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bdsoftware.idorm.core.model.HcmcRequestState
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HcmcScreen(
    onBack: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HcmcViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val primaryBlue = ComponentStyles.PrimaryBlue
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh(showIndicator = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Clear focus when scrolling
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }


    // Filter requests by search query
    val filteredRequests = remember(uiState.requests, searchQuery) {
        uiState.requests.filter { request ->
            if (searchQuery.isBlank()) {
                true
            } else {
                val serviceName = request.service?.name ?: ""
                val name = request.name ?: ""
                val state = request.final_state ?: ""
                serviceName.contains(searchQuery, ignoreCase = true) ||
                        name.contains(searchQuery, ignoreCase = true) ||
                        state.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Group by Date
    val unknownDateLabel = stringResource(DesignR.string.hcmc_unknown_date)
    val groupedRequests = remember(filteredRequests, unknownDateLabel) {
        filteredRequests.groupBy { formatRequestDateHeader(it.request_date, unknownDateLabel) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(primaryBlue)
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        CenterTopBar(
            title = stringResource(DesignR.string.hcmc_title),
            onBack = onBack,
            actions = {
                val unreadCount = uiState.unreadNotificationsCount
                IconButton(onClick = onNavigateToNotifications) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge {
                                    Text(text = unreadCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(DesignR.string.hcmc_notification_desc),
                            tint = Color.White
                        )
                    }
                }
            },
            modifier = Modifier.statusBarsPadding()
        )

        // ── White container with rounded top corners ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                // .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color.White)
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.requests.isEmpty() -> {
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
                uiState.error != null && uiState.requests.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: stringResource(DesignR.string.hcmc_error_fallback),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.refresh() }) {
                                Text(stringResource(DesignR.string.hcmc_retry_button), color = primaryBlue)
                            }
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar
                        AppTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = stringResource(DesignR.string.hcmc_search_placeholder),
                            trailingIcon = Icons.Default.Search,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        if (groupedRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Gray.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(DesignR.string.hcmc_empty_requests),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 32.dp
                                )
                            ) {
                                groupedRequests.forEach { (dateGroup, itemsInGroup) ->
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column {
                                                // Date Header Block (matches invoice styling)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFEBF3FC))
                                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                                ) {
                                                    Text(
                                                        text = dateGroup,
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = Color(0xFF1E3A8A)
                                                    )
                                                }

                                                // Items inside the date group
                                                itemsInGroup.forEachIndexed { index, request ->
                                                    RequestListItem(
                                                        request = request,
                                                        onClick = { onNavigateToDetail(request.id) }
                                                    )
                                                    if (index < itemsInGroup.size - 1) {
                                                        HorizontalDivider(
                                                            color = Color(0xFFEEEEEE),
                                                            modifier = Modifier.padding(horizontal = 16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }

            FloatingActionButton(
                onClick = onNavigateToCreateRequest,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = primaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(DesignR.string.hcmc_new_request_desc)
                )
            }
        }
    }
}

@Composable
private fun RequestListItem(
    request: NetworkHcmcUserRequestItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.service?.name ?: stringResource(DesignR.string.hcmc_service_fallback),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF2C3E50),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val latestHistory = request.histories?.maxByOrNull { it.id }
                RequestBadge(
                    status = latestHistory?.state ?: request.final_state.orEmpty(),
                    label = latestHistory?.step_name
                )
            }

            val name = request.name
            if (!name.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF555555),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatRequestTime(request.request_date),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF5C6BC0)
                )

            }
        }
    }
}

private fun formatRequestDateHeader(dateString: String?, fallback: String): String {
    if (dateString.isNullOrBlank()) return fallback
    return try {
        val datePart = dateString.trim().substringBefore(" ")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            datePart
        }
    } catch (e: Exception) {
        dateString
    }
}

private fun formatRequestTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val timePart = dateString.trim().substringAfter(" ")
        val parts = timePart.split(":")
        if (parts.size >= 2) {
            "${parts[0]}:${parts[1]}"
        } else {
            timePart
        }
    } catch (e: Exception) {
        ""
    }
}

private fun translateState(state: String?): String {
    return when (HcmcRequestState.fromString(state)) {
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
        null -> state ?: "---"
    }
}
