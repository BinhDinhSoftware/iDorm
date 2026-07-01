package com.bdsoftware.idorm.feature.auth.forgotpassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.component.AppTextField

@Composable
fun ForgotPasswordRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ForgotPasswordScreen(
        uiState = uiState,
        onCccdChanged = viewModel::onCccdChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onSendRequestClick = viewModel::onSendRequestClick,
        onDismissError = viewModel::onDismissError,
        onDismissSuccess = viewModel::onDismissSuccess,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onCccdChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSendRequestClick: () -> Unit,
    onDismissError: () -> Unit,
    onDismissSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("", color = Color(0xFF333333)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF333333))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF333333),
                    navigationIconContentColor = Color(0xFF333333)
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        val focusManager = LocalFocusManager.current
        Column(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = com.bdsoftware.idorm.core.designsystem.R.drawable.logo),
                contentDescription = "iDorm Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(DesignR.string.forgot_password_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = uiState.cccd,
                onValueChange = onCccdChanged,
                label = stringResource(DesignR.string.cccd_label),
                placeholder = stringResource(DesignR.string.cccd_placeholder),
                required = true,
                isError = uiState.cccdError != null,
                errorMessage = uiState.cccdError,
                leadingIcon = Icons.Default.Person,
                trailingIcon = if (uiState.cccd.isNotEmpty()) Icons.Default.Clear else null,
                onTrailingIconClick = { onCccdChanged("") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = stringResource(DesignR.string.email_label),
                placeholder = stringResource(DesignR.string.email_placeholder),
                required = true,
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                leadingIcon = Icons.Default.Email,
                trailingIcon = if (uiState.email.isNotEmpty()) Icons.Default.Clear else null,
                onTrailingIconClick = { onEmailChanged("") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            if (uiState.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = onDismissError,
                    title = { Text(stringResource(DesignR.string.submit_fail_title)) },
                    text = { Text(uiState.errorMessage) },
                    confirmButton = {
                        TextButton(onClick = onDismissError) {
                            Text(stringResource(DesignR.string.close_button))
                        }
                    }
                )
            }

            if (uiState.successMessage != null) {
                AlertDialog(
                    onDismissRequest = onDismissSuccess,
                    title = { Text(stringResource(DesignR.string.submit_success_title)) },
                    text = { Text(uiState.successMessage) },
                    confirmButton = {
                        TextButton(onClick = onDismissSuccess) {
                            Text(stringResource(DesignR.string.close_button))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                onClick = onSendRequestClick,
                isLoading = uiState.isLoading,
                loadingText = stringResource(DesignR.string.submitting)
            ) {
                AppButtonText(stringResource(DesignR.string.submit_button))
            }
        }
    }
}
