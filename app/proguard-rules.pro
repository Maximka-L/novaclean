# ProGuard rules for NovaClean
-keepattributes *Annotation*
-dontwarn okio.**

# Yandex Mobile Ads SDK
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.yandex.mobile.ads.** { *; }
-dontwarn com.yandex.mobile.ads.**

