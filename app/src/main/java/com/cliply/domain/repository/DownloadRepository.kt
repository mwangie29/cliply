package com.cliply.domain.repository

import com.cliply.domain.model.DownloadJob
import kotlinx.coroutines.flow.Flow

interface DownloadRepository { suspend fun insert(job: DownloadJob); suspend fun get(id: String): DownloadJob?; fun observe(id: String): Flow<DownloadJob?>; fun history(): Flow<List<DownloadJob>>; suspend fun update(job: DownloadJob); suspend fun delete(id: String) }
