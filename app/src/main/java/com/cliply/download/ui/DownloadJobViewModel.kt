package com.cliply.download.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cliply.domain.model.DownloadJob
import com.cliply.domain.repository.DownloadRepository
import com.cliply.download.scheduler.TransferExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DownloadJobViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: DownloadRepository,
    private val executor: TransferExecutor,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val jobId: String = checkNotNull(savedStateHandle["jobId"])
    val job: StateFlow<DownloadJob?> = repository.observe(jobId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun cancel() { executor.cancel(context, jobId) }
}
