# Install Cliply Runtime Test Build

## Option A — GitHub Release APK

1. Open the Cliply repository: <https://github.com/mwangie29/cliply>.
2. Open **Releases**.
3. Open the release named **Cliply v0.3.0 — Runtime Test Build**.
4. Download the APK asset to your Android phone.
5. Open the downloaded APK.
6. If Android asks for permission to install from that source, allow it temporarily.
7. Install and open Cliply.

This is a debug runtime-test build, not a production-signed release.

## Option B — Direct APK artifact

If the GitHub Release is not available, download `app-debug.apk` from the repository’s published build artifact or from the task’s APK attachment. On the phone, open the APK and follow Android’s installation prompt.

## First-run checklist

### Home download

1. Open Cliply.
2. Confirm the prefilled controlled test URL:

   `https://github.com/mediaelement/mediaelement-files/raw/master/big_buck_bunny.mp4`

3. Tap **Download**.
4. Confirm the download starts and a notification appears if notification permission is granted.
5. Leave Cliply and wait for completion.
6. Open **Downloads** in Cliply.
7. Confirm the completed item remains listed.
8. Use the completion notification’s **Open** or **Share** action.

### Share Target

1. Open a browser or another app containing the controlled test URL.
2. Tap **Share**.
3. Select **Cliply**.
4. Confirm Cliply receives the URL and starts the download immediately.
5. Return to the originating app.
6. Wait for completion.

### Persistence

1. Complete a download.
2. Close Cliply normally.
3. Reopen Cliply.
4. Open **Downloads**.
5. Confirm the item remains visible.

### Cancellation

1. Start a download.
2. Cancel it using the available transfer control or system lifecycle action.
3. Confirm it does not appear as a completed media item.

## Android 13+ permission note

Cliply requests notification permission on Android 13 and newer. If permission is denied, the transfer should still remain usable, but progress notifications will not be visible until notification permission is enabled in Android Settings.

## Scope note

The controlled test source is for runtime validation only. Instagram, TikTok, and Facebook extraction are not implemented in this build.
