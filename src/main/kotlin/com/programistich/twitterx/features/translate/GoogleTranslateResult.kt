package com.programistich.twitterx.features.translate

sealed interface GoogleTranslateResult {
    enum class ErrorType {
        UNKNOWN,
        EMPTY_RESPONSE
    }

    data class Error(val type: ErrorType) : GoogleTranslateResult
    data class Translated(val from: String, val to: String) : GoogleTranslateResult
}
