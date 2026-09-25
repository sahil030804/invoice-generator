package com.kjbilling.app.ui.invoice.quick

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.keypad.AmountInput
import com.kjbilling.app.domain.quickbill.CustomCartItem
import com.kjbilling.app.domain.quickbill.QuickBill
import com.kjbilling.app.domain.quickbill.QuickPaymentMode
import com.kjbilling.app.ui.components.ActionButton
import com.kjbilling.app.ui.components.AmountDisplay
import com.kjbilling.app.ui.components.AmountKeypad
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.theme.Dimens
import java.math.BigDecimal

private val PAYMENT_MODES = listOf(
    QuickPaymentMode.CASH to "Cash",
    QuickPaymentMode.UPI to "UPI",
    QuickPaymentMode.UDHAAR to "Udhaar"
)

/** Cash / UPI / Udhaar selector above the Generate button. Udhaar explains itself when no customer is picked. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentModeRow(
    selected: QuickPaymentMode,
    udhaarAllowed: Boolean,
    onSelect: (QuickPaymentMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // FlowRow wraps chips onto a second line at large font sizes instead of squeezing them.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Sm),
            verticalArrangement = Arrangement.spacedBy(Dimens.Xs)
        ) {
            Text(
                text = "Pay by",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            PAYMENT_MODES.forEach { (mode, label) ->
                FilterChip(
                    selected = mode == selected,
                    onClick = { onSelect(mode) },
                    label = { Text(label, maxLines = 1, softWrap = false) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        if (selected == QuickPaymentMode.UDHAAR && !udhaarAllowed) {
            Text(
                text = "Select a customer for Udhaar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** First grid tile: bill anything that isn't in the catalog. Icon only, so no extra "+" text on screen. */
@Composable
fun CustomAmountTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Md)
                .heightIn(min = 124.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.Dialpad,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Custom amount",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Dimens.Sm)
            )
            Text(
                text = "Not in catalog",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Chips for keypad-entered items, each removable. */
@Composable
fun CustomItemChips(items: List<CustomCartItem>, onRemove: (Long) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Sm),
        contentPadding = PaddingValues(horizontal = Dimens.Xs)
    ) {
        items(items, key = { it.id }) { item ->
            InputChip(
                selected = true,
                onClick = { onRemove(item.id) },
                label = { Text("${item.name} · ${CurrencyFormatter.format(item.amount)}") },
                trailingIcon = {
                    Icon(Icons.Filled.Close, contentDescription = "Remove ${item.name}", modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}

/** Bottom sheet: optional name + calculator keypad. The typed amount is final (GST included). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAmountSheet(onAdd: (name: String, amount: BigDecimal) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    val amount = AmountInput.toAmount(amountText)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .navigationBarsPadding()
                .padding(bottom = Dimens.Lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.Md)
        ) {
            Text("Add custom amount", style = MaterialTheme.typography.titleLarge)
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Item name (optional)",
                placeholder = QuickBill.DEFAULT_CUSTOM_NAME
            )
            AmountDisplay(amountText)
            Text(
                text = "Customer pays exactly this amount (GST included).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            AmountKeypad(text = amountText, onTextChange = { amountText = it })
            ActionButton(
                text = "Add to bill",
                onClick = { amount?.let { onAdd(name, it) } },
                enabled = amount != null
            )
        }
    }
}
