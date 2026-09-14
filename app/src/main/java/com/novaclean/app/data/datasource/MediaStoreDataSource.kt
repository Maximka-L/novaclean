package com.novaclean.app.data.datasource

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.DuplicateType
import com.novaclean.app.domain.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class MediaStoreDataSource(private val context: Context) {

    suspend fun queryImages(): List<MediaFile> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATA
        )
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val dataCol = cursor.getColumnIndex(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val name = cursor.getString(nameCol) ?: "Image_$id"
                val size = cursor.getLong(sizeCol)
                val mime = cursor.getString(mimeCol)
                val date = cursor.getLong(dateCol)
                val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""

                list.add(
                    MediaFile(
                        id = id,
                        uri = uri,
                        path = path,
                        displayName = name,
                        size = size,
                        mimeType = mime,
                        dateModified = date
                    )
                )
            }
        }
        list
    }

    suspend fun queryAudio(): List<MediaFile> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val name = cursor.getString(nameCol) ?: "Audio_$id"
                val size = cursor.getLong(sizeCol)
                val mime = cursor.getString(mimeCol)
                val date = cursor.getLong(dateCol)

                list.add(
                    MediaFile(
                        id = id,
                        uri = uri,
                        path = "",
                        displayName = name,
                        size = size,
                        mimeType = mime,
                        dateModified = date
                    )
                )
            }
        }
        list
    }

    fun calculateFastHash(contentResolver: ContentResolver, uri: Uri, fileSize: Long): String {
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val buffer = ByteArray(64 * 1024)
                val bytesRead = stream.read(buffer)
                if (bytesRead > 0) {
                    val md = MessageDigest.getInstance("MD5")
                    md.update(buffer, 0, bytesRead)
                    md.update(fileSize.toString().toByteArray())
                    val sb = StringBuilder()
                    for (b in md.digest()) {
                        sb.append(String.format("%02x", b))
                    }
                    sb.toString()
                } else {
                    fileSize.toString()
                }
            } ?: fileSize.toString()
        } catch (e: Exception) {
            fileSize.toString()
        }
    }

    suspend fun deleteFiles(files: List<MediaFile>): Int = withContext(Dispatchers.IO) {
        var count = 0
        val resolver = context.contentResolver
        for (file in files) {
            try {
                val deleted = resolver.delete(file.uri, null, null)
                if (deleted > 0) count++
            } catch (e: Exception) {
                // Ignore
            }
        }
        count
    }
}
