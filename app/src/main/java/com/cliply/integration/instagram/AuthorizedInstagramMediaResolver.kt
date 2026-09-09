package com.cliply.integration.instagram

import com.cliply.domain.model.ResolveResult
import com.cliply.domain.model.ResolutionFailureCode
import com.cliply.download.resolver.AuthorizedInstagramRequest
import com.cliply.download.resolver.AuthorizedInstagramResolver
import java.net.URI

/** Backend boundary; implementations must use official Meta APIs and never accept raw provider credentials. */
interface AuthorizedInstagramBackend {
    suspend fun resolve(request: InstagramResolutionRequest): InstagramBackendResult
}

/**
 * Resolves only media returned by the authorized backend. No scraping, arbitrary URL fetch,
 * provider-token handling, or controlled-media fallback is possible through this adapter.
 */
class AuthorizedInstagramMediaResolver(
    private val backend: AuthorizedInstagramBackend,
    private val accessGrant: InstagramAccessGrant,
    private val account: ConnectedInstagramAccount,
    private val externalAccount: Boolean = false
) : AuthorizedInstagramResolver {
    override suspend fun resolve(request: AuthorizedInstagramRequest): ResolveResult {
        if (!isSupportedReelPermalink(request.permalink)) {
            return ResolveResult.Failure(ResolutionFailureCode.INVALID_URL, "Enter a valid Instagram Reel link.")
        }
        if (request.authorizedAccountReference != account.accountReference) {
            return ResolveResult.Failure(ResolutionFailureCode.AUTHORIZED_ACCOUNT_SCOPE_REQUIRED, "Connect an authorized Instagram professional account first.")
        }
        val access = InstagramAccessGate.evaluate(accessGrant, account, externalAccount)
        if (!access.allowed) return accessFailure(access.reason)

        return when (val result = backend.resolve(InstagramResolutionRequest(request.authorizedAccountReference, request.permalink))) {
            is InstagramBackendResult.Failure -> ResolveResult.Failure(result.code, result.safeMessage)
            is InstagramBackendResult.Success -> {
                val normalized = InstagramProviderMediaPolicy.normalize(result.response)
                when (normalized) {
                    is InstagramBackendResult.Failure -> ResolveResult.Failure(normalized.code, normalized.safeMessage)
                    is InstagramBackendResult.Success -> {
                        val media = normalized.response.media.toResolvedMedia(normalized.response.expiresAt)
                        if (media == null) ResolveResult.Failure(ResolutionFailureCode.MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER, "This media is not available for download through the connected Instagram account.")
                        else ResolveResult.Success(media)
                    }
                }
            }
        }
    }

    private fun accessFailure(reason: String): ResolveResult.Failure {
        val code = when {
            account.status == InstagramAccountStatus.REAUTH_REQUIRED || account.status == InstagramAccountStatus.REVOKED -> ResolutionFailureCode.AUTH_REAUTH_REQUIRED
            !account.isProfessional -> ResolutionFailureCode.AUTHORIZED_ACCOUNT_SCOPE_REQUIRED
            else -> ResolutionFailureCode.AUTH_PERMISSION_REQUIRED
        }
        return ResolveResult.Failure(code, reason)
    }
}

fun isSupportedReelPermalink(rawUrl: String): Boolean {
    val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
    val host = uri.host?.lowercase() ?: return false
    val segments = uri.path.trim('/').split('/').filter(String::isNotBlank)
    return uri.scheme.equals("https", ignoreCase = true) &&
        (host == "instagram.com" || host == "www.instagram.com") &&
        uri.userInfo == null &&
        segments.size == 2 && segments[0].equals("reel", ignoreCase = true) &&
        segments[1].matches(Regex("[A-Za-z0-9_-]+"))
}
