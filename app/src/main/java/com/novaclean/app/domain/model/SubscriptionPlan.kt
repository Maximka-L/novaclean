package com.novaclean.app.domain.model

enum class PlanType {
    MONTHLY,
    YEARLY,
    LIFETIME
}

data class SubscriptionPlan(
    val id: String,
    val type: PlanType,
    val titleRes: Int,
    val subtitleRes: Int,
    val priceTextRes: Int,
    val periodTextRes: Int,
    val badgeRes: Int? = null,
    val hasFreeTrial: Boolean = false,
    val trialDays: Int = 0,
    val isRecommended: Boolean = false
)

sealed interface BillingResultState {
    data object Idle : BillingResultState
    data object Loading : BillingResultState
    data class Success(val messageRes: Int) : BillingResultState
    data class Error(val errorMessage: String) : BillingResultState
}
