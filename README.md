# 🌐 TranslateX AI

A **production-ready Android translation app** powered by **GitHub Actions** as a serverless backend — no paid servers required.

---

## ✨ Features

| Feature | Details |
|---|---|
| 20 languages | English, Hindi, Spanish, French, German, Portuguese, Italian, Russian, Chinese, Japanese, Korean, Arabic, Turkish, Dutch, Polish, Thai, Vietnamese, Indonesian, Bengali, Marathi |
| GitHub Actions backend | Triggers `workflow_dispatch`, polls for completion, downloads artifact |
| Room database | Full offline history & favourites |
| DataStore | Persistent user preferences |
| Material Design 3 | Dark/light theme, glassmorphism cards |
| MVVM + Clean Architecture | Repository pattern, Hilt DI, StateFlow |
| Language selector | Bottom sheet with instant search |
| Swap languages | Swaps text + languages simultaneously |
| Copy / Share / Speak | Output card action row |

---

## 🏗 Architecture

```
TranslateX-AI/
├── .github/workflows/
│   └── translate.yml          ← GitHub Actions workflow (backend)
├── backend/
│   └── translate.py           ← Python translation script
└── app/src/main/java/com/translatex/ai/
    ├── ui/
    │   ├── screens/            ← Splash, Home, History, Favourites, Settings
    │   ├── components/         ← Reusable composables
    │   └── theme/              ← Material3 colour scheme + typography
    ├── viewmodel/              ← TranslationViewModel
    ├── repository/             ← TranslationRepository, UserPreferencesRepository
    ├── data/
    │   ├── local/              ← Room DB, DAO, Entity
    │   └── remote/             ← Retrofit, DTOs
    ├── model/                  ← Language, TranslationResult
    ├── navigation/             ← NavGraph
    └── di/                     ← Hilt modules (Network, Database)
```

---

## 🚀 Setup Guide

### Step 1 — Fork / create the GitHub repository

1. Create a new **public** GitHub repository (e.g. `translatex-backend`).
2. Copy the `backend/` folder and `.github/` folder into it and push.

### Step 2 — Generate a GitHub Personal Access Token (PAT)

1. Go to **GitHub → Settings → Developer Settings → Personal Access Tokens → Fine-grained tokens**.
2. Create a token with these permissions on your repo:
   - **Actions** → Read & Write
   - **Contents** → Read
3. Copy the token value — you will use it in Step 4.

### Step 3 — Enable workflow_dispatch on the workflow

The workflow uses `workflow_dispatch`, which requires at least one push to the default branch before it appears in the UI. Push the `.github/workflows/translate.yml` file and it will activate automatically.

### Step 4 — Configure the Android app

Open `app/build.gradle.kts` and fill in your values:

```kotlin
buildConfigField("String", "GITHUB_OWNER",   "\"your-github-username\"")
buildConfigField("String", "GITHUB_REPO",    "\"translatex-backend\"")
buildConfigField("String", "GITHUB_TOKEN",   "\"ghp_xxxxxxxxxxxxxxxxxxxx\"")
buildConfigField("String", "WORKFLOW_FILE",  "\"translate.yml\"")
```

> ⚠️ **Never commit a real PAT to a public repository.**  
> Use a `local.properties` file + `buildConfigField` injection for production.

### Step 5 — Build and run

```bash
# Clone
git clone https://github.com/YOUR_USERNAME/TranslateX-AI
cd TranslateX-AI

# Open in Android Studio (Electric Eel or newer)
# Sync Gradle
# Run on device / emulator (API 26+)
```

---

## 🔄 How the GitHub Actions flow works

```
Android App
    │
    │  POST /repos/{owner}/{repo}/actions/workflows/translate.yml/dispatches
    │  Body: { ref: "main", inputs: { text, source, target, request_id } }
    ▼
GitHub API ──► Queues workflow run
    │
    │  (app polls every 5 s, max 24 attempts ≈ 2 min)
    │
    ▼
GitHub Actions Runner
    │  1. pip install deep-translator
    │  2. python backend/translate.py
    │  3. Writes result.json
    │  4. Uploads result.json as artifact
    ▼
Android App
    │  GET /repos/{owner}/{repo}/actions/runs/{run_id}/artifacts
    │  Downloads ZIP → extracts result.json
    │  Parses { "success": true, "translatedText": "..." }
    ▼
 UI updates with translated text
```

---

## 🎨 Colour Palette

| Token | Hex |
|---|---|
| Primary | `#2563EB` |
| Secondary | `#3B82F6` |
| Accent | `#10B981` |
| Background (light) | `#F8FAFC` |
| Background (dark) | `#121212` |

---

## 📦 Dependencies

| Library | Purpose |
|---|---|
| Jetpack Compose BOM 2024.08 | UI |
| Material3 | Design system |
| Navigation Compose | Screen routing |
| Hilt 2.51 | Dependency injection |
| Retrofit 2.11 + Gson | GitHub API client |
| OkHttp logging interceptor | Debug logging |
| Room 2.6 | Local history DB |
| DataStore Preferences 1.1 | Settings persistence |
| Coroutines 1.8 | Async work |

---

## 🔮 Future features (architecture already supports these)

- Voice translation (microphone input → TTS output)
- OCR / camera translation
- PDF translation
- Offline language packs
- Conversation mode
- AI grammar correction

---

## 📄 Licence

MIT — free for personal and commercial use.
