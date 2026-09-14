package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.LargeFileCategory
import com.novaclean.app.domain.model.LargeFileItem
import com.novaclean.app.domain.repository.LargeFilesRepository

class ScanLargeFilesUseCase(
    private val repository: LargeFilesRepository
) {
    suspend fun execute(
        minSizeBytes: Long,
        category: LargeFileCategory = LargeFileCategory.ALL
    ): List<LargeFileItem> {
        val files = repository.getLargeFiles(minSizeBytes)
        return if (category == LargeFileCategory.ALL) {
            files
        } else {
            files.filter { it.category == category }
        }
    }
}

class DeleteLargeFilesUseCase(
    private val repository: LargeFilesRepository
) {
    suspend fun execute(files: List<LargeFileItem>): Result<Int> {
        return repository.deleteFiles(files)
    }
}
