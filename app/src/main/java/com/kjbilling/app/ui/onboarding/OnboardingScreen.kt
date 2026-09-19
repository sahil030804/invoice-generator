package com.kjbilling.app.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.R
import com.kjbilling.app.domain.validator.GstinValidator
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.PrimaryButton
import com.kjbilling.app.ui.components.SecondaryButton
import java.math.BigDecimal

val INDIAN_STATES = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
    "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh",
    "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh",
    "Uttarakhand", "West Bengal", "Andaman and Nicobar Islands", "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu", "Lakshadweep", "Delhi", "Puducherry",
    "Ladakh", "Jammu and Kashmir"
)

val GST_RATES = listOf(BigDecimal.ZERO, BigDecimal("5.00"), BigDecimal("12.00"), BigDecimal("18.00"), BigDecimal("28.00"))

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.factory(app.container))
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onOnboardingComplete()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "PrimeInvoice Logo",
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "PrimeInvoice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Step ${state.currentStep} of 3",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (state.currentStep) {
                1 -> Step1Content(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
                2 -> Step2Content(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
                3 -> Step3Content(
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // weight(1f) splits the row; without it each button's internal
                // fillMaxWidth collapses the sibling to zero width.
                if (state.currentStep > 1) {
                    SecondaryButton(
                        text = "Back",
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f)
                    )
                }
                PrimaryButton(
                    text = if (state.currentStep < 3) "Next" else "Create First Invoice",
                    onClick = {
                        if (state.currentStep < 3) viewModel.nextStep() else viewModel.completeOnboarding()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Content(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Business Details", style = MaterialTheme.typography.headlineSmall)
        
        AppTextField(
            value = state.businessName,
            onValueChange = viewModel::updateBusinessName,
            label = "Business Name *",
            isError = state.businessNameError != null,
            errorMessage = state.businessNameError
        )
        AppTextField(
            value = state.ownerName,
            onValueChange = viewModel::updateOwnerName,
            label = "Owner Name *",
            isError = state.ownerNameError != null,
            errorMessage = state.ownerNameError
        )
        AppTextField(
            value = state.mobile,
            onValueChange = viewModel::updateMobile,
            label = "Mobile Number *",
            isError = state.mobileError != null,
            errorMessage = state.mobileError
        )
        AppTextField(
            value = state.address,
            onValueChange = viewModel::updateAddress,
            label = "Business Address"
        )

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            AppTextField(
                value = state.state,
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
                            viewModel.updateState(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Content(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tax Settings", style = MaterialTheme.typography.headlineSmall)
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GST Registered?", modifier = Modifier.weight(1f))
            Switch(
                checked = state.isGstRegistered,
                onCheckedChange = viewModel::updateGstRegistered
            )
        }

        if (state.isGstRegistered) {
            val gstinError = remember(state.gstin) {
                if (state.gstin.isBlank()) "GSTIN cannot be empty"
                else GstinValidator.validate(state.gstin).error
            }
            AppTextField(
                value = state.gstin,
                onValueChange = viewModel::updateGstin,
                label = "GSTIN",
                isError = gstinError != null,
                errorMessage = gstinError
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                AppTextField(
                    value = "${state.defaultGstRate.toInt()}%",
                    onValueChange = {},
                    readOnly = true,
                    label = "Default GST Rate",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    GST_RATES.forEach { rate ->
                        DropdownMenuItem(
                            text = { Text("${rate.toInt()}%") },
                            onClick = {
                                viewModel.updateDefaultGstRate(rate)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Step3Content(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You're ready!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your business profile is set up. You can now start creating professional invoices.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
