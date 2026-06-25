# 🌐 TranslateX AI

A **production-ready Android translation app** powered by **GitHub Actions** as a free serverless backend.

---

## ✨ Features

| Feature | Details |
|---|---|
| 20 languages | English, Hindi, Spanish, French, German, Portuguese, Italian, Russian, Chinese, Japanese, Korean, Arabic, Turkish, Dutch, Polish, Thai, Vietnamese, Indonesian, Bengali, Marathi |
| GitHub Actions backend | Triggers `workflow_dispatch`, polls for completion, downloads artifact |
| Room database | Full offline history & favourites |
| DataStore | Persistent user preferences (dark mode, default languages) |
| Material Design 3 | Dark / light theme, glassmorphism cards, smooth animations |
| MVVM + Clean Architecture | Repository pattern, Hilt DI, StateFlow |
| Language selector | Bottom sheet with instant search |
| Swap languages | Swaps text + languages simultaneously |

---

## 🏗 Project structure

```
TranslateX-AI/
├── .github/workflows/
│   ├── translate.yml        ← GitHub Actions translation backend
│   └── build-apk.yml        ← CI workflow that builds the APK
├── backend/
│   └── translate.py         ← Python translation script (deep-translator)
└── app/src/main/java/com/translatex/ai/
    ├── ui/screens/          ← Splash, Home, History, Favourites, Settings
    ├── ui/components/       ← Reusable composables
    ├── ui/theme/            ← Material3 colour scheme + typography
    ├── viewmodel/           ← TranslationViewModel
    ├── repository/          ← TranslationRepository, UserPreferencesRepository
    ├── data/local/          ← Room DB, DAO, Entity
    ├── data/remote/         ← Retrofit service, DTOs
    ├── model/               ← Language, TranslationResult
    ├── navigation/          ← NavGraph
    └── di/                  ← Hilt modules (Network, Database)
```

---

## 🚀 Setup: 5 steps

### Step 1 — Create your GitHub backend repo

1. Create a **new public** GitHub repository (e.g. `translatex-backend`).
2. Push the `backend/` folder and `.github/workflows/translate.yml` into it.
3. Your repo structure should be:
   ```
   translatex-backend/
   ├── .github/workflows/translate.yml
   └── backend/translate.py
   ```

### Step 2 — Generate a GitHub Personal Access Token (PAT)

1. Go to **GitHub → Settings → Developer Settings → Personal Access Tokens → Fine-grained tokens**.
2. Set **Resource owner** to your account.
3. Under **Repository access** select your `translatex-backend` repo.
4. Grant these permissions:
   - **Actions** → Read & Write
   - **Contents** → Read-only
5. Copy the generated token (starts with `ghp_` or `github_pat_`).

### Step 3 — Set GitHub Actions secrets (for CI APK build)

In the **Android project repo** (not the backend repo):
Go to **Settings → Secrets and variables → Actions → New repository secret** and add:

| Secret name | Value |
|---|---|
| `TRANSLATEX_GITHUB_OWNER` | Your GitHub username |
| `TRANSLATEX_GITHUB_REPO` | Your backend repo name (e.g. `translatex-backend`) |
| `TRANSLATEX_GITHUB_TOKEN` | The PAT from Step 2 |

### Step 4 — Configure for local development (Android Studio)

Open `app/build.gradle.kts` and fill in your values:

```kotlin
buildConfigField("String", "GITHUB_OWNER",  "\"your-github-username\"")
buildConfigField("String", "GITHUB_REPO",   "\"translatex-backend\"")
buildConfigField("String", "GITHUB_TOKEN",  "\"ghp_xxxxxxxxxxxxxxxxxxxx\"")
buildConfigField("String", "WORKFLOW_FILE", "\"translate.yml\"")
```

> ⚠️ **NEVER commit a real PAT to a public repo.**
> Use `local.properties` + `buildConfigField` for local dev. The CI workflow reads from GitHub Secrets.

### Step 5 — Build the APK

**Option A: Via GitHub Actions CI (recommended)**
1. Push the Android project to GitHub.
2. Go to **Actions → Build TranslateX APK → Run workflow**.
3. After ~5 minutes, download the APK from the run's **Artifacts** section.
4. Transfer `app-debug.apk` to your Android device and install.

**Option B: Via Android Studio**
1. Open the project in Android Studio (Hedgehog or newer).
2. Sync Gradle.
3. Menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. APK saved at `app/build/outputs/apk/debug/app-debug.apk`.

**Option C: Command line**
```bash
chmod +x gradlew
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔄 How the translation flow works

```
Android App
    │
    │  POST /repos/{owner}/{repo}/actions/workflows/translate.yml/dispatches
    │  inputs: { text, source, target, request_id: "abc12345" }
    ▼
GitHub Actions queues the run
    │
    │  App polls every 6 s (max 3 min) for a run created after dispatch
    │  with a terminal conclusion (success / failure)
    ▼
Runner executes:
    pip install deep-translator
    python backend/translate.py
    → writes result.json
    → uploads as artifact "translation-result-abc12345"
    │
    │  App finds artifact by exact name match
    │  Downloads ZIP → extracts result.json
    │  Parses { "success": true, "translatedText": "..." }
    ▼
UI shows translated text (saved to Room DB)
```

**Typical latency:** 45–90 seconds (GitHub Actions runner boot + Python execution).

---

## 🎨 Colour palette

| Token | Hex |
|---|---|
| Primary | `#2563EB` |
| Secondary | `#3B82F6` |
| Accent | `#10B981` |
| Background (light) | `#F8FAFC` |
| Background (dark) | `#121212` |

---

## 📦 Key dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.08.00 | UI framework |
| Material3 | latest | Design system |
| Hilt | 2.51.1 | Dependency injection |
| Retrofit | 2.11.0 | GitHub REST API client |
| OkHttp logging | 4.12.0 | Debug logging + artifact download |
| Room | 2.6.1 | Local history DB |
| DataStore | 1.1.1 | Settings persistence |
| deep-translator | latest | Python translation via Google |

---

## 🔮 Future features (architecture is ready)

- Voice input / TTS output
- OCR / camera translation
- PDF translation
- Offline language packs
- Conversation mode
- AI grammar correction

---

## 📄 Licence

MIT — free for personal and commercial use.
