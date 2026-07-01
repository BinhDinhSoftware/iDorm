package com.bdsoftware.idorm.core.ui.config

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.OptIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.bdsoftware.idorm.core.designsystem.component.AppButton
import com.bdsoftware.idorm.core.designsystem.component.AppButtonText
import com.bdsoftware.idorm.core.designsystem.R as DesignR

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ForceUpdateDialog(
    latestVersion: String
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = {}, // Non-dismissible
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = {
            Text(text = stringResource(DesignR.string.config_update_title))
        },
        text = {
            Text(text = stringResource(DesignR.string.config_update_message, latestVersion))
        },
        confirmButton = {
            AppButton(
                onClick = {
                    val packageName = context.packageName
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                    }
                }
            ) {
                AppButtonText(stringResource(DesignR.string.config_update_button))
            }
        }
    )
}
