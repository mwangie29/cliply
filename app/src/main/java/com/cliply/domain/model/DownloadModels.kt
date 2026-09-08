package com.cliply.domain.model

import android.net.Uri
import java.time.Instant
import java.util.UUID

enum class Platform { INSTAGRAM, TIKTOK, FACEBOOK, UNKNOWN }
enum class ResolutionSource { DIRECT_MEDIA, AUTHORIZED_INSTAGRAM }
enum class DownloadStatus { CREATED, RESOLVING, RESOLVED, QUEUED, DOWNLOADING, RETRYING, COMPLETED, FAILED, CANCELLED }
data class MediaVariant(val quality: String, val width: Int?, val height: Int?, val mimeType: String, val fileSize: Long?, val downloadUrl: String, val expiresAt: Instant?, val bitrate: Long? = null)
data class ResolvedMedia(val id: String, val platform: Platform, val title: String?, val thumbnailUrl: String?, val mimeType: String, val width: Int?, val height: Int?, val durationMs: Long?, val fileSize: Long?, val variants: List<MediaVariant>, val expiresAt: Instant?, val supportsRange: Boolean, val sourceUrl: String? = null)
data class DownloadJob(val id: String = UUID.randomUUID().toString(), val sourceUrl: String, val resolutionSource: ResolutionSource = ResolutionSource.DIRECT_MEDIA, val providerResolutionId: String? = null, val providerAccountReference: String? = null, val resolutionExpiresAt: Instant? = null, val platform: Platform = Platform.UNKNOWN, val title: String? = null, val thumbnailUrl: String? = null, val status: DownloadStatus = DownloadStatus.CREATED, val mimeType: String? = null, val width: Int? = null, val height: Int? = null, val durationMs: Long? = null, val totalBytes: Long = 0, val downloadedBytes: Long = 0, val speedBytesPerSecond: Long = 0, val progressPercent: Int = 0, val selectedVariant: MediaVariant? = null, val temporaryFilePath: String? = null, val mediaStoreUri: String? = null, val createdAt: Instant = Instant.now(), val startedAt: Instant? = null, val completedAt: Instant? = null, val retryCount: Int = 0, val errorCode: String? = null, val errorMessage: String? = null)

data class ResolveRequest(val url: Uri)
sealed interface ResolveResult { data class Success(val media: ResolvedMedia): ResolveResult; data class Failure(val code: ResolutionFailureCode, val message: String): ResolveResult }
enum class ResolutionFailureCode { UNSUPPORTED_PLATFORM, INVALID_URL, MEDIA_NOT_FOUND, ACCESS_DENIED, RESOLUTION_UNAVAILABLE, AUTHORIZED_INSTAGRAM_NOT_CONFIGURED, AUTHORIZED_ACCOUNT_SCOPE_REQUIRED, AUTH_REAUTH_REQUIRED, AUTH_PERMISSION_REQUIRED, MEDIA_NOT_AVAILABLE_THROUGH_PROVIDER, PROVIDER_RATE_LIMITED, PROVIDER_UNAVAILABLE, NETWORK_ERROR, INVALID_MEDIA_RESPONSE }
class MediaResolutionException(val code: ResolutionFailureCode, override val message: String) : IllegalStateException(message)
