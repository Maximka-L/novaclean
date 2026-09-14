package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.MediaFile

interface AudioRepository {
    suspend fun getDuplicateAudio(): List<DuplicateGroup>
    suspend fun deleteAudio(files: List<MediaFile>): Int
}
