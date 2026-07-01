package com.bdsoftware.idorm.core.data.repository

import android.util.Log
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceResponse
import com.bdsoftware.idorm.core.network.model.NetworkInvoiceEWResponse
import com.bdsoftware.idorm.core.network.model.NetworkGenerateQrResponse
import com.bdsoftware.idorm.core.network.retrofit.RetrofitDefaultNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val defaultNetwork: RetrofitDefaultNetwork
) {
    private val _roomInvoices = MutableStateFlow<List<NetworkInvoiceResponse>>(emptyList())
    val roomInvoices: StateFlow<List<NetworkInvoiceResponse>> = _roomInvoices.asStateFlow()

    private val _serviceInvoices = MutableStateFlow<List<NetworkInvoiceEWResponse>>(emptyList())
    val serviceInvoices: StateFlow<List<NetworkInvoiceEWResponse>> = _serviceInvoices.asStateFlow()

    val unpaidInvoiceCount: Flow<Int> = combine(_roomInvoices, _serviceInvoices) { room, service ->
        room.count { !it.IsPayment } + service.count { !it.IsPayment }
    }

    suspend fun getInvoices(): List<NetworkInvoiceResponse> {
        return try {
            val response = defaultNetwork.getInvoices().sortedByDescending { it.CreatedDate }
            _roomInvoices.value = response
            response
        } catch (e: Exception) {
            Log.e("InvoiceRepository", "Error fetching invoices", e)
            emptyList()
        }
    }

    suspend fun getInvoiceEWs(): List<NetworkInvoiceEWResponse> {
        return try {
            val response = defaultNetwork.getInvoiceEWs().sortedByDescending { it.CreatedDate }
            _serviceInvoices.value = response
            response
        } catch (e: Exception) {
            Log.e("InvoiceRepository", "Error fetching EW invoices", e)
            emptyList()
        }
    }

    suspend fun generateQrCode(param: String): NetworkGenerateQrResponse? {
        return try {
            defaultNetwork.generateQrCode(param)
        } catch (e: Exception) {
            Log.e("InvoiceRepository", "Error generating QR codes", e)
            null
        }
    }
}
