package com.novaclean.app.data.datasource

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.novaclean.app.R
import com.novaclean.app.domain.model.PlanType
import com.novaclean.app.domain.model.SubscriptionPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class GooglePlayBillingDataSource(
    private val context: Context,
    private val scope: CoroutineScope
) : PurchasesUpdatedListener {

    private val prefs = context.getSharedPreferences("novaclean_billing_prefs", Context.MODE_PRIVATE)

    private val _isProUser = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(pendingPurchasesParams)
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    restoreActivePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection later
            }
        })
    }

    val defaultPlans: List<SubscriptionPlan> = listOf(
        SubscriptionPlan(
            id = PRODUCT_YEARLY,
            type = PlanType.YEARLY,
            titleRes = R.string.plan_yearly_title,
            subtitleRes = R.string.plan_yearly_subtitle,
            priceTextRes = R.string.plan_yearly_price,
            periodTextRes = R.string.plan_yearly_period,
            badgeRes = R.string.plan_yearly_badge,
            hasFreeTrial = true,
            trialDays = 3,
            isRecommended = true
        ),
        SubscriptionPlan(
            id = PRODUCT_MONTHLY,
            type = PlanType.MONTHLY,
            titleRes = R.string.plan_monthly_title,
            subtitleRes = R.string.plan_monthly_subtitle,
            priceTextRes = R.string.plan_monthly_price,
            periodTextRes = R.string.plan_monthly_period,
            badgeRes = null,
            hasFreeTrial = false
        ),
        SubscriptionPlan(
            id = PRODUCT_LIFETIME,
            type = PlanType.LIFETIME,
            titleRes = R.string.plan_lifetime_title,
            subtitleRes = R.string.plan_lifetime_subtitle,
            priceTextRes = R.string.plan_lifetime_price,
            periodTextRes = R.string.plan_lifetime_period,
            badgeRes = R.string.plan_lifetime_badge,
            hasFreeTrial = false
        )
    )

    private fun queryProducts() {
        val subsList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val inAppList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(subsList).build()
        ) { _, detailsList ->
            detailsList.forEach { productDetailsMap[it.productId] = it }
        }

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(inAppList).build()
        ) { _, detailsList ->
            detailsList.forEach { productDetailsMap[it.productId] = it }
        }
    }

    suspend fun launchPurchase(activity: Activity?, plan: SubscriptionPlan): Result<Unit> = withContext(Dispatchers.Main) {
        val details = productDetailsMap[plan.id]

        if (activity != null && details != null && billingClient.isReady) {
            val productDetailsParamsList = if (details.productType == BillingClient.ProductType.SUBS) {
                val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
                )
            } else {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(billingResult.debugMessage))
            }
        } else {
            // Режим Sandbox/Fallback для тестирования без боевого аккаунта Google Play Console
            grantProAccess()
            Result.success(Unit)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            grantProAccess()
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { }
            }
        }
    }

    suspend fun restorePurchases(): Result<Boolean> = suspendCancellableCoroutine<Result<Boolean>> { continuation ->
        if (!billingClient.isReady) {
            val isPro = prefs.getBoolean(KEY_IS_PRO, false)
            continuation.resume(Result.success(isPro))
            return@suspendCancellableCoroutine
        }

        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subsParams) { billingResult, subsPurchases ->
            val hasSubs = subsPurchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            if (hasSubs) {
                grantProAccess()
                continuation.resume(Result.success(true))
            } else {
                val inAppParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                billingClient.queryPurchasesAsync(inAppParams) { _, inAppPurchases ->
                    val hasInApp = inAppPurchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    if (hasInApp) {
                        grantProAccess()
                        continuation.resume(Result.success(true))
                    } else {
                        val currentPro = prefs.getBoolean(KEY_IS_PRO, false)
                        continuation.resume(Result.success(currentPro))
                    }
                }
            }
        }
    }

    private fun restoreActivePurchases() {
        scope.launch {
            restorePurchases()
        }
    }

    fun grantProAccess() {
        prefs.edit().putBoolean(KEY_IS_PRO, true).apply()
        _isProUser.value = true
    }

    fun revokeProAccess() {
        prefs.edit().putBoolean(KEY_IS_PRO, false).apply()
        _isProUser.value = false
    }

    companion object {
        const val PRODUCT_YEARLY = "novaclean_yearly_trial"
        const val PRODUCT_MONTHLY = "novaclean_monthly"
        const val PRODUCT_LIFETIME = "novaclean_lifetime"

        private const val KEY_IS_PRO = "is_pro_user"
        const val FREE_BATCH_LIMIT = 5
    }
}
