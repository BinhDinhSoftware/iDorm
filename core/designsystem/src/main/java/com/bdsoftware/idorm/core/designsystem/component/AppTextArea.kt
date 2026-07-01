@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.bdsoftware.idorm.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles

@Composable
fun AppTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    required: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minLines: Int = 3,
    maxLines: Int = 5,
    maxLength: Int? = null,
    containerColor: Color = Color.White,
    modifier: Modifier = Modifier,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource)
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .styleable(styleState, ComponentStyles.appTextAreaStyle, style)
    ) {
        Spacer(modifier = Modifier.height(6.dp)) // Spacer to prevent label clipping when offset upwards
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (maxLength == null || newValue.length <= maxLength) {
                        onValueChange(newValue)
                    }
                },
                isError = isError,
                singleLine = false,
                minLines = minLines,
                maxLines = maxLines,
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = placeholder?.let {
                    {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                },
                keyboardOptions = keyboardOptions,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = ComponentStyles.PrimaryBlue,
                    unfocusedBorderColor = Color.LightGray,
                    errorBorderColor = ComponentStyles.ErrorRed
                )
            )

            if (label != null) {
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
                    color = if (isError) ComponentStyles.ErrorRed else if (isFocused) ComponentStyles.PrimaryBlue else Color.Gray,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp)
                        .offset(y = (-8).dp)
                        .background(containerColor)
                        .padding(horizontal = 4.dp)
                )
            }

            if (maxLength != null) {
                Text(
                    text = "${value.length}/$maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 12.dp)
                )
            }
        }

        if (isError && !errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = ComponentStyles.ErrorRed,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
