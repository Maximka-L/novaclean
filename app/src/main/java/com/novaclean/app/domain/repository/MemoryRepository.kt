package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.RamInfo

interface MemoryRepository {
    fun getRamInfo(): RamInfo
    suspend fun optimizeRam(): Long
}
