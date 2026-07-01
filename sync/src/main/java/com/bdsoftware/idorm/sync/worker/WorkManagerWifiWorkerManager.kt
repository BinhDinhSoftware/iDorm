package com.bdsoftware.idorm.sync.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.bdsoftware.idorm.core.data.repository.WifiWorkerManager
import com.bdsoftware.idorm.sync.service.WifiAutoRenewService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkManagerWifiWorkerManager @Inject constructor(
    @ApplicationContext private val context: Context
) : WifiWorkerManager {

    companion object {
        private const val TAG = "WifiWorkerManager"
    }

    override fun startAutoRenewWorker() {
        Log.d(TAG, "[DEBUG] Khởi động WifiAutoRenewService...")
        val intent = Intent(context, WifiAutoRenewService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun stopAutoRenewWorker() {
        Log.d(TAG, "[DEBUG] Dừng WifiAutoRenewService...")
        val intent = Intent(context, WifiAutoRenewService::class.java)
        context.stopService(intent)
    }
}
