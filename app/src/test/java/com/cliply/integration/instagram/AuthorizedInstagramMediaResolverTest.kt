package com.cliply.integration.instagram

import com.cliply.domain.model.ResolveResult
import com.cliply.domain.model.ResolutionFailureCode
import com.cliply.download.resolver.AuthorizedInstagramRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AuthorizedInstagramMediaResolverTest {
    private val grant = InstagramAccessGrant(
        configuration = InstagramApiConfiguration.INSTAGRAM_LOGIN,
        apiVersion = "v26.0",
        apiHost = "graph.instagram.com",
        tokenType = "Instagram User",
        permissions = setOf("instagram_business_basic"),
        accessLevel = InstagramAccessLevel.STANDARD,
        appReviewApproved = false,
        businessVerificationComplete = false,
        featureFlag = InstagramFeatureFlag.INTERNAL
    )
    private val account = ConnectedInstagramAccount("account-ref", "ig-user-id", "cliply", "Cliply", true, InstagramAccountStatus.ACTIVE, setOf("instagram_business_basic"))
    private val request = AuthorizedInstagramRequest("https://www.instagram.com/reel/ABC_123/", "account-ref")

    @Test fun supportedReelPermalinksAreAcceptedAndOtherInstagramPagesRejected() {
        assertTrue(isSupportedReelPermalink("https://www.instagram.com/reel/ABC_123/"))
        assertTrue(isSupportedReelPermalink("https://instagram.com/reel/ABC-123"))
        assertEquals(false, isSupportedReelPermalink("https://www.instagram.com/p/ABC_123/"))
        assertEquals(false, isSupportedReelPermalink("https://www.instagram.com/accounts/login/"))
        assertEquals(false, isSupportedReelPermalink("http://www.instagram.com/reel/ABC_123/"))
    }

    @Test fun invalidPermalinkNeverCallsBackend() = runBlocking {
        var calls = 0
        val resolver = resolver { calls++ ; error("must not be called") }
        val result = resolver.resolve(request.copy(permalink = "https://www.instagram.com/p/not-a-reel/"))
        assertFailure(ResolutionFailureCode.INVALID_URL, result)
        assertEquals(0, calls)
    }

    @Test fun validProviderMediaIsNormalizedWithExpiry() = runBlocking {
        val expiry = Instant.parse("2026-09-09T13:00:00Z")
        val resolver = resolver {
            InstagramBackendResult.Success(InstagramResolutionResponse("resolution-1", payload("https://scontent.cdninstagram.com/video.mp4"), expiry))
        }
        val result = resolver.resolve(request)
        assertTrue(result is ResolveResult.Success)
        val media = (result as ResolveResult.Success).media
        assertEquals("media-1", media.id)
        assertEquals(expiry, media.expiresAt)
        assertEquals("https://scontent.cdninstagram.com/video.mp4", media.variants.single().downloadUrl)
    }

    @Test fun missingMediaUrlIsProviderFailureWithoutFallback() = runBlocking {
        val resolver = resolver { InstagramBackendResult.Success(InstagramResolutionResponse("resolution-1", payload(null), null)) }
        assertFailure(ResolutionFailureCode.MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER, resolver.resolve(request))
    }

    @Test fun accountScopeAndReauthAreBlockedBeforeBackendCall() = runBlocking {
        var calls = 0
        val resolver = resolver { calls++ ; error("must not be called") }
        assertFailure(ResolutionFailureCode.AUTHORIZED_ACCOUNT_SCOPE_REQUIRED, resolver.resolve(request.copy(authorizedAccountReference = "other")))
        assertEquals(0, calls)
        val reauthResolver = AuthorizedInstagramMediaResolver(backend = backend { calls++ ; error("must not be called") }, accessGrant = grant, account = account.copy(status = InstagramAccountStatus.REAUTH_REQUIRED))
        assertFailure(ResolutionFailureCode.AUTH_REAUTH_REQUIRED, reauthResolver.resolve(request))
        assertEquals(0, calls)
    }

    @Test fun backendFailureIsPassedThroughAsSafeTypedFailure() = runBlocking {
        val resolver = resolver { InstagramBackendResult.Failure(ResolutionFailureCode.PROVIDER_RATE_LIMITED, "Try again later.") }
        assertFailure(ResolutionFailureCode.PROVIDER_RATE_LIMITED, resolver.resolve(request))
    }

    private fun resolver(block: suspend () -> InstagramBackendResult) = AuthorizedInstagramMediaResolver(backend(block), grant, account)

    private fun backend(block: suspend () -> InstagramBackendResult) = object : AuthorizedInstagramBackend {
        override suspend fun resolve(request: InstagramResolutionRequest): InstagramBackendResult = block()
    }

    private fun payload(mediaUrl: String?) = InstagramResolvedMediaPayload("media-1", "A Reel", null, "video/mp4", 1080, 1920, 1000, 1234, mediaUrl, true)

    private fun assertFailure(code: ResolutionFailureCode, result: ResolveResult) {
        assertTrue(result is ResolveResult.Failure)
        assertEquals(code, (result as ResolveResult.Failure).code)
    }
}
