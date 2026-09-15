package com.aditya.present

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PresentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)

        // Register test device so test ads actually render in debug
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("6618F766D6427DAD8C482D8D7E6B6EFB"))
                .build(),
        )
    }
}
