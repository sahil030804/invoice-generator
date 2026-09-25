package com.kjbilling.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kjbilling.app.R

// Bundled Noto Sans (OFL, see assets/licenses): same look on every phone, has ₹, works offline.
val NotoSans = FontFamily(
    Font(R.font.notosans_regular, FontWeight.Normal),
    Font(R.font.notosans_medium, FontWeight.Medium),
    Font(R.font.notosans_semibold, FontWeight.SemiBold),
    Font(R.font.notosans_bold, FontWeight.Bold)
)

// Tabular figures: digits share one width so amounts line up and don't jiggle while typing.
private const val TABULAR_FIGURES = "tnum"

private fun style(size: Int, lineHeight: Int, weight: FontWeight): TextStyle {
    return TextStyle(
        fontFamily = NotoSans,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TABULAR_FIGURES
    )
}

// Senior-friendly scale: body 16sp, secondary 14sp, nothing below 12sp.
val AppTypography = Typography(
    displayLarge = style(48, 56, FontWeight.SemiBold),
    displayMedium = style(40, 48, FontWeight.SemiBold),
    displaySmall = style(36, 44, FontWeight.SemiBold),
    headlineLarge = style(32, 40, FontWeight.SemiBold),
    headlineMedium = style(28, 36, FontWeight.SemiBold),
    headlineSmall = style(24, 32, FontWeight.SemiBold),
    titleLarge = style(22, 28, FontWeight.SemiBold),
    titleMedium = style(18, 24, FontWeight.SemiBold),
    titleSmall = style(16, 22, FontWeight.SemiBold),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(16, 24, FontWeight.Normal),
    bodySmall = style(14, 20, FontWeight.Normal),
    labelLarge = style(16, 20, FontWeight.SemiBold),
    labelMedium = style(14, 18, FontWeight.Medium),
    labelSmall = style(12, 16, FontWeight.Medium)
)
