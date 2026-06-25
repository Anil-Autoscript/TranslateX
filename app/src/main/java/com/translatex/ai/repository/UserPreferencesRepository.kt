package com.translatex.ai.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

/**
 * Persists user settings via Jetpack DataStore.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    companion object {
        val KEY_DARK_MODE         = stringPreferencesKey("dark_mode")   // system|light|dark
        val KEY_SOURCE_LANG       = stringPreferencesKey("source_lang")
        val KEY_TARGET_LANG       = stringPreferencesKey("target_lang")
        val KEY_FONT_SIZE         = stringPreferencesKey("font_size")   // small|medium|large
    }

    val darkModeFlow: Flow<String> =
        ctx.dataStore.data.map { it[KEY_DARK_MODE] ?: "system" }

    val sourceLangFlow: Flow<String> =
        ctx.dataStore.data.map { it[KEY_SOURCE_LANG] ?: "en" }

    val targetLangFlow: Flow<String> =
        ctx.dataStore.data.map { it[KEY_TARGET_LANG] ?: "hi" }

    val fontSizeFlow: Flow<String> =
        ctx.dataStore.data.map { it[KEY_FONT_SIZE] ?: "medium" }

    suspend fun setDarkMode(value: String) =
        ctx.dataStore.edit { it[KEY_DARK_MODE] = value }

    suspend fun setSourceLang(code: String) =
        ctx.dataStore.edit { it[KEY_SOURCE_LANG] = code }

    suspend fun setTargetLang(code: String) =
        ctx.dataStore.edit { it[KEY_TARGET_LANG] = code }

    suspend fun setFontSize(value: String) =
        ctx.dataStore.edit { it[KEY_FONT_SIZE] = value }
}
