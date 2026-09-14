package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.JunkItem
import com.novaclean.app.domain.model.JunkScanResult
import com.novaclean.app.domain.repository.JunkRepository

class ScanJunkUseCase(private val repository: JunkRepository) {
    suspend operator fun invoke(): JunkScanResult {
        return repository.scanJunk()
    }
}

class CleanJunkUseCase(private val repository: JunkRepository) {
    suspend operator fun invoke(items: List<JunkItem>): Pair<Int, Long> {
        return repository.cleanJunk(items)
    }
}
