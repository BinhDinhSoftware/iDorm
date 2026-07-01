@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.bdsoftware.idorm.core.designsystem.theme

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ComponentStyles {
    val DashboardBackground = Color(0xFFF8F9FA)
    val PrimaryBlue = Color(0xFF0073C0)
    val AccentCyan = Color(0xFF80DEEA)
    val PromoGradientStart = Color(0xFF26C6DA)
    val PromoGradientEnd = Color(0xFF0073C0)
    val PrideGradientStart = Color(0xFF78140F)
    val PrideGradientEnd = Color(0xFFD32F2F)
    val ErrorRed = Color(0xFFE53935)
    val SuccessGreen = Color(0xFF43A047)
    val AppFontFamily = Roboto

    val primaryButtonStyle = Style {
        background(PrimaryBlue) // Primary blue
        shape(RoundedCornerShape(24.dp)) // Pill shape
    }

    val errorButtonStyle = Style {
        background(ErrorRed)
        shape(RoundedCornerShape(24.dp))
    }

    val outlinePrimaryButtonStyle = Style {
        background(Color.White)
        borderWidth(1.dp)
        borderColor(PrimaryBlue)
        shape(RoundedCornerShape(24.dp))
    }

    val outlineErrorButtonStyle = Style {
        background(Color.White)
        borderWidth(1.dp)
        borderColor(ErrorRed)
        shape(RoundedCornerShape(24.dp))
    }

    val notificationItemStyle = Style {
        // padding(horizontal = 24.dp, vertical = 12.dp)
        // Note: padding not supported in this version of StyleScope
    }

    val appTextAreaStyle = Style {
        // OutlinedTextField handles its own styling via Material 3;
        // this style is a placeholder for layout-level overrides if needed.
    }
}
