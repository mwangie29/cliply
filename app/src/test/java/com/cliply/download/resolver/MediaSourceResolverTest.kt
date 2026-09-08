package com.cliply.download.resolver

import com.cliply.domain.model.ResolutionFailureCode
import com.cliply.domain.model.ResolveResult
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSourceResolverTest {
    private val direct = DirectMediaSourceResolver(DirectHttpsMediaResolver(OkHttpClient(), enforceRedirects = false))
    private val instagram = AuthorizedInstagramSourceResolver(UnconfiguredAuthorizedInstagramResolver())

    @Test fun directRequestSelectsOnlyDirectResolver() {
        val request = ResolutionRequest("https://filesamples.com/video.mp4", MediaSourceCapability.DIRECT_MEDIA_URL)
        val registry = MediaSourceResolverRegistry(listOf(instagram, direct))
        assertTrue(registry.canHandle(request))
        assertTrue(direct.canHandle(request))
        assertFalse(instagram.canHandle(request))
    }

    @Test fun authorizedInstagramRequiresAccountReference() {
        val request = ResolutionRequest("https://www.instagram.com/reel/abc123/", MediaSourceCapability.AUTHORIZED_INSTAGRAM_MEDIA)
        assertFalse(instagram.canHandle(request))
        val withAccount = request.copy(authorizedAccountReference = "account-ref")
        assertTrue(instagram.canHandle(withAccount))
    }

    @Test fun unconfiguredInstagramReturnsTypedFailureAndNeverMedia() = runBlocking {
        val result = instagram.resolve(ResolutionRequest("https://www.instagram.com/reel/abc123/", MediaSourceCapability.AUTHORIZED_INSTAGRAM_MEDIA, "account-ref"))
        assertTrue(result is ResolveResult.Failure)
        assertEquals(ResolutionFailureCode.AUTHORIZED_INSTAGRAM_NOT_CONFIGURED, (result as ResolveResult.Failure).code)
    }

    @Test fun unsupportedRequestFailsClosedWithoutResolver() = runBlocking {
        val result = MediaSourceResolverRegistry(listOf(direct, instagram)).resolve(ResolutionRequest("https://www.instagram.com/reel/abc123/", MediaSourceCapability.UNSUPPORTED_SOCIAL_PAGE))
        assertTrue(result is ResolveResult.Failure)
        assertEquals(ResolutionFailureCode.UNSUPPORTED_PLATFORM, (result as ResolveResult.Failure).code)
    }
}
