# Install Cliply Milestone 4 Build

Cliply is a debug build intended for testing on an Android phone. It is not a production release and is intended for media the user has the right or permission to save.

## Install

Open the Cliply repository at <https://github.com/mwangie29/cliply>. If a Milestone 4 release is available, open **Releases**, download its APK, open the downloaded file on the phone, allow installation from that source if Android asks, and install Cliply. If no Milestone 4 release is available yet, use the latest APK published in the repository or the task attachment.

## Direct-media test

1. Open Cliply.
2. Confirm the Home field contains:

   `https://filesamples.com/samples/video/mp4/sample_640x360.mp4`

3. Tap **Download**.
4. Confirm the notification identifies a download in progress.
5. Leave Cliply and wait for completion.
6. Open **Cliply → Downloads**.
7. Confirm the completed direct-media item remains listed after reopening Cliply.
8. Use **Open** to launch the media through Android’s standard compatible player selection.
9. Use **Share** to open Android’s native Sharesheet.

The resolver validates the HTTPS response and media MIME type before transferring the actual media URL. The direct-media source is not a social-platform extractor.

## Share Target test

Use an Android app that can share the direct media URL as `text/plain`. Tap **Share**, select **Cliply**, and confirm that Cliply receives the URL and starts its background resolution/download workflow. Return to the original app and observe the Cliply notification.

Instagram, TikTok, and Facebook shared-page links are not supported by this milestone. Cliply will report an unsupported-resolution failure rather than substitute controlled test media.

## Cancellation and failure

Start a download and exercise the available cancellation path. The expected result is `CANCELLED`, removal or update of the notification, deletion of the temporary file, and no incomplete MediaStore item. For an invalid or unavailable direct-media response, the expected result is `FAILED` with a user-safe message and no stuck downloading state.
