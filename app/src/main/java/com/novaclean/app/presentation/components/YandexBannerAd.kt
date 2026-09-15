package com.novaclean.app.presentation.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.novaclean.app.ads.AdsConfig
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

@Composable
fun YandexBannerAd(
    isProUser: Boolean,
    modifier: Modifier = Modifier,
    adUnitId: String = AdsConfig.BANNER_AD_UNIT_ID
) {
    if (isProUser) return

    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                BannerAdView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    val widthDp = context.resources.configuration.screenWidthDp
                    setAdSize(BannerAdSize.sticky(context, widthDp))
                    setBannerAdEventListener(object : BannerAdEventListener {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                        }

                        override fun onAdFailedToLoad(error: AdRequestError) {
                            isAdLoaded = false
                        }

                        override fun onAdClicked() {}
                        override fun onImpression(impressionData: ImpressionData?) {}
                    })
                    loadAd(AdRequest.Builder(adUnitId).build())
                }
            }
        )
    }
}
