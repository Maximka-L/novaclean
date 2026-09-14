package com.novaclean.app

import com.novaclean.app.domain.model.UserProfile
import com.novaclean.app.domain.repository.ProfileRepository
import com.novaclean.app.domain.usecase.AddCleanedBytesUseCase
import com.novaclean.app.domain.usecase.ObserveUserProfileUseCase
import com.novaclean.app.domain.usecase.RegisterProfileUseCase
import com.novaclean.app.domain.usecase.SetNotificationsEnabledUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserProfileRegistrationTest {

    private class FakeProfileRepository : ProfileRepository {
        private val _profile = MutableStateFlow(UserProfile())
        override val userProfile: StateFlow<UserProfile> = _profile.asStateFlow()

        override fun setNotificationsEnabled(enabled: Boolean) {
            _profile.value = _profile.value.copy(notificationsEnabled = enabled)
        }

        override fun addCleanedBytes(bytes: Long) {
            val current = _profile.value
            _profile.value = current.copy(totalCleanedBytes = current.totalCleanedBytes + bytes)
        }

        override fun registerOrUpdateProfile(displayName: String, email: String) {
            _profile.value = _profile.value.copy(
                displayName = displayName.trim(),
                email = email.trim(),
                isRegistered = email.isNotBlank()
            )
        }
    }

    private lateinit var repository: FakeProfileRepository
    private lateinit var observeUseCase: ObserveUserProfileUseCase
    private lateinit var registerUseCase: RegisterProfileUseCase
    private lateinit var setNotificationsUseCase: SetNotificationsEnabledUseCase
    private lateinit var addCleanedBytesUseCase: AddCleanedBytesUseCase

    @Before
    fun setUp() {
        repository = FakeProfileRepository()
        observeUseCase = ObserveUserProfileUseCase(repository)
        registerUseCase = RegisterProfileUseCase(repository)
        setNotificationsUseCase = SetNotificationsEnabledUseCase(repository)
        addCleanedBytesUseCase = AddCleanedBytesUseCase(repository)
    }

    @Test
    fun testDefaultUserProfile() {
        val defaultProfile = observeUseCase().value
        assertFalse(defaultProfile.isRegistered)
        assertNull(defaultProfile.email)
        assertEquals("Пользователь", defaultProfile.displayName)
        assertTrue(defaultProfile.notificationsEnabled)
        assertEquals(0L, defaultProfile.totalCleanedBytes)
    }

    @Test
    fun testRegistrationUpdatesProfile() {
        registerUseCase("Алексей Смирнов", "alexey@example.com")

        val profile = observeUseCase().value
        assertTrue(profile.isRegistered)
        assertEquals("Алексей Смирнов", profile.displayName)
        assertEquals("alexey@example.com", profile.email)
    }

    @Test
    fun testProfileUpdatePreservesStats() {
        addCleanedBytesUseCase(1024 * 1024 * 500) // 500 MB
        setNotificationsUseCase(false)

        registerUseCase("Максим", "maksim@novaclean.app")

        val profile = observeUseCase().value
        assertTrue(profile.isRegistered)
        assertEquals("Максим", profile.displayName)
        assertEquals("maksim@novaclean.app", profile.email)
        assertEquals(1024 * 1024 * 500L, profile.totalCleanedBytes)
        assertFalse(profile.notificationsEnabled)
    }

    @Test
    fun testProfileReRegistration() {
        registerUseCase("User 1", "user1@test.com")
        assertEquals("user1@test.com", observeUseCase().value.email)

        registerUseCase("User 2 Updated", "user2@newmail.com")
        val updated = observeUseCase().value
        assertEquals("User 2 Updated", updated.displayName)
        assertEquals("user2@newmail.com", updated.email)
        assertTrue(updated.isRegistered)
    }
}
