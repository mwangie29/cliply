package com.cliply.download.resolver

import com.cliply.domain.model.MediaResolutionException
import org.junit.Assert.*
import org.junit.Test

class MediaResolverTest {
    @Test fun directResolverRecognizesMediaExtension() {
        val resolver = DirectHttpsMediaResolver(okhttp3.OkHttpClient())
        assertTrue(resolver.canResolveComponents("https", "github.com", "/example/video.mp4"))
        assertFalse(resolver.canResolveComponents("https", "www.instagram.com", "/reel/123"))
    }

    @Test fun controlledResolverMatchesOnlyExactDevelopmentUrl() {
        val url = "https://github.com/mediaelement/mediaelement-files/raw/master/big_buck_bunny.mp4"
        val resolver = ControlledTestMediaResolver(url)
        assertFalse(resolver.matches("https://www.instagram.com/reel/123"))
        assertTrue(resolver.matches(url) || !com.cliply.BuildConfig.DEBUG)
    }

    @Test fun registryReportsTypedUnsupportedFailure() {
        val error = MediaResolverRegistry(emptyList()).unsupportedFailure("www.instagram.com")
        assertTrue(error is MediaResolutionException)
        assertEquals("UNSUPPORTED_PLATFORM", error.code.name)
    }

    @Test fun registrySelectsDirectResolverByComponents() {
        val direct = DirectHttpsMediaResolver(okhttp3.OkHttpClient())
        val selected = MediaResolverRegistry(listOf(direct)).resolverFor("https", "filesamples.com", "/video.mp4")
        assertSame(direct, selected)
    }
}
