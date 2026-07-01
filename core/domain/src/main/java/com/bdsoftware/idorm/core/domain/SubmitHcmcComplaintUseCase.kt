package com.bdsoftware.idorm.core.domain

import com.bdsoftware.idorm.core.data.repository.HcmcRepository
import com.bdsoftware.idorm.core.data.repository.SelectedFile
import javax.inject.Inject

class SubmitHcmcComplaintUseCase @Inject constructor(
    private val repository: HcmcRepository
) {
    suspend operator fun invoke(requestId: Int, userId: String, content: String, attachments: List<SelectedFile>): Result<Unit> {
        return repository.createComplaint(requestId, userId, content, attachments)
    }
}
