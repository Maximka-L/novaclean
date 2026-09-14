package com.novaclean.app.data.repository

import com.novaclean.app.data.datasource.FileSystemDataSource
import com.novaclean.app.domain.model.JunkItem
import com.novaclean.app.domain.model.JunkScanResult
import com.novaclean.app.domain.repository.JunkRepository

class JunkRepositoryImpl(
    private val dataSource: FileSystemDataSource
) : JunkRepository {

    override suspend fun scanJunk(): JunkScanResult {
        return dataSource.scanJunk()
    }

    override suspend fun cleanJunk(items: List<JunkItem>): Pair<Int, Long> {
        return dataSource.cleanFiles(items)
    }
}
