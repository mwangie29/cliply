package com.cliply.download.resolver

import android.net.Uri
import com.cliply.domain.model.ResolveResult
import com.cliply.domain.model.ResolutionFailureCode

/** Product-level source classification used by URL ingestion and future resolver implementations. */
enum class MediaSourceCapability {
    DIRECT_MEDIA_URL,
    AUTHORIZED_INSTAGRAM_MEDIA,
    UNSUPPORTED_SOCIAL_PAGE,
    INVALID_OR_UNSUPPORTED_URL
}

data class MediaSourceAssessment(val capability: MediaSourceCapability, val reason: String? = null)

/** The only Instagram resolver contract Cliply is prepared to add later. */
interface AuthorizedInstagramResolver {
    suspend fun resolve(request: AuthorizedInstagramRequest): ResolveResult
}

data class AuthorizedInstagramRequest(
    val permalink: Uri,
    val authorizedAccountReference: String
)

/** Explicit placeholder: it fails closed and can never return controlled or scraped media. */
class UnconfiguredAuthorizedInstagramResolver : AuthorizedInstagramResolver {
    fun unavailable(): ResolveResult.Failure = ResolveResult.Failure(
        ResolutionFailureCode.AUTHORIZED_INSTAGRAM_NOT_CONFIGURED.name,
        "Authorized Instagram access is not configured for this account."
    )
    override suspend fun resolve(request: AuthorizedInstagramRequest): ResolveResult = unavailable()
}

fun assessMediaSource(scheme: String?, host: String?, path: String?): MediaSourceAssessment {
    val normalizedHost = host?.lowercase()
    if (scheme != "https" || normalizedHost.isNullOrBlank()) {
        return MediaSourceAssessment(MediaSourceCapability.INVALID_OR_UNSUPPORTED_URL, "HTTPS is required.")
    }
    if (normalizedHost == "instagram.com" || normalizedHost == "www.instagram.com") {
        return MediaSourceAssessment(MediaSourceCapability.UNSUPPORTED_SOCIAL_PAGE, "Instagram page resolution requires an authorized integration.")
    }
    return if (normalizedHost in com.cliply.share.SupportedHosts.all && path.orEmpty().substringAfterLast('.', "").lowercase() in setOf("mp4", "mov", "m4v", "webm", "mkv")) {
        MediaSourceAssessment(MediaSourceCapability.DIRECT_MEDIA_URL)
    } else {
        MediaSourceAssessment(MediaSourceCapability.INVALID_OR_UNSUPPORTED_URL, "The URL is not a supported direct media source.")
    }
}
