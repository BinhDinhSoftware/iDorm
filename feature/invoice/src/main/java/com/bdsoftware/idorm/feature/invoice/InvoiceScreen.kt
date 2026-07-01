package com.bdsoftware.idorm.feature.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Context
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import com.bdsoftware.idorm.core.designsystem.component.topbar.CenterTopBar
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceResponse
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceEWResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun InvoiceScreen(
    onNavigateToPayment: (invoiceId: Int, amount: Double, isEw: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedInvoice by remember { mutableStateOf<InvoiceUiModel?>(null) }

    val roomInvoices by viewModel.roomInvoices.collectAsStateWithLifecycle()
    val serviceInvoices by viewModel.serviceInvoices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Clear focus when scrolling or switching tabs
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }
    LaunchedEffect(selectedTab) {
        focusManager.clearFocus()
    }

    val context = LocalContext.current
    // Map network entities to UI models
    val uiInvoices = remember(roomInvoices, serviceInvoices, selectedTab, context) {
        if (selectedTab == 0) {
            roomInvoices.map { it.toUiModel(context) }
        } else {
            serviceInvoices.map { it.toUiModel(context) }
        }
    }

    // Filter by search query (search by content/detail)
    val filteredInvoices = remember(uiInvoices, searchQuery) {
        uiInvoices.filter { invoice ->
            if (searchQuery.isBlank()) {
                true
            } else {
                invoice.detail.contains(searchQuery, ignoreCase = true) ||
                        invoice.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Group by Date
    val groupedInvoices = remember(filteredInvoices) {
        filteredInvoices.groupBy { formatDateHeader(it.dateString) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ComponentStyles.PrimaryBlue)
    ) {
        CenterTopBar(
            title = stringResource(DesignR.string.invoice_title),
            modifier = Modifier.statusBarsPadding()
        )

        // ── Scrollable content in white sheet with rounded top corners ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color.White)
        ) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.fetchInvoices() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tab Row
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFFF2F5F9),
                        contentColor = ComponentStyles.PrimaryBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = ComponentStyles.PrimaryBlue
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = stringResource(DesignR.string.invoice_tab_room),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTab == 0) ComponentStyles.PrimaryBlue else Color.Gray
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = stringResource(DesignR.string.invoice_tab_service),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTab == 1) ComponentStyles.PrimaryBlue else Color.Gray
                                )
                            }
                        )
                    }

                    // Search bar
                    AppTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = stringResource(DesignR.string.invoice_search_placeholder),
                        trailingIcon = Icons.Default.Search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    if (groupedInvoices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(DesignR.string.invoice_empty_text),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            groupedInvoices.forEach { (dateGroup, itemsInGroup) ->
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column {
                                            // Date Header Block
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFEBF3FC))
                                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = dateGroup,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color(0xFF1E3A8A)
                                                )
                                            }

                                            // Items inside the date group
                                            itemsInGroup.forEachIndexed { index, invoice ->
                                                InvoiceListItem(
                                                    invoice = invoice,
                                                    onClick = { selectedInvoice = invoice }
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

    selectedInvoice?.let { invoice ->
        InvoiceDetailBottomSheet(
            invoice = invoice,
            studentName = studentName,
            onPayClick = { inv ->
                selectedInvoice = null
                onNavigateToPayment(
                    inv.id,
                    inv.total.toDouble(),
                    inv.type == InvoiceType.SERVICE
                )
            },
            onDismiss = { selectedInvoice = null }
        )
    }
}

@Composable
private fun InvoiceListItem(
    invoice: InvoiceUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Style = Style
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        // Track state if needed
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, indication = null, interactionSource = interactionSource)
            .styleable(styleState, ComponentStyles.notificationItemStyle, style)
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
                    text = invoice.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF2C3E50)
                )
                
                // Blue status dot if unpaid
                if (!invoice.isPaid) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(8.dp)
                            .background(Color(0xFF29B6F6), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            
            val statusPaidViaStr = stringResource(DesignR.string.invoice_status_paid_via, invoice.paymentMethod)
            val statusUnpaidStr = stringResource(DesignR.string.invoice_status_unpaid)
            val detailFormatStr = stringResource(DesignR.string.invoice_item_detail_format)
            
            val detailText = remember(invoice, statusPaidViaStr, statusUnpaidStr, detailFormatStr) {
                val statusText = if (invoice.isPaid) {
                    statusPaidViaStr
                } else {
                    statusUnpaidStr
                }
                detailFormatStr.format(formatCurrency(invoice.total), statusText, invoice.detail)
            }

            Text(
                text = detailText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time
            Text(
                text = formatTime(invoice.dateString),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF5C6BC0)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceDetailBottomSheet(
    invoice: InvoiceUiModel,
    studentName: String,
    onPayClick: (InvoiceUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp)
        ) {
            Text(
                text = stringResource(DesignR.string.invoice_details_header),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(16.dp))

            // Details rows
            DetailRow(label = stringResource(DesignR.string.invoice_label_student_name), value = studentName.ifEmpty { stringResource(DesignR.string.home_loading) })
            DetailRow(label = stringResource(DesignR.string.invoice_label_room), value = invoice.roomName)
            DetailRow(label = stringResource(DesignR.string.invoice_label_academic_year), value = invoice.scholastic)
            DetailRow(label = stringResource(DesignR.string.invoice_label_content), value = invoice.detail)
            DetailRow(label = stringResource(DesignR.string.invoice_label_time), value = formatDateTimeFull(invoice.dateString))
            
            if (invoice.isPaid && invoice.paymentMethod.isNotEmpty()) {
                DetailRow(label = stringResource(DesignR.string.invoice_label_payment_method), value = invoice.paymentMethod)
            }
            
            DetailRow(
                label = stringResource(DesignR.string.invoice_label_status),
                value = if (invoice.isPaid) stringResource(DesignR.string.invoice_status_value_paid) else stringResource(DesignR.string.invoice_status_value_unpaid),
                valueColor = if (invoice.isPaid) ComponentStyles.SuccessGreen else ComponentStyles.ErrorRed,
                valueBold = true
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(DesignR.string.invoice_total_amount_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Text(
                    text = formatCurrency(invoice.total),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (invoice.isPaid) ComponentStyles.SuccessGreen else ComponentStyles.ErrorRed
                )
            }
            
            if (!invoice.isPaid) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onPayClick(invoice) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComponentStyles.PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(stringResource(DesignR.string.invoice_pay_now_button), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal
            ),
            color = valueColor,
            modifier = Modifier.weight(1.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun formatDateHeader(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString)
        DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dateTime)
    } catch (e: Exception) {
        if (dateString.contains("T")) {
            dateString.substringBefore("T")
        } else {
            dateString
        }
    }
}

private fun formatTime(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString)
        DateTimeFormatter.ofPattern("HH:mm").format(dateTime)
    } catch (e: Exception) {
        ""
    }
}

private fun formatDateTimeFull(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString)
        DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").format(dateTime)
    } catch (e: Exception) {
        dateString
    }
}

private fun formatCurrency(amount: Int): String {
    return try {
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        numberFormat.format(amount) + " đ"
    } catch (e: Exception) {
        "$amount đ"
    }
}

private enum class InvoiceType {
    ROOM, SERVICE
}

private data class InvoiceUiModel(
    val id: Int,
    val title: String,
    val dateString: String,
    val total: Int,
    val detail: String,
    val paymentMethod: String,
    val isPaid: Boolean,
    val scholastic: String,
    val roomName: String,
    val type: InvoiceType
)

private fun NetworkInvoiceResponse.toUiModel(context: Context): InvoiceUiModel {
    val firstDetail = ListInvoiceDetail.firstOrNull()
    val feeName = firstDetail?.FeeName ?: ""
    val title = when {
        feeName.contains("Ký túc xá", ignoreCase = true) || firstDetail?.Code == "BoardingFee" -> context.getString(DesignR.string.invoice_title_room_fee)
        feeName.contains("Bảo hiểm", ignoreCase = true) || firstDetail?.Code == "BHTN" -> context.getString(DesignR.string.invoice_title_accident_insurance)
        feeName.contains("Khám sức khỏe", ignoreCase = true) || firstDetail?.Code == "KCB" -> context.getString(DesignR.string.invoice_title_medical_exam)
        else -> context.getString(DesignR.string.invoice_title_service)
    }

    return InvoiceUiModel(
        id = Id,
        title = title,
        dateString = CreatedDate,
        total = Total.toInt(),
        detail = feeName,
        paymentMethod = PaymentMethodList ?: context.getString(DesignR.string.invoice_payment_method_online),
        isPaid = IsPayment,
        scholastic = Scholastic ?: "",
        roomName = DormitoryFullName ?: "",
        type = InvoiceType.ROOM
    )
}

private fun NetworkInvoiceEWResponse.toUiModel(context: Context): InvoiceUiModel {
    val electricityTotal = ETotal ?: (ListInvoiceEWSubDetail.filter { it.Type }.sumOf { it.Amount } * 1.1)
    val waterTotal = WTotal ?: ListInvoiceEWSubDetail.filter { !it.Type }.sumOf { it.Amount }
    val totalAmount = (electricityTotal + waterTotal).toInt()
    
    val eFirst = EFirstIndex
    val eLast = ELastIndex
    val eDetails = if (eFirst != null && eLast != null) {
        context.getString(DesignR.string.invoice_detail_electricity, eFirst.toInt(), eLast.toInt(), (eLast - eFirst).toInt())
    } else ""
    
    val wFirst = WFirstIndex
    val wLast = WLastIndex
    val wDetails = if (wFirst != null && wLast != null) {
        context.getString(DesignR.string.invoice_detail_water, wFirst.toInt(), wLast.toInt(), (wLast - wFirst).toInt())
    } else ""
    
    val detail = listOfNotNull(
        if (eDetails.isNotEmpty()) eDetails else null,
        if (wDetails.isNotEmpty()) wDetails else null,
        context.getString(DesignR.string.invoice_detail_total_students, TotalStudent)
    ).joinToString(" | ")

    val monthStr = Month.toString().padStart(2, '0')
    return InvoiceUiModel(
        id = Id,
        title = context.getString(DesignR.string.invoice_title_electricity_water, monthStr, Year),
        dateString = CreatedDate,
        total = totalAmount,
        detail = detail,
        paymentMethod = if (IsPayment) context.getString(DesignR.string.invoice_payment_method_transfer_wallet) else "",
        isPaid = IsPayment,
        scholastic = "${Year - 1} - ${Year}",
        roomName = context.getString(DesignR.string.invoice_room_name_default),
        type = InvoiceType.SERVICE
    )
}
