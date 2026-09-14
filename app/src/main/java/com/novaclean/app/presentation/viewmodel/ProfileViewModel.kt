package com.novaclean.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.novaclean.app.domain.model.UserProfile
import com.novaclean.app.domain.usecase.ObserveUserProfileUseCase
import com.novaclean.app.domain.usecase.SetNotificationsEnabledUseCase
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(
    observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = observeUserProfileUseCase()

    fun toggleNotifications(enabled: Boolean) {
        setNotificationsEnabledUseCase(enabled)
    }
}
