package com.novaclean.app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.DuplicateGroup
import com.novaclean.app.domain.model.MediaFile
import com.novaclean.app.domain.usecase.CheckBatchCleanAllowedUseCase
import com.novaclean.app.domain.usecase.DeleteAudioUseCase
import com.novaclean.app.domain.usecase.GetDuplicateAudioUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioDuplicatesViewModel(
    private val getDuplicateAudioUseCase: GetDuplicateAudioUseCase,
    private val deleteAudioUseCase: DeleteAudioUseCase,
    private val checkBatchCleanAllowedUseCase: CheckBatchCleanAllowedUseCase
) : ViewModel() {

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _audioGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val audioGroups: StateFlow<List<DuplicateGroup>> = _audioGroups.asStateFlow()

    private val _selectedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedUris: StateFlow<Set<Uri>> = _selectedUris.asStateFlow()

    private val _navigateToPaywallEvent = MutableSharedFlow<Unit>()
    val navigateToPaywallEvent: SharedFlow<Unit> = _navigateToPaywallEvent.asSharedFlow()

    init {
        scanAudio()
    }

    fun scanAudio() {
        viewModelScope.launch {
            _isScanning.value = true
            val groups = getDuplicateAudioUseCase()
            _audioGroups.value = groups
            _selectedUris.value = groups.flatMap { it.duplicates }.map { it.uri }.toSet()
            _isScanning.value = false
        }
    }

    fun toggleUriSelection(uri: Uri) {
        val current = _selectedUris.value.toMutableSet()
        if (uri in current) current.remove(uri) else current.add(uri)
        _selectedUris.value = current
    }

    fun deleteSelectedAudio() {
        val candidates = _audioGroups.value.flatMap { it.duplicates }.filter { it.uri in _selectedUris.value }
        if (candidates.isEmpty()) return

        if (!checkBatchCleanAllowedUseCase(candidates.size)) {
            viewModelScope.launch { _navigateToPaywallEvent.emit(Unit) }
            return
        }

        viewModelScope.launch {
            deleteAudioUseCase(candidates)
            scanAudio()
        }
    }
}
