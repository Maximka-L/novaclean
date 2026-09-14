package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.JunkCategory
import com.novaclean.app.domain.model.JunkScanResult
import com.novaclean.app.domain.usecase.CleanJunkUseCase
import com.novaclean.app.domain.usecase.ScanJunkUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JunkCleanerViewModel(
    private val scanJunkUseCase: ScanJunkUseCase,
    private val cleanJunkUseCase: CleanJunkUseCase
) : ViewModel() {

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<JunkScanResult?>(null)
    val scanResult: StateFlow<JunkScanResult?> = _scanResult.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Map<JunkCategory, Boolean>>(
        JunkCategory.values().associateWith { true }
    )
    val selectedCategories: StateFlow<Map<JunkCategory, Boolean>> = _selectedCategories.asStateFlow()

    private val _cleanSuccessEvent = MutableSharedFlow<Pair<Int, Long>>()
    val cleanSuccessEvent: SharedFlow<Pair<Int, Long>> = _cleanSuccessEvent.asSharedFlow()

    init {
        scanJunk()
    }

    fun scanJunk() {
        viewModelScope.launch {
            _isScanning.value = true
            val result = scanJunkUseCase()
            _scanResult.value = result
            _selectedCategories.value = JunkCategory.values().associateWith { true }
            _isScanning.value = false
        }
    }

    fun toggleCategory(category: JunkCategory) {
        val current = _selectedCategories.value.toMutableMap()
        val isChecked = current[category] ?: true
        current[category] = !isChecked
        _selectedCategories.value = current
    }

    fun cleanJunk() {
        val result = _scanResult.value ?: return
        val activeCategories = _selectedCategories.value
        val itemsToClean = result.items.filter { activeCategories[it.category] == true }
        if (itemsToClean.isEmpty()) return

        viewModelScope.launch {
            val stats = cleanJunkUseCase(itemsToClean)
            _cleanSuccessEvent.emit(stats)
            scanJunk()
        }
    }
}
