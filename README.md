# Cliply

**Share it. Keep scrolling.**

Cliply is an Android application for saving media that the user has the right or permission to download. This repository contains a Milestone 4 debug build focused on the real-media resolution pipeline. It is not a production release.

## Current implemented state

Cliply now uses a generic `MediaResolver` abstraction and `MediaResolverRegistry`. The download runner no longer depends on `TestMediaProvider`; it receives normalized `ResolvedMedia` from the registry and then uses the existing transfer, UIDT, Android 13 compatibility, Room, notification, temporary-file, and MediaStore infrastructure.

The first legitimate source is a direct HTTPS media URL. `DirectHttpsMediaResolver` accepts explicitly allowlisted HTTPS hosts and media-file paths, performs a HEAD validation, checks the returned media MIME type, preserves the actual source URL, and downloads that resolved URL through the existing engine. The default Home URL is:

```text
https://filesamples.com/samples/video/mp4/sample_640x360.mp4
```

That endpoint was verified as publicly reachable and returned `video/mp4` over HTTPS during build preparation. The final downloaded file is the media represented by the direct URL, not a substituted social-platform video.

The development controlled resolver remains available only for deterministic debug testing and only for the exact configured test URL. It does not handle Instagram, TikTok, Facebook, or arbitrary URLs. It is not injected as a social-platform fallback.

## Platform limitations

Instagram, TikTok, and Facebook arbitrary shared-page resolution are **not implemented**. Cliply does not scrape private or internal APIs, use credentials or cookies, bypass DRM or access controls, or circumvent platform protections. Where a compliant public resolver is unavailable, the registry returns a typed unsupported-resolution failure rather than silently downloading unrelated test media.

The application is intended for content the user has the right or permission to save.

## Runtime flow

For a legitimate direct media URL, the flow is:

```text
URL validation
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

Completed entries retain source URL, platform, title, thumbnail when available, MIME type, dimensions, duration, file size, timestamps, status, error state, and MediaStore URI. The Downloads screen provides standard Android `ACTION_VIEW` and `ACTION_SEND` Sharesheet actions for completed media.

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

The required build passed and the local unit suite passed. Resolver selection, direct-media path recognition, typed unsupported-resolution behavior, URL validation, state transitions, transfer behavior, and API-aware executor selection are covered by deterministic tests. No live social-media network calls are part of the unit suite.

The development environment does not currently provide a usable physical Android device. The prior emulator environment failed during Android Package Manager initialization, so device-level installation, Share Target execution, UIDT runtime behavior, notification rendering, MediaStore publication, and actual Open/Share actions remain physical-device verification tasks.

## Repository and release

Repository: <https://github.com/mwangie29/cliply>

The previous runtime-test release remains available at <https://github.com/mwangie29/cliply/releases/tag/v0.3.0-runtime-test>. A new Milestone 4 release should be created after the physical-device test of the direct-media flow.
