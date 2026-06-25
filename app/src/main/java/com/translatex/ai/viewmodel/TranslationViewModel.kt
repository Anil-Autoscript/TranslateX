package com.translatex.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.translatex.ai.data.local.entity.TranslationEntity
import com.translatex.ai.model.*
import com.translatex.ai.repository.TranslationRepository
import com.translatex.ai.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single ViewModel that drives the Home, History, and Favourites screens.
 */
@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val repo:  TranslationRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    // ── UI state ──────────────────────────────────────────────────────────

    private val _inputText    = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _sourceLang   = MutableStateFlow(SupportedLanguages.first())
    val sourceLang: StateFlow<Language> = _sourceLang.asStateFlow()

    private val _targetLang   = MutableStateFlow(languageByCode("hi"))
    val targetLang: StateFlow<Language> = _targetLang.asStateFlow()

    private val _translationResult = MutableStateFlow<TranslationResult>(TranslationResult.Idle)
    val translationResult: StateFlow<TranslationResult> = _translationResult.asStateFlow()

    // ── History / favourites ──────────────────────────────────────────────

    private val _historyQuery = MutableStateFlow("")

    val history: StateFlow<List<TranslationEntity>> = _historyQuery
        .flatMapLatest { q ->
            if (q.isBlank()) repo.getHistory() else repo.searchHistory(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<TranslationEntity>> = repo.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Preferences ───────────────────────────────────────────────────────

    val darkMode: StateFlow<String> = prefs.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    // ── Actions ───────────────────────────────────────────────────────────

    fun onInputChanged(text: String) {
        _inputText.value = text
        if (text.isBlank()) _translationResult.value = TranslationResult.Idle
    }

    fun setSourceLanguage(lang: Language) {
        _sourceLang.value = lang
        viewModelScope.launch { prefs.setSourceLang(lang.code) }
    }

    fun setTargetLanguage(lang: Language) {
        _targetLang.value = lang
        viewModelScope.launch { prefs.setTargetLang(lang.code) }
    }

    fun swapLanguages() {
        val tmp = _sourceLang.value
        _sourceLang.value = _targetLang.value
        _targetLang.value = tmp

        // If there is a translated result, swap the texts too
        val result = _translationResult.value
        if (result is TranslationResult.Success) {
            val oldInput = _inputText.value
            _inputText.value = result.translatedText
            _translationResult.value = TranslationResult.Success(oldInput)
        }
    }

    fun translate() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _translationResult.value = TranslationResult.Loading
            _translationResult.value = repo.translate(
                text       = text,
                sourceCode = _sourceLang.value.code,
                targetCode = _targetLang.value.code
            )
        }
    }

    fun loadFromHistory(entity: TranslationEntity) {
        _inputText.value    = entity.sourceText
        _sourceLang.value   = languageByCode(entity.sourceLanguageCode)
        _targetLang.value   = languageByCode(entity.targetLanguageCode)
        _translationResult.value = TranslationResult.Success(entity.translatedText)
    }

    fun setHistoryQuery(q: String) { _historyQuery.value = q }

    fun toggleFavorite(entity: TranslationEntity) =
        viewModelScope.launch { repo.toggleFavorite(entity) }

    fun deleteTranslation(entity: TranslationEntity) =
        viewModelScope.launch { repo.deleteTranslation(entity) }

    fun clearHistory() = viewModelScope.launch { repo.clearHistory() }

    fun setDarkMode(value: String) =
        viewModelScope.launch { prefs.setDarkMode(value) }

    fun clearOutput() { _translationResult.value = TranslationResult.Idle }
}
