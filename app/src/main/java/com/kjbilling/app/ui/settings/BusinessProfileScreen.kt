package com.kjbilling.app.ui.settings

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
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.PrimaryButton

val INDIAN_STATES = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
    "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh",
    "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh",
    "Uttarakhand", "West Bengal", "Andaman and Nicobar Islands", "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu", "Lakshadweep", "Delhi", "Puducherry",
    "Ladakh", "Jammu and Kashmir"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    onNavigateBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app.container))
    val state by viewModel.state.collectAsState()

    var businessName by remember(state.profile?.businessName) { mutableStateOf(state.profile?.businessName ?: "") }
    var ownerName by remember(state.profile?.ownerName) { mutableStateOf(state.profile?.ownerName ?: "") }
    var mobile by remember(state.profile?.mobile) { mutableStateOf(state.profile?.mobile ?: "") }
    var address by remember(state.profile?.address) { mutableStateOf(state.profile?.address ?: "") }
    var stateSelection by remember(state.profile?.state) { mutableStateOf(state.profile?.state ?: "") }
    var gstin by remember(state.profile?.gstin) { mutableStateOf(state.profile?.gstin ?: "") }
    var email by remember(state.profile?.email) { mutableStateOf(state.profile?.email ?: "") }
    var city by remember(state.profile?.city) { mutableStateOf(state.profile?.city ?: "") }
    var pincode by remember(state.profile?.pincode) { mutableStateOf(state.profile?.pincode ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                PrimaryButton(
                    text = "Save",
                    onClick = {
                        state.profile?.let {
                            viewModel.saveBusinessProfile(
                                it.copy(
                                    businessName = businessName,
                                    ownerName = ownerName,
                                    mobile = mobile,
                                    address = address,
                                    state = stateSelection,
                                    gstin = gstin,
                                    email = email,
                                    city = city,
                                    pincode = pincode
                                )
                            )
                        } ?: run {
                            viewModel.saveBusinessProfile(
                                BusinessProfile(
                                    id = 1L,
                                    businessName = businessName,
                                    ownerName = ownerName,
                                    mobile = mobile,
                                    address = address,
                                    state = stateSelection,
                                    gstin = gstin,
                                    email = email,
                                    city = city,
                                    pincode = pincode
                                )
                            )
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = "Business Name"
            )
            AppTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = "Owner Name"
            )
            AppTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = "Mobile"
            )
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )
            AppTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address"
            )
            AppTextField(
                value = city,
                onValueChange = { city = it },
                label = "City"
            )
            AppTextField(
                value = pincode,
                onValueChange = { pincode = it },
                label = "Pincode"
            )
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                AppTextField(
                    value = stateSelection,
                    onValueChange = {},
                    readOnly = true,
                    label = "State",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    INDIAN_STATES.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                stateSelection = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            AppTextField(
                value = gstin,
                onValueChange = { gstin = it },
                label = "GSTIN"
            )
        }
    }
}
