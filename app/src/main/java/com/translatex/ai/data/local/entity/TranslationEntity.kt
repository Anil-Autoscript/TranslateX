package com.translatex.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted translation record stored in Room.
 */
@Entity(tableName = "translation_history")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Original input text. */
    val sourceText: String,

    /** Translated output text. */
    val translatedText: String,

    /** BCP-47 code of the source language. */
    val sourceLanguageCode: String,

    /** BCP-47 code of the target language. */
    val targetLanguageCode: String,

    /** Unix epoch millis when the translation was performed. */
    val timestamp: Long = System.currentTimeMillis(),

    /** Whether the user has starred this item. */
    val isFavorite: Boolean = false
)
