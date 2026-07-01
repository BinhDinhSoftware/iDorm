package com.bdsoftware.idorm.core.ui.config

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.OptIn
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.R as DesignR

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun MaintenanceDialog(
    message: String,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {}, // Cannot be dismissed by clicking outside
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = {
            Text(text = stringResource(DesignR.string.config_maintenance_title))
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            AppButton(onClick = onRetry) {
                AppButtonText(stringResource(DesignR.string.config_maintenance_retry_button))
            }
        }
    )
}
