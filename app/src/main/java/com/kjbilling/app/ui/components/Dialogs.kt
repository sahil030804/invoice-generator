package com.kjbilling.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kjbilling.app.domain.formatter.label
import com.kjbilling.app.domain.model.PaymentMethod
import java.math.BigDecimal

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    grandTotal: BigDecimal,
    amountPaid: BigDecimal,
    currentMethod: PaymentMethod?,
    onConfirm: (BigDecimal, PaymentMethod?) -> Unit,
    onDismiss: () -> Unit
) {
    val balance = (grandTotal - amountPaid).max(BigDecimal.ZERO)
    var amountStr by remember { mutableStateOf(balance.stripTrailingZeros().toPlainString()) }
    var method by remember { mutableStateOf(currentMethod ?: PaymentMethod.CASH) }
    var methodExpanded by remember { mutableStateOf(false) }
    val parsed = amountStr.toBigDecimalOrNull()
    // Only the remaining balance can be paid; larger amounts used to be silently ignored.
    val isValid = parsed != null && parsed > BigDecimal.ZERO && parsed <= balance

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MoneyInput(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = "Amount (Balance ${balance.stripTrailingZeros().toPlainString()})",
                    isError = !isValid && amountStr.isNotBlank(),
                    errorMessage = if (!isValid && amountStr.isNotBlank()) "Enter 0 < amount ≤ balance" else null,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = !methodExpanded }
                ) {
                    AppTextField(
                        value = method.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = "Payment Method",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        PaymentMethod.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label()) },
                                onClick = {
                                    method = option
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid && parsed != null) {
                        onConfirm(parsed, method)
                    }
                },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
