@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.bdsoftware.idorm.core.designsystem.component

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Style = Style,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isButtonClickable = enabled && !isLoading
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = isButtonClickable
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(
                onClick = onClick,
                enabled = isButtonClickable,
                indication = null,
                interactionSource = interactionSource
            )
            .styleable(styleState, ComponentStyles.primaryButtonStyle, style),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = loadingText ?: "Đang xử lý...",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        } else {
            content()
        }
    }
}

@Composable
fun AppButtonText(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
}
