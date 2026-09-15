package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.CompressedResult
import com.novaclean.app.domain.model.CompressionPreset
import com.novaclean.app.domain.usecase.CompressMediaUseCase
import com.novaclean.app.domain.usecase.ObserveProStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompressorUiState(
    val selectedUri: String? = null,
    val originalSizeBytes: Long = 0L,
    val selectedPreset: CompressionPreset = CompressionPreset.BALANCED,
    val isCompressing: Boolean = false,
    val compressedResult: CompressedResult? = null,
    val remainingFree: Int = 2,
    val isPro: Boolean = false,
    val showLimitDialog: Boolean = false,
    val errorMessage: String? = null
)

class MediaCompressorViewModel(
    private val compressMediaUseCase: CompressMediaUseCase,
    private val observeProStatusUseCase: ObserveProStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompressorUiState())
    val uiState: StateFlow<CompressorUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        val isPro = observeProStatusUseCase().value
        val remaining = compressMediaUseCase.getRemainingFree()
        _uiState.update { it.copy(isPro = isPro, remainingFree = remaining) }
    }

    fun onImageSelected(uriString: String) {
        val size = compressMediaUseCase.getFileSize(uriString)
        _uiState.update {
            it.copy(
                selectedUri = uriString,
                originalSizeBytes = size,
                compressedResult = null,
                errorMessage = null
            )
        }
    }

    fun onPresetSelected(preset: CompressionPreset) {
        _uiState.update { it.copy(selectedPreset = preset) }
    }

    fun dismissLimitDialog() {
        _uiState.update { it.copy(showLimitDialog = false) }
    }

    fun onRewardedAdWatched() {
        compressMediaUseCase.addBonusFree(1)
        refreshStatus()
        _uiState.update { it.copy(showLimitDialog = false) }
    }

    fun compressSelectedImage() {
        val uri = _uiState.value.selectedUri ?: return
        viewModelScope.launch {
            if (!compressMediaUseCase.canCompress()) {
                _uiState.update { it.copy(showLimitDialog = true) }
                return@launch
            }

            _uiState.update { it.copy(isCompressing = true, errorMessage = null) }
            val result = compressMediaUseCase.execute(uri, _uiState.value.selectedPreset)

            result.onSuccess { compressed ->
                val remaining = compressMediaUseCase.getRemainingFree()
                _uiState.update {
                    it.copy(
                        isCompressing = false,
                        compressedResult = compressed,
                        remainingFree = remaining
                    )
                }
            }.onFailure { err ->
                if (err.message == "LIMIT_REACHED") {
                    _uiState.update { it.copy(isCompressing = false, showLimitDialog = true) }
                } else {
                    _uiState.update { it.copy(isCompressing = false, errorMessage = err.localizedMessage) }
                }
            }
        }
    }

    fun reset() {
        _uiState.update {
            it.copy(
                selectedUri = null,
                originalSizeBytes = 0L,
                compressedResult = null,
                errorMessage = null
            )
        }
    }
}
