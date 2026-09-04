package com.cliply.download.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cliply.MainActivity
import com.cliply.R

class CliplyNotificationManager(private val context: Context) {
    companion object { const val CHANNEL_ID = "cliply_downloads"; const val EXTRA_JOB_ID = "job_id" }
    private val manager = context.getSystemService(NotificationManager::class.java)
    init { manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW)) }
    fun showProgress(jobId: String, downloaded: Long, total: Long?, speed: Long) { val progress = if (total != null && total > 0) ((downloaded * 100) / total).toInt() else 0; val builder = base("Downloading video", jobId).setOngoing(true).setOnlyAlertOnce(true).setProgress(100, progress, total == null).setContentText("${format(downloaded)} / ${total?.let(::format) ?: "?"}   ${formatSpeed(speed)}"); manager.notify(jobId.hashCode(), builder.build()) }
    fun showComplete(jobId: String, name: String, uri: android.net.Uri?) { val open = PendingIntent.getActivity(context, jobId.hashCode(), Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); manager.notify(jobId.hashCode(), base("Download complete", jobId).setOngoing(false).setContentText(name).setContentIntent(open).build()) }
    fun showFailure(jobId: String, message: String) { manager.notify(jobId.hashCode(), base("Download failed", jobId).setOngoing(false).setContentText(message).build()) }
    fun cancel(jobId: String) { manager.cancel(jobId.hashCode()) }
    private fun base(title: String, jobId: String) = NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.cliply_logo_placeholder).setContentTitle("Cliply").setSubText(title).setStyle(NotificationCompat.BigTextStyle().bigText(title)).setAutoCancel(true)
    private fun format(value: Long) = "%.1f MB".format(value / 1_000_000.0)
    private fun formatSpeed(value: Long) = if (value > 0) "%.1f MB/s".format(value / 1_000_000.0) else ""
}
