package com.novaclean.app.data.repository

import com.novaclean.app.data.datasource.MemoryDataSource
import com.novaclean.app.domain.model.RamInfo
import com.novaclean.app.domain.repository.MemoryRepository

class MemoryRepositoryImpl(
    private val dataSource: MemoryDataSource
) : MemoryRepository {

    override fun getRamInfo(): RamInfo {
        return dataSource.getRamInfo()
    }

    override suspend fun optimizeRam(): Long {
        return dataSource.optimizeRam()
    }
}
