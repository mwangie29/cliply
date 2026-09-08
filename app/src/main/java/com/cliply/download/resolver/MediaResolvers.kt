package com.cliply.download.resolver

import android.net.Uri
import com.cliply.BuildConfig
import com.cliply.domain.model.*
import com.cliply.share.SupportedHosts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private val directMediaHosts = setOf("github.com", "raw.githubusercontent.com", "filesamples.com")

interface MediaResolver {
    fun canResolve(url: Uri): Boolean
    suspend fun resolve(url: Uri): ResolvedMedia
}

class MediaResolverRegistry(private val resolvers: List<MediaResolver>) {
    fun canResolve(url: Uri): Boolean = resolvers.any { it.canResolve(url) }
    suspend fun resolve(url: Uri): ResolvedMedia = resolvers.firstOrNull { it.canResolve(url) }?.resolve(url)
        ?: throw unsupported(url.host)
    fun resolverFor(scheme: String?, host: String?, path: String?): MediaResolver? = resolvers.firstOrNull { resolver -> resolver is DirectHttpsMediaResolver && resolver.canResolveComponents(scheme, host, path) }
    fun unsupportedFailure(host: String?): MediaResolutionException = unsupported(host)
    private fun unsupported(host: String?) = MediaResolutionException(ResolutionFailureCode.UNSUPPORTED_PLATFORM, "This type of link isn't supported yet.")
}

/** Rejects redirects that leave Cliply's HTTPS host allowlist before OkHttp follows them. */
fun isAllowedRedirect(rawLocation: String?): Boolean {
    val target = rawLocation?.toHttpUrlOrNull()
    return target != null && target.scheme == "https" && target.host.lowercase() in directMediaHosts
}

fun parseContentLength(raw: String?): Long? = raw?.toLongOrNull()?.takeIf { it >= 0 }

class RedirectAllowlistInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code in 300..399) {
            if (!isAllowedRedirect(response.header("Location"))) {
                response.close()
                throw MediaResolutionException(ResolutionFailureCode.INVALID_MEDIA_RESPONSE, "The media redirected to an unsupported location.")
            }
        }
        return response
    }
}

/** Resolves a user-provided HTTPS URL only when it directly serves an allowed media MIME type. */
class DirectHttpsMediaResolver(client: OkHttpClient, enforceRedirects: Boolean = true) : MediaResolver {
    private val safeClient = if (enforceRedirects) client.newBuilder().addNetworkInterceptor(RedirectAllowlistInterceptor()).build() else client
    private val allowedMimePrefixes = setOf("video/", "audio/")
    override fun canResolve(url: Uri): Boolean = canResolveComponents(url.scheme, url.host, url.path)
    fun canResolveComponents(scheme: String?, host: String?, path: String?): Boolean = assessMediaSource(scheme, host, path).capability == MediaSourceCapability.DIRECT_MEDIA_URL
    override suspend fun resolve(url: Uri): ResolvedMedia = resolve(url.toString())
    suspend fun resolve(rawUrl: String): ResolvedMedia = withContext(Dispatchers.IO) {
        val parsed = rawUrl.toHttpUrlOrNull()
        if (parsed == null || assessMediaSource(parsed.scheme, parsed.host, parsed.encodedPath).capability != MediaSourceCapability.DIRECT_MEDIA_URL) {
            throw MediaResolutionException(ResolutionFailureCode.INVALID_URL, "Enter a valid supported HTTPS media URL.")
        }
        val request = Request.Builder().url(parsed).head().build()
        val response = runCatching { safeClient.newCall(request).execute() }.getOrElse { error ->
            if (error is MediaResolutionException) throw error
            throw MediaResolutionException(ResolutionFailureCode.NETWORK_ERROR, "Cliply couldn't resolve this link right now.")
        }
        response.use {
            if (!it.isSuccessful) throw MediaResolutionException(if (it.code == 403 || it.code == 401) ResolutionFailureCode.ACCESS_DENIED else ResolutionFailureCode.MEDIA_NOT_FOUND, "The media is unavailable.")
            val mime = it.header("Content-Type")?.substringBefore(';')?.lowercase()
                ?: throw MediaResolutionException(ResolutionFailureCode.INVALID_MEDIA_RESPONSE, "Cliply couldn't identify this media.")
            if (allowedMimePrefixes.none { prefix -> mime.startsWith(prefix) }) throw MediaResolutionException(ResolutionFailureCode.INVALID_MEDIA_RESPONSE, "This link does not point directly to media.")
            val size = parseContentLength(it.header("Content-Length"))
            val supportsRange = it.header("Accept-Ranges")?.equals("bytes", ignoreCase = true) == true
            val title = parsed.pathSegments.lastOrNull().takeUnless { it.isNullOrBlank() } ?: "Cliply media"
            ResolvedMedia("direct-${rawUrl.hashCode()}", Platform.UNKNOWN, title, null, mime, null, null, null, size, listOf(MediaVariant("Original", null, null, mime, size, rawUrl, null)), null, supportsRange, rawUrl)
        }
    }
}

/** Development-only resolver retained for deterministic tests; it never handles social URLs. */
class ControlledTestMediaResolver(private val testUrl: String) : MediaResolver {
    override fun canResolve(url: Uri): Boolean = matches(url.toString())
    fun matches(rawUrl: String): Boolean = BuildConfig.DEBUG && rawUrl == testUrl
    override suspend fun resolve(url: Uri): ResolvedMedia = ResolvedMedia("controlled-test-media", Platform.UNKNOWN, "Controlled test media", null, "video/mp4", null, null, null, null, listOf(MediaVariant("Test", null, null, "video/mp4", null, testUrl, null)), null, false, testUrl)
}
