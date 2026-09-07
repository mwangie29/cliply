package com.cliply.download.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cliply.domain.usecase.CreateDownloadJobUseCase
import com.cliply.download.scheduler.TransferExecutor
import com.cliply.download.resolver.MediaResolverRegistry
import com.cliply.share.UrlValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(private val createDownloadJob: CreateDownloadJobUseCase, private val executor: TransferExecutor, private val resolverRegistry: MediaResolverRegistry, @ApplicationContext private val context: android.content.Context) : ViewModel() {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    private val validator = UrlValidator()
    fun startDownload(rawUrl: String) { val result = validator.validate(rawUrl); val normalized = result.normalizedUrl; if (!result.isValid || normalized == null) { _message.value = "Enter a valid HTTPS media URL"; return }; if (!resolverRegistry.canResolve(normalized)) { _message.value = "This type of link isn't supported yet."; return }; viewModelScope.launch { val job = createDownloadJob(normalized.toString()); executor.start(context, job.id, job.sourceUrl); _message.value = "Download started" } }
    fun clearMessage() { _message.value = null }
}
