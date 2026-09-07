package com.cliply.download.resolver

import android.net.Uri
import com.cliply.BuildConfig
import com.cliply.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import com.cliply.share.SupportedHosts

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

/** Resolves a user-provided HTTPS URL only when it directly serves an allowed media MIME type. */
class DirectHttpsMediaResolver(private val client: OkHttpClient) : MediaResolver {
    private val allowedMimePrefixes = setOf("video/", "audio/")
    override fun canResolve(url: Uri): Boolean = canResolveComponents(url.scheme, url.host, url.path)
    fun canResolveComponents(scheme: String?, host: String?, path: String?): Boolean = scheme == "https" && host?.lowercase() in SupportedHosts.all && isDirectMediaPath(path.orEmpty())
    override suspend fun resolve(url: Uri): ResolvedMedia = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url.toString()).head().build()
        val response = runCatching { client.newCall(request).execute() }.getOrElse { throw MediaResolutionException(ResolutionFailureCode.NETWORK_ERROR, "Cliply couldn't resolve this link right now.") }
        response.use {
            if (!it.isSuccessful) throw MediaResolutionException(if (it.code == 403 || it.code == 401) ResolutionFailureCode.ACCESS_DENIED else ResolutionFailureCode.MEDIA_NOT_FOUND, "The media is unavailable.")
            val mime = it.header("Content-Type")?.substringBefore(';')?.lowercase()
                ?: throw MediaResolutionException(ResolutionFailureCode.INVALID_MEDIA_RESPONSE, "Cliply couldn't identify this media.")
            if (allowedMimePrefixes.none { prefix -> mime.startsWith(prefix) }) throw MediaResolutionException(ResolutionFailureCode.INVALID_MEDIA_RESPONSE, "This link does not point directly to media.")
            val size = it.header("Content-Length")?.toLongOrNull()
            val supportsRange = it.header("Accept-Ranges")?.equals("bytes", ignoreCase = true) == true
            val extension = if (mime.startsWith("audio/")) "audio" else "video"
            val title = url.path?.substringAfterLast('/').takeUnless { it.isNullOrBlank() } ?: "Cliply media"
            ResolvedMedia("direct-${url.toString().hashCode()}", Platform.UNKNOWN, title, null, mime, null, null, null, size, listOf(MediaVariant("Original", null, null, mime, size, url.toString(), null)), null, supportsRange, url.toString())
        }
    }
    private fun isDirectMediaPath(path: String): Boolean = path.substringAfterLast('.', "").lowercase() in setOf("mp4", "mov", "m4v", "webm", "mkv", "mp3", "m4a", "wav", "ogg")
}

/** Development-only resolver retained for deterministic tests; it never handles social URLs. */
class ControlledTestMediaResolver(private val testUrl: String) : MediaResolver {
    override fun canResolve(url: Uri): Boolean = matches(url.toString())
    fun matches(rawUrl: String): Boolean = BuildConfig.DEBUG && rawUrl == testUrl
    override suspend fun resolve(url: Uri): ResolvedMedia = ResolvedMedia("controlled-test-media", Platform.UNKNOWN, "Controlled test media", null, "video/mp4", null, null, null, null, listOf(MediaVariant("Test", null, null, "video/mp4", null, testUrl, null)), null, false, testUrl)
}
