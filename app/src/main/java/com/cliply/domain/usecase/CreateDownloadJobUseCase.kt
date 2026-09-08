package com.cliply.domain.usecase

import com.cliply.domain.model.DownloadJob
import com.cliply.domain.model.ResolutionSource
import com.cliply.domain.repository.DownloadRepository

class CreateDownloadJobUseCase(private val repository: DownloadRepository) { suspend operator fun invoke(url: String, resolutionSource: ResolutionSource = ResolutionSource.DIRECT_MEDIA, providerResolutionId: String? = null, providerAccountReference: String? = null): DownloadJob { val job = DownloadJob(sourceUrl = url, resolutionSource = resolutionSource, providerResolutionId = providerResolutionId, providerAccountReference = providerAccountReference); repository.insert(job); return job } }
