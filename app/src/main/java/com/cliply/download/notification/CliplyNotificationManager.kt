package com.cliply.download.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.cliply.MainActivity
import com.cliply.R

class CliplyNotificationManager(private val context: Context) {
    companion object { const val CHANNEL_ID = "cliply_downloads" }
    private val manager = context.getSystemService(NotificationManager::class.java)
    init { manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW)) }
    fun progressNotification(jobId: String, downloaded: Long, total: Long?, speed: Long): Notification { val progress = if (total != null && total > 0) ((downloaded * 100) / total).toInt() else 0; return base("Downloading video", jobId).setOngoing(true).setOnlyAlertOnce(true).setProgress(100, progress, total == null).setContentText("${format(downloaded)} / ${total?.let(::format) ?: "?"}   ${formatSpeed(speed)}").build() }
    fun showProgress(jobId: String, downloaded: Long, total: Long?, speed: Long) { manager.notify(jobId.hashCode(), progressNotification(jobId, downloaded, total, speed)) }
    fun showComplete(jobId: String, name: String, uri: Uri?, mimeType: String) { val builder = base("Download complete", jobId).setOngoing(false).setContentText(name).setAutoCancel(true); if (uri != null) { val open = PendingIntent.getActivity(context, jobId.hashCode(), Intent(Intent.ACTION_VIEW, uri).setDataAndType(uri, mimeType).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); val share = PendingIntent.getActivity(context, jobId.hashCode() + 1, Intent.createChooser(Intent(Intent.ACTION_SEND).setType(mimeType).putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "Share media"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); builder.addAction(NotificationCompat.Action(0, "Open", open)).addAction(NotificationCompat.Action(0, "Share", share)) }; manager.notify(jobId.hashCode(), builder.build()) }
    fun showFailure(jobId: String, message: String) { manager.notify(jobId.hashCode(), base("Download failed", jobId).setOngoing(false).setContentText(message).build()) }
    fun cancel(jobId: String) { manager.cancel(jobId.hashCode()) }
    private fun base(title: String, jobId: String) = NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_stat_cliply).setContentTitle("Cliply").setSubText(title).setStyle(NotificationCompat.BigTextStyle().bigText(title)).setAutoCancel(true)
    private fun format(value: Long) = "%.1f MB".format(value / 1_000_000.0)
    private fun formatSpeed(value: Long) = if (value > 0) "%.1f MB/s".format(value / 1_000_000.0) else ""
}
