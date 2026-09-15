package com.novaclean.app

import android.app.Application
import com.yandex.mobile.ads.common.MobileAds

class NovaCleanApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Yandex Mobile Ads SDK
        MobileAds.initialize(this) {
            // SDK initialized
        }
    }
}
