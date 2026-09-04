package com.cliply.download.scheduler

import android.app.job.JobScheduler
import android.content.Context
import android.os.Build

class TransferExecutorFactory(private val context: Context) {
    fun create(): TransferExecutor = if (Build.VERSION.SDK_INT >= 34) UidtTransferExecutor(context.getSystemService(JobScheduler::class.java)) else AndroidTransferExecutor()
    companion object { fun pathForApi(api: Int): String = if (api >= 34) "UIDT" else "ANDROID_13_COMPATIBILITY" }
}
