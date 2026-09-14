package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.RamInfo
import com.novaclean.app.domain.repository.MemoryRepository

class GetRamInfoUseCase(private val repository: MemoryRepository) {
    operator fun invoke(): RamInfo {
        return repository.getRamInfo()
    }
}

class OptimizeRamUseCase(private val repository: MemoryRepository) {
    suspend operator fun invoke(): Long {
        return repository.optimizeRam()
    }
}
