package com.novaclean.app.data.repository

import android.content.Context
import com.novaclean.app.data.datasource.MediaStoreDataSource
import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.DuplicateType
import com.novaclean.app.domain.model.MediaFile
import com.novaclean.app.domain.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoRepositoryImpl(
    private val context: Context,
    private val dataSource: MediaStoreDataSource
) : PhotoRepository {

    override suspend fun getDuplicatePhotos(
        onProgress: (scanned: Int, total: Int) -> Unit
    ): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val imageFiles = dataSource.queryImages()
        val total = imageFiles.size

        // 1. Точные дубликаты
        val exactGroups = mutableListOf<DuplicateGroup>()
        val sizeBuckets = imageFiles.groupBy { it.size }
            .filter { it.value.size > 1 && it.key > 0 }

        var scanned = 0
        for ((_, candidateFiles) in sizeBuckets) {
            val hashedFiles = candidateFiles.map { file ->
                scanned++
                if (scanned % 10 == 0 || scanned == total) {
                    onProgress(scanned, total)
                }
                val hash = dataSource.calculateFastHash(context.contentResolver, file.uri, file.size)
                file.copy(hash = hash)
            }

            val hashGroups = hashedFiles.groupBy { it.hash }.filter { it.value.size > 1 }
            for ((hash, duplicatesList) in hashGroups) {
                val sorted = duplicatesList.sortedBy { it.dateModified }
                val original = sorted.first().copy(isOriginal = true, isSelected = false)
                val dupes = sorted.drop(1).map { it.copy(isOriginal = false, isSelected = true) }
                exactGroups.add(
                    DuplicateGroup(
                        id = "exact_$hash",
                        original = original,
                        duplicates = dupes,
                        type = DuplicateType.EXACT
                    )
                )
            }
        }

        // 2. Похожие фото
        val similarGroups = mutableListOf<DuplicateGroup>()
        val handledUris = exactGroups.flatMap { g -> listOf(g.original.uri) + g.duplicates.map { it.uri } }.toSet()
        val remainingImages = imageFiles.filterNot { it.uri in handledUris }.sortedBy { it.dateModified }

        var currentGroup = mutableListOf<MediaFile>()
        for (i in 0 until remainingImages.size) {
            val current = remainingImages[i]
            if (currentGroup.isEmpty()) {
                currentGroup.add(current)
            } else {
                val last = currentGroup.last()
                val diffSeconds = Math.abs(current.dateModified - last.dateModified)
                if (diffSeconds <= 3) {
                    currentGroup.add(current)
                } else {
                    if (currentGroup.size > 1) {
                        similarGroups.add(
                            DuplicateGroup(
                                id = "similar_${currentGroup.first().id}",
                                original = currentGroup.first().copy(isOriginal = true, isSelected = false),
                                duplicates = currentGroup.drop(1).map { it.copy(isOriginal = false, isSelected = true) },
                                type = DuplicateType.SIMILAR_BURST
                            )
                        )
                    }
                    currentGroup = mutableListOf(current)
                }
            }
        }
        if (currentGroup.size > 1) {
            similarGroups.add(
                DuplicateGroup(
                    id = "similar_${currentGroup.first().id}",
                    original = currentGroup.first().copy(isOriginal = true, isSelected = false),
                    duplicates = currentGroup.drop(1).map { it.copy(isOriginal = false, isSelected = true) },
                    type = DuplicateType.SIMILAR_BURST
                )
            )
        }

        exactGroups + similarGroups
    }

    override suspend fun deletePhotos(files: List<MediaFile>): Int {
        return dataSource.deleteFiles(files)
    }
}
