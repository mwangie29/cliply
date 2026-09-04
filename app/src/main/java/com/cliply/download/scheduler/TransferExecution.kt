package com.cliply.download.scheduler

import android.content.Context
import android.content.Intent
import android.os.Build
import com.cliply.download.engine.DownloadService

interface TransferExecutor { fun start(context: Context, jobId: String, sourceUrl: String); fun cancel(context: Context, jobId: String) }
class AndroidTransferExecutor : TransferExecutor {
    override fun start(context: Context, jobId: String, sourceUrl: String) { context.startForegroundService(Intent(context, DownloadService::class.java).putExtra(DownloadService.EXTRA_JOB_ID, jobId).putExtra(DownloadService.EXTRA_SOURCE_URL, sourceUrl).putExtra(DownloadService.EXTRA_API_PATH, if (Build.VERSION.SDK_INT >= 34) "uidt-compatible" else "api33-compatible")) }
    override fun cancel(context: Context, jobId: String) { context.stopService(Intent(context, DownloadService::class.java).putExtra(DownloadService.EXTRA_JOB_ID, jobId)) }
}
