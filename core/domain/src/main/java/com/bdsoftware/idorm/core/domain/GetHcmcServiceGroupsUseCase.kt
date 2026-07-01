package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.network.model.NetworkHcmcServiceGroup
import javax.inject.Inject

class GetHcmcServiceGroupsUseCase @Inject constructor(
    private val hcmcRepository: HcmcRepository
) {
    suspend operator fun invoke(): Result<List<NetworkHcmcServiceGroup>> {
        return hcmcRepository.getServiceGroups().map { groups ->
            groups.map { group ->
                val enabledServices = group.services.filter { service ->
                    service.state?.lowercase() != "disabled"
                }
                group.copy(services = enabledServices)
            }
        }
    }
}
