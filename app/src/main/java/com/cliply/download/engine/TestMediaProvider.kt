package com.cliply.download.engine

import android.net.Uri
import com.cliply.BuildConfig
import com.cliply.domain.model.*

interface TestMediaProvider { fun canHandle(url: Uri): Boolean; fun resolve(url: Uri): ResolvedMedia }
class ControlledTestMediaProvider : TestMediaProvider {
    override fun canHandle(url: Uri): Boolean = url.host == "instagram.com" || url.host == "www.instagram.com" || url.host == "tiktok.com" || url.host == "www.tiktok.com" || url.host == "facebook.com" || url.host == "www.facebook.com" || url.host == "github.com" || url.host == "raw.githubusercontent.com"
    override fun resolve(url: Uri): ResolvedMedia = ResolvedMedia("controlled-test-media", Platform.UNKNOWN, "Cliply controlled test video", null, "video/mp4", null, null, null, null, listOf(MediaVariant("Best available", null, null, "video/mp4", null, BuildConfig.TEST_MEDIA_URL, null)), null, false)
}
