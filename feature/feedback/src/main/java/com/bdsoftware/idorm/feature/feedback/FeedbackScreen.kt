package com.bdsoftware.idorm.feature.feedback

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Email
import coil.compose.AsyncImage
import com.bdsoftware.idorm.core.designsystem.component.IDormTopAppBar
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.component.AppImageField
import com.bdsoftware.idorm.core.designsystem.component.AppTextArea
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR

@Composable
fun FeedbackRoute(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedbackScreen(
        uiState = uiState,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onAddImage = viewModel::onAddImage,
        onRemoveImage = viewModel::onRemoveImage,
        onSubmitClick = { viewModel.submitFeedback(onSuccess = {}) },
        onDismissError = viewModel::dismissError,
        onDismissSuccess = {
            viewModel.dismissSuccess()
            onBack()
        },
        onBack = onBack,
        onHomeClick = onHomeClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedbackScreen(
    uiState: FeedbackUiState,
    onDescriptionChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddImage: (Uri) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onSubmitClick: () -> Unit,
    onDismissError: () -> Unit,
    onDismissSuccess: () -> Unit,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val appContext = LocalContext.current

    val primaryBlue = ComponentStyles.PrimaryBlue
    val backgroundColor = Color(0xFFF4F6F8)

    // Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onAddImage(uri)
        }
    }

    Scaffold(
        topBar = {
            IDormTopAppBar(
                title = stringResource(DesignR.string.feedback_screen_title),
                onBack = onBack,
                onHomeClick = onHomeClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(
                        width = 0.5.dp,
                        color = Color.Black.copy(alpha = 0.08f)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                AppButton(
                    onClick = onSubmitClick,
                    isLoading = uiState.isLoading,
                    loadingText = stringResource(DesignR.string.feedback_submitting)
                ) {
                    AppButtonText(stringResource(DesignR.string.feedback_submit_button))
                }
            }
        },
        containerColor = backgroundColor,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
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
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Description Label and Input
                        AppTextArea(
                            value = uiState.description,
                            onValueChange = onDescriptionChanged,
                            label = stringResource(DesignR.string.feedback_description_label),
                            placeholder = stringResource(DesignR.string.feedback_description_placeholder),
                            required = true,
                            isError = uiState.descriptionError != null,
                            errorMessage = uiState.descriptionError,
                            minLines = 3,
                            maxLines = 5,
                            maxLength = 500,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.showEmailField) {
                            Spacer(modifier = Modifier.height(16.dp))
                            AppTextField(
                                value = uiState.email,
                                onValueChange = onEmailChanged,
                                label = stringResource(DesignR.string.feedback_email_label),
                                placeholder = "email@example.com",
                                required = true,
                                isError = uiState.emailError != null,
                                errorMessage = uiState.emailError,
                                leadingIcon = Icons.Default.Email,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hình ảnh đính kèm
                        AppImageField(
                            images = uiState.images,
                            onAddImage = { imagePickerLauncher.launch("image/*") },
                            onRemoveImage = onRemoveImage,
                            label = stringResource(DesignR.string.feedback_images_label),
                            maxImages = 5,
                            modifier = Modifier.fillMaxWidth()
                        ) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = stringResource(DesignR.string.feedback_image_content_desc),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Dialogs

            if (uiState.success) {
                AlertDialog(
                    onDismissRequest = onDismissSuccess,
                    title = { Text(stringResource(DesignR.string.feedback_success_title)) },
                    text = { Text(stringResource(DesignR.string.feedback_success_message)) },
                    confirmButton = {
                        TextButton(onClick = onDismissSuccess) {
                            Text(stringResource(DesignR.string.feedback_confirm_button), color = primaryBlue)
                        }
                    }
                )
            }

            uiState.error?.let { err ->
                AlertDialog(
                    onDismissRequest = onDismissError,
                    title = { Text(stringResource(DesignR.string.feedback_error_title)) },
                    text = { Text(err) },
                    confirmButton = {
                        TextButton(onClick = onDismissError) {
                            Text(stringResource(DesignR.string.feedback_close_button), color = primaryBlue)
                        }
                    }
                )
            }
        }
    }
}
