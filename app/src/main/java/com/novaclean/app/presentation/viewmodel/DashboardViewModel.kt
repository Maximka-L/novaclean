package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.RamInfo
import com.novaclean.app.domain.usecase.GetRamInfoUseCase
import com.novaclean.app.domain.usecase.ObserveProStatusUseCase
import com.novaclean.app.domain.usecase.OptimizeRamUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getRamInfoUseCase: GetRamInfoUseCase,
    private val optimizeRamUseCase: OptimizeRamUseCase,
    observeProStatusUseCase: ObserveProStatusUseCase
) : ViewModel() {

    val isProUser: StateFlow<Boolean> = observeProStatusUseCase()

    private val _ramInfo = MutableStateFlow(getRamInfoUseCase())
    val ramInfo: StateFlow<RamInfo> = _ramInfo.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    private val _freedBytesEvent = MutableSharedFlow<Long>()
    val freedBytesEvent: SharedFlow<Long> = _freedBytesEvent.asSharedFlow()

    fun refreshRamInfo() {
        _ramInfo.value = getRamInfoUseCase()
    }

    fun optimizeRam() {
        if (_isOptimizing.value) return
        viewModelScope.launch {
            _isOptimizing.value = true
            val freed = optimizeRamUseCase()
            _ramInfo.value = getRamInfoUseCase()
            _isOptimizing.value = false
            _freedBytesEvent.emit(freed)
        }
    }
}
