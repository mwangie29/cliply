package com.cliply.domain.usecase

import com.cliply.domain.model.DownloadJob
import com.cliply.domain.repository.DownloadRepository

class CreateDownloadJobUseCase(private val repository: DownloadRepository) { suspend operator fun invoke(url: String): DownloadJob { val job = DownloadJob(sourceUrl = url); repository.insert(job); return job } }
