package com.novaclean.app.domain.model

enum class CompressionPreset(val quality: Int, val scaleFactor: Float, val estimatedSavingsPercent: Int) {
    HIGH_SAVINGS(50, 0.75f, 75),
    BALANCED(70, 0.9f, 50),
    LOW_COMPRESSION(85, 1.0f, 25)
}

data class CompressionConfig(
    val preset: CompressionPreset = CompressionPreset.BALANCED
)

data class CompressedResult(
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val savedBytes: Long,
    val savedPercent: Int,
    val outputUri: String
)
