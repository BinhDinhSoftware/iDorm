package com.bdsoftware.idorm.core.domain

import android.util.Log
import com.bdsoftware.idorm.core.common.util.AesUtils
import com.bdsoftware.idorm.core.data.repository.InvoiceRepository
import com.bdsoftware.idorm.core.data.repository.UserRepository
import com.bdsoftware.idorm.core.model.PaymentDetails
import javax.inject.Inject

/**
 * Use case to prepare the payment details by fetching VCB and BIDV dynamic QR codes.
 */
class GetPaymentParamsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val invoiceRepository: InvoiceRepository
) {
    suspend operator fun invoke(invoiceId: Int, totalAmount: Double, isEw: Boolean): Result<PaymentDetails> {
        return try {
            val profile = userRepository.getStudentInfo()
            val suffix = if (isEw) "InvoiceEW_WEB" else "Invoice_WEB"
            val plaintext = "${profile.id}_${invoiceId}_$suffix"
            Log.d("Payment", "Plaintext: $plaintext")
            val encrypted = AesUtils.encrypt(plaintext)

            // Fetch dynamic QR codes (pass raw encrypted Base64 string, Retrofit will url-encode it)
            val qrResponse = invoiceRepository.generateQrCode(encrypted)

            val bidvBase64 = qrResponse?.bidvQr?.base64.orEmpty()
            val vcbBase64 = qrResponse?.vcbQr?.base64.orEmpty()

            val paymentDetails = PaymentDetails(
                amount = totalAmount,
                bidvQrBase64 = bidvBase64,
                vcbQrBase64 = vcbBase64
            )
            Result.success(paymentDetails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

