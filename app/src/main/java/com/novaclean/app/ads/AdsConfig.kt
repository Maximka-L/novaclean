package com.novaclean.app.ads

object AdsConfig {
    // Official Yandex Ad Unit IDs for NovaClean (App ID: 20048089)
    const val BANNER_AD_UNIT_ID = "R-M-20048089-1"
    const val INTERSTITIAL_AD_UNIT_ID = "R-M-20048089-2"
    const val REWARDED_AD_UNIT_ID = "R-M-20048089-3"

    // Fallback demo IDs (available for testing/debugging)
    const val DEMO_BANNER_ID = "demo-banner-yandex"
    const val DEMO_INTERSTITIAL_ID = "demo-interstitial-yandex"
    const val DEMO_REWARDED_ID = "demo-rewarded-yandex"

    // Cooldown interval between Interstitial Ads (45 seconds) to maintain good UX
    const val INTERSTITIAL_COOLDOWN_MS = 45_000L
}
