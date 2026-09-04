package com.cliply.download.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File

class MediaStorePublisher(private val resolver: ContentResolver) {
    fun publish(source: File, displayName: String): Uri? {
        val values = ContentValues().apply { put(MediaStore.Video.Media.DISPLAY_NAME, displayName); put(MediaStore.Video.Media.MIME_TYPE, "video/mp4"); put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Cliply"); if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Video.Media.IS_PENDING, 1) }
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        return try { resolver.openOutputStream(uri)?.use { output -> source.inputStream().use { it.copyTo(output) } }; if (Build.VERSION.SDK_INT >= 29) resolver.update(uri, ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }, null, null); source.delete(); uri } catch (t: Throwable) { resolver.delete(uri, null, null); throw t }
    }
}
