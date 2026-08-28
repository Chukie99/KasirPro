package com.kasirpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kasirpro.ui.activation.ActivationScreen
import com.kasirpro.ui.activation.SplashScreen
import com.kasirpro.ui.main.MainScreen
import com.kasirpro.ui.theme.Theme
import com.kasirpro.utils.DeviceIdHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var devicePrefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        // Pre-compute device ID (cheap, cached)
        DeviceIdHelper.getDeviceId(this)

        setContent {
            Theme {
                var showSplash by remember { mutableStateOf(true) }
                var isActivated by remember {
                    mutableStateOf(devicePrefs.getBoolean("kasirpro_activated", false))
                }

                if (showSplash && !isActivated) {
                    // Splash → Activation
                    SplashScreen(onTimeout = {
                        showSplash = false
                    })
                } else if (!isActivated) {
                    // Activation screen
                    ActivationScreen(onActivated = {
                        isActivated = true
                    })
                } else {
                    // App is activated → Main dashboard
                    MainScreen()
                }
            }
        }
    }
}
