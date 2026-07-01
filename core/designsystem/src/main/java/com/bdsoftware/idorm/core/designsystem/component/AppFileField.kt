package com.bdsoftware.idorm.core.designsystem.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles

data class AppFileItem(
    val uri: Uri,
    val name: String,
    val size: Long
)

@Composable
fun AppFileField(
    files: List<AppFileItem>,
    onAddFile: () -> Unit,
    onRemoveFile: (Uri) -> Unit,
    label: String = "Tệp đính kèm",
    maxFiles: Int = 3,
    required: Boolean = false,
    containerColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val primaryBlue = ComponentStyles.PrimaryBlue

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(6.dp)) // Spacer to prevent label clipping when offset upwards
        Box(modifier = Modifier.fillMaxWidth()) {
            // Outer container styled like OutlinedTextField
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(containerColor)
                    .padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 14.dp)
            ) {
                // File picker trigger button
                if (files.size < maxFiles) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .clickable { onAddFile() }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Đính kèm tệp",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Chọn tệp đính kèm...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // List selected files
                files.forEachIndexed { index, file ->
                    if (index == 0 && files.size < maxFiles) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    val sizeInMb = file.size / (1024f * 1024f)
                    val sizeText = String.format("%.2f MB", sizeInMb)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "File",
                            tint = primaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = sizeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { onRemoveFile(file.uri) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Xóa tệp",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Floating label styled like AppTextField
            val labelText = buildAnnotatedString {
                append(label)
                if (required) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append("*")
                    }
                }
            }
            Text(
                text = labelText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
                    .offset(y = (-8).dp)
                    .background(containerColor)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}
