package com.bdsoftware.idorm.feature.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bdsoftware.idorm.core.common.util.getAppVersion
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.component.AppPasswordTextField
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import com.bdsoftware.idorm.core.ui.language.LanguageSwitcher

@Composable
fun LoginRoute(
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        onCccdChanged = viewModel::onCccdChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLoginClick = { viewModel.onLoginClick(onLoginSuccess) },
        onDismissError = viewModel::onDismissError,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        onChangeLanguage = onChangeLanguage,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginScreen(
    uiState: LoginUiState,
    onCccdChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onDismissError: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("", color = Color(0xFF333333)) },
                actions = {
                    LanguageSwitcher(
                        onChangeLanguage = onChangeLanguage,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF333333)
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
        
        // Logo iDorm
        Image(
            painter = painterResource(id = com.bdsoftware.idorm.core.designsystem.R.drawable.logo), 
            contentDescription = "iDorm Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(DesignR.string.login_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            ),
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(48.dp))

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

        AppPasswordTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = stringResource(DesignR.string.password_label),
            placeholder = stringResource(DesignR.string.password_placeholder),
            required = true,
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError
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

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = onLoginClick,
            isLoading = uiState.isLoading,
            loadingText = stringResource(DesignR.string.logging_in),
        ) {
            AppButtonText(stringResource(DesignR.string.login_button))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(DesignR.string.forgot_password_link),
                modifier = Modifier.clickable(onClick = onNavigateToForgotPassword),
                color = Color(0xFF333333)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(DesignR.string.login_terms),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val context = LocalContext.current
        val currentVersion = remember(context) { context.getAppVersion() }

        Text(
            text = "${stringResource(DesignR.string.version_label)}: v$currentVersion",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
}
