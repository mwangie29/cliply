# Cliply

**Share it. Keep scrolling.**

## Project status

Milestone 2 — controlled download vertical slice

## Tech stack

Kotlin, Jetpack Compose, Hilt, Room, DataStore, Retrofit, OkHttp, Coil, Android notifications, MediaStore.

## Build

```bash
./gradlew assembleDebug
```

## Tests

```bash
./gradlew test
```

## Implemented in Milestone 2

The project now supports the controlled development flow from Android Share Target through URL validation, `DownloadJob` creation, streamed OkHttp transfer, persisted progress, notification updates, cancellation abstraction, and MediaStore publication with `IS_PENDING` protection.

The controlled provider uses a configurable development-only test URL. Instagram, TikTok, and Facebook extraction remain intentionally unimplemented.

## Current limitations

The current transfer execution uses the Android foreground-service compatibility boundary for the user-initiated transfer. API-aware strategy interfaces exist, but a dedicated Android 14+ UIDT `JobService` implementation and device-level verification remain follow-up work. Notification permission is requested on Android 13+ and denial does not block navigation.
