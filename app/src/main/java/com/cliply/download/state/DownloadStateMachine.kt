package com.cliply.download.state

import com.cliply.domain.model.DownloadStatus

class InvalidDownloadTransition(from: DownloadStatus, to: DownloadStatus) : IllegalStateException("Invalid download transition: $from -> $to")

object DownloadStateMachine {
    private val transitions = mapOf(
        DownloadStatus.CREATED to setOf(DownloadStatus.RESOLVING),
        DownloadStatus.RESOLVING to setOf(DownloadStatus.RESOLVED, DownloadStatus.FAILED),
        DownloadStatus.RESOLVED to setOf(DownloadStatus.QUEUED),
        DownloadStatus.QUEUED to setOf(DownloadStatus.DOWNLOADING),
        DownloadStatus.DOWNLOADING to setOf(DownloadStatus.COMPLETED, DownloadStatus.FAILED, DownloadStatus.CANCELLED, DownloadStatus.RETRYING),
        DownloadStatus.RETRYING to setOf(DownloadStatus.DOWNLOADING),
        DownloadStatus.FAILED to setOf(DownloadStatus.RETRYING),
        DownloadStatus.COMPLETED to emptySet(),
        DownloadStatus.CANCELLED to emptySet(),
    )
    fun canTransition(from: DownloadStatus, to: DownloadStatus): Boolean = transitions[from].orEmpty().contains(to)
    fun transition(from: DownloadStatus, to: DownloadStatus): DownloadStatus { if (!canTransition(from, to)) throw InvalidDownloadTransition(from, to); return to }
}
