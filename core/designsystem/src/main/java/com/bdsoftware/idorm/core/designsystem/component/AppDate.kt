package com.bdsoftware.idorm.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AppDateDialog(
    label: String,
    selectedDate: String?, // YYYY-MM-DD
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var tempSelectedDate by remember { mutableStateOf(selectedDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ComponentStyles.PrimaryBlue,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Month Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
                    }
                    Text(
                        text = "Tháng ${currentMonth.monthValue.toString().padStart(2, '0')} / ${currentMonth.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Tháng sau")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                CalendarGrid(
                    yearMonth = currentMonth,
                    selectedDates = tempSelectedDate?.let { listOf(it) } ?: emptyList(),
                    onDateClick = { date ->
                        tempSelectedDate = date
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            tempSelectedDate?.let {
                                onConfirm(it.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            }
                        },
                        enabled = tempSelectedDate != null,
                        colors = ButtonDefaults.buttonColors(containerColor = ComponentStyles.PrimaryBlue)
                    ) {
                        Text("Xác nhận", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AppDateMultiDialog(
    label: String,
    selectedDates: List<String>, // List of YYYY-MM-DD
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var tempSelectedDates by remember {
        mutableStateOf(
            selectedDates.mapNotNull {
                try { LocalDate.parse(it) } catch (e: Exception) { null }
            }.toMutableStateList()
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ComponentStyles.PrimaryBlue,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Month Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
                    }
                    Text(
                        text = "Tháng ${currentMonth.monthValue.toString().padStart(2, '0')} / ${currentMonth.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Tháng sau")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                CalendarGrid(
                    yearMonth = currentMonth,
                    selectedDates = tempSelectedDates,
                    onDateClick = { date ->
                        if (tempSelectedDates.contains(date)) {
                            tempSelectedDates.remove(date)
                        } else {
                            tempSelectedDates.add(date)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val formattedDates = tempSelectedDates.map {
                                it.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            }
                            onConfirm(formattedDates)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ComponentStyles.PrimaryBlue)
                    ) {
                        Text("Xác nhận", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDates: List<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Thứ Hai, 7 = Chủ Nhật
    val emptyPreDays = firstDayOfWeek - 1

    val weekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Weekdays Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weeks Days Grid using Rows
        var currentDay = 1
        val totalCells = emptyPreDays + daysInMonth
        val weeksCount = (totalCells + 6) / 7

        for (week in 0 until weeksCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dayOfWeek in 0 until 7) {
                    val cellIndex = week * 7 + dayOfWeek
                    if (cellIndex < emptyPreDays || currentDay > daysInMonth) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        val date = yearMonth.atDay(currentDay)
                        val isSelected = selectedDates.contains(date)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) ComponentStyles.PrimaryBlue else Color.Transparent)
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                        currentDay++
                    }
                }
            }
        }
    }
}
