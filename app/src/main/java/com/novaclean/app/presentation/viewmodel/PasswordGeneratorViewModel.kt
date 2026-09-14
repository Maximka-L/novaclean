package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.domain.model.PasswordEvaluation
import com.novaclean.app.domain.model.PasswordOptions
import com.novaclean.app.domain.usecase.PasswordGeneratorUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordGeneratorViewModel(
    private val generatorUseCase: PasswordGeneratorUseCase
) : ViewModel() {

    private val _options = MutableStateFlow(PasswordOptions())
    val options: StateFlow<PasswordOptions> = _options.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _evaluation = MutableStateFlow(generatorUseCase.evaluateStrength(""))
    val evaluation: StateFlow<PasswordEvaluation> = _evaluation.asStateFlow()

    private val _copyEvent = MutableSharedFlow<Unit>()
    val copyEvent: SharedFlow<Unit> = _copyEvent.asSharedFlow()

    init {
        generateNewPassword()
    }

    fun generateNewPassword() {
        val newPass = generatorUseCase.generatePassword(_options.value)
        _password.value = newPass
        _evaluation.value = generatorUseCase.evaluateStrength(newPass)
    }

    fun updateLength(newLength: Int) {
        _options.value = _options.value.copy(length = newLength.coerceIn(6, 32))
        generateNewPassword()
    }

    fun toggleUppercase() {
        val current = _options.value
        _options.value = current.copy(includeUppercase = !current.includeUppercase)
        generateNewPassword()
    }

    fun toggleLowercase() {
        val current = _options.value
        _options.value = current.copy(includeLowercase = !current.includeLowercase)
        generateNewPassword()
    }

    fun toggleDigits() {
        val current = _options.value
        _options.value = current.copy(includeDigits = !current.includeDigits)
        generateNewPassword()
    }

    fun toggleSymbols() {
        val current = _options.value
        _options.value = current.copy(includeSymbols = !current.includeSymbols)
        generateNewPassword()
    }

    fun toggleExcludeSimilar() {
        val current = _options.value
        _options.value = current.copy(excludeSimilar = !current.excludeSimilar)
        generateNewPassword()
    }

    fun onCopyTriggered() {
        viewModelScope.launch {
            _copyEvent.emit(Unit)
        }
    }
}
