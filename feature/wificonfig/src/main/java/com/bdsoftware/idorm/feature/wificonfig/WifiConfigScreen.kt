package com.bdsoftware.idorm.feature.wificonfig

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.component.AppTextField
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.model.WifiNetworkConfig
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import kotlinx.coroutines.flow.collectLatest

private val DEFAULT_SSIDS = IDormPreferencesDataSource.DEFAULT_WIFI_CONFIGS.map {
    it.ssid.lowercase()
}.toSet()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiConfigScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WifiConfigViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val wifiConfigs by viewModel.wifiConfigs.collectAsStateWithLifecycle()
    val isAutoBypassEnabled by viewModel.isAutoBypassEnabled.collectAsStateWithLifecycle()
    val isWifiActive by viewModel.isWifiActive.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val primaryBlue = ComponentStyles.PrimaryBlue
    val backgroundColor = Color(0xFFF8F9FA)

    // Launcher xin quyền thông báo khi bật tự động gia hạn (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.toggleAutoBypass(true)
    }

    val handleToggleAutoBypass: (Boolean) -> Unit = { enabled ->
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.toggleAutoBypass(true)
            }
        } else {
            viewModel.toggleAutoBypass(enabled)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            val text = when (msg) {
                is WifiUiMessage.Resource -> context.getString(msg.resId, *msg.formatArgs.toTypedArray())
                is WifiUiMessage.Raw -> msg.message
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(DesignR.string.wifi_config_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignR.string.wifi_config_back_desc),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Card 1: Bật / Tắt tính năng tự động gia hạn & vượt QC
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(DesignR.string.wifi_config_status_label),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAutoBypassEnabled) stringResource(DesignR.string.wifi_config_status_running) else stringResource(DesignR.string.wifi_config_status_disabled),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAutoBypassEnabled) ComponentStyles.SuccessGreen else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Segmented Toggle Switch (BẬT | TẮT) kiểu màn Tôi / AccountScreen
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEBF3FC))
                            .padding(2.dp)
                            .clickable { handleToggleAutoBypass(!isAutoBypassEnabled) }
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAutoBypassEnabled) primaryBlue else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(DesignR.string.wifi_config_toggle_on),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isAutoBypassEnabled) Color.White else Color.Gray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isAutoBypassEnabled) primaryBlue else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(DesignR.string.wifi_config_toggle_off),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (!isAutoBypassEnabled) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            // Card 2: Danh sách cấu hình các mạng WiFi Awing KTX
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primaryBlue.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(DesignR.string.wifi_config_list_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = stringResource(DesignR.string.wifi_config_list_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color.Black.copy(alpha = 0.06f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (wifiConfigs.isEmpty()) {
                        Text(
                            text = stringResource(DesignR.string.wifi_config_list_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        wifiConfigs.forEachIndexed { index, config ->
                            val isDefault = config.ssid.lowercase() in DEFAULT_SSIDS
                            WifiConfigItemRow(
                                config = config,
                                isAutoBypassEnabled = isAutoBypassEnabled,
                                isDefault = isDefault,
                                primaryColor = primaryBlue,
                                onManualLogin = {
                                    viewModel.manualConnectAndLogin(config)
                                },
                                showDivider = index < wifiConfigs.size - 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiConfigItemRow(
    config: WifiNetworkConfig,
    isAutoBypassEnabled: Boolean,
    isDefault: Boolean,
    primaryColor: Color,
    onManualLogin: () -> Unit,
    showDivider: Boolean
) {
    val isMonitored = isAutoBypassEnabled

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = config.ssid,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = primaryColor.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = stringResource(DesignR.string.wifi_config_label_default),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gateway: ${config.gatewayUrl}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "AWING: ${config.awingUrl}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onManualLogin) {
                    Icon(
                        imageVector = if (isMonitored) Icons.Default.Sync else Icons.Default.PlayArrow,
                        contentDescription = if (isMonitored) stringResource(DesignR.string.wifi_config_desc_monitored) else stringResource(DesignR.string.wifi_config_desc_manual_login),
                        tint = if (isMonitored) ComponentStyles.SuccessGreen else primaryColor
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = Color.Black.copy(alpha = 0.04f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

