package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.data.repository.SelectedFile
import javax.inject.Inject

class UpdateHcmcRequestUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(
        requestId: Int,
        userId: String,
        note: String,
        dynamicData: String?,
        removedImageIds: List<Int>,
        newAttachments: List<SelectedFile>
    ): Result<Unit> {
        return repository.updateRequest(
            requestId = requestId,
            userId = userId,
            note = note,
            dynamicData = dynamicData,
            removedImageIds = removedImageIds,
            newAttachments = newAttachments
        )
    }
}
