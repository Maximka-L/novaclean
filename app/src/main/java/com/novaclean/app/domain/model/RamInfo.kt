package com.novaclean.app.domain.model

data class RamInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Int,
    val isLowMemory: Boolean
) {
    val formattedTotal: String
        get() = formatFileSize(totalBytes)
    val formattedAvailable: String
        get() = formatFileSize(availableBytes)
    val formattedUsed: String
        get() = formatFileSize(usedBytes)
}
