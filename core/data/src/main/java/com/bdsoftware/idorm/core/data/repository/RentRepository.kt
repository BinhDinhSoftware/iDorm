package com.bdsoftware.idorm.core.data.repository

import android.util.Log
import com.bdsoftware.idorm.core.network.model.NetworkRentItem
import com.bdsoftware.idorm.core.network.retrofit.RetrofitDefaultNetwork
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RentRepository @Inject constructor(
    private val defaultNetwork: RetrofitDefaultNetwork
) {
    suspend fun getRentList(): List<NetworkRentItem> {
        return try {
            defaultNetwork.getRentList().sortedByDescending { it.CreatedDate ?: "" }
        } catch (e: Exception) {
            Log.e("RentRepository", "Error fetching rent list", e)
            emptyList()
        }
    }
}
