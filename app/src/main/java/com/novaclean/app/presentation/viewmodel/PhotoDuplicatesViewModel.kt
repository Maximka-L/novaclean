package com.novaclean.app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.DuplicateType
import com.novaclean.app.domain.model.MediaFile
import com.novaclean.app.domain.usecase.CheckBatchCleanAllowedUseCase
import com.novaclean.app.domain.usecase.DeletePhotosUseCase
import com.novaclean.app.domain.usecase.GetDuplicatePhotosUseCase
import com.novaclean.app.domain.usecase.ObserveProStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotoDuplicatesViewModel(
    private val getDuplicatePhotosUseCase: GetDuplicatePhotosUseCase,
    private val deletePhotosUseCase: DeletePhotosUseCase,
    observeProStatusUseCase: ObserveProStatusUseCase,
    private val checkBatchCleanAllowedUseCase: CheckBatchCleanAllowedUseCase
) : ViewModel() {

    val isProUser: StateFlow<Boolean> = observeProStatusUseCase()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val scanProgress: StateFlow<Pair<Int, Int>?> = _scanProgress.asStateFlow()

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Exact, 1: Similar
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedUris: StateFlow<Set<Uri>> = _selectedUris.asStateFlow()

    private val _navigateToPaywallEvent = MutableSharedFlow<Unit>()
    val navigateToPaywallEvent: SharedFlow<Unit> = _navigateToPaywallEvent.asSharedFlow()

    init {
        scanPhotos()
    }

    fun scanPhotos() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = null
            val groups = getDuplicatePhotosUseCase { scanned, total ->
                _scanProgress.value = Pair(scanned, total)
            }
            _duplicateGroups.value = groups
            // По умолчанию выделяем дубликаты
            val initialSelected = groups.flatMap { it.duplicates }.map { it.uri }.toSet()
            _selectedUris.value = initialSelected
            _isScanning.value = false
        }
    }

    fun selectTab(index: Int) {
        if (index == 1 && !isProUser.value) {
            viewModelScope.launch { _navigateToPaywallEvent.emit(Unit) }
        } else {
            _selectedTab.value = index
        }
    }

    fun toggleUriSelection(uri: Uri) {
        val current = _selectedUris.value.toMutableSet()
        if (uri in current) current.remove(uri) else current.add(uri)
        _selectedUris.value = current
    }

    fun deleteSelectedPhotos() {
        val currentType = if (_selectedTab.value == 0) DuplicateType.EXACT else DuplicateType.SIMILAR_BURST
        val candidates = _duplicateGroups.value.filter { it.type == currentType }
            .flatMap { it.duplicates }
            .filter { it.uri in _selectedUris.value }

        if (candidates.isEmpty()) return

        if (!checkBatchCleanAllowedUseCase(candidates.size)) {
            viewModelScope.launch { _navigateToPaywallEvent.emit(Unit) }
            return
        }

        viewModelScope.launch {
            deletePhotosUseCase(candidates)
            scanPhotos()
        }
    }
}
