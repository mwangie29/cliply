package com.cliply.share

import org.junit.Assert.*
import org.junit.Test

class UrlValidatorTest {
    private val validator = UrlValidator()
    @Test fun validHttpsUrlPasses() { assertTrue(validator.validate("https://www.instagram.com/reel/123?utm_source=x").isValid) }
    @Test fun emptyFails() { assertEquals("empty", validator.validate("").reason) }
    @Test fun unsupportedSchemeFails() { assertEquals("unsupported_scheme", validator.validate("http://www.instagram.com/reel/123").reason) }
    @Test fun unsupportedDomainFails() { assertEquals("unsupported_domain", validator.validate("https://example.com/video/123").reason) }
    @Test fun malformedFails() { assertFalse(validator.validate("not a url").isValid) }
}
