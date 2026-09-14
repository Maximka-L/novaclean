package com.novaclean.app.domain.model

enum class JunkCategory {
    ZERO_BYTE_FILES,
    EMPTY_FOLDERS,
    OLD_APKS,
    TEMP_FILES,
    APP_CACHE
}

data class JunkItem(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val category: JunkCategory,
    val isSelected: Boolean = true
) {
    val formattedSize: String
        get() = formatFileSize(size)
}

data class JunkScanResult(
    val items: List<JunkItem>,
    val totalSize: Long,
    val categoryCount: Map<JunkCategory, Int>,
    val categorySizes: Map<JunkCategory, Long>
)
