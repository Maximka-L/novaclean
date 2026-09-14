package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.MediaFile

interface PhotoRepository {
    suspend fun getDuplicatePhotos(onProgress: (scanned: Int, total: Int) -> Unit = { _, _ -> }): List<DuplicateGroup>
    suspend fun deletePhotos(files: List<MediaFile>): Int
}
