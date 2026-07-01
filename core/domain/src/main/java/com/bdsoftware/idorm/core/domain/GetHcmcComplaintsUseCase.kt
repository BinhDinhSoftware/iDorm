package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcComplaintItem
import javax.inject.Inject

class GetHcmcComplaintsUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(requestId: Int, userId: Int): Result<List<NetworkHcmcComplaintItem>> {
        return repository.getComplaints(requestId, userId)
    }
}
