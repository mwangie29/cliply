# Install Cliply Milestone 5 Build

Cliply is a debug build intended for testing on an Android 13+ phone. It is not a production release and is intended for media the user has the right or permission to save.

## Build locally

From the Cliply project directory:

```bash
./gradlew clean test assembleDebug --no-daemon
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install it on a connected physical device with Android Debug Bridge:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The current development environment had no connected physical device, so the runtime procedures below remain a test plan rather than completed evidence.

## Direct-media test

1. Open Cliply.
2. Confirm the Home field contains:

   `https://filesamples.com/samples/video/mp4/sample_640x360.mp4`

3. Tap **Download**.
4. Confirm that the download begins.
5. Leave Cliply and return to the originating app.
6. Observe the Cliply progress notification.
7. Lock the phone if practical and wait for completion.
8. Open **Cliply → Downloads**.
9. Confirm the completed item appears with plausible size and `COMPLETED` status.
10. Confirm the file appears in `Movies/Cliply` through MediaStore/gallery.
11. Use **Open** and verify playback through Android’s normal compatible-player flow.
12. Use **Share** and verify Android’s standard Sharesheet receives the content URI.

The source itself was independently downloaded during Milestone 5 preparation and identified as a valid MP4/MOV container. That result does not prove that the Android app completed the same flow on a device.

## Share Target test

From an Android application that can share text URLs:

1. Share the direct media URL.
2. Select **Cliply**.
3. Confirm Cliply appears as an `ACTION_SEND` text target.
4. Confirm a `DownloadJob` is created.
5. Confirm the Share Target finishes quickly and returns to the source app.
6. Observe the Cliply notification and wait for completion.

Instagram, TikTok, and Facebook shared-page URLs are intentionally unsupported in this milestone. Cliply must show an unsupported message and must not substitute controlled test media.

## Cancellation test

Start a direct-media download and invoke the available cancellation path. Confirm that the transfer stops, the job becomes `CANCELLED`, the temporary file is removed, no falsely completed MediaStore row remains, and the notification is stopped or cancelled.

## Failure test

Use an allowlisted direct-media-shaped URL that is unreachable or returns a non-media response. Confirm that the job becomes `FAILED`, the user sees a readable safe error, no successful item appears in Downloads, and no temporary or corrupted MediaStore file remains.

## Physical-device status

Physical-device installation, launcher icon rendering, background continuation, notifications, MediaStore, playback, Open, Sharesheet, cancellation, failure, and Share Target behavior are not claimed as verified until they have been exercised on real Android hardware.
