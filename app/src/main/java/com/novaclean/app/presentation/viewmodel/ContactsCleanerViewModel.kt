package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.DuplicateContactGroup
import com.novaclean.app.domain.usecase.DeleteContactUseCase
import com.novaclean.app.domain.usecase.GetDuplicateContactsUseCase
import com.novaclean.app.domain.usecase.MergeContactsUseCase
import com.novaclean.app.domain.usecase.ObserveProStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactsCleanerViewModel(
    private val getDuplicateContactsUseCase: GetDuplicateContactsUseCase,
    private val mergeContactsUseCase: MergeContactsUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    observeProStatusUseCase: ObserveProStatusUseCase
) : ViewModel() {

    val isProUser: StateFlow<Boolean> = observeProStatusUseCase()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _duplicateGroups = MutableStateFlow<List<DuplicateContactGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateContactGroup>> = _duplicateGroups.asStateFlow()

    private val _navigateToPaywallEvent = MutableSharedFlow<Unit>()
    val navigateToPaywallEvent: SharedFlow<Unit> = _navigateToPaywallEvent.asSharedFlow()

    init {
        scanContacts()
    }

    fun scanContacts() {
        viewModelScope.launch {
            _isScanning.value = true
            _duplicateGroups.value = getDuplicateContactsUseCase()
            _isScanning.value = false
        }
    }

    fun mergeAllContacts() {
        if (!isProUser.value) {
            viewModelScope.launch { _navigateToPaywallEvent.emit(Unit) }
            return
        }

        viewModelScope.launch {
            mergeContactsUseCase(_duplicateGroups.value)
            scanContacts()
        }
    }

    fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            deleteContactUseCase(contactId)
            scanContacts()
        }
    }
}
