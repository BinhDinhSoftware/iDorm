package com.bdsoftware.idorm.sync.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit
import com.bdsoftware.idorm.core.data.repository.WifiAuthRepository
import com.bdsoftware.idorm.core.network.wifi.WifiAuthDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WifiAutoRenewWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val wifiAuthDataSource: WifiAuthDataSource,
    private val wifiAuthRepository: WifiAuthRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WifiAutoRenew"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "[DEBUG] ======== doWork() bắt đầu @ ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())} ========")
        val result = wifiAuthDataSource.loginWifi()
        if (result.isSuccess) {
            Log.d(TAG, "[DEBUG] Gia hạn Wifi thành công. Lên lịch chạy tiếp sau 15 giây...")
            
            val nextWorkRequest = OneTimeWorkRequestBuilder<WifiAutoRenewWorker>()
                // .setInitialDelay(15, TimeUnit.SECONDS)
                // 30 phút
                .setInitialDelay(30, TimeUnit.MINUTES)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "WifiAutoRenewWorker",
                ExistingWorkPolicy.REPLACE,
                nextWorkRequest
            )
            
            Result.success()
        } else {
            val errorMessage = result.exceptionOrNull()?.message ?: getStringByName("notification_error_fallback")
            Log.e(TAG, "[DEBUG] Gia hạn Wifi thất bại: $errorMessage")
            
            // 1. Hiển thị thông báo lỗi gia hạn
            showErrorNotification(errorMessage)
            
            // 2. Hủy thông báo trạng thái hoạt động của Wifi (ID 1001)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001)
            
            // 3. Cập nhật trạng thái trong repository thành inactive
            wifiAuthRepository.setWifiActiveState(false)
            
            // 4. Hủy Worker định kỳ để dừng chạy ngầm vô ích
            WorkManager.getInstance(context).cancelUniqueWork("WifiAutoRenewWorker")
            Log.d(TAG, "[DEBUG] Worker đã tự hủy do lỗi")
            
            Result.failure()
        }
    }

    private fun getStringByName(name: String, vararg formatArgs: Any): String {
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId, *formatArgs)
        } else {
            name
        }
    }

    private fun showErrorNotification(message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "wifi_error_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getStringByName("notification_error_channel_name"),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(getStringByName("notification_error_title"))
            .setContentText(message)
            .setSmallIcon(com.bdsoftware.idorm.core.data.R.drawable.ic_notification_logo)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }
}
