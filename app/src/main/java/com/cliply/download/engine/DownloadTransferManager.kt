package com.cliply.download.engine

import kotlinx.coroutines.flow.Flow

data class TransferProgress(val bytesDownloaded: Long, val totalBytes: Long?, val bytesPerSecond: Long, val completed: Boolean = false, val cancelled: Boolean = false, val error: Throwable? = null)
interface DownloadTransferManager { fun transfer(url: String, destination: java.io.File): Flow<TransferProgress>; fun cancel(jobId: String) }
