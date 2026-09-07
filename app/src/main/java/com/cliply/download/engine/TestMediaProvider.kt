package com.cliply.download.engine

import android.net.Uri
import com.cliply.BuildConfig
import com.cliply.domain.model.MediaVariant
import com.cliply.domain.model.Platform
import com.cliply.domain.model.ResolvedMedia

/** Legacy development adapter retained for deterministic tests; not injected into production downloads. */
@Deprecated("Use MediaResolverRegistry")
interface TestMediaProvider { fun canHandle(url: Uri): Boolean; fun resolve(url: Uri): ResolvedMedia }

@Deprecated("Use ControlledTestMediaResolver through MediaResolverRegistry")
class ControlledTestMediaProvider : TestMediaProvider {
    override fun canHandle(url: Uri): Boolean = BuildConfig.DEBUG && url.toString() == BuildConfig.TEST_MEDIA_URL
    override fun resolve(url: Uri): ResolvedMedia = ResolvedMedia("controlled-test-media", Platform.UNKNOWN, "Controlled test media", null, "video/mp4", null, null, null, null, listOf(MediaVariant("Test", null, null, "video/mp4", null, BuildConfig.TEST_MEDIA_URL, null)), null, false, BuildConfig.TEST_MEDIA_URL)
}
