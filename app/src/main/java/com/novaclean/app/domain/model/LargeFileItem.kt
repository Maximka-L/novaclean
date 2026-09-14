package com.novaclean.app.domain.model

enum class LargeFileCategory {
    ALL,
    VIDEO,
    AUDIO,
    DOCUMENT,
    ARCHIVE,
    OTHER
}

enum class SizeThreshold(val bytes: Long, val label: String) {
    SIZE_50MB(50L * 1024 * 1024, "> 50 МБ"),
    SIZE_100MB(100L * 1024 * 1024, "> 100 МБ"),
    SIZE_500MB(500L * 1024 * 1024, "> 500 МБ"),
    SIZE_1GB(1024L * 1024 * 1024, "> 1 ГБ")
}

data class LargeFileItem(
    val id: Long,
    val name: String,
    val path: String,
    val uriString: String,
    val sizeBytes: Long,
    val mimeType: String,
    val category: LargeFileCategory,
    val dateModified: Long,
    val isSelected: Boolean = false
)
