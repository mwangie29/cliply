package com.cliply.download.engine

import android.net.Uri
import android.util.Log
import com.cliply.download.notification.CliplyNotificationManager
import com.cliply.download.state.DownloadStateMachine
import com.cliply.download.storage.MediaStorePublisher
import com.cliply.domain.model.*
import com.cliply.domain.repository.DownloadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import javax.inject.Inject

class DownloadJobRunner @Inject constructor(private val repository: DownloadRepository, private val provider: TestMediaProvider, private val transfer: DownloadTransferManager, private val publisher: MediaStorePublisher, private val notifications: CliplyNotificationManager) {
    companion object { private const val TAG = "CliplyDownload" }
    suspend fun run(jobId: String) {
        val existing = repository.get(jobId) ?: error("Unknown download job: $jobId")
        var temp: File? = null
        Log.i(TAG, "job_started id=$jobId")
        try {
            repository.update(existing.copy(status = DownloadStateMachine.transition(existing.status, DownloadStatus.RESOLVING)))
            val media = provider.resolve(Uri.parse(existing.sourceUrl))
            val resolved = existing.copy(platform = media.platform, title = media.title, status = DownloadStatus.RESOLVED, selectedVariant = media.variants.first())
            repository.update(resolved)
            val queued = resolved.copy(status = DownloadStatus.QUEUED)
            repository.update(queued)
            val downloading = queued.copy(status = DownloadStatus.DOWNLOADING, startedAt = java.time.Instant.now())
            repository.update(downloading)
            temp = File.createTempFile("cliply_$jobId", ".part")
            repository.update(downloading.copy(temporaryFilePath = temp!!.absolutePath))
            Log.i(TAG, "transfer_started id=$jobId")
            transfer.transfer(media.variants.first().downloadUrl, temp).collectLatest { progress ->
                val updated = downloading.copy(downloadedBytes = progress.bytesDownloaded, totalBytes = progress.totalBytes ?: 0, speedBytesPerSecond = progress.bytesPerSecond, progressPercent = if (progress.totalBytes != null && progress.totalBytes > 0) ((progress.bytesDownloaded * 100) / progress.totalBytes).toInt() else 0)
                repository.update(updated)
                notifications.showProgress(jobId, progress.bytesDownloaded, progress.totalBytes, progress.bytesPerSecond)
                progress.error?.let { throw it }
                if (progress.completed) {
                    val name = "cliply_${jobId.take(8)}.mp4"
                    val completedTemp = temp ?: error("Missing temporary file")
                    val uri = publisher.publish(completedTemp, name)
                    repository.update(updated.copy(status = DownloadStatus.COMPLETED, completedAt = java.time.Instant.now(), temporaryFilePath = null, mediaStoreUri = uri?.toString()))
                    notifications.showComplete(jobId, name, uri)
                    Log.i(TAG, "transfer_completed id=$jobId")
                }
            }
        } catch (t: CancellationException) {
            temp?.delete()
            repository.get(jobId)?.let { current -> if (current.status == DownloadStatus.DOWNLOADING) repository.update(current.copy(status = DownloadStatus.CANCELLED)) }
            notifications.cancel(jobId)
            Log.i(TAG, "transfer_cancelled id=$jobId")
            throw t
        } catch (t: Throwable) {
            temp?.delete()
            repository.get(jobId)?.let { current -> if (current.status != DownloadStatus.COMPLETED && current.status != DownloadStatus.CANCELLED) repository.update(current.copy(status = DownloadStatus.FAILED, errorMessage = t.message ?: "Download failed")) }
            notifications.showFailure(jobId, t.message ?: "Download failed")
            Log.e(TAG, "transfer_failed id=$jobId reason=${t.javaClass.simpleName}")
            throw t
        }
    }
    fun cancel(jobId: String) { transfer.cancel(jobId) }
}
