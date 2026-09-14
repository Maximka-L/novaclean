package com.novaclean.app.data.repository

import android.content.Context
import com.novaclean.app.data.datasource.MediaStoreDataSource
import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.DuplicateType
import com.novaclean.app.domain.model.MediaFile
import com.novaclean.app.domain.repository.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioRepositoryImpl(
    private val context: Context,
    private val dataSource: MediaStoreDataSource
) : AudioRepository {

    override suspend fun getDuplicateAudio(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val audioFiles = dataSource.queryAudio()
        val sizeBuckets = audioFiles.groupBy { it.size }.filter { it.value.size > 1 && it.key > 0 }

        val groups = mutableListOf<DuplicateGroup>()
        for ((_, candidateFiles) in sizeBuckets) {
            val hashedFiles = candidateFiles.map { file ->
                val hash = dataSource.calculateFastHash(context.contentResolver, file.uri, file.size)
                file.copy(hash = hash)
            }

            val hashGroups = hashedFiles.groupBy { it.hash }.filter { it.value.size > 1 }
            for ((hash, duplicatesList) in hashGroups) {
                val sorted = duplicatesList.sortedBy { it.dateModified }
                val original = sorted.first().copy(isOriginal = true, isSelected = false)
                val dupes = sorted.drop(1).map { it.copy(isOriginal = false, isSelected = true) }
                groups.add(
                    DuplicateGroup(
                        id = "audio_$hash",
                        original = original,
                        duplicates = dupes,
                        type = DuplicateType.EXACT
                    )
                )
            }
        }
        groups
    }

    override suspend fun deleteAudio(files: List<MediaFile>): Int {
        return dataSource.deleteFiles(files)
    }
}
