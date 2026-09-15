package com.novaclean.app

import android.app.Application
import com.yandex.mobile.ads.common.YandexAds

class NovaCleanApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Yandex Mobile Ads SDK (v8)
        YandexAds.initialize(this) {
            // SDK initialized
        }
    }
}
