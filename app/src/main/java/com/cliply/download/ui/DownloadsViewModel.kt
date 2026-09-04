package com.cliply.download.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cliply.domain.model.DownloadJob
import com.cliply.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DownloadsViewModel @Inject constructor(repository: DownloadRepository) : ViewModel() {
    val jobs: StateFlow<List<DownloadJob>> = repository.history().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
