package com.kjbilling.app.ui.settings

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.PrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            LogoSection(viewModel)

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

/**
 * Logo picker + preview. Changes are saved immediately (independent of the form's Save button)
 * and are used by every invoice PDF generated afterwards, including re-downloads of old invoices.
 */
@Composable
private fun LogoSection(viewModel: SettingsViewModel) {
    val logoVersion by viewModel.logoVersion.collectAsState()
    val isBusy by viewModel.isLogoBusy.collectAsState()
    val error by viewModel.logoError.collectAsState()

    val preview by produceState<ImageBitmap?>(initialValue = null, logoVersion) {
        value = withContext(Dispatchers.IO) {
            viewModel.logoFile()?.let { file ->
                runCatching { BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() }.getOrNull()
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.uploadLogo(uri)
        }
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val image = preview
                    when {
                        isBusy -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        image != null -> Image(
                            bitmap = image,
                            contentDescription = "Business logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                        else -> Text("No logo", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Business Logo", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Shown on all invoices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            viewModel.clearLogoError()
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        enabled = !isBusy
                    ) {
                        Text(if (preview != null) "Change" else "Upload")
                    }
                    if (preview != null) {
                        TextButton(onClick = { viewModel.removeLogo() }, enabled = !isBusy) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )
        }
    }
}
