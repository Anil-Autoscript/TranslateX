package com.translatex.ai.model

/**
 * Represents a supported translation language.
 *
 * @param code      BCP-47 language code understood by the translation backend.
 * @param englishName  Human-readable name in English.
 * @param nativeName   Name as written in the language itself.
 * @param flag      Unicode flag emoji for the primary country.
 */
data class Language(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val flag: String
)

/** All languages supported by TranslateX AI. */
val SupportedLanguages = listOf(
    Language("en",  "English",              "English",      "🇬🇧"),
    Language("hi",  "Hindi",                "हिन्दी",         "🇮🇳"),
    Language("es",  "Spanish",              "Español",      "🇪🇸"),
    Language("fr",  "French",               "Français",     "🇫🇷"),
    Language("de",  "German",               "Deutsch",      "🇩🇪"),
    Language("pt",  "Portuguese",           "Português",    "🇧🇷"),
    Language("it",  "Italian",              "Italiano",     "🇮🇹"),
    Language("ru",  "Russian",              "Русский",      "🇷🇺"),
    Language("zh",  "Chinese (Simplified)", "中文",          "🇨🇳"),
    Language("ja",  "Japanese",             "日本語",         "🇯🇵"),
    Language("ko",  "Korean",               "한국어",          "🇰🇷"),
    Language("ar",  "Arabic",               "العربية",      "🇸🇦"),
    Language("tr",  "Turkish",              "Türkçe",       "🇹🇷"),
    Language("nl",  "Dutch",                "Nederlands",   "🇳🇱"),
    Language("pl",  "Polish",               "Polski",       "🇵🇱"),
    Language("th",  "Thai",                 "ภาษาไทย",      "🇹🇭"),
    Language("vi",  "Vietnamese",           "Tiếng Việt",   "🇻🇳"),
    Language("id",  "Indonesian",           "Bahasa Indonesia", "🇮🇩"),
    Language("bn",  "Bengali",              "বাংলা",          "🇧🇩"),
    Language("mr",  "Marathi",              "मराठी",          "🇮🇳")
)

fun languageByCode(code: String): Language =
    SupportedLanguages.find { it.code == code } ?: SupportedLanguages.first()
