package com.cliply.download.engine

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class OkHttpDownloadTransferManager(private val client: OkHttpClient = OkHttpClient()) : DownloadTransferManager {
    private val calls = ConcurrentHashMap<String, okhttp3.Call>()
    override fun transfer(url: String, destination: File): Flow<TransferProgress> = callbackFlow {
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)
        val key = destination.name.removeSuffix(".part")
        calls[key] = call
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { if (isActive) trySend(TransferProgress(0, null, 0, error = e)); close() }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) { trySend(TransferProgress(0, response.body?.contentLength(), 0, error = java.io.IOException("HTTP ${response.code}"))); close(); return }
                val body = response.body ?: run { close(java.io.IOException("Empty response")); return }
                val total = body.contentLength().takeIf { it >= 0 }
                var downloaded = 0L
                val started = System.nanoTime()
                try {
                    body.byteStream().use { input ->
                        destination.outputStream().use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (isActive) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                output.write(buffer, 0, count)
                                downloaded += count
                                val elapsed = (System.nanoTime() - started).coerceAtLeast(1L)
                                trySend(TransferProgress(downloaded, total, downloaded * 1_000_000_000L / elapsed))
                            }
                        }
                    }
                    if (isActive) trySend(TransferProgress(downloaded, total, 0, completed = true))
                    close()
                } catch (t: Throwable) { if (isActive) trySend(TransferProgress(downloaded, total, 0, error = t)); close() } finally { calls.remove(key) }
            }
        })
        awaitClose { call.cancel(); calls.remove(key) }
    }
    override fun cancel(jobId: String) { calls[jobId]?.cancel() }
}
