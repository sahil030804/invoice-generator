package com.kjbilling.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue600,
    onPrimary = Color.White,
    primaryContainer = ElectricBlue50,
    onPrimaryContainer = ElectricBlue900,
    secondary = Slate600,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate800,
    tertiary = Emerald500,
    onTertiary = Color.White,
    tertiaryContainer = Emerald100,
    onTertiaryContainer = Emerald900,
    surface = Color.White,
    onSurface = Slate800,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate600,
    background = Color.White,
    onBackground = Slate800,
    error = Rose500,
    onError = Color.White,
    errorContainer = Rose100,
    onErrorContainer = Rose900,
    outline = Slate200,
    outlineVariant = Slate100
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue600,
    onPrimary = Color.White,
    primaryContainer = ElectricBlue900,
    onPrimaryContainer = ElectricBlue100,
    secondary = Slate400,
    onSecondary = DeepSlate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate100,
    tertiary = Emerald500,
    onTertiary = DeepSlate950,
    tertiaryContainer = Emerald900,
    onTertiaryContainer = Emerald100,
    surface = DeepSlate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    background = DeepSlate950,
    onBackground = Slate100,
    error = Rose500,
    onError = Color.White,
    errorContainer = Rose900,
    onErrorContainer = Rose100,
    outline = Slate700,
    outlineVariant = Slate800
)

@Composable
fun KJInvoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
