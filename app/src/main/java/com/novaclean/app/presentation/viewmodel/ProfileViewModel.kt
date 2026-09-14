package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.novaclean.app.domain.model.UserProfile
import com.novaclean.app.domain.usecase.ObserveUserProfileUseCase
import com.novaclean.app.domain.usecase.RegisterProfileUseCase
import com.novaclean.app.domain.usecase.SetNotificationsEnabledUseCase
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(
    observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
    private val registerProfileUseCase: RegisterProfileUseCase
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = observeUserProfileUseCase()

    fun toggleNotifications(enabled: Boolean) {
        setNotificationsEnabledUseCase(enabled)
    }

    fun registerOrUpdate(name: String, email: String) {
        registerProfileUseCase(name, email)
    }
}
