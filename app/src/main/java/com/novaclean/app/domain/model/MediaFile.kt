package com.novaclean.app.domain.model

import android.net.Uri
import java.util.Locale

data class MediaFile(
    val id: Long,
    val uri: Uri,
    val path: String,
    val displayName: String,
    val size: Long,
    val mimeType: String?,
    val dateModified: Long,
    val hash: String = "",
    val isSelected: Boolean = false,
    val isOriginal: Boolean = false
) {
    val formattedSize: String
        get() = formatFileSize(size)
}

data class DuplicateGroup(
    val id: String,
    val original: MediaFile,
    val duplicates: List<MediaFile>,
    val type: DuplicateType = DuplicateType.EXACT,
    val isExpanded: Boolean = true
) {
    val totalDuplicatesSize: Long
        get() = duplicates.sumOf { it.size }

    val formattedReclaimableSize: String
        get() = formatFileSize(totalDuplicatesSize)
}

enum class DuplicateType {
    EXACT,
    SIMILAR_BURST
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val clampedGroup = digitGroups.coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, clampedGroup.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[clampedGroup])
}
