package com.bdsoftware.idorm.feature.hcmc

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.component.AppSelect
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.designsystem.component.topbar.CenterTopBar
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.automirrored.filled.Assignment
import com.bdsoftware.idorm.core.network.model.NetworkHcmcService
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceGroup
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HcmcCreateRequestScreen(
    onBack: () -> Unit,
    onNavigateToForm: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HcmcCreateRequestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val primaryBlue = ComponentStyles.PrimaryBlue
    val context = LocalContext.current

    var showGroupSheet by remember { mutableStateOf(false) }
    var showServiceSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, context.getString(DesignR.string.hcmc_create_success_toast), Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
            onBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(primaryBlue)
    ) {
        CenterTopBar(
            title = stringResource(DesignR.string.hcmc_create_title),
            onBack = onBack,
            modifier = Modifier.statusBarsPadding()
        )

        // White container with rounded top corners
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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
                uiState.error != null && uiState.serviceGroups.isEmpty() -> {
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
                            TextButton(onClick = { viewModel.loadServiceGroups() }) {
                                Text(stringResource(DesignR.string.hcmc_retry_button), color = primaryBlue)
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Scrollable Selections
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = stringResource(DesignR.string.hcmc_create_guide),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Selector 1: Nhóm dịch vụ (Parent)
                            AppSelect(
                                label = stringResource(DesignR.string.hcmc_service_group_label),
                                value = uiState.selectedGroup?.name ?: stringResource(DesignR.string.hcmc_service_group_placeholder),
                                isSelected = uiState.selectedGroup != null,
                                onClick = { showGroupSheet = true },
                                leadingIcon = Icons.Default.Category
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Selector 2: Dịch vụ (Child)
                            val isServiceEnabled = uiState.selectedGroup != null
                            AppSelect(
                                label = stringResource(DesignR.string.hcmc_service_label),
                                value = uiState.selectedService?.name ?: stringResource(DesignR.string.hcmc_service_placeholder),
                                isSelected = uiState.selectedService != null,
                                enabled = isServiceEnabled,
                                onClick = { showServiceSheet = true },
                                leadingIcon = Icons.AutoMirrored.Filled.Assignment
                            )
                        }

                        // Fixed Bottom Button
                        if (uiState.selectedGroup != null && uiState.selectedService != null) {
                            AppButton(
                                onClick = { onNavigateToForm(uiState.selectedService!!.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(vertical = 12.dp)
                            ) {
                                AppButtonText(stringResource(DesignR.string.hcmc_create_button))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGroupSheet) {
        val groupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showGroupSheet = false },
            sheetState = groupSheetState,
            containerColor = Color.White
        ) {
            BottomSheetContent(
                title = stringResource(DesignR.string.hcmc_service_group_placeholder),
                items = uiState.serviceGroups,
                selectedItem = uiState.selectedGroup,
                onItemSelect = { group ->
                    viewModel.selectGroup(group)
                    showGroupSheet = false
                },
                itemContent = { group ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2C3E50)
                        )
                        val desc = group.description
                        if (!desc.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            )
        }
    }

    if (showServiceSheet && uiState.selectedGroup != null) {
        val serviceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showServiceSheet = false },
            sheetState = serviceSheetState,
            containerColor = Color.White
        ) {
            BottomSheetContent(
                title = stringResource(DesignR.string.hcmc_service_placeholder),
                items = uiState.selectedGroup!!.services,
                selectedItem = uiState.selectedService,
                onItemSelect = { service ->
                    viewModel.selectService(service)
                    showServiceSheet = false
                },
                itemContent = { service ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2C3E50)
                        )
                        val desc = service.description
                        if (!desc.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            )
        }
    }
}


@Composable
private fun <T> BottomSheetContent(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelect: (T) -> Unit,
    itemContent: @Composable RowScope.(T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 200.dp)
                .heightIn(max = 400.dp)
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onItemSelect(item) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemContent(item)
                    if (item == selectedItem) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = ComponentStyles.PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }
        }
    }
}
