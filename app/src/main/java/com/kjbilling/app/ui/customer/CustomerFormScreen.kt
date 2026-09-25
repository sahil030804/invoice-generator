package com.kjbilling.app.ui.customer

import androidx.activity.compose.BackHandler
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
import com.kjbilling.app.domain.validator.StateResolver
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.PrimaryButton
import com.kjbilling.app.ui.components.SectionCard
import com.kjbilling.app.ui.settings.INDIAN_STATES

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

    val isDirty = name != (existingCustomer?.name ?: "") ||
        mobile != (existingCustomer?.mobile ?: "") ||
        email != (existingCustomer?.email ?: "") ||
        address != (existingCustomer?.billingAddress ?: "") ||
        state != (existingCustomer?.state ?: "") ||
        pincode != (existingCustomer?.pincode ?: "") ||
        gstin != (existingCustomer?.gstin ?: "") ||
        businessName != (existingCustomer?.businessName ?: "") ||
        notes != (existingCustomer?.notes ?: "")
    var showDiscardDialog by remember { mutableStateOf(false) }
    val handleBack = { if (isDirty) showDiscardDialog = true else onNavigateBack() }
    BackHandler(onBack = handleBack)

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved changes will be lost.") },
            confirmButton = { TextButton(onClick = onNavigateBack) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (customerId != null) "Edit Customer" else "New Customer") },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
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
            SectionCard(title = "Contact details") {
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
            }

            SectionCard(title = "Address & tax") {
                AppTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Billing Address",
                    modifier = Modifier.fillMaxWidth()
                )

                var stateMenuOpen by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = stateMenuOpen,
                    onExpandedChange = { stateMenuOpen = !stateMenuOpen }
                ) {
                    AppTextField(
                        value = state,
                        onValueChange = {},
                        readOnly = true,
                        label = "State",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateMenuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = stateMenuOpen,
                        onDismissRequest = { stateMenuOpen = false }
                    ) {
                        INDIAN_STATES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    state = option
                                    stateMenuOpen = false
                                }
                            )
                        }
                    }
                }

                AppTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = "Pincode",
                    modifier = Modifier.fillMaxWidth()
                )

                AppTextField(
                    value = gstin,
                    onValueChange = { input ->
                        gstin = input.uppercase()
                        // The GSTIN's first two digits fix the state, so keep the state field in sync.
                        StateResolver.fromGstin(gstin)?.let { state = it }
                    },
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
            }

            SectionCard(title = "Extra") {
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Save",
                onClick = {
                    if (isFormValid) {
                        viewModel.saveCustomer(
                            // copy() keeps isWalkIn, lastUsedAt and createdAt of an existing customer.
                            (existingCustomer ?: Customer(name = name)).copy(
                                name = name,
                                mobile = mobile.takeIf { it.isNotBlank() },
                                email = email.takeIf { it.isNotBlank() },
                                billingAddress = address.takeIf { it.isNotBlank() },
                                state = state.takeIf { it.isNotBlank() },
                                pincode = pincode.takeIf { it.isNotBlank() },
                                gstin = gstin.takeIf { it.isNotBlank() },
                                businessName = businessName.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() },
                                updatedAt = System.currentTimeMillis()
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
