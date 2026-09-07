package com.cliply.download.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File

class MediaStorePublisher(private val resolver: ContentResolver) {
    fun publish(source: File, displayName: String, mimeType: String = "video/mp4"): Uri? {
        require(mimeType.startsWith("video/")) { "Cliply only publishes video media" }
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Cliply")
            if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        return try {
            val output = resolver.openOutputStream(uri) ?: throw IllegalStateException("Unable to open MediaStore output")
            output.use { destination -> source.inputStream().use { input -> input.copyTo(destination) } }
            if (Build.VERSION.SDK_INT >= 29) resolver.update(uri, ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }, null, null)
            source.delete()
            uri
        } catch (t: Throwable) {
            resolver.delete(uri, null, null)
            throw t
        }
    }
}
