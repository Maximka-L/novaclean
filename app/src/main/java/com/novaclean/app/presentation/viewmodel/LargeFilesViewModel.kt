package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.LargeFileCategory
import com.novaclean.app.domain.model.LargeFileItem
import com.novaclean.app.domain.model.SizeThreshold
import com.novaclean.app.domain.usecase.DeleteLargeFilesUseCase
import com.novaclean.app.domain.usecase.ScanLargeFilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LargeFilesUiState(
    val files: List<LargeFileItem> = emptyList(),
    val activeThreshold: SizeThreshold = SizeThreshold.SIZE_50MB,
    val activeCategory: LargeFileCategory = LargeFileCategory.ALL,
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val deletedMessage: String? = null
) {
    val totalSizeBytes: Long get() = files.sumOf { it.sizeBytes }
    val selectedSizeBytes: Long get() = files.filter { selectedIds.contains(it.id) }.sumOf { it.sizeBytes }
    val selectedCount: Int get() = selectedIds.size
}

class LargeFilesViewModel(
    private val scanUseCase: ScanLargeFilesUseCase,
    private val deleteUseCase: DeleteLargeFilesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LargeFilesUiState())
    val uiState: StateFlow<LargeFilesUiState> = _uiState.asStateFlow()

    init {
        loadFiles()
    }

    fun loadFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val results = scanUseCase.execute(
                minSizeBytes = _uiState.value.activeThreshold.bytes,
                category = _uiState.value.activeCategory
            )
            _uiState.update {
                it.copy(
                    files = results,
                    selectedIds = emptySet(),
                    isLoading = false
                )
            }
        }
    }

    fun setThreshold(threshold: SizeThreshold) {
        if (_uiState.value.activeThreshold != threshold) {
            _uiState.update { it.copy(activeThreshold = threshold) }
            loadFiles()
        }
    }

    fun setCategory(category: LargeFileCategory) {
        if (_uiState.value.activeCategory != category) {
            _uiState.update { it.copy(activeCategory = category) }
            loadFiles()
        }
    }

    fun toggleSelection(id: Long) {
        _uiState.update { state ->
            val updated = if (state.selectedIds.contains(id)) {
                state.selectedIds - id
            } else {
                state.selectedIds + id
            }
            state.copy(selectedIds = updated)
        }
    }

    fun deleteSelected() {
        val selected = _uiState.value.files.filter { _uiState.value.selectedIds.contains(it.id) }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = deleteUseCase.execute(selected)
            result.onSuccess {
                loadFiles()
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
