package com.aditya.present

import android.app.Application
import com.aditya.present.util.AlarmScheduler
import com.aditya.present.util.NotificationChannels
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        // Create notification channels
        NotificationChannels.create(this)

        // Schedule auto-mark alarm based on saved prefs
        CoroutineScope(Dispatchers.IO).launch {
            val themeRepo = com.aditya.present.data.ThemeRepository(this@PresentApplication)
            val prefs = themeRepo.themePrefs.first()
            if (prefs.autoMarkEnabled) {
                AlarmScheduler.scheduleAutoMark(this@PresentApplication, prefs.autoMarkHour)
            }
        }
    }
}
