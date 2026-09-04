package com.cliply.download.scheduler

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.os.PersistableBundle
import com.cliply.download.engine.DownloadJobRunner
import com.cliply.download.engine.DownloadTransferManager
import com.cliply.download.notification.CliplyNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

class UidtTransferExecutor(private val scheduler: JobScheduler) : TransferExecutor {
    override fun start(context: android.content.Context, jobId: String, sourceUrl: String) {
        val extras = PersistableBundle().apply { putString(UidtJobService.EXTRA_JOB_ID, jobId) }
        val info = JobInfo.Builder(jobId.hashCode(), ComponentName(context, UidtJobService::class.java)).setUserInitiated(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setEstimatedNetworkBytes(1L, 100L * 1024L * 1024L).setExtras(extras).build()
        check(scheduler.schedule(info) == JobScheduler.RESULT_SUCCESS) { "Unable to schedule UIDT transfer" }
    }
    override fun cancel(context: android.content.Context, jobId: String) { scheduler.cancel(jobId.hashCode()) }
}

@AndroidEntryPoint
class UidtJobService : JobService() {
    companion object { const val EXTRA_JOB_ID = "downloadJobId" }
    @Inject lateinit var runner: DownloadJobRunner
    @Inject lateinit var transfer: DownloadTransferManager
    @Inject lateinit var notifications: CliplyNotificationManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<Int, Job>()
    override fun onStartJob(params: JobParameters): Boolean {
        val id = params.extras.getString(EXTRA_JOB_ID) ?: run { jobFinished(params, false); return false }
        if (!params.isUserInitiatedJob) { jobFinished(params, false); return false }
        val notification = notifications.progressNotification(id, 0, null, 0)
        setNotification(params, id.hashCode(), notification, JobService.JOB_END_NOTIFICATION_POLICY_REMOVE)
        val work = scope.launch { try { runner.run(id); jobFinished(params, false) } catch (_: CancellationException) { jobFinished(params, false) } catch (_: Throwable) { jobFinished(params, false) } finally { jobs.remove(params.jobId) } }
        jobs[params.jobId] = work
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { val id = params.extras.getString(EXTRA_JOB_ID); if (id != null) transfer.cancel(id); jobs.remove(params.jobId)?.cancel(); return false }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
