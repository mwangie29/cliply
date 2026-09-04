# Cliply

**Share it. Keep scrolling.**

## Project status

Milestone 3 — integration validation and hardening

## Tech stack

Kotlin, Jetpack Compose, Hilt, Room, DataStore, Retrofit, OkHttp, Coil, Android JobScheduler, UIDT, notifications, and MediaStore.

## Build

```bash
./gradlew assembleDebug
```

## Tests

```bash
./gradlew test
```

## Implemented and hardened

The project supports a controlled development download flow from Android Share Target through URL validation, `DownloadJob` creation, API-aware transfer selection, streamed OkHttp transfer, persisted progress, notifications, cancellation propagation, and MediaStore publication with `IS_PENDING` protection.

Android 13 selects the compatibility foreground-service executor. Android 14+ selects the registered, user-initiated `JobService` path using `JobInfo.Builder.setUserInitiated(true)`. Both paths invoke the same `DownloadJobRunner`, transfer engine, notification facade, repository, and MediaStore publisher.

Milestone 3 hardening includes a certificate-valid HTTPS controlled media URL, lifecycle diagnostics without full URL logging, guaranteed temporary-file cleanup on failure/cancellation, persistence of active temporary paths, Room persistence of completion metadata, destructive migration support for the development schema, and a Room-backed Downloads history ViewModel.

## Verification

The final `./gradlew clean test assembleDebug --no-daemon` run passed. The test suite contains 14 passing unit tests. No Android emulator or physical device could complete installation: the newly created API 33 emulator booted partially but Package Manager failed with a null `StorageManager`, and the headless emulator later exited with code 139. Consequently, Share Target, UIDT runtime scheduling, notification rendering, MediaStore output, and connected Android tests remain device-unverified.

The controlled source is configured in `BuildConfig.TEST_MEDIA_URL` and remains development-only. Instagram, TikTok, and Facebook extraction remain intentionally unimplemented.
