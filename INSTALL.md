# Install and Test Cliply

Cliply is a debug Android build for testing content the user has the right or permission to save. The current supported source is a direct HTTPS media URL. Arbitrary Instagram, TikTok, and Facebook page URLs are intentionally unsupported.

## Build locally

From the Cliply project directory:

```bash
./gradlew clean test assembleDebug --no-daemon
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install on connected hardware with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Direct-media test

Use:

```text
https://filesamples.com/samples/video/mp4/sample_640x360.mp4
```

1. Open Cliply.
2. Paste the direct media URL.
3. Tap **Download**.
4. Confirm Cliply creates a job and opens the real Download screen.
5. Observe resolving, queued, and downloading state from the persisted job.
6. Leave Cliply and observe the native progress notification.
7. Lock the phone if practical and wait for completion.
8. Confirm the job becomes `COMPLETED`.
9. Open **Cliply → Downloads**.
10. Confirm the completed item has plausible size and MIME metadata.
11. Confirm the file appears in `Movies/Cliply` through MediaStore/gallery.
12. Tap **Open** and verify Android’s normal compatible-player flow.
13. Tap **Share** and verify the Android Sharesheet receives the content URI.

## Cancellation test

Start a download and tap **Cancel** on the real Download screen. Confirm the transfer stops, the job becomes `CANCELLED`, the temporary file is removed, the active notification is handled, and no falsely completed MediaStore row remains.

## Failure test

Use an allowlisted direct-media-shaped URL that is unreachable or returns a non-media response. Confirm the job becomes `FAILED`, a readable safe error is shown, no fake successful download is recorded, and temporary/MediaStore cleanup occurs.

## Share Target test

From an Android application that can share text URLs:

1. Share the direct media URL.
2. Select **Cliply**.
3. Confirm Cliply receives the URL and creates a job.
4. Confirm Cliply finishes quickly and returns to the source app.
5. Observe the notification and verify background progress.

## Unsupported social URLs

Instagram Reel/page URLs, TikTok page URLs, and Facebook page/video URLs must be rejected as unsupported. They must not create a fake completed job and must never be routed to the controlled development media.

## Future Instagram integration

The repository contains an `AuthorizedInstagramResolver` contract and a fail-closed placeholder only. It does not contain a Meta login flow, credentials, backend, App Review approval, or Instagram media resolver.

Any future implementation must be restricted to authorized Instagram professional-account media and official Meta-provided media URLs. If Meta does not provide `media_url`, Cliply must report the media as unavailable rather than scraping or bypassing Instagram protections.

## Verification boundary

Build verification and source verification are separate from physical-device verification. The current environment has no connected physical Android device, so device-level installation, notifications, background execution, MediaStore, playback, Open, Sharesheet, cancellation, failure, and Share Target results must not be claimed until tested on real hardware.
