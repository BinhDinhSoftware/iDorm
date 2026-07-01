package com.bdsoftware.idorm.core.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.app.NotificationCompat
import com.bdsoftware.idorm.core.data.R
import com.bdsoftware.idorm.core.data.repository.WifiConnectionManager
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed interface WifiNotificationStatus {
    object WaitingForConnection : WifiNotificationStatus
    object WifiDisabled : WifiNotificationStatus
    object AwingAcquiring : WifiNotificationStatus
    object InternetOk : WifiNotificationStatus
    data class RenewFailed(val error: String) : WifiNotificationStatus
}

@Singleton
class WifiNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiConnectionManager: WifiConnectionManager,
    private val preferencesDataSource: IDormPreferencesDataSource
) {
    companion object {
        private const val CHANNEL_ID = "wifi_free_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Tạo context với locale đúng theo cài đặt ngôn ngữ của người dùng.
     * Giải quyết lỗi Service context không cập nhật locale khi đổi ngôn ngữ.
     */
    private fun getLocalizedContext(): Context {
        val langPref = try {
            runBlocking { preferencesDataSource.appLanguage.first() }
        } catch (_: Exception) {
            "VI"
        }
        val langCode = if (langPref.equals("VI", ignoreCase = true)) "vi" else "en"
        val locale = Locale(langCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }

    private fun getLocalizedString(resId: Int, vararg formatArgs: Any): String {
        return getLocalizedContext().getString(resId, *formatArgs)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getLocalizedString(R.string.notification_channel_name)
            val descriptionText = getLocalizedString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Cập nhật lại tên notification channel khi ngôn ngữ thay đổi.
     */
    fun refreshNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getLocalizedString(R.string.notification_channel_name)
            val descriptionText = getLocalizedString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createWifiActiveNotification(status: WifiNotificationStatus? = null): android.app.Notification {
        val currentSsid = wifiConnectionManager.getCurrentWifiSsid()
        val isAwing = wifiConnectionManager.isConnectedToAwing()

        val title = if (currentSsid != null) {
            getLocalizedString(R.string.notification_title_connected, currentSsid)
        } else {
            getLocalizedString(R.string.notification_title_active)
        }

        val contentText = if (status != null) {
            when (status) {
                WifiNotificationStatus.WaitingForConnection -> getLocalizedString(R.string.notification_status_waiting)
                WifiNotificationStatus.WifiDisabled -> getLocalizedString(R.string.notification_status_disabled)
                WifiNotificationStatus.AwingAcquiring -> getLocalizedString(R.string.notification_status_acquiring)
                WifiNotificationStatus.InternetOk -> getLocalizedString(R.string.notification_status_internet_ok)
                is WifiNotificationStatus.RenewFailed -> getLocalizedString(R.string.notification_status_renew_failed, status.error)
            }
        } else {
            if (isAwing) {
                getLocalizedString(R.string.notification_status_awing_monitoring)
            } else if (currentSsid != null) {
                getLocalizedString(R.string.notification_status_other_waiting)
            } else {
                getLocalizedString(R.string.notification_status_waiting)
            }
        }

        // Intent to open app when clicking on the notification itself
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingLaunchIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true) // Cannot be swiped away
            .setContentIntent(pendingLaunchIntent)

        return builder.build()
    }

    fun showWifiActiveNotification(status: WifiNotificationStatus? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        notificationManager.notify(NOTIFICATION_ID, createWifiActiveNotification(status))
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
