package com.cliply.share

import android.net.Uri
import java.net.URI

data class UrlValidationResult(val isValid: Boolean, val normalizedUrl: Uri? = null, val hostname: String? = null, val reason: String? = null)

class UrlValidator(private val allowedHosts: Set<String> = setOf("instagram.com", "www.instagram.com", "tiktok.com", "www.tiktok.com", "facebook.com", "www.facebook.com", "github.com", "raw.githubusercontent.com")) {
    fun validate(raw: String?): UrlValidationResult {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return UrlValidationResult(false, reason = "empty")
        val parsed = runCatching { URI(value) }.getOrNull() ?: return UrlValidationResult(false, reason = "malformed")
        val scheme = parsed.scheme?.lowercase()
        val host = parsed.host?.lowercase()
        if (scheme != "https") return UrlValidationResult(false, hostname = host, reason = "unsupported_scheme")
        if (host.isNullOrBlank()) return UrlValidationResult(false, reason = "missing_host")
        if (host !in allowedHosts) return UrlValidationResult(false, hostname = host, reason = "unsupported_domain")
        val normalized = URI("https", host, parsed.path ?: "/", null, null).toString()
        val androidUri = runCatching { Uri.parse(normalized) }.getOrNull()
        return UrlValidationResult(true, androidUri, host)
    }
}
