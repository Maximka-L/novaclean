package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    val userProfile: StateFlow<UserProfile>
    fun setNotificationsEnabled(enabled: Boolean)
    fun addCleanedBytes(bytes: Long)
}
