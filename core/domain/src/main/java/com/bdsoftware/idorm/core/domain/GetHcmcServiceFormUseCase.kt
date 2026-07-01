package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceFormResponse
import javax.inject.Inject

class GetHcmcServiceFormUseCase @Inject constructor(
    private val hcmcRepository: HcmcRepository
) {
    suspend operator fun invoke(serviceId: Int): Result<NetworkHcmcServiceFormResponse> {
        return hcmcRepository.getServiceForm(serviceId)
    }
}
