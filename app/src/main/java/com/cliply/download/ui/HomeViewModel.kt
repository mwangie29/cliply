package com.cliply.download.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cliply.domain.usecase.CreateDownloadJobUseCase
import com.cliply.download.scheduler.TransferExecutor
import com.cliply.share.UrlValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(private val createDownloadJob: CreateDownloadJobUseCase, private val executor: TransferExecutor, @ApplicationContext private val context: android.content.Context) : ViewModel() {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    private val validator = UrlValidator()
    fun startDownload(rawUrl: String) { val result = validator.validate(rawUrl); if (!result.isValid || result.normalizedUrl == null) { _message.value = "Enter a supported HTTPS test URL"; return }; viewModelScope.launch { val job = createDownloadJob(result.normalizedUrl.toString()); executor.start(context, job.id, job.sourceUrl); _message.value = "Download started" } }
    fun clearMessage() { _message.value = null }
}
