package com.cliply.domain.model

import com.cliply.download.engine.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.toList
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TransferManagerTest {
    @Test fun progressAndCompletionAreObservable() = runBlocking { val fake = FakeTransfer(); val values = fake.transfer("https://example.test/video.mp4", File("job.part")).toList(); assertEquals(2, values.size); assertEquals(50L, values[0].bytesDownloaded); assertTrue(values[1].completed) }
    @Test fun cancellationIsPropagated() { val fake = FakeTransfer(); fake.cancel("job"); assertEquals("job", fake.cancelled) }
    private class FakeTransfer : DownloadTransferManager { var cancelled: String? = null; override fun transfer(url: String, destination: File) = flow { emit(TransferProgress(50, 100, 50)); emit(TransferProgress(100, 100, 50, completed = true)) }; override fun cancel(jobId: String) { cancelled = jobId } }
}
