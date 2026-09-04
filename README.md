# Cliply

**Share it. Keep scrolling.**

## Project status

Milestone 2.1 — API-aware background transfer execution

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

## Implemented

The project supports a controlled development download flow from Android Share Target through URL validation, `DownloadJob` creation, API-aware transfer selection, streamed OkHttp transfer, persisted progress, notifications, cancellation propagation, and MediaStore publication with `IS_PENDING` protection.

Android 13 selects the compatibility foreground-service executor. Android 14+ selects the registered, user-initiated `JobService` path using `JobInfo.Builder.setUserInitiated(true)`. Both paths invoke the same `DownloadJobRunner`, transfer engine, notification facade, repository, and MediaStore publisher.

The controlled provider uses a configurable development-only test URL. Instagram, TikTok, and Facebook extraction remain intentionally unimplemented.

## Verification status

The Gradle debug build and unit tests pass. No Android emulator or physical device was available, so device-level verification of Share Target, UIDT scheduling, notification rendering, and MediaStore output has not been performed.
