# Cliply

**Share it. Keep scrolling.**

Cliply is an Android application for saving media that the user has the right or permission to download. This repository contains the Milestone 5 runtime-hardening build. It is not a production release.

## Implemented

Cliply uses a generic `MediaResolver` abstraction and `MediaResolverRegistry`. The download runner no longer depends on `TestMediaProvider`; it receives normalized `ResolvedMedia` from the registry and uses the existing transfer, UIDT, Android 13 compatibility, Room, notification, temporary-file, and MediaStore infrastructure.

The supported media source is a direct HTTPS media URL. `DirectHttpsMediaResolver` requires HTTPS, checks the existing allowlist, requires a recognized media-file path, validates the response MIME type with a HEAD request, and preserves the actual resolved URL. The Android transfer then downloads that actual URL.

The documented direct-media source is:

```text
https://filesamples.com/samples/video/mp4/sample_640x360.mp4
```

The source was independently downloaded during Milestone 5 verification: the file was 574,823 bytes, identified as an MP4/MOV container, and reported a duration of approximately 13.35 seconds. This confirms the source itself, but does not constitute physical-device verification of the Android app.

The controlled resolver remains available only for deterministic debug testing and only for its exact configured URL. It does not handle Instagram, TikTok, Facebook, or arbitrary URLs and is not a social-platform fallback.

## Unsupported social page resolution

Instagram, TikTok, and Facebook arbitrary shared-page resolution are **not implemented**. Cliply does not scrape private or internal APIs, use credentials or cookies, bypass DRM or access controls, or circumvent platform protections. Unsupported social URLs are rejected before a download job is created and cannot become fake completed downloads.

The application is intended for content the user has the right or permission to save.

## Runtime flow

```text
URL validation
→ resolver capability check
→ DownloadJob creation
→ RESOLVING
→ MediaResolverRegistry
→ DirectHttpsMediaResolver
→ RESOLVED
→ QUEUED
→ DOWNLOADING
→ progress notification
→ MediaStore with IS_PENDING
→ COMPLETED
→ Room-backed Downloads history
```

Completed entries retain source URL, platform, title, thumbnail when available, MIME type, dimensions, duration, file size, timestamps, status, error state, and MediaStore URI. The Downloads screen provides standard Android `ACTION_VIEW` and `ACTION_SEND` Sharesheet actions. Missing compatible Open or Share handlers fail with a readable toast instead of crashing.

MediaStore publication now uses the resolved MIME type, requires a video MIME type, checks that the output stream opens, clears `IS_PENDING` only after the copy succeeds, deletes failed MediaStore rows, and removes the completed temporary file. Notifications use the approved monochrome Cliply icon.

Android 13 uses the compatibility foreground-service path. Android 14 and newer use the existing UIDT `JobService` path. Both paths delegate to the same resolver-aware `DownloadJobRunner`.

## Build configuration

| Setting | Value |
|---|---:|
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 33 |
| versionName | 0.4.0 |
| versionCode | 4 |

Build and test with:

```bash
./gradlew clean test assembleDebug --no-daemon
```

## Verification status

### Build verified

The required command passed after the Milestone 5 hardening changes. The deterministic unit suite completed with 18 tests and 0 failures. Resolver, URL-validation, transfer, state-machine, and API-aware executor tests do not call live Instagram, TikTok, or Facebook infrastructure.

The direct source was also downloaded outside Android and verified as a valid MP4/MOV container. This is source verification only.

### Physical device verified

**Not verified.** No physical Android device was connected to the development environment. `adb devices -l` returned no attached devices. The available emulator environment was not used as a substitute for the requested physical-device claim.

Consequently, installation, launcher rendering, Share Target execution, UIDT runtime behavior, notification rendering, background continuation, MediaStore publication, playback, Open, Sharesheet delivery, cancellation, and failure behavior remain pending physical-device testing.

## Repository

Repository: <https://github.com/mwangie29/cliply>

Milestone 5 should receive a separate release only after physical-device verification is completed.
