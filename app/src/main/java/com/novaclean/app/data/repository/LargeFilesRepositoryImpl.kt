package com.novaclean.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.novaclean.app.domain.model.LargeFileCategory
import com.novaclean.app.domain.model.LargeFileItem
import com.novaclean.app.domain.repository.LargeFilesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LargeFilesRepositoryImpl(private val context: Context) : LargeFilesRepository {

    override suspend fun getLargeFiles(minSizeBytes: Long): List<LargeFileItem> = withContext(Dispatchers.IO) {
        val files = mutableListOf<LargeFileItem>()
        val collection = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        val selection = "${MediaStore.Files.FileColumns.SIZE} >= ?"
        val selectionArgs = arrayOf(minSizeBytes.toString())
        val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Unnamed"
                    val path = cursor.getString(dataCol) ?: ""
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol) ?: ""
                    val date = cursor.getLong(dateCol)

                    val uri = ContentUris.withAppendedId(collection, id).toString()
                    val category = categorize(mime, name)

                    files.add(
                        LargeFileItem(
                            id = id,
                            name = name,
                            path = path,
                            uriString = uri,
                            sizeBytes = size,
                            mimeType = mime,
                            category = category,
                            dateModified = date
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }

        files
    }

    override suspend fun deleteFiles(files: List<LargeFileItem>): Result<Int> = withContext(Dispatchers.IO) {
        var deletedCount = 0
        try {
            for (item in files) {
                var deleted = false
                try {
                    val uri = Uri.parse(item.uriString)
                    val rows = context.contentResolver.delete(uri, null, null)
                    if (rows > 0) deleted = true
                } catch (e: Exception) {
                    // Fallback to File deletion
                }

                if (!deleted && item.path.isNotEmpty()) {
                    val file = File(item.path)
                    if (file.exists() && file.delete()) {
                        deleted = true
                    }
                }

                if (deleted) deletedCount++
            }
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun categorize(mime: String, name: String): LargeFileCategory {
        val lowerMime = mime.lowercase()
        val lowerName = name.lowercase()

        return when {
            lowerMime.startsWith("video/") || lowerName.endsWith(".mp4") || lowerName.endsWith(".mkv") || lowerName.endsWith(".avi") -> LargeFileCategory.VIDEO
            lowerMime.startsWith("audio/") || lowerName.endsWith(".mp3") || lowerName.endsWith(".flac") || lowerName.endsWith(".wav") -> LargeFileCategory.AUDIO
            lowerMime.contains("pdf") || lowerMime.contains("document") || lowerMime.contains("text") ||
                    lowerName.endsWith(".pdf") || lowerName.endsWith(".docx") || lowerName.endsWith(".xlsx") -> LargeFileCategory.DOCUMENT
            lowerMime.contains("zip") || lowerMime.contains("rar") || lowerMime.contains("tar") || lowerMime.contains("7z") ||
                    lowerName.endsWith(".zip") || lowerName.endsWith(".rar") || lowerName.endsWith(".apk") -> LargeFileCategory.ARCHIVE
            else -> LargeFileCategory.OTHER
        }
    }
}
