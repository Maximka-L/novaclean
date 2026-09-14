package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.CompressedResult
import com.novaclean.app.domain.model.CompressionPreset

interface CompressorRepository {
    suspend fun compressImage(uriString: String, preset: CompressionPreset): Result<CompressedResult>
    fun getRemainingFreeCompressions(): Int
    fun decrementFreeCompressions()
    fun getFileSizeBytes(uriString: String): Long
}
