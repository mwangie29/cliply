# Cliply

**Share it. Keep scrolling.**

Cliply is an Android runtime-test build for validating the core share-and-save experience. It is **not a production release**. This build uses a controlled HTTPS test media source; Instagram, TikTok, and Facebook extraction are **not implemented**.

## Current runtime test

The controlled test URL is:

```text
https://github.com/mediaelement/mediaelement-files/raw/master/big_buck_bunny.mp4
```

The intended in-app test is:

1. Open Cliply.
2. Confirm the controlled test URL is in the Home field.
3. Tap **Download**.
4. Observe the download state and notification.
5. Leave Cliply and continue using the phone.
6. Wait for completion.
7. Open **Cliply → Downloads**.
8. Confirm the completed item appears in Room-backed history.
9. Use the completion notification’s **Open** or **Share** action.

The Share Target test is:

1. Open a browser or another app containing the test URL.
2. Tap **Share**.
3. Select **Cliply**.
4. Confirm Cliply receives the URL.
5. Confirm the download begins without manually pressing Download.
6. Return to the originating app.
7. Wait for the notification and completion.

## Architecture and compatibility

- Android 13 / API 33 uses the compatibility foreground-service transfer path.
- Android 14–16 / API 34–36 uses the user-initiated data-transfer `JobService` path.
- Both paths share the same `DownloadJobRunner`, streamed OkHttp transfer engine, Room repository, notification facade, and MediaStore publisher.
- MediaStore publication uses `IS_PENDING` so incomplete files are not exposed as completed media.
- The Home flow creates a persisted `DownloadJob`; Downloads history is loaded from Room rather than placeholder data.

## Build configuration

| Setting | Value |
|---|---:|
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 33 |
| versionName | 0.3.0 |
| versionCode | 3 |

Build locally with:

```bash
./gradlew clean test assembleDebug --no-daemon
```

## Scope and limitations

This is a runtime-test build for Android hardware. It does not include production social-platform extraction, authentication, subscriptions, cloud history, batch downloads, or content editing. Device-level verification depends on the tester’s Android phone; the development emulator environment used for automated work could not complete APK installation because Android Package Manager failed with a `StorageManager` initialization error.

## Repository

[https://github.com/mwangie29/cliply](https://github.com/mwangie29/cliply)
