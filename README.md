# Cliply

**Share it. Keep scrolling.**

Cliply is an Android application for saving media that the user has the right or permission to download. The current implementation is a direct-media MVP with a deliberate architecture seam for a future authorized Instagram integration. It is not an arbitrary public Instagram Reel downloader.

## Supported source today

Cliply supports direct HTTPS media URLs from explicitly allowlisted hosts and recognized video paths. The current development source is:

```text
https://filesamples.com/samples/video/mp4/sample_640x360.mp4
```

The direct flow is:

```text
URL validation
→ source capability assessment
→ DownloadJob creation
→ RESOLVING
→ DirectHttpsMediaResolver
→ RESOLVED
→ QUEUED
→ DOWNLOADING
→ progress notification
→ MediaStore with IS_PENDING
→ COMPLETED
→ Room-backed Downloads history
```

The Download screen now observes the persisted `DownloadJob`, shows real resolving/queued/downloading/completed/failed/cancelled states, and routes cancellation through the active Android transfer executor. Completed media can be opened with Android `ACTION_VIEW` or shared through the standard Sharesheet.

## Instagram direction

Arbitrary Instagram Reel/page URLs are intentionally unsupported. Cliply rejects them before creating a download job and never substitutes controlled test media.

The future integration seam is `AuthorizedInstagramResolver`. It is fail-closed until a compliant backend, Meta authorization flow, permissions, App Review/Advanced Access requirements, token handling, and rights/deletion policies are approved and implemented.

The planned future workflow is limited to Instagram professional-account media that the user has connected and is authorized to manage:

```text
Business Login for Instagram
or Facebook Login for Business
→ authorized professional account
→ official IG Media lookup
→ optional Meta-provided media_url
→ normalized ResolvedMedia
→ existing DownloadJob and MediaStore pipeline
```

If Meta omits `media_url` because of licensed audio, copyright status, or Reel download controls, Cliply must report that the media is unavailable through the authorized API. It must not scrape the Reel page or use private endpoints, cookies, tokens, DRM bypass, or access-control circumvention.

## Implemented architecture

Cliply uses Clean Architecture-style boundaries with Kotlin, Jetpack Compose, Hilt, Coroutines/Flow, Room, OkHttp, UIDT on Android 14+, a compatibility foreground-service path on Android 13, Android notifications, and MediaStore.

The generic resolver layer contains:

- `MediaResolver`
- `MediaResolverRegistry`
- `DirectHttpsMediaResolver`
- `MediaSourceCapability`
- `AuthorizedInstagramResolver` future contract
- `UnconfiguredAuthorizedInstagramResolver` fail-closed placeholder
- `MediaSourceResolver` request-scoped contract
- `MediaSourceResolverRegistry`
- `DirectMediaSourceResolver` and `AuthorizedInstagramSourceResolver` adapters
- Typed provider/authentication failures such as `MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER`, `AUTH_REAUTH_REQUIRED`, and `PROVIDER_RATE_LIMITED`

`DownloadJob` now carries non-secret `resolutionSource`, provider resolution reference, authorized-account reference, and optional resolution expiry metadata. Room migration 3→4 preserves existing history while adding these fields. The existing transfer engine, state machine, notification system, Room history, and MediaStore publication remain the shared execution path for any future compliant resolver.

The unified registry is currently fail-closed for Instagram: it contains no Meta client, OAuth implementation, token store, backend call, or live Instagram resolver. This is intentional until official access, App Review scope, backend security controls, and legal/privacy review are complete.

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

The current execution passed the required build and deterministic unit suite. The suite covers state transitions, transfer behavior, URL validation, resolver selection, direct-media capability assessment, unsupported Instagram-page classification, fail-closed authorized-Instagram behavior, and API-aware executor selection. Tests do not call live Instagram, TikTok, or Facebook infrastructure.

### Physical device verified

**Not verified.** No physical Android device is connected to the development environment. Therefore installation, launcher rendering, Share Target behavior, UIDT runtime behavior, notification rendering, background continuation, MediaStore publication, playback, Open, Sharesheet delivery, cancellation, and failure behavior remain physical-device tasks.

### Authorized Instagram verified

**Not implemented or verified.** The future resolver contract exists, but no Meta credentials, connected account, backend, App Review approval, or Instagram API integration is present.

## Rights and compliance

Cliply is intended for content the user has the right or permission to save. The product must not be marketed as an Instagram downloader until a legitimate authorized integration is implemented and physically verified.

## Repository

Repository: <https://github.com/mwangie29/cliply>
