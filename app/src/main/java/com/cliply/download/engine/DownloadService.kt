package com.cliply.download.engine

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cliply.R
import com.cliply.download.notification.CliplyNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {
    companion object { const val EXTRA_JOB_ID = "job_id"; const val EXTRA_SOURCE_URL = "source_url"; const val EXTRA_API_PATH = "api_path" }
    @Inject lateinit var runner: DownloadJobRunner
    @Inject lateinit var notifications: CliplyNotificationManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val jobId = intent?.getStringExtra(EXTRA_JOB_ID) ?: return START_NOT_STICKY
        startForeground(jobId.hashCode(), notifications.progressNotification(jobId, 0, null, 0))
        scope.launch { runCatching { runner.run(jobId) }; stopSelf(startId) }
        return START_NOT_STICKY
    }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
