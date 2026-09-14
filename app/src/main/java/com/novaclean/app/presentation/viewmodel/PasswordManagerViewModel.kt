package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.PasswordCategory
import com.novaclean.app.domain.model.SavedPassword
import com.novaclean.app.domain.usecase.DeletePasswordUseCase
import com.novaclean.app.domain.usecase.GetSavedPasswordsUseCase
import com.novaclean.app.domain.usecase.SavePasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PasswordManagerUiState(
    val query: String = "",
    val activeCategory: PasswordCategory = PasswordCategory.ALL,
    val passwords: List<SavedPassword> = emptyList(),
    val visiblePasswordIds: Set<String> = emptySet(),
    val showAddDialog: Boolean = false,
    val prefilledPassword: String = "",
    val isLoading: Boolean = false
)

class PasswordManagerViewModel(
    private val getSavedPasswordsUseCase: GetSavedPasswordsUseCase,
    private val savePasswordUseCase: SavePasswordUseCase,
    private val deletePasswordUseCase: DeletePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordManagerUiState())
    val uiState: StateFlow<PasswordManagerUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        loadPasswords()
    }

    fun loadPasswords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val list = getSavedPasswordsUseCase.execute(
                query = _uiState.value.query,
                category = _uiState.value.activeCategory
            )
            _uiState.update { it.copy(passwords = list, isLoading = false) }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        loadPasswords()
    }

    fun onCategorySelected(category: PasswordCategory) {
        if (_uiState.value.activeCategory != category) {
            _uiState.update { it.copy(activeCategory = category) }
            loadPasswords()
        }
    }

    fun toggleVisibility(id: String) {
        _uiState.update { state ->
            val set = if (state.visiblePasswordIds.contains(id)) {
                state.visiblePasswordIds - id
            } else {
                state.visiblePasswordIds + id
            }
            state.copy(visiblePasswordIds = set)
        }
    }

    fun openAddDialog(prefilledPass: String = "") {
        _uiState.update {
            it.copy(showAddDialog = true, prefilledPassword = prefilledPass)
        }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, prefilledPassword = "") }
    }

    fun savePassword(
        serviceName: String,
        login: String,
        password: String,
        category: PasswordCategory,
        notes: String?
    ) {
        viewModelScope.launch {
            val item = SavedPassword(
                id = UUID.randomUUID().toString(),
                serviceName = serviceName.trim(),
                login = login.trim(),
                password = password.trim(),
                category = category,
                createdAt = System.currentTimeMillis(),
                notes = notes?.trim()?.ifEmpty { null }
            )
            val result = savePasswordUseCase.execute(item)
            if (result.isSuccess) {
                dismissAddDialog()
                loadPasswords()
                _toastEvent.emit("SAVED")
            }
        }
    }

    fun deletePassword(id: String) {
        viewModelScope.launch {
            deletePasswordUseCase.execute(id)
            loadPasswords()
            _toastEvent.emit("DELETED")
        }
    }
}
