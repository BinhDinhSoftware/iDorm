package com.bdsoftware.idorm.core.network.retrofit

import kotlinx.coroutines.flow.Flow

/**
 * Cung cấp token và refresh token cho HCMC (Hành chính một cửa).
 * Được implement bởi IDormPreferencesDataSource.
 */
interface HcmcAuthTokenProvider {
    val hcmcAccessToken: Flow<String?>
    val hcmcRefreshToken: Flow<String?>
}
