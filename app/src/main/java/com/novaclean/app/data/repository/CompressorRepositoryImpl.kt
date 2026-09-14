package com.novaclean.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.novaclean.app.domain.model.CompressedResult
import com.novaclean.app.domain.model.CompressionPreset
import com.novaclean.app.domain.repository.CompressorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CompressorRepositoryImpl(private val context: Context) : CompressorRepository {

    private val prefs = context.getSharedPreferences("novaclean_compressor_prefs", Context.MODE_PRIVATE)

    override fun getRemainingFreeCompressions(): Int {
        return prefs.getInt(KEY_FREE_COMPRESSIONS, DEFAULT_FREE_LIMIT)
    }

    override fun decrementFreeCompressions() {
        val current = getRemainingFreeCompressions()
        if (current > 0) {
            prefs.edit().putInt(KEY_FREE_COMPRESSIONS, current - 1).apply()
        }
    }

    override fun getFileSizeBytes(uriString: String): Long {
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun compressImage(
        uriString: String,
        preset: CompressionPreset
    ): Result<CompressedResult> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val originalSize = getFileSizeBytes(uriString).let {
                if (it > 0) it else 1024 * 1024L
            }

            // 1. Measure dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            // 2. Compute sample size to avoid OOM
            var sampleSize = 1
            val maxDimension = 2560
            while ((options.outWidth / sampleSize) > maxDimension || (options.outHeight / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            // 3. Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val originalBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Cannot decode image"))

            // 4. Apply preset scaling if required
            val scaledBitmap = if (preset.scaleFactor < 1.0f) {
                val targetW = (originalBitmap.width * preset.scaleFactor).toInt().coerceAtLeast(100)
                val targetH = (originalBitmap.height * preset.scaleFactor).toInt().coerceAtLeast(100)
                val scaled = Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
                if (scaled != originalBitmap) originalBitmap.recycle()
                scaled
            } else {
                originalBitmap
            }

            // 5. Compress to output file
            val outputDir = File(context.cacheDir, "compressed_media").apply { mkdirs() }
            val outputFile = File(outputDir, "compressed_${System.currentTimeMillis()}.jpg")

            FileOutputStream(outputFile).use { fos ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, preset.quality, fos)
            }
            scaledBitmap.recycle()

            val compressedSize = outputFile.length()
            val savedBytes = (originalSize - compressedSize).coerceAtLeast(0L)
            val savedPercent = if (originalSize > 0) {
                ((savedBytes.toDouble() / originalSize.toDouble()) * 100).toInt().coerceIn(0, 99)
            } else 0

            Result.success(
                CompressedResult(
                    originalSizeBytes = originalSize,
                    compressedSizeBytes = compressedSize,
                    savedBytes = savedBytes,
                    savedPercent = savedPercent,
                    outputUri = Uri.fromFile(outputFile).toString()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val KEY_FREE_COMPRESSIONS = "remaining_free_compressions"
        private const val DEFAULT_FREE_LIMIT = 2
    }
}
