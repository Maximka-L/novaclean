package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.CompressedResult
import com.novaclean.app.domain.model.CompressionPreset
import com.novaclean.app.domain.repository.BillingRepository
import com.novaclean.app.domain.repository.CompressorRepository

class CompressMediaUseCase(
    private val compressorRepository: CompressorRepository,
    private val billingRepository: BillingRepository
) {
    fun canCompress(): Boolean {
        if (billingRepository.isProUser.value) return true
        return compressorRepository.getRemainingFreeCompressions() > 0
    }

    fun getRemainingFree(): Int = compressorRepository.getRemainingFreeCompressions()

    fun getFileSize(uriString: String): Long = compressorRepository.getFileSizeBytes(uriString)

    suspend fun execute(uriString: String, preset: CompressionPreset): Result<CompressedResult> {
        val isPro = billingRepository.isProUser.value
        if (!isPro && compressorRepository.getRemainingFreeCompressions() <= 0) {
            return Result.failure(IllegalStateException("LIMIT_REACHED"))
        }

        val result = compressorRepository.compressImage(uriString, preset)
        if (result.isSuccess && !isPro) {
            compressorRepository.decrementFreeCompressions()
        }
        return result
    }
}
