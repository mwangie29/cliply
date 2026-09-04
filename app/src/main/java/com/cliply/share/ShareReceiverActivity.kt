package com.cliply.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.cliply.domain.usecase.CreateDownloadJobUseCase
import com.cliply.download.scheduler.AndroidTransferExecutor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

class ShareIntentParser { fun extractText(intent: Intent): String? = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() }
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {
    @Inject lateinit var createDownloadJob: CreateDownloadJobUseCase
    @Inject lateinit var executor: AndroidTransferExecutor
    private val validator = UrlValidator()
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); val raw = ShareIntentParser().extractText(intent); val result = validator.validate(raw); if (!result.isValid || result.normalizedUrl == null) { Toast.makeText(this, "This link is not supported", Toast.LENGTH_SHORT).show(); finish(); return }; lifecycleScope.launch { val job = createDownloadJob(result.normalizedUrl.toString()); executor.start(this@ShareReceiverActivity, job.id, job.sourceUrl); Toast.makeText(this@ShareReceiverActivity, "Download started", Toast.LENGTH_SHORT).show(); finish() } }
}
