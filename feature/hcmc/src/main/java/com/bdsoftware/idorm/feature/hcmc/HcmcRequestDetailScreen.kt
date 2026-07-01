package com.bdsoftware.idorm.feature.hcmc

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import com.bdsoftware.idorm.core.designsystem.component.AppTextArea
import com.bdsoftware.idorm.core.designsystem.component.AppImageField
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bdsoftware.idorm.core.common.util.formatRequestDate
import com.bdsoftware.idorm.core.common.util.splitDateTime
import com.bdsoftware.idorm.core.data.repository.SelectedFile
import com.bdsoftware.idorm.core.designsystem.component.AppTimeline
import com.bdsoftware.idorm.core.designsystem.component.TimelineItemData
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.designsystem.component.topbar.CenterTopBar
import com.bdsoftware.idorm.core.network.model.NetworkHcmcComplaintItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcHistoryItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRequestDetail
import com.bdsoftware.idorm.core.network.model.NetworkHcmcReviewItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcStepItem
import com.bdsoftware.idorm.core.network.model.NetworkHcmcStepHistoryItem
import com.bdsoftware.idorm.core.ui.request.RequestBadge
import android.util.Log
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import androidx.compose.ui.res.stringResource

import com.bdsoftware.idorm.core.model.HcmcRequestState

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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HcmcRequestDetailScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (requestId: Int, serviceId: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: HcmcRequestDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hcmcAccessToken by viewModel.hcmcAccessToken.collectAsStateWithLifecycle()
    val primaryBlue = ComponentStyles.PrimaryBlue
    val backgroundColor = Color(0xFFF8F9FA)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showCancelBottomSheet by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDetail()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val imageLoader = remember(viewModel.okHttpClient) {
        coil.ImageLoader.Builder(context)
            .okHttpClient(viewModel.okHttpClient)
            .build()
    }

    // Toast messages for submit success/error
    LaunchedEffect(uiState.submitSuccessMessage) {
        uiState.submitSuccessMessage?.let {
            val msg = if (it.contains("thành công", ignoreCase = true)) {
                context.getString(DesignR.string.hcmc_create_success_toast)
            } else if (it.contains("khiếu nại", ignoreCase = true)) {
                context.getString(DesignR.string.hcmc_detail_complaint_status_sent)
            } else {
                it
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.submitError) {
        uiState.submitError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    if (showCancelBottomSheet) {
        val focusManager = LocalFocusManager.current
        ModalBottomSheet(
            onDismissRequest = { showCancelBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(DesignR.string.hcmc_detail_cancel_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ComponentStyles.ErrorRed,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(DesignR.string.hcmc_detail_cancel_confirm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AppTextArea(
                    value = cancelReason,
                    onValueChange = { cancelReason = it },
                    label = stringResource(DesignR.string.hcmc_detail_cancel_reason_label),
                    placeholder = stringResource(DesignR.string.hcmc_detail_cancel_reason_placeholder),
                    required = false,
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    onClick = {
                        val reason = cancelReason.ifBlank { "." }
                        viewModel.cancelRequest(reason)
                        showCancelBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = ComponentStyles.errorButtonStyle,
                    isLoading = uiState.isCancelling,
                    loadingText = stringResource(DesignR.string.hcmc_detail_cancel_loading)
                ) {
                    Text(
                        text = stringResource(DesignR.string.hcmc_detail_cancel_submit),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(primaryBlue)
    ) {
        CenterTopBar(
            title = uiState.detail?.service_name ?: stringResource(DesignR.string.hcmc_detail_title),
            onBack = onBack,
            modifier = Modifier.statusBarsPadding()
        )

        // ── Main Content Container ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            when {
                uiState.isLoading -> {
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
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = uiState.error ?: stringResource(DesignR.string.hcmc_detail_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadDetail() },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                            ) {
                                Text(stringResource(DesignR.string.hcmc_retry_button), color = Color.White)
                            }
                        }
                    }
                }
                uiState.detail != null -> {
                    val detail = uiState.detail!!

                    // TabRow matching InvoiceScreen tabs styling
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color(0xFFF2F5F9),
                        contentColor = primaryBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = primaryBlue
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Text(
                                    text = stringResource(DesignR.string.hcmc_detail_tab_info),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTabIndex == 0) primaryBlue else Color.Gray
                                )
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Text(
                                    text = stringResource(DesignR.string.hcmc_detail_tab_history),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTabIndex == 1) primaryBlue else Color.Gray
                                )
                            }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (selectedTabIndex == 0) {
                            RequestInfoTab(
                                detail = detail,
                                hcmcAccessToken = hcmcAccessToken,
                                imageLoader = imageLoader,
                                primaryColor = primaryBlue,
                                isCancelling = uiState.isCancelling,
                                onCancelClick = { showCancelBottomSheet = true },
                                onEditClick = {
                                    val serviceId = detail.service_id
                                    if (serviceId != null) {
                                        onNavigateToEdit(detail.id, serviceId)
                                    }
                                }
                            )
                        } else {
                            RequestHistoryTab(
                                steps = detail.step_ids ?: emptyList(),
                                reviews = uiState.reviews,
                                complaints = uiState.complaints,
                                isReviewSubmitting = uiState.isReviewSubmitting,
                                isComplaintSubmitting = uiState.isComplaintSubmitting,
                                hcmcAccessToken = hcmcAccessToken,
                                onSubmitReview = viewModel::submitReview,
                                onSubmitComplaint = viewModel::submitComplaint,
                                primaryColor = primaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Section (matches ProfileCard styling)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(
                color = Color.Black.copy(alpha = 0.06f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Content Items
            content()
        }
    }
}

@Composable
private fun DetailInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    showDivider: Boolean = true,
    valueContent: @Composable (RowScope.() -> Unit)? = null
) {
    val displayValue = value.ifEmpty { "---" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.4f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (valueContent != null) {
            Row(
                modifier = Modifier.weight(0.6f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                valueContent()
            }
        } else {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(0.6f)
            )
        }
    }

    if (showDivider) {
        HorizontalDivider(
            color = Color.Black.copy(alpha = 0.04f),
            modifier = Modifier.padding(start = 30.dp)
        )
    }
}

@Composable
private fun RequestInfoTab(
    detail: NetworkHcmcRequestDetail,
    hcmcAccessToken: String?,
    imageLoader: coil.ImageLoader,
    primaryColor: Color,
    isCancelling: Boolean,
    onCancelClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Section 1: Chi tiết yêu cầu ──
        DetailCard(
            title = stringResource(DesignR.string.hcmc_detail_card_info_title),
            subtitle = stringResource(DesignR.string.hcmc_detail_card_info_subtitle),
            icon = Icons.Default.Info,
            primaryColor = primaryColor
        ) {

            DetailInfoItem(
                icon = Icons.Default.CheckCircle,
                label = stringResource(DesignR.string.hcmc_detail_label_status),
                value = "",
                showDivider = true
            ) {
                val latestHistory = detail.histories?.maxByOrNull { it.id }
                RequestBadge(
                    status = latestHistory?.state ?: detail.final_state.orEmpty(),
                    label = latestHistory?.step_name
                )
            }

            DetailInfoItem(icon = Icons.Default.Tag, label = stringResource(DesignR.string.hcmc_detail_label_id), value = detail.id.toString())
            DetailInfoItem(icon = Icons.Default.ListAlt, label = stringResource(DesignR.string.hcmc_detail_label_name), value = detail.name ?: "---")
            DetailInfoItem(icon = Icons.Default.Person, label = stringResource(DesignR.string.hcmc_detail_label_requester), value = detail.request_user_name ?: "---")
            DetailInfoItem(icon = Icons.Default.CalendarToday, label = stringResource(DesignR.string.hcmc_detail_label_date), value = formatRequestDate(detail.request_date))
            val isPending = HcmcRequestState.fromString(detail.final_state) == HcmcRequestState.PENDING

            DetailInfoItem(
                icon = Icons.AutoMirrored.Filled.Note,
                label = stringResource(DesignR.string.hcmc_detail_label_note),
                value = detail.note ?: "---",
                showDivider = isPending
            )

            if (isPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                        style = ComponentStyles.outlinePrimaryButtonStyle
                    ) {
                        Text(
                            text = stringResource(DesignR.string.hcmc_detail_edit_button),
                            color = primaryColor,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    AppButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        style = ComponentStyles.outlineErrorButtonStyle,
                        isLoading = isCancelling,
                        loadingText = stringResource(DesignR.string.hcmc_detail_cancel_loading)
                    ) {
                        Text(
                            text = stringResource(DesignR.string.hcmc_detail_cancel_button),
                            color = ComponentStyles.ErrorRed,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // ── Section 2: Thông tin bổ sung ──
        val inputs = detail.inputs
        if (!inputs.isNullOrEmpty()) {
            DetailCard(
                title = stringResource(DesignR.string.hcmc_detail_card_additional_title),
                subtitle = stringResource(DesignR.string.hcmc_detail_card_additional_subtitle),
                icon = Icons.Default.Description,
                primaryColor = primaryColor
            ) {
                inputs.forEachIndexed { index, input ->
                    DetailInfoItem(
                        icon = Icons.Default.Label,
                        label = input.label,
                        value = input.value_display ?: "---",
                        showDivider = index < inputs.size - 1
                    )
                }
            }
        }

        // ── Section 3: File đính kèm ──
        val attachments = detail.image_attachment_ids
        if (!attachments.isNullOrEmpty()) {
            DetailCard(
                title = stringResource(DesignR.string.hcmc_detail_card_attachment_title),
                subtitle = stringResource(DesignR.string.hcmc_detail_card_attachment_subtitle),
                icon = Icons.Default.Attachment,
                primaryColor = primaryColor
            ) {
                attachments.forEachIndexed { index, attachment ->
                    val fullImageUrl = "https://hanhchinhmotcua.ktxhcm.edu.vn" + attachment.url
                    Log.d("Image URL", fullImageUrl)
                    val imageRequest = remember(fullImageUrl, hcmcAccessToken) {
                        ImageRequest.Builder(context)
                            .data(fullImageUrl)
                            .crossfade(true)
                            .build()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageRequest,
                                imageLoader = imageLoader,
                                contentDescription = attachment.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = attachment.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (index < attachments.size - 1) {
                        HorizontalDivider(
                            color = Color.Black.copy(alpha = 0.04f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestHistoryTab(
    steps: List<NetworkHcmcStepItem>,
    reviews: List<NetworkHcmcReviewItem>,
    complaints: List<NetworkHcmcComplaintItem>,
    isReviewSubmitting: Boolean,
    isComplaintSubmitting: Boolean,
    hcmcAccessToken: String?,
    onSubmitReview: (String, String) -> Unit,
    onSubmitComplaint: (String, List<SelectedFile>) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(DesignR.string.hcmc_detail_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    } else {
        val context = LocalContext.current
        var showComplaintBottomSheet by remember { mutableStateOf(false) }

        // Sort steps by sequence descending (bottom-up) and filter out disabled steps
        val sortedSteps = remember(steps) {
            steps.filter { it.disabled != true }
                .sortedByDescending { it.sequence ?: 0 }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(
                        width = 0.5.dp,
                        color = Color.Black.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val timelineItems = sortedSteps.map { step ->
                        val stepStateEnum = HcmcRequestState.fromString(step.state) ?: HcmcRequestState.PENDING
                        TimelineItemData(
                            title = step.step_name ?: step.name ?: context.getString(DesignR.string.hcmc_detail_step_fallback),
                            date = step.approve_date,
                            subtitle = when (stepStateEnum) {
                                HcmcRequestState.APPROVED -> step.assign_user_name?.let { context.getString(DesignR.string.hcmc_detail_approver_prefix, it) }
                                HcmcRequestState.ASSIGNED -> step.assign_user_name?.let { context.getString(DesignR.string.hcmc_detail_assigned_prefix, it) }
                                else -> null
                            },
                            description = step.approve_content?.takeIf { it.isNotBlank() },
                            isActive = stepStateEnum == HcmcRequestState.ASSIGNED
                        )
                    }

                    AppTimeline(
                        items = timelineItems,
                        activeColor = Color(0xFF2196F3),
                        titleContent = { index, item ->
                            val step = sortedSteps[index]
                            val stepStateEnum = HcmcRequestState.fromString(step.state) ?: HcmcRequestState.PENDING
                            val titleWeight = if (stepStateEnum == HcmcRequestState.ASSIGNED || stepStateEnum == HcmcRequestState.APPROVED) FontWeight.Bold else FontWeight.Normal
                            val titleColor = if (stepStateEnum == HcmcRequestState.PENDING) Color(0xFF78909C) else Color(0xFF1E293B)

                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = titleWeight,
                                        fontSize = 15.sp
                                    ),
                                    color = titleColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                RequestBadge(
                                    status = stepStateEnum.value
                                )
                            }
                        },
                        itemContent = { index, _ ->
                            val step = sortedSteps[index]
                            val stepState = step.state?.lowercase()?.trim() ?: "pending"
                            val stepName = step.step_name?.lowercase() ?: ""

                            // Show assigned department
                            val deptName = step.assigned_department_name
                            if (!deptName.isNullOrBlank()) {
                                Text(
                                    text = deptName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF90A4AE),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Render embedded history_ids for this step
                            val histories = step.history_ids
                            if (!histories.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                histories.forEach { history ->
                                    StepHistoryEntry(
                                        history = history,
                                        primaryColor = primaryColor
                                    )
                                }
                            }

                            // Review section on "đánh giá chất lượng" step
                            val isReviewStep = stepName.contains("đánh giá chất lượng") || stepName.contains("quality assessment")
                            if (isReviewStep && stepState != "pending") {
                                ReviewSection(
                                    reviews = reviews,
                                    isSubmitting = isReviewSubmitting,
                                    onSubmitReview = onSubmitReview,
                                    primaryColor = primaryColor
                                )
                            }

                            // Complaint link + section on "hoàn tất" step
                            val isCompleteStep = stepName.contains("hoàn tất") || stepName.contains("completed") || stepName.contains("finished")
                            if (isCompleteStep && stepState != "pending") {
                                Text(
                                    text = stringResource(DesignR.string.hcmc_detail_complaint_link),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = primaryColor,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable { showComplaintBottomSheet = true }
                                )
                                ComplaintSection(
                                    complaints = complaints,
                                    hcmcAccessToken = hcmcAccessToken,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                    )
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        if (showComplaintBottomSheet) {
            var content by remember { mutableStateOf("") }
            var selectedFiles by remember { mutableStateOf<List<SelectedFile>>(emptyList()) }

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    val (name, size) = getUriNameAndSize(context, uri)
                    val actualSize = if (size > 0) size else getUriSize(context, uri)
                    selectedFiles = selectedFiles + SelectedFile(uri, name, actualSize)
                }
            }

            ModalBottomSheet(
                onDismissRequest = { showComplaintBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                val focusManager = LocalFocusManager.current
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        }
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp)
                ) {
                    Text(
                        text = stringResource(DesignR.string.hcmc_detail_complaint_sheet_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AppTextArea(
                        value = content,
                        onValueChange = { content = it },
                        label = stringResource(DesignR.string.hcmc_detail_complaint_label),
                        placeholder = stringResource(DesignR.string.hcmc_detail_complaint_placeholder),
                        required = true,
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppImageField(
                        images = selectedFiles.map { it.uri },
                        onAddImage = { imagePickerLauncher.launch("image/*") },
                        onRemoveImage = { uri ->
                            selectedFiles = selectedFiles.filterNot { it.uri == uri }
                        },
                        label = stringResource(DesignR.string.hcmc_detail_complaint_image_label),
                        maxImages = 5,
                        modifier = Modifier.fillMaxWidth()
                    ) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(DesignR.string.hcmc_detail_complaint_image_desc),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        onClick = {
                            if (content.isNotBlank()) {
                                onSubmitComplaint(content, selectedFiles)
                                showComplaintBottomSheet = false
                            }
                        },
                        enabled = content.isNotBlank() && !isComplaintSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isComplaintSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        AppButtonText(stringResource(DesignR.string.hcmc_detail_complaint_submit))
                    }
                }
            }
        }
    }
}

/**
 * Render a single history entry inside a step.
 */
@Composable
private fun StepHistoryEntry(
    history: NetworkHcmcStepHistoryItem,
    primaryColor: Color
) {
    val historyState = history.state?.lowercase()?.trim() ?: ""
    val stateColor = when (historyState) {
        "new", "mới" -> Color(0xFF1E3A8A)
        "pending", "waiting" -> Color(0xFF4A148C)
        "assigned", "đã phân công", "phân công" -> Color(0xFF006064)
        "repairing", "đang sửa", "sửa chữa" -> Color(0xFF33691E)
        "processing", "đang xử lý" -> Color(0xFFE65100)
        "done", "completed", "finished", "hoàn thành", "hoàn tất", "approved", "đã duyệt" -> Color(0xFF1B5E20)
        "rejected", "từ chối", "overdue", "quá hạn" -> Color(0xFFC62828)
        "cancelled", "đã hủy" -> Color(0xFF37474F)
        else -> Color(0xFF78909C)
    }
    val (datePart, timePart) = splitDateTime(history.date)
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = stateColor.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // State dot
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(stateColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = history.user_name ?: "---",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = translateStepState(historyState, context),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = stateColor
                    )
                }
                if (datePart != "---" || timePart.isNotBlank()) {
                    Text(
                        text = listOf(datePart, timePart).filter { it.isNotBlank() && it != "---" }.joinToString(" "),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF90A4AE),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                val note = history.note
                if (!note.isNullOrBlank()) {
                    Text(
                        text = context.getString(DesignR.string.hcmc_detail_label_note) + ": $note",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF546E7A),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

private fun translateStepState(state: String, context: Context): String {
    return when (state) {
        "approved" -> context.getString(DesignR.string.hcmc_state_approved)
        "assigned" -> context.getString(DesignR.string.hcmc_state_assigned)
        "pending" -> context.getString(DesignR.string.hcmc_state_pending)
        "cancelled" -> context.getString(DesignR.string.hcmc_state_cancelled)
        "rejected" -> context.getString(DesignR.string.hcmc_state_rejected)
        "done" -> context.getString(DesignR.string.hcmc_state_done)
        else -> state.replaceFirstChar { it.uppercase() }
    }
}



@Composable
private fun ReviewSection(
    reviews: List<NetworkHcmcReviewItem>,
    isSubmitting: Boolean,
    onSubmitReview: (String, String) -> Unit,
    primaryColor: Color
) {
    val hasReview = reviews.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 84.dp, bottom = 16.dp, end = 8.dp)
    ) {
        if (hasReview) {
            // Display existing review
            val review = reviews.first()
            val rating = review.rating?.toDoubleOrNull()?.toInt() ?: 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(DesignR.string.hcmc_detail_review_status_done),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF1B5E20)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        for (i in 1..5) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFBBF24) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    val reviewComments = review.comments
                    if (!reviewComments.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            reviewComments,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF546E7A)
                        )
                    }
                }
            }
        } else {
            // Rating input form
            var selectedRating by remember { mutableIntStateOf(5) }
            var comments by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        stringResource(DesignR.string.hcmc_detail_review_card_title),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..5) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = stringResource(DesignR.string.hcmc_detail_review_star_desc, i),
                                tint = if (i <= selectedRating) Color(0xFFFBBF24) else Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { selectedRating = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        placeholder = { Text(stringResource(DesignR.string.hcmc_detail_review_comment_placeholder), style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall,
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onSubmitReview(selectedRating.toString(), comments) },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(DesignR.string.hcmc_detail_review_submit), color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplaintSection(
    complaints: List<NetworkHcmcComplaintItem>,
    hcmcAccessToken: String?,
    primaryColor: Color
) {
    val hasComplaint = complaints.isNotEmpty()
    val context = LocalContext.current

    if (hasComplaint) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 84.dp, bottom = 16.dp, end = 8.dp)
        ) {
            // Display existing complaints
            complaints.forEach { complaint ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            stringResource(DesignR.string.hcmc_detail_complaint_status_sent),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFC62828)
                        )
                        val complaintDesc = complaint.description
                        if (!complaintDesc.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                complaintDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF546E7A)
                            )
                        }
                        val imageIds = complaint.image_ids
                        if (!imageIds.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            imageIds.forEach { imageId ->
                                val imgUrl = "https://hanhchinhmotcua.ktxhcm.edu.vn/api/service/request/attachment/$imageId"
                                val imgRequest = remember(imgUrl, hcmcAccessToken) {
                                    ImageRequest.Builder(context)
                                        .data(imgUrl)
                                        .apply {
                                            if (!hcmcAccessToken.isNullOrBlank()) {
                                                addHeader("Authorization", "Bearer $hcmcAccessToken")
                                            }
                                        }
                                        .crossfade(true)
                                        .build()
                                }
                                AsyncImage(
                                    model = imgRequest,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(bottom = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        if (!complaint.complaint_date.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatRequestDate(complaint.complaint_date),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }
                }
            }
        }
    }
}


// add to util common
private fun getUriSize(context: Context, uri: Uri): Long {
    return try {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
            it.length
        } ?: 0L
    } catch (e: Exception) {
        0L
    }
}

private fun getUriNameAndSize(context: Context, uri: Uri): Pair<String, Long> {
    var name = "unknown_file"
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return Pair(name, size)
}
