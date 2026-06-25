#!/usr/bin/env python3
"""
TranslateX AI – GitHub Actions translation backend.

Environment variables (injected by the workflow):
  INPUT_TEXT   : text to translate
  SOURCE_LANG  : BCP-47 source language code
  TARGET_LANG  : BCP-47 target language code
  REQUEST_ID   : opaque ID from the Android app (unused by script, useful for logs)

Output:
  result.json  : { "success": bool, "translatedText": str|null, "error": str|null }
"""

import json
import os
import sys

def write_result(success: bool, translated_text: str | None, error: str | None) -> None:
    payload = {
        "success": success,
        "translatedText": translated_text,
        "error": error,
    }
    with open("result.json", "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)
    print(json.dumps(payload, ensure_ascii=False))


def map_lang_code(code: str) -> str:
    """
    deep-translator uses full language names for GoogleTranslator.
    Map BCP-47 codes to the names it accepts.
    """
    mapping = {
        "en": "english",
        "hi": "hindi",
        "es": "spanish",
        "fr": "french",
        "de": "german",
        "pt": "portuguese",
        "it": "italian",
        "ru": "russian",
        "zh": "chinese (simplified)",
        "ja": "japanese",
        "ko": "korean",
        "ar": "arabic",
        "tr": "turkish",
        "nl": "dutch",
        "pl": "polish",
        "th": "thai",
        "vi": "vietnamese",
        "id": "indonesian",
        "bn": "bengali",
        "mr": "marathi",
    }
    return mapping.get(code.lower(), code.lower())


def main() -> None:
    text        = os.environ.get("INPUT_TEXT", "").strip()
    source_code = os.environ.get("SOURCE_LANG", "en").strip()
    target_code = os.environ.get("TARGET_LANG", "hi").strip()
    request_id  = os.environ.get("REQUEST_ID", "unknown")

    print(f"[TranslateX] request_id={request_id}  {source_code}→{target_code}")
    print(f"[TranslateX] input text: {text[:120]}{'...' if len(text) > 120 else ''}")

    if not text:
        write_result(False, None, "Input text is empty.")
        sys.exit(0)

    source_lang = map_lang_code(source_code)
    target_lang = map_lang_code(target_code)

    try:
        from deep_translator import GoogleTranslator

        translator = GoogleTranslator(source=source_lang, target=target_lang)
        translated = translator.translate(text)

        if not translated:
            write_result(False, None, "Translator returned an empty result.")
            sys.exit(0)

        print(f"[TranslateX] translated: {translated[:120]}{'...' if len(translated) > 120 else ''}")
        write_result(True, translated, None)

    except Exception as exc:
        error_msg = f"Translation error: {exc}"
        print(f"[TranslateX] ERROR: {error_msg}", file=sys.stderr)
        write_result(False, None, error_msg)
        sys.exit(1)


if __name__ == "__main__":
    main()
