package com.cliply.download.engine

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import com.cliply.download.notification.CliplyNotificationManager
import com.cliply.download.storage.MediaStorePublisher
import com.cliply.download.state.DownloadStateMachine
import com.cliply.domain.model.*
import com.cliply.domain.repository.DownloadRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {
    companion object { const val EXTRA_JOB_ID = "job_id"; const val EXTRA_SOURCE_URL = "source_url"; const val EXTRA_API_PATH = "api_path" }
    @Inject lateinit var repository: DownloadRepository
    @Inject lateinit var provider: TestMediaProvider
    @Inject lateinit var transfer: DownloadTransferManager
    @Inject lateinit var publisher: MediaStorePublisher
    @Inject lateinit var notifications: CliplyNotificationManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { val jobId = intent?.getStringExtra(EXTRA_JOB_ID) ?: return START_NOT_STICKY; startForeground(jobId.hashCode(), NotificationCompat.Builder(this, CliplyNotificationManager.CHANNEL_ID).setSmallIcon(com.cliply.R.drawable.cliply_logo_placeholder).setContentTitle("Cliply").setContentText("Preparing download").setOngoing(true).build()); scope.launch { runJob(jobId, intent.getStringExtra(EXTRA_SOURCE_URL).orEmpty()) }; return START_NOT_STICKY }
    private suspend fun runJob(jobId: String, sourceUrl: String) { val existing = repository.get(jobId) ?: DownloadJob(id = jobId, sourceUrl = sourceUrl); try { repository.update(existing.copy(status = DownloadStateMachine.transition(existing.status, DownloadStatus.RESOLVING))); val media = provider.resolve(android.net.Uri.parse(sourceUrl)); val resolved = existing.copy(platform = media.platform, title = media.title, status = DownloadStatus.RESOLVED, selectedVariant = media.variants.first()); repository.update(resolved); val queued = resolved.copy(status = DownloadStatus.QUEUED); repository.update(queued); val downloading = queued.copy(status = DownloadStatus.DOWNLOADING, startedAt = java.time.Instant.now()); repository.update(downloading); val temp = File(cacheDir, "$jobId.part"); transfer.transfer(media.variants.first().downloadUrl, temp).collectLatest { progress -> val updated = downloading.copy(downloadedBytes = progress.bytesDownloaded, totalBytes = progress.totalBytes ?: 0, speedBytesPerSecond = progress.bytesPerSecond, progressPercent = if (progress.totalBytes != null && progress.totalBytes > 0) ((progress.bytesDownloaded * 100) / progress.totalBytes).toInt() else 0); repository.update(updated); notifications.showProgress(jobId, progress.bytesDownloaded, progress.totalBytes, progress.bytesPerSecond); if (progress.error != null) throw progress.error; if (progress.completed) { val uri = publisher.publish(temp, "cliply_${jobId.take(8)}.mp4"); repository.update(updated.copy(status = DownloadStatus.COMPLETED, completedAt = java.time.Instant.now(), mediaStoreUri = uri?.toString())); notifications.showComplete(jobId, "cliply_${jobId.take(8)}.mp4", uri); stopSelf() } } } catch (t: Throwable) { repository.get(jobId)?.let { if (it.status != DownloadStatus.COMPLETED) repository.update(it.copy(status = DownloadStatus.FAILED, errorMessage = t.message ?: "Download failed")) }; notifications.showFailure(jobId, t.message ?: "Download failed"); stopSelf() } }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
