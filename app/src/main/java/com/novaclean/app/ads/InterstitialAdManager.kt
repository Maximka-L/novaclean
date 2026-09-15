package com.novaclean.app.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

class InterstitialAdManager(
    private val context: Context,
    private val adUnitId: String = AdsConfig.INTERSTITIAL_AD_UNIT_ID
) {
    private var interstitialAd: InterstitialAd? = null
    private var adLoader: InterstitialAdLoader? = null
    private var isLoading = false
    private var lastAdShownTimestamp = 0L

    init {
        loadAd()
    }

    fun loadAd() {
        if (isLoading || interstitialAd != null) return
        isLoading = true

        val loader = InterstitialAdLoader(context)
        loader.setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isLoading = false
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                interstitialAd = null
                isLoading = false
            }
        })
        adLoader = loader
        val config = AdRequestConfiguration.Builder(adUnitId).build()
        loader.loadAd(config)
    }

    fun showAdIfAvailable(
        activity: Activity,
        isProUser: Boolean,
        onDismissed: () -> Unit = {}
    ) {
        if (isProUser) {
            onDismissed()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastAdShownTimestamp < AdsConfig.INTERSTITIAL_COOLDOWN_MS) {
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {}

                override fun onAdFailedToShow(adError: AdError) {
                    interstitialAd = null
                    loadAd()
                    onDismissed()
                }

                override fun onAdDismissed() {
                    interstitialAd = null
                    lastAdShownTimestamp = System.currentTimeMillis()
                    loadAd()
                    onDismissed()
                }

                override fun onAdClicked() {}
                override fun onAdImpression(impressionData: ImpressionData?) {}
            })
            ad.show(activity)
        } else {
            loadAd()
            onDismissed()
        }
    }
}
