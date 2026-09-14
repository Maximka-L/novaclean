package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.UserProfile
import com.novaclean.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveUserProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): StateFlow<UserProfile> = repository.userProfile
}

class SetNotificationsEnabledUseCase(private val repository: ProfileRepository) {
    operator fun invoke(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
    }
}

class AddCleanedBytesUseCase(private val repository: ProfileRepository) {
    operator fun invoke(bytes: Long) {
        repository.addCleanedBytes(bytes)
    }
}

class RegisterProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(displayName: String, email: String) {
        repository.registerOrUpdateProfile(displayName, email)
    }
}
