package com.bdsoftware.idorm.feature.payment

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    invoiceId: Int,
    amount: Double,
    isEw: Boolean,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load QR codes on first composition
    LaunchedEffect(invoiceId, amount, isEw) {
        viewModel.loadQrCodes(invoiceId, amount, isEw)
    }

    // Polling every 10 seconds to check payment status
    LaunchedEffect(invoiceId, isEw) {
        if (invoiceId > 0) {
            while (true) {
                delay(10000)
                val isPaid = viewModel.checkPaymentStatus(invoiceId, isEw)
                if (isPaid) {
                    Toast.makeText(context, context.getString(DesignR.string.payment_success_toast), Toast.LENGTH_LONG).show()
                    onPaymentSuccess()
                    break
                }
            }
        }
    }

    // Styles matching ComponentStyles
    val titleStyle = TextStyle(
        fontFamily = ComponentStyles.AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    val subtitleStyle = TextStyle(
        fontFamily = ComponentStyles.AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
    val bodyStyle = TextStyle(
        fontFamily = ComponentStyles.AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
    val labelStyle = TextStyle(
        fontFamily = ComponentStyles.AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("BIDV", "Vietcombank (VCB)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(DesignR.string.payment_title),
                        style = titleStyle,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignR.string.payment_back_desc),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ComponentStyles.PrimaryBlue
                )
            )
        },
        containerColor = Color(0xFFF8F9FA),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedTab == index) ComponentStyles.PrimaryBlue else Color.Gray
                            )
                        }
                    )
                }
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (uiState.isLoading) {
                        // Skeleton loading matching actual UI shape
                        // 1. QR Card Skeleton
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Text Title Skeleton
                                SkeletonBox(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(20.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // QR Image Skeleton
                                SkeletonBox(
                                    modifier = Modifier
                                        .size(280.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Save QR Button Skeleton
                                SkeletonBox(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(36.dp)
                                )
                            }
                        }

                        // 2. Amount info Card Skeleton
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SkeletonBox(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(16.dp)
                                )
                                SkeletonBox(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Guide Box Skeleton
                        SkeletonBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        )
                    } else if (uiState.errorMessage != null) {
                    // Error state
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = uiState.errorMessage ?: stringResource(DesignR.string.payment_error_fallback),
                        style = bodyStyle,
                        color = ComponentStyles.ErrorRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.loadQrCodes(invoiceId, amount, isEw) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ComponentStyles.PrimaryBlue)
                    ) {
                        Text(stringResource(DesignR.string.payment_retry_button), style = labelStyle)
                    }
                } else {
                    val currentBase64 = if (selectedTab == 0) uiState.bidvQrBase64 else uiState.vcbQrBase64
                    val bankName = if (selectedTab == 0) "BIDV" else "VCB"

                    // QR Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(DesignR.string.payment_scan_qr_desc, bankName),
                                style = subtitleStyle,
                                color = ComponentStyles.PrimaryBlue,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            if (currentBase64.isNotEmpty()) {
                                Base64QrImage(
                                    base64String = currentBase64,
                                    modifier = Modifier
                                        .size(280.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(280.dp)
                                        .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(DesignR.string.payment_no_qr_desc, bankName),
                                        style = bodyStyle,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Save QR button
                            if (currentBase64.isNotEmpty()) {
                                var isSaving by remember { mutableStateOf(false) }
                                OutlinedButton(
                                    onClick = {
                                        if (!isSaving) {
                                            isSaving = true
                                            coroutineScope.launch {
                                                val result = saveBase64ImageToGallery(context, currentBase64, bankName)
                                                result.onSuccess {
                                                    Toast.makeText(context, context.getString(DesignR.string.payment_save_success_toast, bankName), Toast.LENGTH_LONG).show()
                                                }.onFailure { e ->
                                                    Toast.makeText(context, context.getString(DesignR.string.payment_save_error_toast, e.localizedMessage), Toast.LENGTH_LONG).show()
                                                }
                                                isSaving = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(top = 16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ComponentStyles.PrimaryBlue)
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ComponentStyles.PrimaryBlue)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = stringResource(DesignR.string.payment_download_desc),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(DesignR.string.payment_download_button), style = labelStyle)
                                    }
                                }
                            }
                        }
                    }

                    // Amount info
                    val formattedAmount = String.format("%,.0f", uiState.amount) + "đ"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(DesignR.string.payment_amount_label),
                                style = labelStyle,
                                color = Color.Gray
                            )
                            Text(
                                text = formattedAmount,
                                style = subtitleStyle.copy(fontSize = 16.sp),
                                color = ComponentStyles.PrimaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guide
                    Text(
                        text = stringResource(DesignR.string.payment_guide_text),
                        style = bodyStyle.copy(fontSize = 13.sp),
                        lineHeight = 20.sp,
                        color = Color(0xFF4A4A4A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF9E6), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
}


/**
 * Composable that decodes a Base64 string and displays it as an image.
 */
@Composable
fun Base64QrImage(base64String: String, modifier: Modifier = Modifier) {
    val bitmap = remember(base64String) {
        try {
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(DesignR.string.payment_qr_image_desc),
            modifier = modifier
        )
    }
}

/**
 * Skeleton shimmer box used during loading state.
 */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE0E0E0),
            Color(0xFFF5F5F5),
            Color(0xFFE0E0E0)
        ),
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .background(shimmerBrush, shape = RoundedCornerShape(12.dp))
    )
}

/**
 * Saves a Base64-encoded image string to the device gallery via MediaStore.
 */
suspend fun saveBase64ImageToGallery(context: Context, base64String: String, bankName: String): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            ?: return@withContext Result.failure(Exception("Không thể giải mã hình ảnh QR"))

        val resolver = context.contentResolver
        val imageCollection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "QR_${bankName}_iDorm_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(imageCollection, imageDetails)
            ?: return@withContext Result.failure(Exception("Không thể tạo mục trong thư viện ảnh"))

        resolver.openOutputStream(imageUri).use { outputStream ->
            if (outputStream == null) return@withContext Result.failure(Exception("Không thể mở luồng ghi file"))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            imageDetails.clear()
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, imageDetails, null, null)
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}


