package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.VaultFileType
import com.novaclean.app.domain.model.VaultItem
import com.novaclean.app.domain.usecase.ObserveProStatusUseCase
import com.novaclean.app.domain.usecase.VaultUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VaultStep {
    PRO_REQUIRED,
    SETUP_PIN,
    CONFIRM_PIN,
    ENTER_PIN,
    UNLOCKED
}

data class VaultUiState(
    val step: VaultStep = VaultStep.ENTER_PIN,
    val enteredPin: String = "",
    val setupFirstPin: String = "",
    val items: List<VaultItem> = emptyList(),
    val isPro: Boolean = false,
    val activeTab: Int = 0, // 0: Photos, 1: Notes
    val errorMessage: String? = null,
    val showNoteDialog: Boolean = false,
    val isLoading: Boolean = false
) {
    val photoItems: List<VaultItem> get() = items.filter { it.fileType == VaultFileType.PHOTO }
    val noteItems: List<VaultItem> get() = items.filter { it.fileType == VaultFileType.NOTE }
}

class VaultViewModel(
    private val vaultUseCases: VaultUseCases,
    private val observeProStatusUseCase: ObserveProStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        checkAccess()
    }

    fun checkAccess() {
        val isPro = observeProStatusUseCase().value
        val hasPin = vaultUseCases.isPinSet()

            val initialStep = when {
                !isPro -> VaultStep.PRO_REQUIRED
                !hasPin -> VaultStep.SETUP_PIN
                else -> VaultStep.ENTER_PIN
            }

            _uiState.update {
                it.copy(
                    isPro = isPro,
                    step = initialStep,
                    enteredPin = "",
                    setupFirstPin = "",
                    errorMessage = null
                )
            }
    }

    fun onPinDigit(digit: String) {
        val current = _uiState.value.enteredPin
        if (current.length >= 4) return

        val newPin = current + digit
        _uiState.update { it.copy(enteredPin = newPin, errorMessage = null) }

        if (newPin.length == 4) {
            handlePinCompleted(newPin)
        }
    }

    fun onPinBackspace() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = current.dropLast(1), errorMessage = null) }
        }
    }

    private fun handlePinCompleted(pin: String) {
        when (_uiState.value.step) {
            VaultStep.SETUP_PIN -> {
                _uiState.update {
                    it.copy(
                        step = VaultStep.CONFIRM_PIN,
                        setupFirstPin = pin,
                        enteredPin = "",
                        errorMessage = null
                    )
                }
            }
            VaultStep.CONFIRM_PIN -> {
                if (pin == _uiState.value.setupFirstPin) {
                    vaultUseCases.setPin(pin)
                    _uiState.update {
                        it.copy(step = VaultStep.UNLOCKED, enteredPin = "")
                    }
                    loadItems()
                } else {
                    _uiState.update {
                        it.copy(
                            enteredPin = "",
                            errorMessage = "PIN_MISMATCH"
                        )
                    }
                }
            }
            VaultStep.ENTER_PIN -> {
                if (vaultUseCases.verifyPin(pin)) {
                    _uiState.update {
                        it.copy(step = VaultStep.UNLOCKED, enteredPin = "")
                    }
                    loadItems()
                } else {
                    _uiState.update {
                        it.copy(
                            enteredPin = "",
                            errorMessage = "WRONG_PIN"
                        )
                    }
                }
            }
            else -> {}
        }
    }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val items = vaultUseCases.getItems()
            _uiState.update { it.copy(items = items, isLoading = false) }
        }
    }

    fun setTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun addPhoto(uriString: String, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = vaultUseCases.addPhoto(uriString, fileName)
            result.onSuccess {
                loadItems()
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showNoteDialog(show: Boolean) {
        _uiState.update { it.copy(showNoteDialog = show) }
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showNoteDialog = false) }
            vaultUseCases.addNote(title, content)
            loadItems()
        }
    }

    fun deleteItem(item: VaultItem) {
        viewModelScope.launch {
            vaultUseCases.deleteItem(item)
            loadItems()
        }
    }

    fun restoreItem(item: VaultItem) {
        viewModelScope.launch {
            vaultUseCases.restoreItem(item)
            loadItems()
        }
    }

    fun lockVault() {
        _uiState.update {
            it.copy(
                step = if (it.isPro) VaultStep.ENTER_PIN else VaultStep.PRO_REQUIRED,
                enteredPin = ""
            )
        }
    }
}
