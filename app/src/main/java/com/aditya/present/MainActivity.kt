package com.aditya.present

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.aditya.present.data.ThemeRepository
import com.aditya.present.ui.navigation.PresentNavHost
import com.aditya.present.ui.theme.LocalHaptics
import com.aditya.present.ui.theme.PresentTheme
import com.aditya.present.ui.theme.rememberHapticController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                    .launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val prefs by themeRepository.themePrefs.collectAsState(
                initial = com.aditya.present.data.ThemePrefs()
            )

            PresentTheme(
                themeMode = prefs.mode,
                accentName = prefs.accentName,
                dynamicColor = prefs.dynamicColor,
                animationsEnabled = prefs.animationsEnabled,
                cardStyle = prefs.cardStyle,
                showPercentageOnCards = prefs.showPercentageOnCards,
                compactMode = prefs.compactMode,
            ) {
                val haptics = rememberHapticController(
                    enabled = prefs.hapticFeedback,
                    intensityName = prefs.hapticIntensity,
                )
                CompositionLocalProvider(LocalHaptics provides haptics) {
                    PresentNavHost()
                }
            }
        }
    }
}
