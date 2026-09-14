package com.novaclean.app.data.repository

import android.app.Activity
import com.novaclean.app.data.datasource.GooglePlayBillingDataSource
import com.novaclean.app.domain.model.SubscriptionPlan
import com.novaclean.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow

class BillingRepositoryImpl(
    private val dataSource: GooglePlayBillingDataSource
) : BillingRepository {

    override val isProUser: StateFlow<Boolean> = dataSource.isProUser

    override fun getAvailablePlans(): List<SubscriptionPlan> {
        return dataSource.defaultPlans
    }

    override suspend fun purchasePlan(activity: Activity?, plan: SubscriptionPlan): Result<Unit> {
        return dataSource.launchPurchase(activity, plan)
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        return dataSource.restorePurchases()
    }

    override fun checkBatchCleanAllowed(itemCount: Int): Boolean {
        if (isProUser.value) return true
        return itemCount <= GooglePlayBillingDataSource.FREE_BATCH_LIMIT
    }
}
