package com.kjbilling.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.validator.GstinValidator
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
    customerId: Long?,
    onNavigateBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.factory(app.container))

    val customers by viewModel.customers.collectAsState()
    val existingCustomer = customers.find { it.id == customerId }

    // Key on id so async Flow load populates edit fields (was stuck empty).
    var name by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.name ?: "") }
    var mobile by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.mobile ?: "") }
    var email by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.email ?: "") }
    var address by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.billingAddress ?: "") }
    var state by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.state ?: "") }
    var pincode by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.pincode ?: "") }
    var gstin by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.gstin ?: "") }
    var businessName by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.businessName ?: "") }
    var notes by remember(existingCustomer?.id) { mutableStateOf(existingCustomer?.notes ?: "") }

    val isGstinValid = remember(gstin) { gstin.isBlank() || GstinValidator.validate(gstin).isValid }
    val isFormValid = name.isNotBlank() && isGstinValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (customerId != null) "Edit Customer" else "New Customer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name*",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = "Business Name",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = "Mobile",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = address,
                onValueChange = { address = it },
                label = "Billing Address",
                modifier = Modifier.fillMaxWidth()
            )

            // Simplification: using a text field for state dropdown since no standard Dropdown exists in components map, 
            // but normally we'd build an ExposedDropdownMenuBox. Let's build a basic one if needed, or stick to TextField
            AppTextField(
                value = state,
                onValueChange = { state = it },
                label = "State",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = pincode,
                onValueChange = { pincode = it },
                label = "Pincode",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = gstin,
                onValueChange = { gstin = it },
                label = "GSTIN",
                modifier = Modifier.fillMaxWidth(),
                isError = !isGstinValid
            )
            if (!isGstinValid && gstin.isNotBlank()) {
                Text(
                    text = "Invalid GSTIN format",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Save",
                onClick = {
                    if (isFormValid) {
                        viewModel.saveCustomer(
                            Customer(
                                id = existingCustomer?.id ?: 0L,
                                name = name,
                                mobile = mobile.takeIf { it.isNotBlank() },
                                email = email.takeIf { it.isNotBlank() },
                                billingAddress = address.takeIf { it.isNotBlank() },
                                state = state.takeIf { it.isNotBlank() },
                                pincode = pincode.takeIf { it.isNotBlank() },
                                gstin = gstin.takeIf { it.isNotBlank() },
                                businessName = businessName.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() }
                            )
                        )
                        onNavigateBack()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
