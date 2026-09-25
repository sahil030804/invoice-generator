package com.kjbilling.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = IndigoContainerLight,
    onPrimaryContainer = OnIndigoContainerLight,
    inversePrimary = IndigoPrimaryDark,
    secondary = SlateSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SlateContainerLight,
    onSecondaryContainer = OnSlateContainerLight,
    tertiary = SaffronLight,
    onTertiary = OnSaffronLight,
    tertiaryContainer = SaffronContainerLight,
    onTertiaryContainer = OnSaffronContainerLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = IndigoPrimaryLight,
    inverseSurface = Color(0xFF2F3036),
    inverseOnSurface = Color(0xFFF1F0F7),
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Color.Black,
    surfaceBright = SurfaceLight,
    surfaceDim = Color(0xFFDCDDE5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF9F9FD),
    surfaceContainer = Color(0xFFF1F2F8),
    surfaceContainerHigh = Color(0xFFEBECF3),
    surfaceContainerHighest = Color(0xFFE4E5EE)
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = OnIndigoPrimaryDark,
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = OnIndigoContainerDark,
    inversePrimary = IndigoPrimaryLight,
    secondary = SlateSecondaryDark,
    onSecondary = OnSlateSecondaryDark,
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = OnSlateContainerDark,
    tertiary = SaffronDark,
    onTertiary = OnSaffronDark,
    tertiaryContainer = SaffronContainerDark,
    onTertiaryContainer = OnSaffronContainerDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = IndigoPrimaryDark,
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = Color(0xFF2F3036),
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Color.Black,
    surfaceBright = Color(0xFF393940),
    surfaceDim = BackgroundDark,
    surfaceContainerLowest = Color(0xFF0E0E13),
    surfaceContainerLow = Color(0xFF1B1B21),
    surfaceContainer = Color(0xFF1F1F25),
    surfaceContainerHigh = Color(0xFF292A31),
    surfaceContainerHighest = Color(0xFF34343B)
)

/**
 * App theme. Fixed brand palette (no wallpaper colours) so status colours always mean the same.
 * [darkTheme] comes from the in-app System/Light/Dark choice (see MainActivity).
 */
@Composable
fun KJInvoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
