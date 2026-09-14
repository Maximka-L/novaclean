package com.novaclean.app.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novaclean.app.R
import com.novaclean.app.domain.model.BillingResultState
import com.novaclean.app.domain.model.SubscriptionPlan
import com.novaclean.app.domain.usecase.GetSubscriptionPlansUseCase
import com.novaclean.app.domain.usecase.PurchasePlanUseCase
import com.novaclean.app.domain.usecase.RestorePurchasesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaywallViewModel(
    getSubscriptionPlansUseCase: GetSubscriptionPlansUseCase,
    private val purchasePlanUseCase: PurchasePlanUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase
) : ViewModel() {

    val plans: List<SubscriptionPlan> = getSubscriptionPlansUseCase()

    private val _selectedPlanId = MutableStateFlow(plans.firstOrNull { it.isRecommended }?.id ?: plans.first().id)
    val selectedPlanId: StateFlow<String> = _selectedPlanId.asStateFlow()

    private val _billingState = MutableStateFlow<BillingResultState>(BillingResultState.Idle)
    val billingState: StateFlow<BillingResultState> = _billingState.asStateFlow()

    fun selectPlan(planId: String) {
        _selectedPlanId.value = planId
    }

    fun purchaseSelectedPlan(activity: Activity?) {
        val plan = plans.first { it.id == _selectedPlanId.value }
        viewModelScope.launch {
            _billingState.value = BillingResultState.Loading
            val result = purchasePlanUseCase(activity, plan)
            if (result.isSuccess) {
                _billingState.value = BillingResultState.Success(R.string.paywall_activated_toast)
            } else {
                _billingState.value = BillingResultState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _billingState.value = BillingResultState.Loading
            val result = restorePurchasesUseCase()
            if (result.isSuccess && result.getOrDefault(false)) {
                _billingState.value = BillingResultState.Success(R.string.paywall_restored_success)
            } else {
                _billingState.value = BillingResultState.Success(R.string.paywall_restored_empty)
            }
        }
    }

    fun resetBillingState() {
        _billingState.value = BillingResultState.Idle
    }
}
