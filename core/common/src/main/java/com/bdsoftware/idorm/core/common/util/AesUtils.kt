package com.bdsoftware.idorm.core.common.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AesUtils {
    private const val KEY = "f3g7fd3g4fd7s6gry5fg4y45yghfdgh4" // 32 bytes Key for AES-256

    /**
     * Encrypts the plaintext using AES-256-ECB with ZeroPadding, then encodes to Base64 (NO_WRAP).
     */
    fun encrypt(plaintext: String): String {
        val rawData = plaintext.toByteArray(Charsets.UTF_8)
        val paddedData = zeroPad(rawData, 16)
        
        val secretKey = SecretKeySpec(KEY.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val encryptedBytes = cipher.doFinal(paddedData)
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    /**
     * Pads the data with 0x00 bytes to ensure its length is a multiple of [blockSize].
     * If the data is already a multiple, no padding is added.
     */
    private fun zeroPad(data: ByteArray, blockSize: Int): ByteArray {
        val remainder = data.size % blockSize
        if (remainder == 0) {
            return data
        }
        val padSize = blockSize - remainder
        val padded = ByteArray(data.size + padSize)
        System.arraycopy(data, 0, padded, 0, data.size)
        // Note: New elements in a Kotlin/Java ByteArray are automatically initialized to 0.
        return padded
    }
}
