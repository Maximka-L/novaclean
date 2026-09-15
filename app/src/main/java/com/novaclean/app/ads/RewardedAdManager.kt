package com.novaclean.app.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader

class RewardedAdManager(
    private val context: Context,
    private val adUnitId: String = AdsConfig.REWARDED_AD_UNIT_ID
) {
    private var rewardedAd: RewardedAd? = null
    private var adLoader: RewardedAdLoader? = null
    private var isLoading = false

    init {
        loadAd()
    }

    fun loadAd() {
        if (isLoading || rewardedAd != null) return
        isLoading = true

        val loader = RewardedAdLoader(context)
        loader.setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoading = false
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                rewardedAd = null
                isLoading = false
            }
        })
        adLoader = loader
        val config = AdRequestConfiguration.Builder(adUnitId).build()
        loader.loadAd(config)
    }

    val isAdLoaded: Boolean
        get() = rewardedAd != null

    fun showAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onClosed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var earnedReward = false
            ad.setAdEventListener(object : RewardedAdEventListener {
                override fun onRewarded(reward: Reward) {
                    earnedReward = true
                    onRewarded()
                }

                override fun onAdShown() {}

                override fun onAdFailedToShow(adError: AdError) {
                    rewardedAd = null
                    loadAd()
                    onClosed()
                }

                override fun onAdDismissed() {
                    rewardedAd = null
                    loadAd()
                    onClosed()
                }

                override fun onAdClicked() {}
                override fun onAdImpression(impressionData: ImpressionData?) {}
            })
            ad.show(activity)
        } else {
            loadAd()
            onClosed()
        }
    }
}
