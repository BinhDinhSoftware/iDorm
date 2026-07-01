package com.bdsoftware.idorm.feature.wificonfig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import com.bdsoftware.idorm.core.common.util.WifiLogCollector
import com.bdsoftware.idorm.core.common.util.WifiLogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiLogBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by WifiLogCollector.logs.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = Color(0xFF1E1E1E), // Terminal dark theme
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) },
        modifier = modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(DesignR.string.wifi_log_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = stringResource(DesignR.string.wifi_log_count, logs.size),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }

                TextButton(
                    onClick = { WifiLogCollector.clear() }
                ) {
                    Text(stringResource(DesignR.string.wifi_log_clear), color = Color(0xFFFF6B6B))
                }
            }

            HorizontalDivider(color = Color(0xFF333333))

            // Log Console
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(DesignR.string.wifi_log_empty),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs, key = { it.id }) { item ->
                        LogItemView(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItemView(entry: WifiLogEntry) {
    val bgColor = when {
        entry.isError -> Color(0xFF3A1E1E)
        entry.isSuccess -> Color(0xFF1E3A24)
        else -> Color(0xFF252526)
    }

    val textColor = when {
        entry.isError -> Color(0xFFFF8888)
        entry.isSuccess -> Color(0xFF88FF88)
        else -> Color(0xFFD4D4D4)
    }

    val tagColor = when {
        entry.isError -> Color(0xFFFF5555)
        entry.isSuccess -> Color(0xFF55FF55)
        else -> Color(0xFF569CD6)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "[${entry.timestamp}]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = entry.tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = tagColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
