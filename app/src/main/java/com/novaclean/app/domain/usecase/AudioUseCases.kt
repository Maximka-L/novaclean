package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.MediaFile
import com.novaclean.app.domain.repository.AudioRepository

class GetDuplicateAudioUseCase(private val repository: AudioRepository) {
    suspend operator fun invoke(): List<DuplicateGroup> {
        return repository.getDuplicateAudio()
    }
}

class DeleteAudioUseCase(private val repository: AudioRepository) {
    suspend operator fun invoke(files: List<MediaFile>): Int {
        return repository.deleteAudio(files)
    }
}
