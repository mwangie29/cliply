package com.cliply.platform.registry

import android.net.Uri
import com.cliply.domain.model.ResolveRequest
import com.cliply.domain.model.ResolveResult

interface PlatformAdapter { fun canHandle(url: Uri): Boolean; suspend fun resolve(request: ResolveRequest): ResolveResult }
class PlatformAdapterRegistry(private val adapters: List<PlatformAdapter> = emptyList()) { fun adapterFor(url: Uri): PlatformAdapter? = adapters.firstOrNull { it.canHandle(url) } }
