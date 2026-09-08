package com.cliply.integration.instagram

import com.cliply.domain.model.MediaVariant
import com.cliply.domain.model.Platform
import com.cliply.domain.model.ResolvedMedia
import com.cliply.domain.model.ResolutionFailureCode
import java.time.Instant

/** Request sent to Cliply's backend; it intentionally contains no provider token or media URL. */
data class InstagramResolutionRequest(val accountReference: String, val permalink: String)

data class InstagramResolutionResponse(
    val resolutionId: String,
    val media: InstagramResolvedMediaPayload,
    val expiresAt: Instant?
)

data class InstagramResolvedMediaPayload(
    val providerMediaId: String,
    val title: String?,
    val thumbnailUrl: String?,
    val mimeType: String,
    val width: Int?,
    val height: Int?,
    val durationMs: Long?,
    val fileSize: Long?,
    val mediaUrl: String?,
    val supportsRange: Boolean
) {
    fun toResolvedMedia(resolutionExpiresAt: Instant? = null): ResolvedMedia? {
        val url = mediaUrl ?: return null
        return ResolvedMedia(
            id = providerMediaId,
            platform = Platform.INSTAGRAM,
            title = title,
            thumbnailUrl = thumbnailUrl,
            mimeType = mimeType,
            width = width,
            height = height,
            durationMs = durationMs,
            fileSize = fileSize,
            variants = listOf(MediaVariant("Original", width, height, mimeType, fileSize, url, resolutionExpiresAt)),
            expiresAt = resolutionExpiresAt,
            supportsRange = supportsRange,
            sourceUrl = null
        )
    }
}

sealed interface InstagramBackendResult {
    data class Success(val response: InstagramResolutionResponse) : InstagramBackendResult
    data class Failure(val code: ResolutionFailureCode, val safeMessage: String) : InstagramBackendResult
}

object InstagramProviderMediaPolicy {
    private fun isApprovedHost(host: String): Boolean =
        host == "cdninstagram.com" || host.endsWith(".cdninstagram.com") ||
            host == "fbcdn.net" || host.endsWith(".fbcdn.net")

    fun validateMediaUrl(rawUrl: String?): Boolean {
        val uri = runCatching { java.net.URI(rawUrl ?: "") }.getOrNull() ?: return false
        val host = uri.host?.lowercase() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) && isApprovedHost(host) && uri.userInfo == null
    }

    fun normalize(response: InstagramResolutionResponse): InstagramBackendResult {
        val media = response.media
        if (media.mediaUrl == null) return InstagramBackendResult.Failure(ResolutionFailureCode.MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER, "This media is not available for download through the connected Instagram account.")
        if (!media.mimeType.lowercase().startsWith("video/")) return InstagramBackendResult.Failure(ResolutionFailureCode.INVALID_MEDIA_RESPONSE, "This Instagram item is not a supported video.")
        if (!validateMediaUrl(media.mediaUrl)) return InstagramBackendResult.Failure(ResolutionFailureCode.INVALID_MEDIA_RESPONSE, "The provider returned an unsupported media location.")
        return InstagramBackendResult.Success(response)
    }
}
