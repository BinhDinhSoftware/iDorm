package com.bdsoftware.idorm.feature.rent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.common.util.formatShortDate
import com.bdsoftware.idorm.core.designsystem.component.AppTimeline
import com.bdsoftware.idorm.core.designsystem.component.TimelineItemData
import com.bdsoftware.idorm.core.designsystem.component.IDormTopAppBar
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentHistoryScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RentHistoryViewModel = hiltViewModel()
) {
    val rentList by viewModel.rentList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val primaryBlue = ComponentStyles.PrimaryBlue
    val backgroundColor = Color(0xFFF8F9FA)

    Scaffold(
        topBar = {
            IDormTopAppBar(
                title = stringResource(DesignR.string.rent_history_title),
                onBack = onBack,
                onHomeClick = onNavigateToHome
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryBlue)
                }
            } else if (rentList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(DesignR.string.no_rent_history),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    val academicYearText = stringResource(DesignR.string.academic_year_label)
                        val unknownStatusText = stringResource(DesignR.string.rent_status_unknown)
                    val timelineItems = rentList.mapIndexed { index, rent ->
                        val description = buildString {
                            append("$academicYearText: ${rent.Scholastic ?: "---"}")
                            rent.Semester?.let { append(" • $it") }
                            rent.Note?.let {
                                if (it.isNotBlank()) append("\n$it")
                            }
                        }
                        val checkInShort = formatShortDate(rent.CheckInDate)
                        val checkOutShort = formatShortDate(rent.CheckOutDate)
                        TimelineItemData(
                            title = rent.Status ?: unknownStatusText,
                            date = "$checkOutShort $checkInShort",
                            subtitle = null,
                            description = description.ifBlank { null },
                            isActive = index == 0
                        )
                    }

                    AppTimeline(
                        items = timelineItems,
                        activeColor = primaryBlue,
                        titleContent = { index, item ->
                            val rent = rentList[index]
                            Text(
                                text = buildString {
                                    rent.DormitoryFullName?.let { append(it) }
                                    append(" - ${item.title}")
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF1E293B)
                            )
                        },
                        itemContent = { index, _ ->
                            val rent = rentList[index]
                            rent.DormitoryRoomTypeName?.let { roomType ->
                                Text(
                                    text = roomType,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFF546E7A),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
