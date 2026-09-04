package com.cliply.download.scheduler

import android.app.job.JobScheduler
import android.content.Context
import android.os.Build
import android.util.Log

class TransferExecutorFactory(private val context: Context) {
    fun create(): TransferExecutor = if (Build.VERSION.SDK_INT >= 34) { Log.i("CliplyTransfer", "executor_selected path=UIDT api=${Build.VERSION.SDK_INT}"); UidtTransferExecutor(context.getSystemService(JobScheduler::class.java)) } else { Log.i("CliplyTransfer", "executor_selected path=ANDROID_13_COMPATIBILITY api=${Build.VERSION.SDK_INT}"); AndroidTransferExecutor() }
    companion object { fun pathForApi(api: Int): String = if (api >= 34) "UIDT" else "ANDROID_13_COMPATIBILITY" }
}
