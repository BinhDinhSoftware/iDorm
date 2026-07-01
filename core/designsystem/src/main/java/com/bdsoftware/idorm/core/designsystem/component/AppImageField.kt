package com.bdsoftware.idorm.core.designsystem.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R

@Composable
fun AppImageField(
    images: List<Uri>,
    onAddImage: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    label: String? = null,
    maxImages: Int = 5,
    required: Boolean = false,
    containerColor: Color = Color.White,
    modifier: Modifier = Modifier,
    imageContent: @Composable (Uri) -> Unit
) {
    val primaryBlue = ComponentStyles.PrimaryBlue
    val resolvedLabel = label ?: stringResource(R.string.image_field_default_label)

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Image button — always first
                    if (images.size < maxImages) {
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = primaryBlue.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(primaryBlue.copy(alpha = 0.04f))
                                .clickable { onAddImage() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = stringResource(R.string.image_field_add_photo),
                                    tint = primaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.image_field_add_photo),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Display selected images
                    images.forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        ) {
                            imageContent(uri)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { onRemoveImage(uri) }
                                    .size(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.image_field_delete_photo),
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Floating label styled like AppTextField
            val labelText = buildAnnotatedString {
                append(resolvedLabel)
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

