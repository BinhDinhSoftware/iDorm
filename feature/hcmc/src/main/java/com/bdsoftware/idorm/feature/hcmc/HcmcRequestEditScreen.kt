package com.bdsoftware.idorm.feature.hcmc

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.component.AppCheckbox
import com.bdsoftware.idorm.core.designsystem.component.AppDateDialog
import com.bdsoftware.idorm.core.designsystem.component.AppDateMultiDialog
import com.bdsoftware.idorm.core.designsystem.component.AppFileField
import com.bdsoftware.idorm.core.designsystem.component.AppFileItem
import com.bdsoftware.idorm.core.designsystem.component.AppImageField
import com.bdsoftware.idorm.core.designsystem.component.AppSelect
import com.bdsoftware.idorm.core.designsystem.component.AppTextArea
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import com.bdsoftware.idorm.core.designsystem.component.topbar.CenterTopBar
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.network.model.NetworkHcmcAttachment
import com.bdsoftware.idorm.core.network.model.NetworkHcmcFormField
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR

import android.content.Context
import android.provider.OpenableColumns

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNCHECKED_CAST")
@Composable
fun HcmcRequestEditScreen(
    onBack: () -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HcmcRequestEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hcmcAccessToken by viewModel.hcmcAccessToken.collectAsStateWithLifecycle()
    val primaryBlue = ComponentStyles.PrimaryBlue
    val context = LocalContext.current

    val imageLoader = remember(viewModel.okHttpClient) {
        coil.ImageLoader.Builder(context)
            .okHttpClient(viewModel.okHttpClient)
            .build()
    }

    var showSelectMultiSheet by remember { mutableStateOf(false) }
    var showSelectSingleSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDatePickerMulti by remember { mutableStateOf(false) }
    var currentSelectField by remember { mutableStateOf<NetworkHcmcFormField?>(null) }

    // Điều hướng sau khi cập nhật thành công
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, context.getString(DesignR.string.hcmc_edit_success_toast), Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
            onNavigateToDetail()
        }
    }

    // Hiển thị lỗi submit
    LaunchedEffect(uiState.submitError) {
        uiState.submitError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Image picker — ảnh mới thêm vào
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val size = getUriSize(context, uri)
            val error = viewModel.onAddNewImage(uri, size)
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    // File picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val (name, size) = getUriNameAndSize(context, uri)
            val actualSize = if (size > 0) size else getUriSize(context, uri)
            val error = viewModel.onAddFile(uri, name, actualSize)
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterTopBar(
                title = stringResource(DesignR.string.hcmc_edit_title),
                onBack = onBack
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                AppButton(
                    onClick = { viewModel.submitUpdate() },
                    isLoading = uiState.isSubmitting,
                    loadingText = stringResource(DesignR.string.hcmc_edit_loading),
                    enabled = viewModel.isFormValid(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppButtonText(stringResource(DesignR.string.hcmc_edit_button))
                }
            }
        },
        containerColor = Color(0xFFF4F6F8),
        modifier = modifier
    ) { paddingValues ->
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryBlue, strokeWidth = 2.dp)
                    }
                }
                uiState.error != null && uiState.form == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: stringResource(DesignR.string.hcmc_error_fallback),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { /* TODO: retry */ }) {
                                Text(stringResource(DesignR.string.hcmc_retry_button), color = primaryBlue)
                            }
                        }
                    }
                }
                else -> {
                    val form = uiState.form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 0.5.dp,
                                    color = Color.Black.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(DesignR.string.hcmc_detail_tab_info),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1E3A8A),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Dynamic fields
                                form?.fields?.sortedBy { it.sequence }?.forEach { field ->
                                    when (field.type) {
                                        "textarea" -> {
                                            val value = uiState.fieldValues[field.id] as? String ?: ""
                                            AppTextArea(
                                                value = value,
                                                onValueChange = { viewModel.onFieldValueChange(field.id, it) },
                                                label = field.label,
                                                placeholder = field.placeholder ?: stringResource(DesignR.string.hcmc_edit_field_textarea_placeholder),
                                                required = field.required,
                                                minLines = 3, maxLines = 5,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        "checkbox" -> {
                                            val checked = uiState.fieldValues[field.id] as? Boolean ?: false
                                            AppCheckbox(
                                                checked = checked,
                                                onCheckedChange = { viewModel.onFieldValueChange(field.id, it) },
                                                label = field.label
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        "select_multi" -> {
                                            val selectedIds = uiState.fieldValues[field.id] as? List<Int> ?: emptyList()
                                            val displayValue = field.options
                                                .filter { it.id in selectedIds }
                                                .joinToString(", ") { it.name }
                                                .ifEmpty { field.placeholder ?: stringResource(DesignR.string.hcmc_edit_field_select_placeholder) }
                                            AppSelect(
                                                label = field.label, value = displayValue,
                                                isSelected = selectedIds.isNotEmpty(),
                                                onClick = { currentSelectField = field; showSelectMultiSheet = true },
                                                leadingIcon = Icons.AutoMirrored.Filled.FormatListBulleted
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        "select" -> {
                                            val selectedId = uiState.fieldValues[field.id] as? Int
                                            val displayValue = field.options.find { it.id == selectedId }?.name
                                                ?: (field.placeholder ?: stringResource(DesignR.string.hcmc_edit_field_select_placeholder))
                                            AppSelect(
                                                label = field.label, value = displayValue,
                                                isSelected = selectedId != null,
                                                onClick = { currentSelectField = field; showSelectSingleSheet = true },
                                                leadingIcon = Icons.AutoMirrored.Filled.FormatListBulleted
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        "date" -> {
                                            val selectedDate = uiState.fieldValues[field.id] as? String ?: ""
                                            val displayValue = if (selectedDate.isNotEmpty()) {
                                                com.bdsoftware.idorm.core.common.util.formatIsoDate(selectedDate)
                                            } else field.placeholder ?: stringResource(DesignR.string.hcmc_edit_field_date_placeholder)
                                            AppSelect(
                                                label = field.label, value = displayValue,
                                                isSelected = selectedDate.isNotEmpty(),
                                                onClick = { currentSelectField = field; showDatePicker = true },
                                                leadingIcon = Icons.Default.CalendarToday
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        "date_multi" -> {
                                            val selectedDates = uiState.fieldValues[field.id] as? List<String> ?: emptyList()
                                            val displayValue = if (selectedDates.isNotEmpty()) {
                                                selectedDates.map { com.bdsoftware.idorm.core.common.util.formatIsoDate(it) }.joinToString(", ")
                                            } else field.placeholder ?: stringResource(DesignR.string.hcmc_edit_field_date_multi_placeholder)
                                            AppSelect(
                                                label = field.label, value = displayValue,
                                                isSelected = selectedDates.isNotEmpty(),
                                                onClick = { currentSelectField = field; showDatePickerMulti = true },
                                                leadingIcon = Icons.Default.CalendarToday
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        "text" -> {
                                            val value = uiState.fieldValues[field.id] as? String ?: ""
                                            AppTextField(
                                                value = value,
                                                onValueChange = { viewModel.onFieldValueChange(field.id, it) },
                                                label = field.label,
                                                placeholder = field.placeholder ?: stringResource(DesignR.string.hcmc_edit_field_text_placeholder),
                                                required = field.required,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }

                                // Chi tiết yêu cầu (note)
                                AppTextArea(
                                    value = uiState.requestDetails,
                                    onValueChange = { viewModel.onRequestDetailsChange(it) },
                                    label = stringResource(DesignR.string.hcmc_edit_note_label),
                                    placeholder = stringResource(DesignR.string.hcmc_edit_note_placeholder),
                                    required = true,
                                    minLines = 4, maxLines = 6,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Ảnh: hiển thị ảnh cũ (server) + ảnh mới (local URI)
                                val existingImageUris = uiState.existingImages.map {
                                    android.net.Uri.parse("https://hanhchinhmotcua.ktxhcm.edu.vn" + it.url)
                                }
                                val allImageUris = existingImageUris + uiState.newImages

                                // AppImageField hiển thị tất cả ảnh
                                AppImageField(
                                    images = allImageUris,
                                    onAddImage = { imagePickerLauncher.launch("image/*") },
                                    onRemoveImage = { uri ->
                                        // Kiểm tra xem là ảnh cũ (server) hay ảnh mới (local)
                                        val existingImg = uiState.existingImages.find {
                                            android.net.Uri.parse("https://hanhchinhmotcua.ktxhcm.edu.vn" + it.url) == uri
                                        }
                                        if (existingImg != null) {
                                            viewModel.onRemoveExistingImage(existingImg.id)
                                        } else {
                                            viewModel.onRemoveNewImage(uri)
                                        }
                                    },
                                    label = stringResource(DesignR.string.hcmc_edit_images_label),
                                    maxImages = 5,
                                    modifier = Modifier.fillMaxWidth()
                                ) { uri ->
                                    val model = remember(uri, hcmcAccessToken) {
                                        if (uri.toString().startsWith("http")) {
                                            ImageRequest.Builder(context)
                                                .data(uri.toString())
                                                .apply {
                                                     if (!hcmcAccessToken.isNullOrBlank() && !uri.toString().contains("/api/download/image/")) {
                                                         addHeader("Authorization", "Bearer $hcmcAccessToken")
                                                     }
                                                 }
                                                .crossfade(true)
                                                .build()
                                        } else {
                                            uri
                                        }
                                    }
                                    AsyncImage(
                                        model = model,
                                        imageLoader = imageLoader,
                                        contentDescription = stringResource(DesignR.string.hcmc_edit_image_desc),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                AppFileField(
                                    files = uiState.files.map { AppFileItem(it.uri, it.name, it.size) },
                                    onAddFile = { filePickerLauncher.launch("*/*") },
                                    onRemoveFile = { viewModel.onRemoveFile(it) },
                                    label = stringResource(DesignR.string.hcmc_edit_files_label),
                                    maxFiles = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Multi-Select Bottom Sheet
    if (showSelectMultiSheet && currentSelectField != null) {
        val field = currentSelectField!!
        val selectedIds = uiState.fieldValues[field.id] as? List<Int> ?: emptyList()
        var tempSelectedIds by remember(selectedIds) { mutableStateOf(selectedIds) }
        ModalBottomSheet(
            onDismissRequest = { showSelectMultiSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(field.label, style = MaterialTheme.typography.titleMedium, color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(field.options) { option ->
                        val isChecked = option.id in tempSelectedIds
                        AppCheckbox(
                            checked = isChecked,
                            onCheckedChange = { check ->
                                tempSelectedIds = if (check) tempSelectedIds + option.id else tempSelectedIds - option.id
                            },
                            label = option.name, modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                AppButton(onClick = { viewModel.onFieldValueChange(field.id, tempSelectedIds); showSelectMultiSheet = false },
                    modifier = Modifier.fillMaxWidth()) { AppButtonText(stringResource(DesignR.string.hcmc_edit_confirm_button)) }
            }
        }
    }

    // Single-Select Bottom Sheet
    if (showSelectSingleSheet && currentSelectField != null) {
        val field = currentSelectField!!
        val selectedId = uiState.fieldValues[field.id] as? Int
        ModalBottomSheet(
            onDismissRequest = { showSelectSingleSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(field.label, style = MaterialTheme.typography.titleMedium, color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(field.options) { option ->
                        val isSelected = option.id == selectedId
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { viewModel.onFieldValueChange(field.id, option.id); showSelectSingleSheet = false }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.name, style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) primaryBlue else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = primaryBlue)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Date Dialog
    if (showDatePicker && currentSelectField != null) {
        val field = currentSelectField!!
        AppDateDialog(
            label = field.label,
            selectedDate = uiState.fieldValues[field.id] as? String,
            onDismiss = { showDatePicker = false },
            onConfirm = { date -> viewModel.onFieldValueChange(field.id, date); showDatePicker = false }
        )
    }

    // Date Multi Dialog
    if (showDatePickerMulti && currentSelectField != null) {
        val field = currentSelectField!!
        AppDateMultiDialog(
            label = field.label,
            selectedDates = uiState.fieldValues[field.id] as? List<String> ?: emptyList(),
            onDismiss = { showDatePickerMulti = false },
            onConfirm = { dates -> viewModel.onFieldValueChange(field.id, dates); showDatePickerMulti = false }
        )
    }
}

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
