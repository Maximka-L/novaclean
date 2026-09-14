package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.LargeFileItem

interface LargeFilesRepository {
    suspend fun getLargeFiles(minSizeBytes: Long): List<LargeFileItem>
    suspend fun deleteFiles(files: List<LargeFileItem>): Result<Int>
}
