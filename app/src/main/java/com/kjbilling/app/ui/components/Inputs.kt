package com.kjbilling.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.medium,
        colors = appTextFieldColors()
    )
}

@Composable
fun MoneyInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    AppTextField(
        value = value,
        onValueChange = { newValue ->
            // Allow only digits and one decimal point
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            val parts = filtered.split(".")
            val result = when {
                parts.size <= 1 -> filtered
                else -> parts[0] + "." + parts[1].take(2)
            }
            onValueChange(result)
        },
        label = label,
        modifier = modifier,
        isError = isError,
        errorMessage = errorMessage,
        keyboardType = KeyboardType.Decimal,
        placeholder = "₹0.00"
    )
}

@Composable
fun QuantityInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Quantity",
    modifier: Modifier = Modifier
) {
    AppTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            val parts = filtered.split(".")
            val result = when {
                parts.size <= 1 -> filtered
                else -> parts[0] + "." + parts[1].take(2)
            }
            onValueChange(result)
        },
        label = label,
        modifier = modifier,
        keyboardType = KeyboardType.Decimal,
        placeholder = "1"
    )
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = appTextFieldColors()
    )
}

/** White/surface fields with a clear outline, so inputs stand out on the tinted background. */
@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline
)
