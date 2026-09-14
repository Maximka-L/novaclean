package com.novaclean.app.domain.repository

import android.app.Activity
import com.novaclean.app.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isProUser: StateFlow<Boolean>
    fun getAvailablePlans(): List<SubscriptionPlan>
    suspend fun purchasePlan(activity: Activity?, plan: SubscriptionPlan): Result<Unit>
    suspend fun restorePurchases(): Result<Boolean>
    fun checkBatchCleanAllowed(itemCount: Int): Boolean
}
