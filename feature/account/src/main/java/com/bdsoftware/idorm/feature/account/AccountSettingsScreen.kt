package com.bdsoftware.idorm.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.component.AppPasswordTextField
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import com.bdsoftware.idorm.core.designsystem.component.IDormTopAppBar
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR

enum class SettingsBottomSheetType {
    ChangePassword,
    ForgotPassword
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val changePasswordState by viewModel.changePasswordState.collectAsStateWithLifecycle()
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var activeBottomSheet by remember { mutableStateOf<SettingsBottomSheetType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val primaryBlue = ComponentStyles.PrimaryBlue
    val backgroundColor = Color(0xFFF4F6F8)

    Scaffold(
        topBar = {
            IDormTopAppBar(
                title = stringResource(DesignR.string.account_settings_title),
                onBack = onBack,
                onHomeClick = onHomeClick
            )
        },
        containerColor = backgroundColor,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Section Label
                Text(
                    text = stringResource(DesignR.string.login_security_section),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                // Main card
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
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Change password option
                        SettingsOptionItem(
                            icon = Icons.Default.Lock,
                            title = stringResource(DesignR.string.change_password_opt),
                            subtitle = stringResource(DesignR.string.change_password_desc),
                            primaryColor = primaryBlue,
                            onClick = {
                                viewModel.clearChangePasswordForm()
                                activeBottomSheet = SettingsBottomSheetType.ChangePassword
                            }
                        )

                        HorizontalDivider(
                            color = Color.Black.copy(alpha = 0.06f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Forgot password option
                        SettingsOptionItem(
                            icon = Icons.Default.LockOpen,
                            title = stringResource(DesignR.string.forgot_password_opt),
                            subtitle = stringResource(DesignR.string.forgot_password_desc),
                            primaryColor = primaryBlue,
                            onClick = {
                                viewModel.clearForgotPasswordForm()
                                activeBottomSheet = SettingsBottomSheetType.ForgotPassword
                            }
                        )
                    }
                }
            }

            // Bottom Sheets
            if (activeBottomSheet != null) {
                ModalBottomSheet(
                    onDismissRequest = { activeBottomSheet = null },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    modifier = Modifier.imePadding()
                ) {
                    val focusManager = LocalFocusManager.current
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (activeBottomSheet) {
                            SettingsBottomSheetType.ChangePassword -> {
                                Text(
                                    text = stringResource(DesignR.string.change_password_opt),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )

                                AppPasswordTextField(
                                    value = changePasswordState.oldPin,
                                    onValueChange = viewModel::onOldPinChanged,
                                    label = stringResource(DesignR.string.old_password_label),
                                    placeholder = stringResource(DesignR.string.old_password_placeholder),
                                    required = true,
                                    isError = changePasswordState.oldPinError != null,
                                    errorMessage = changePasswordState.oldPinError,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AppPasswordTextField(
                                    value = changePasswordState.newPin,
                                    onValueChange = viewModel::onNewPinChanged,
                                    label = stringResource(DesignR.string.new_password_label),
                                    placeholder = stringResource(DesignR.string.new_password_placeholder),
                                    required = true,
                                    isError = changePasswordState.newPinError != null,
                                    errorMessage = changePasswordState.newPinError,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AppPasswordTextField(
                                    value = changePasswordState.confirmPin,
                                    onValueChange = viewModel::onConfirmPinChanged,
                                    label = stringResource(DesignR.string.confirm_new_password_label),
                                    placeholder = stringResource(DesignR.string.confirm_new_password_placeholder),
                                    required = true,
                                    isError = changePasswordState.confirmPinError != null,
                                    errorMessage = changePasswordState.confirmPinError,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                AppButton(
                                    onClick = {
                                        viewModel.onChangePinClick(
                                            onSuccess = {
                                                // close sheet on successful trigger
                                            }
                                        )
                                    },
                                    isLoading = changePasswordState.isLoading,
                                    loadingText = stringResource(DesignR.string.processing_text),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AppButtonText(stringResource(DesignR.string.save_button))
                                }
                            }

                            SettingsBottomSheetType.ForgotPassword -> {
                                Text(
                                    text = stringResource(DesignR.string.forgot_password_opt),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )

                                AppTextField(
                                    value = forgotPasswordState.cccd,
                                    onValueChange = viewModel::onCccdChanged,
                                    label = stringResource(DesignR.string.cccd_label),
                                    placeholder = stringResource(DesignR.string.cccd_placeholder_settings),
                                    required = true,
                                    isError = forgotPasswordState.cccdError != null,
                                    errorMessage = forgotPasswordState.cccdError,
                                    leadingIcon = Icons.Default.Person,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AppTextField(
                                    value = forgotPasswordState.email,
                                    onValueChange = viewModel::onEmailChanged,
                                    label = stringResource(DesignR.string.email_label),
                                    placeholder = stringResource(DesignR.string.email_placeholder_settings),
                                    required = true,
                                    isError = forgotPasswordState.emailError != null,
                                    errorMessage = forgotPasswordState.emailError,
                                    leadingIcon = Icons.Default.Email,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                AppButton(
                                    onClick = {
                                        viewModel.onForgetPinClick(
                                            onSuccess = {
                                                // close sheet on successful trigger
                                            }
                                        )
                                    },
                                    isLoading = forgotPasswordState.isLoading,
                                    loadingText = stringResource(DesignR.string.submitting),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AppButtonText(stringResource(DesignR.string.submit_button))
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Global Dialogs for Success / Error inside Screen
            changePasswordState.successMessage?.let { msg ->
                AlertDialog(
                    onDismissRequest = {
                        viewModel.dismissChangePasswordAlerts()
                        activeBottomSheet = null
                    },
                    title = { Text(stringResource(DesignR.string.dialog_success)) },
                    text = { Text(msg) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.dismissChangePasswordAlerts()
                                activeBottomSheet = null
                            }
                        ) {
                            Text(stringResource(DesignR.string.close_button), color = primaryBlue)
                        }
                    }
                )
            }

            changePasswordState.errorMessage?.let { err ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissChangePasswordAlerts() },
                    title = { Text(stringResource(DesignR.string.dialog_error)) },
                    text = { Text(err) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissChangePasswordAlerts() }) {
                            Text(stringResource(DesignR.string.close_button), color = primaryBlue)
                        }
                    }
                )
            }

            forgotPasswordState.successMessage?.let { msg ->
                AlertDialog(
                    onDismissRequest = {
                        viewModel.dismissForgotPasswordAlerts()
                        activeBottomSheet = null
                    },
                    title = { Text(stringResource(DesignR.string.dialog_success)) },
                    text = { Text(msg) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.dismissForgotPasswordAlerts()
                                activeBottomSheet = null
                            }
                        ) {
                            Text(stringResource(DesignR.string.close_button), color = primaryBlue)
                        }
                    }
                )
            }

            forgotPasswordState.errorMessage?.let { err ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissForgotPasswordAlerts() },
                    title = { Text(stringResource(DesignR.string.dialog_error)) },
                    text = { Text(err) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissForgotPasswordAlerts() }) {
                            Text(stringResource(DesignR.string.close_button), color = primaryBlue)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
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
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}
