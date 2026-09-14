package com.novaclean.app.domain.usecase

import android.app.Activity
import com.novaclean.app.domain.model.SubscriptionPlan
import com.novaclean.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow

class GetSubscriptionPlansUseCase(private val repository: BillingRepository) {
    operator fun invoke(): List<SubscriptionPlan> = repository.getAvailablePlans()
}

class ObserveProStatusUseCase(private val repository: BillingRepository) {
    operator fun invoke(): StateFlow<Boolean> = repository.isProUser
}

class PurchasePlanUseCase(private val repository: BillingRepository) {
    suspend operator fun invoke(activity: Activity?, plan: SubscriptionPlan): Result<Unit> {
        return repository.purchasePlan(activity, plan)
    }
}

class RestorePurchasesUseCase(private val repository: BillingRepository) {
    suspend operator fun invoke(): Result<Boolean> {
        return repository.restorePurchases()
    }
}

class CheckBatchCleanAllowedUseCase(private val repository: BillingRepository) {
    operator fun invoke(count: Int): Boolean {
        return repository.checkBatchCleanAllowed(count)
    }
}
