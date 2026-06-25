package com.translatex.ai.model

/** Holds the outcome of a translation request. */
sealed class TranslationResult {
    data class Success(val translatedText: String) : TranslationResult()
    data class Error(val message: String)          : TranslationResult()
    object Loading                                 : TranslationResult()
    object Idle                                    : TranslationResult()
}
