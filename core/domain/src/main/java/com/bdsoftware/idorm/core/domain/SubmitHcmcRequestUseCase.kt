package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.data.repository.SelectedFile
import javax.inject.Inject

class SubmitHcmcRequestUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(
        serviceId: Int,
        userId: String,
        note: String,
        dynamicData: String?,
        attachments: List<SelectedFile>
    ): Result<Unit> {
        return repository.createRequest(serviceId, userId, note, dynamicData, attachments)
    }
}
