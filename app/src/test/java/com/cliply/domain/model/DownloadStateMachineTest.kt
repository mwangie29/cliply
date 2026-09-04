package com.cliply.domain.model

import com.cliply.download.state.DownloadStateMachine
import org.junit.Assert.*
import org.junit.Test

class DownloadStateMachineTest {
    @Test fun validTransitionsAreAccepted() { assertTrue(DownloadStateMachine.canTransition(DownloadStatus.CREATED, DownloadStatus.RESOLVING)); assertTrue(DownloadStateMachine.canTransition(DownloadStatus.DOWNLOADING, DownloadStatus.COMPLETED)); assertTrue(DownloadStateMachine.canTransition(DownloadStatus.FAILED, DownloadStatus.RETRYING)) }
    @Test fun invalidTransitionsAreRejected() { assertFalse(DownloadStateMachine.canTransition(DownloadStatus.COMPLETED, DownloadStatus.DOWNLOADING)); assertFalse(DownloadStateMachine.canTransition(DownloadStatus.CANCELLED, DownloadStatus.DOWNLOADING)) }
    @Test(expected = IllegalStateException::class) fun invalidTransitionThrows() { DownloadStateMachine.transition(DownloadStatus.COMPLETED, DownloadStatus.DOWNLOADING) }
}
