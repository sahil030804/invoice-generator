package com.kjbilling.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kjbilling.app.domain.keypad.AmountInput
import com.kjbilling.app.domain.keypad.KeypadKey
import com.kjbilling.app.ui.theme.Dimens

private val KEY_HEIGHT = 60.dp

private val KEY_ROWS: List<List<KeypadKey>> = listOf(
    listOf(KeypadKey.Digit(1), KeypadKey.Digit(2), KeypadKey.Digit(3)),
    listOf(KeypadKey.Digit(4), KeypadKey.Digit(5), KeypadKey.Digit(6)),
    listOf(KeypadKey.Digit(7), KeypadKey.Digit(8), KeypadKey.Digit(9)),
    listOf(KeypadKey.Dot, KeypadKey.Digit(0), KeypadKey.Back)
)

/** Big amount readout for the keypad text ("₹12,34,567.5"). */
@Composable
fun AmountDisplay(text: String, modifier: Modifier = Modifier) {
    Text(
        text = AmountInput.display(text),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Calculator-style keypad (1-9, ., 0, ⌫) with large 60dp keys for quick counter use.
 * State lives with the caller: [onTextChange] receives the next text from [AmountInput.press].
 */
@Composable
fun AmountKeypad(text: String, onTextChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.Sm)) {
        KEY_ROWS.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Sm), verticalAlignment = Alignment.CenterVertically) {
                row.forEach { key ->
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTextChange(AmountInput.press(text, key))
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(KEY_HEIGHT)
                            .testTag(tagFor(key))
                    ) {
                        when (key) {
                            is KeypadKey.Digit -> Text(key.value.toString(), style = MaterialTheme.typography.headlineSmall)
                            KeypadKey.Dot -> Text(".", style = MaterialTheme.typography.headlineSmall)
                            KeypadKey.Back -> Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = "Delete digit")
                        }
                    }
                }
            }
        }
    }
}

private fun tagFor(key: KeypadKey): String = when (key) {
    is KeypadKey.Digit -> "keypad-${key.value}"
    KeypadKey.Dot -> "keypad-dot"
    KeypadKey.Back -> "keypad-back"
}
