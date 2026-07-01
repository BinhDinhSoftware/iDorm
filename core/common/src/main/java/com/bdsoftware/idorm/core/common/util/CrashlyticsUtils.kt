package com.bdsoftware.idorm.core.common.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Standard utility wrapper for Firebase Crashlytics to centralize crash reporting,
 * user identification, custom logs, and keys across the multi-module project.
 */
object CrashlyticsUtils {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    /**
     * Associates a user identifier with crash reports.
     * Use non-sensitive identifiers (e.g., student user IDs or obfuscated hashes).
     */
    fun setUserId(userId: String) {
        if (userId.isNotBlank()) {
            crashlytics.setUserId(userId)
        }
    }

    /**
     * Sets a custom key and string value to associate with crash reports.
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Sets a custom key and boolean value to associate with crash reports.
     */
    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Sets a custom key and integer value to associate with crash reports.
     */
    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Sets a custom key and double value to associate with crash reports.
     */
    fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Sets a custom key and float value to associate with crash reports.
     */
    fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Logs a custom message to be included in the crash report.
     * These logs appear as breadcrumbs leading up to a crash.
     */
    fun log(message: String) {
        if (message.isNotBlank()) {
            crashlytics.log(message)
        }
    }

    /**
     * Records a non-fatal exception to Firebase Crashlytics.
     * You can optionally provide additional context keys to associate with this specific error.
     */
    fun recordException(throwable: Throwable, contextKeys: Map<String, Any>? = null) {
        contextKeys?.forEach { (key, value) ->
            when (value) {
                is String -> setCustomKey(key, value)
                is Boolean -> setCustomKey(key, value)
                is Int -> setCustomKey(key, value)
                is Double -> setCustomKey(key, value)
                is Float -> setCustomKey(key, value)
                else -> setCustomKey(key, value.toString())
            }
        }
        crashlytics.recordException(throwable)
    }
}
