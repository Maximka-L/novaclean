package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.JunkItem
import com.novaclean.app.domain.model.JunkScanResult

interface JunkRepository {
    suspend fun scanJunk(): JunkScanResult
    suspend fun cleanJunk(items: List<JunkItem>): Pair<Int, Long>
}
