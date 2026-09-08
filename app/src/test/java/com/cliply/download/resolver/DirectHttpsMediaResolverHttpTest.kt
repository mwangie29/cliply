package com.cliply.download.resolver

import com.cliply.domain.model.MediaResolutionException
import com.cliply.domain.model.ResolutionFailureCode
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectHttpsMediaResolverHttpTest {
    @Test fun redirectPolicyRequiresAllowedHttpsHost() {
        assertTrue(isAllowedRedirect("https://filesamples.com/samples/video/mp4/next.mp4"))
        assertFalse(isAllowedRedirect("https://evil.example/video.mp4"))
        assertFalse(isAllowedRedirect("http://filesamples.com/video.mp4"))
        assertFalse(isAllowedRedirect("https://filesamples.com@evil.example/video.mp4"))
        assertFalse(isAllowedRedirect("https://www.instagram.com/reel/abc123/"))
        assertFalse(isAllowedRedirect(null))
    }

    @Test fun validVideoResponseProducesResolvedMedia() = runBlocking {
        val resolver = resolverReturning(200, "video/mp4; charset=binary", "123", "bytes")
        val media = resolver.resolve("https://filesamples.com/samples/video/mp4/sample.mp4")
        assertEquals("video/mp4", media.mimeType)
        assertEquals(123L, media.fileSize)
        assertTrue(media.supportsRange)
        assertEquals("https://filesamples.com/samples/video/mp4/sample.mp4", media.sourceUrl)
    }

    @Test fun htmlAndMissingMimeResponsesFailAsInvalidMedia() = runBlocking {
        assertFailure(200, "text/html", ResolutionFailureCode.INVALID_MEDIA_RESPONSE)
        assertFailure(200, null, ResolutionFailureCode.INVALID_MEDIA_RESPONSE)
        assertFailure(200, "application/json", ResolutionFailureCode.INVALID_MEDIA_RESPONSE)
    }

    @Test fun unauthorizedAndMissingResponsesMapToSafeTypedErrors() = runBlocking {
        assertFailure(401, "video/mp4", ResolutionFailureCode.ACCESS_DENIED)
        assertFailure(403, "video/mp4", ResolutionFailureCode.ACCESS_DENIED)
        assertFailure(404, "video/mp4", ResolutionFailureCode.MEDIA_NOT_FOUND)
        assertFailure(500, "video/mp4", ResolutionFailureCode.MEDIA_NOT_FOUND)
    }

    @Test fun malformedContentLengthDoesNotBecomeNegativeMetadata() {
        assertEquals(null, parseContentLength("not-a-number"))
        assertEquals(null, parseContentLength("-1"))
        assertEquals(123L, parseContentLength("123"))
    }

    private fun resolverReturning(code: Int, mime: String?, length: String?, ranges: String?): DirectHttpsMediaResolver {
        val fake = Interceptor { chain ->
            val builder = Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(code).message(if (code < 400) "OK" else "Error")
            mime?.let { builder.header("Content-Type", it) }
            length?.let { builder.header("Content-Length", it) }
            ranges?.let { builder.header("Accept-Ranges", it) }
            builder.body("".toResponseBody(null)).build()
        }
        return DirectHttpsMediaResolver(OkHttpClient.Builder().addInterceptor(fake).build(), enforceRedirects = false)
    }

    private suspend fun assertFailure(code: Int, mime: String?, expected: ResolutionFailureCode) {
        val error = runCatching { resolverReturning(code, mime, null, null).resolve("https://filesamples.com/video.mp4") }.exceptionOrNull()
        assertTrue(error is MediaResolutionException)
        assertEquals(expected, (error as MediaResolutionException).code)
    }
}
