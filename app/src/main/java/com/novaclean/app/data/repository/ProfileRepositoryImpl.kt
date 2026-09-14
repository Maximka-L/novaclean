package com.novaclean.app.data.repository

import android.content.Context
import com.novaclean.app.domain.model.UserProfile
import com.novaclean.app.domain.repository.BillingRepository
import com.novaclean.app.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileRepositoryImpl(
    context: Context,
    private val billingRepository: BillingRepository,
    private val scope: CoroutineScope
) : ProfileRepository {

    private val prefs = context.getSharedPreferences("novaclean_user_profile", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        scope.launch {
            billingRepository.isProUser.collectLatest { isPro ->
                _userProfile.value = _userProfile.value.copy(isPro = isPro)
            }
        }
    }

    private fun loadProfile(): UserProfile {
        val totalCleaned = prefs.getLong("total_cleaned_bytes", 4_600_000_000L) // дефолтные 4.6 ГБ
        val notifications = prefs.getBoolean("notifications_enabled", true)
        val userId = prefs.getString("user_id", null) ?: run {
            val generated = (100000..999999).random().toString()
            prefs.edit().putString("user_id", generated).apply()
            generated
        }
        return UserProfile(
            displayName = "Пользователь NovaClean",
            userId = userId,
            isPro = billingRepository.isProUser.value,
            totalCleanedBytes = totalCleaned,
            notificationsEnabled = notifications
        )
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _userProfile.value = _userProfile.value.copy(notificationsEnabled = enabled)
    }

    override fun addCleanedBytes(bytes: Long) {
        val newTotal = _userProfile.value.totalCleanedBytes + bytes
        prefs.edit().putLong("total_cleaned_bytes", newTotal).apply()
        _userProfile.value = _userProfile.value.copy(totalCleanedBytes = newTotal)
    }
}
