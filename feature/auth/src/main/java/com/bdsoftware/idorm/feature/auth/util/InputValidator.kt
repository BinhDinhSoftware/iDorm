package com.bdsoftware.idorm.feature.auth.util

object InputValidator {
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    private val DIGITS_REGEX = "^[0-9]+$".toRegex()

    fun validateNotBlank(value: String, errorMessage: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult.Invalid(errorMessage)
        } else {
            ValidationResult.Valid
        }
    }

    fun validateEmail(value: String, emptyMessage: String, formatMessage: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Invalid(emptyMessage)
            !EMAIL_REGEX.matches(value) -> ValidationResult.Invalid(formatMessage)
            else -> ValidationResult.Valid
        }
    }

    fun validateDigitsOnly(value: String, emptyMessage: String, formatMessage: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Invalid(emptyMessage)
            !DIGITS_REGEX.matches(value) -> ValidationResult.Invalid(formatMessage)
            else -> ValidationResult.Valid
        }
    }

    fun validateCustomRegex(value: String, regex: Regex, emptyMessage: String, formatMessage: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Invalid(emptyMessage)
            !regex.matches(value) -> ValidationResult.Invalid(formatMessage)
            else -> ValidationResult.Valid
        }
    }
}

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val errorMessage: String) : ValidationResult
}
