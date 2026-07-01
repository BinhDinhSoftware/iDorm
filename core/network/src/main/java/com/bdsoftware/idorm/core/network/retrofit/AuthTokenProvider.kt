package com.bdsoftware.idorm.core.network.retrofit

import kotlinx.coroutines.flow.Flow

interface AuthTokenProvider {
    val token: Flow<String?>
}
