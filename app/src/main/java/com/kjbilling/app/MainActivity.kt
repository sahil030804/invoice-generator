package com.kjbilling.app

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kjbilling.app.navigation.AppNavigation
import com.kjbilling.app.ui.theme.KJInvoiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animateSplashExit()

        val themePreferences = (application as KJInvoiceApp).container.themePreferences
        // Before the first frame, so status-bar icons are right from the start.
        applySystemBars(themePreferences.mode.value.isDark(isSystemDark()))

        setContent {
            val mode by themePreferences.mode.collectAsState()
            val dark = mode.isDark(systemDark = isSystemInDarkTheme())

            // Bar icon colour must follow the in-app choice, not only the phone setting.
            LaunchedEffect(dark) {
                applySystemBars(dark)
            }

            KJInvoiceTheme(darkTheme = dark) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AppNavigation()
                }
            }
        }
    }

    /** Android 12+: fade the splash out with a slight logo zoom instead of an abrupt cut. */
    private fun animateSplashExit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        // The app is ready in a few hundred ms; hold the first frame until the logo animation has played.
        val shownAt = SystemClock.uptimeMillis()
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (SystemClock.uptimeMillis() - shownAt < SPLASH_MIN_VISIBLE_MS) {
                    content.postInvalidateDelayed(SPLASH_POLL_MS)
                    return false
                }
                content.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })

        splashScreen.setOnExitAnimationListener { splashView ->
            splashView.iconView?.animate()
                ?.scaleX(SPLASH_ICON_EXIT_SCALE)
                ?.scaleY(SPLASH_ICON_EXIT_SCALE)
                ?.setDuration(SPLASH_EXIT_MS)
                ?.start()
            splashView.animate()
                .alpha(0f)
                .setDuration(SPLASH_EXIT_MS)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { splashView.remove() }
                .start()
        }
    }

    private fun isSystemDark(): Boolean {
        val nightBits = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightBits == Configuration.UI_MODE_NIGHT_YES
    }

    private fun applySystemBars(dark: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
            navigationBarStyle = SystemBarStyle.auto(NAV_SCRIM_LIGHT, NAV_SCRIM_DARK) { dark }
        )
    }

    private companion object {
        const val SPLASH_EXIT_MS = 300L
        const val SPLASH_MIN_VISIBLE_MS = 1000L // matches windowSplashScreenAnimationDuration
        const val SPLASH_POLL_MS = 50L
        const val SPLASH_ICON_EXIT_SCALE = 1.15f

        // Same translucent scrims androidx.activity uses by default for 3-button navigation.
        val NAV_SCRIM_LIGHT = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val NAV_SCRIM_DARK = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}
