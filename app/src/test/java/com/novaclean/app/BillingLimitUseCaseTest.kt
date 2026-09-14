package com.novaclean.app

import com.novaclean.app.domain.model.SubscriptionPlan
import com.novaclean.app.domain.repository.BillingRepository
import com.novaclean.app.domain.usecase.CheckBatchCleanAllowedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingLimitUseCaseTest {

    private class FakeBillingRepository(private val isPro: Boolean) : BillingRepository {
        override val isProUser: StateFlow<Boolean> = MutableStateFlow(isPro)
        override fun getAvailablePlans(): List<SubscriptionPlan> = emptyList()
        override suspend fun purchasePlan(activity: android.app.Activity?, plan: SubscriptionPlan): Result<Unit> = Result.success(Unit)
        override suspend fun restorePurchases(): Result<Boolean> = Result.success(isPro)
        override fun checkBatchCleanAllowed(itemCount: Int): Boolean {
            if (isPro) return true
            return itemCount <= 5
        }
    }

    @Test
    fun testFreeUserLimit() {
        val freeRepo = FakeBillingRepository(isPro = false)
        val useCase = CheckBatchCleanAllowedUseCase(freeRepo)

        assertTrue(useCase(1))
        assertTrue(useCase(5))
        assertFalse(useCase(6))
        assertFalse(useCase(50))
    }

    @Test
    fun testProUserUnlimited() {
        val proRepo = FakeBillingRepository(isPro = true)
        val useCase = CheckBatchCleanAllowedUseCase(proRepo)

        assertTrue(useCase(1))
        assertTrue(useCase(5))
        assertTrue(useCase(6))
        assertTrue(useCase(1000))
    }
}
