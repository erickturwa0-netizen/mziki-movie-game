# Mziki · Movie · Game

Native **Android (Kotlin)** app with 3 tabs:

| Tab | Data Source |
|-----|-------------|
| **Mziki** | iTunes Search API (Afrobeats) |
| **Movie** | TVMaze API |
| **Game** | FreeToGame API |

## Build APK (GitHub Actions)

1. Open **Actions** tab → **Build Android APK**
2. Click **Run workflow**
3. Wait ~5–8 minutes
4. Download artifact `app-release-apk`

## Local build

```bash
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release-unsigned.apk
```

## Tech
- Kotlin
- Material Design 3 (Bottom Navigation)
- OkHttp + org.json
- Glide (images)
- minSdk 24 · targetSdk 34
