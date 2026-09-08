package com.cliply.download.resolver

import android.net.Uri
import com.cliply.domain.model.ResolveResult
import com.cliply.domain.model.ResolvedMedia
import com.cliply.domain.model.ResolutionFailureCode

/** Unified request boundary for direct media and future authorized provider sources. */
data class ResolutionRequest(
    val sourceUrl: String,
    val sourceCapability: MediaSourceCapability,
    val authorizedAccountReference: String? = null,
    val userInitiated: Boolean = true
)

interface MediaSourceResolver {
    fun canHandle(request: ResolutionRequest): Boolean
    suspend fun resolve(request: ResolutionRequest): ResolveResult
}

/** Internal handoff returned by a future backend after authorized provider resolution. */
data class ResolutionHandoff(
    val resolutionId: String,
    val media: ResolvedMedia,
    val expiresAtEpochMs: Long?
)

/** Safe backend-facing request shape; it intentionally has no provider token or arbitrary media URL. */
data class AuthorizedInstagramResolutionRequest(
    val accountReference: String,
    val permalink: String
)

class DirectMediaSourceResolver(private val delegate: DirectHttpsMediaResolver) : MediaSourceResolver {
    override fun canHandle(request: ResolutionRequest): Boolean {
        val parsed = runCatching { java.net.URI(request.sourceUrl) }.getOrNull()
        return request.sourceCapability == MediaSourceCapability.DIRECT_MEDIA_URL && parsed != null && delegate.canResolveComponents(parsed.scheme?.lowercase(), parsed.host?.lowercase(), parsed.rawPath)
    }
    override suspend fun resolve(request: ResolutionRequest): ResolveResult = if (canHandle(request)) ResolveResult.Success(delegate.resolve(android.net.Uri.parse(request.sourceUrl))) else ResolveResult.Failure(ResolutionFailureCode.INVALID_URL, "Enter a valid supported HTTPS media URL.")
}

class AuthorizedInstagramSourceResolver(private val delegate: AuthorizedInstagramResolver) : MediaSourceResolver {
    override fun canHandle(request: ResolutionRequest): Boolean = request.sourceCapability == MediaSourceCapability.AUTHORIZED_INSTAGRAM_MEDIA && !request.authorizedAccountReference.isNullOrBlank()
    override suspend fun resolve(request: ResolutionRequest): ResolveResult = if (canHandle(request)) delegate.resolve(AuthorizedInstagramRequest(request.sourceUrl, request.authorizedAccountReference!!)) else ResolveResult.Failure(ResolutionFailureCode.AUTHORIZED_ACCOUNT_SCOPE_REQUIRED, "Connect an authorized Instagram professional account first.")
}

class MediaSourceResolverRegistry(private val resolvers: List<MediaSourceResolver>) {
    fun canHandle(request: ResolutionRequest): Boolean = resolvers.any { it.canHandle(request) }
    suspend fun resolve(request: ResolutionRequest): ResolveResult = resolvers.firstOrNull { it.canHandle(request) }?.resolve(request)
        ?: ResolveResult.Failure(ResolutionFailureCode.UNSUPPORTED_PLATFORM, "This type of link isn't supported yet.")
}
