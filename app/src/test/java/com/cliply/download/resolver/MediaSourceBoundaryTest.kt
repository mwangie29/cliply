package com.cliply.download.resolver

import com.cliply.domain.model.ResolveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSourceBoundaryTest {
    @Test fun filesamplesMp4IsDirectMedia() {
        assertEquals(MediaSourceCapability.DIRECT_MEDIA_URL, assessMediaSource("https", "filesamples.com", "/samples/video/mp4/sample_640x360.mp4").capability)
    }

    @Test fun instagramReelIsUnsupportedUntilAuthorizedIntegrationExists() {
        assertEquals(MediaSourceCapability.UNSUPPORTED_SOCIAL_PAGE, assessMediaSource("https", "www.instagram.com", "/reel/abc123/").capability)
    }

    @Test fun nonHttpsAndUnknownHostsAreRejected() {
        assertEquals(MediaSourceCapability.INVALID_OR_UNSUPPORTED_URL, assessMediaSource("http", "filesamples.com", "/video.mp4").capability)
        assertEquals(MediaSourceCapability.INVALID_OR_UNSUPPORTED_URL, assessMediaSource("https", "example.com", "/video.mp4").capability)
    }

    @Test fun authorizedInstagramPlaceholderFailsClosed() {
        val result = UnconfiguredAuthorizedInstagramResolver().unavailable()
        assertTrue(result is ResolveResult.Failure)
        assertEquals("AUTHORIZED_INSTAGRAM_NOT_CONFIGURED", result.code)
    }
}
