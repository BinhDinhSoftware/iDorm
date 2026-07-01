package com.bdsoftware.idorm.core.data.repository

interface WifiWorkerManager {
    fun startAutoRenewWorker()
    fun stopAutoRenewWorker()
}
