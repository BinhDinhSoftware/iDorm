package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcRequestDetail
import javax.inject.Inject

class GetHcmcRequestDetailUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(requestId: Int): Result<NetworkHcmcRequestDetail> {
        return repository.getRequestDetail(requestId)
    }
}
