package com.kjbilling.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** One money status: chip background, chip text, and text colour used on plain surfaces. */
@Immutable
data class StatusColors(
    val container: Color,
    val onContainer: Color,
    val text: Color
)

/**
 * Colours Material 3 has no role for.
 *
 *   action / onAction  → saffron fill for the main "make a bill" actions
 *   actionText         → saffron-toned TEXT (saffron itself fails contrast as text)
 *   paid/partial/unpaid→ invoice money status, always shown with an icon + word
 *   hero / onHero      → big dashboard card: indigo in light, deep indigo (not glaring lavender) in dark
 */
@Immutable
data class ExtendedColors(
    val action: Color,
    val onAction: Color,
    val actionText: Color,
    val paid: StatusColors,
    val partial: StatusColors,
    val unpaid: StatusColors,
    val draft: StatusColors,
    val whatsApp: Color,
    val onWhatsApp: Color,
    val hero: Color,
    val onHero: Color
)

val LightExtendedColors = ExtendedColors(
    action = SaffronLight,
    onAction = OnSaffronLight,
    actionText = ActionTextLight,
    paid = StatusColors(PaidContainerLight, OnPaidContainerLight, PaidTextLight),
    partial = StatusColors(PartialContainerLight, OnPartialContainerLight, PartialTextLight),
    unpaid = StatusColors(UnpaidContainerLight, OnUnpaidContainerLight, UnpaidTextLight),
    draft = StatusColors(SurfaceVariantLight, OnSurfaceVariantLight, OnSurfaceVariantLight),
    whatsApp = WhatsAppGreen,
    onWhatsApp = OnWhatsAppGreen,
    hero = IndigoPrimaryLight,
    onHero = Color.White
)

val DarkExtendedColors = ExtendedColors(
    action = SaffronDark,
    onAction = OnSaffronDark,
    actionText = ActionTextDark,
    paid = StatusColors(PaidContainerDark, OnPaidContainerDark, PaidTextDark),
    partial = StatusColors(PartialContainerDark, OnPartialContainerDark, PartialTextDark),
    unpaid = StatusColors(UnpaidContainerDark, OnUnpaidContainerDark, UnpaidTextDark),
    draft = StatusColors(SurfaceVariantDark, OnSurfaceVariantDark, OnSurfaceVariantDark),
    whatsApp = WhatsAppGreen,
    onWhatsApp = OnWhatsAppGreen,
    hero = IndigoContainerDark,
    onHero = OnIndigoContainerDark
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** Access as `MaterialTheme.ext.paid.text` etc. */
val MaterialTheme.ext: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
