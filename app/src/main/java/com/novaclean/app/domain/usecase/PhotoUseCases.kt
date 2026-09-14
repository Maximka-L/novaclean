package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.MediaFile
import com.novaclean.app.domain.repository.PhotoRepository

class GetDuplicatePhotosUseCase(private val repository: PhotoRepository) {
    suspend operator fun invoke(onProgress: (scanned: Int, total: Int) -> Unit = { _, _ -> }): List<DuplicateGroup> {
        return repository.getDuplicatePhotos(onProgress)
    }
}

class DeletePhotosUseCase(private val repository: PhotoRepository) {
    suspend operator fun invoke(files: List<MediaFile>): Int {
        return repository.deletePhotos(files)
    }
}
